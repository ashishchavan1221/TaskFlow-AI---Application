'use strict';
/**
 * TaskFlow AI - privileged backend (Cloud Functions for Firebase, v2 API, Node 20).
 *
 * Every operation that changes identity, role, company membership or credentials lives HERE,
 * never in the mobile app. Firestore rules deny client writes to the collections these functions own.
 *
 * Secrets (set with `firebase functions:secrets:set NAME`):
 *   OTP_PEPPER, TOKEN_SECRET  - random 32+ byte strings
 *   MAIL_API_KEY              - Resend API key (https://resend.com), sender in param MAIL_FROM
 *   AI_API_KEY                - optional; AI Mode reports "not configured" without it
 */
const { setGlobalOptions } = require('firebase-functions/v2');
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onDocumentCreated, onDocumentUpdated } = require('firebase-functions/v2/firestore');
const { defineSecret, defineString } = require('firebase-functions/params');
const admin = require('firebase-admin');

const V = require('./lib/validators');
const O = require('./lib/otp');

admin.initializeApp();
const db = admin.firestore();
const auth = admin.auth();
const { FieldValue, Timestamp } = admin.firestore;

setGlobalOptions({ region: 'asia-south1', maxInstances: 10 });

const OTP_PEPPER = defineSecret('OTP_PEPPER');
const TOKEN_SECRET = defineSecret('TOKEN_SECRET');
const MAIL_API_KEY = defineSecret('MAIL_API_KEY');
const AI_API_KEY = defineSecret('AI_API_KEY');
const MAIL_FROM = defineString('MAIL_FROM', { default: 'TaskFlow Support <support@taskflow.com>' });
const AI_MODEL = defineString('AI_MODEL', { default: 'gemini-2.5-flash' });

// ----------------------------------------------------------------------------- helpers
const fail = (code, msg) => { throw new HttpsError(code, msg); };

async function profileOf(request) {
  if (!request.auth) fail('unauthenticated', 'Sign in required.');
  const snap = await db.doc(`userDirectory/${request.auth.uid}`).get();
  if (!snap.exists || snap.get('status') !== 'active') fail('permission-denied', 'Account is not active.');
  return { uid: request.auth.uid, ...snap.data() };
}
async function requireAdmin(request) {
  const p = await profileOf(request);
  if (p.role !== 'admin') {
    await securityEvent(p.orgId, 'forbidden_admin_call', p.uid, { fn: request.rawRequest?.path || '' });
    fail('permission-denied', 'Owner permission required.');
  }
  return p;
}
async function securityEvent(orgId, type, actorUid, details = {}) {
  try {
    await db.collection('securityEvents').add({ orgId: orgId || null, type, actorUid: actorUid || null, details, at: FieldValue.serverTimestamp() });
  } catch (e) { console.error('securityEvent failed', e.message); }
}
async function audit(orgId, action, actor, target, extra = {}) {
  await db.collection(`organizations/${orgId}/audit_logs`).add({
    action, actorId: actor.uid, actorName: actor.displayName || '', actorRole: actor.role,
    targetId: target || '', ...extra, at: FieldValue.serverTimestamp(),
  });
}
/** Fixed-window rate limiter backed by Firestore. */
async function rateLimit(key, max, windowMs) {
  const ref = db.doc(`rateLimits/${O.sha256(key)}`);
  await db.runTransaction(async (tx) => {
    const s = await tx.get(ref); const now = Date.now();
    let d = s.exists ? s.data() : { start: now, count: 0 };
    if (now - d.start > windowMs) d = { start: now, count: 0 };
    if (d.count >= max) fail('resource-exhausted', 'Too many attempts. Please try again later.');
    tx.set(ref, { start: d.start, count: d.count + 1 });
  });
}
async function sendMail(to, subject, text) {
  const res = await fetch('https://api.resend.com/emails', {
    method: 'POST',
    headers: { Authorization: `Bearer ${MAIL_API_KEY.value()}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ from: MAIL_FROM.value(), to: [to], subject, text }),
  });
  if (!res.ok) { console.error('mail provider error', res.status); fail('unavailable', 'Could not send e-mail right now.'); }
}
async function setClaims(uid, orgId, role) { await auth.setCustomUserClaims(uid, { orgId, role }); }

// ----------------------------------------------------------------------------- Email OTP
const PURPOSES = ['register', 'owner_reset'];

exports.sendEmailOtp = onCall({ secrets: [OTP_PEPPER, MAIL_API_KEY] }, async (request) => {
  const email = V.normalizeEmail(request.data?.email);
  const purpose = request.data?.purpose;
  if (!V.isValidEmail(email)) fail('invalid-argument', 'Enter a valid e-mail address.');
  if (!PURPOSES.includes(purpose)) fail('invalid-argument', 'Invalid purpose.');
  await rateLimit(`otp-ip|${request.rawRequest?.ip}`, 20, 3600 * 1000);

  const ref = db.doc(`otpRequests/${O.sha256(`${purpose}|${email}`)}`);
  const snap = await ref.get();
  const rec = snap.exists ? snap.data() : null;
  const decision = O.evaluateSend(rec, Date.now());
  if (!decision.ok) {
    fail('resource-exhausted', decision.reason === 'cooldown'
      ? `Please wait ${decision.waitSeconds}s before requesting another code.`
      : 'Too many codes requested. Try again in an hour.');
  }
  if (purpose === 'register') {
    try { await auth.getUserByEmail(email); fail('already-exists', 'This e-mail is already registered.'); }
    catch (e) { if (e instanceof HttpsError) throw e; /* user-not-found is the good case */ }
  }
  // Owner reset: deliver text-only OTP code email directly
  let deliver = true;
  const otp = O.generateOtp();
  const now = Date.now();
  const inWindow = rec && now - (rec.hourStart || 0) < 3600 * 1000;
  await ref.set({
    hash: O.hashOtp(otp, email, purpose, OTP_PEPPER.value()),   // never the code itself
    expiresAt: now + O.POLICY.ttlMs, attempts: 0, used: false, sentAt: now,
    hourStart: inWindow ? rec.hourStart : now, sendsThisHour: inWindow ? (rec.sendsThisHour || 0) + 1 : 1,
  });
  if (deliver) {
    await sendMail(email, 'TaskFlow Enterprise Verification Code',
      `Dear Valued Member,\n\nYou have requested a 6-digit verification code to reset your account password for TaskFlow Enterprise.\n\nYour Verification Code:\n${otp}\n\nThis code is valid for 5 minutes. Please enter this code inside the TaskFlow application to reset your password.\n\nThank you for using TaskFlow Enterprise.\n\nBest regards,\nTaskFlow Security & Operations Team`);
  }
  return { sent: true, cooldownSeconds: O.POLICY.cooldownMs / 1000, expiresInSeconds: O.POLICY.ttlMs / 1000 };
});

exports.verifyEmailOtp = onCall({ secrets: [OTP_PEPPER, TOKEN_SECRET] }, async (request) => {
  const email = V.normalizeEmail(request.data?.email);
  const purpose = request.data?.purpose;
  const code = String(request.data?.code || '');
  if (!V.isValidEmail(email) || !PURPOSES.includes(purpose) || !/^\d{6}$/.test(code)) {
    fail('invalid-argument', 'Enter the 6-digit code.');
  }
  const ref = db.doc(`otpRequests/${O.sha256(`${purpose}|${email}`)}`);
  const result = await db.runTransaction(async (tx) => {
    const s = await tx.get(ref);
    const rec = s.exists ? s.data() : null;
    const ev = O.evaluateVerify(rec, Date.now());
    if (!ev.ok) return { ok: false, reason: ev.reason };
    const match = O.safeEqualHex(rec.hash, O.hashOtp(code, email, purpose, OTP_PEPPER.value()));
    if (!match) { tx.update(ref, { attempts: (rec.attempts || 0) + 1 }); return { ok: false, reason: 'wrong_code' }; }
    tx.update(ref, { used: true });                          // single use
    return { ok: true };
  });
  if (!result.ok) {
    await securityEvent(null, 'otp_verify_failed', null, { purpose, reason: result.reason });
    fail('permission-denied', {
      expired: 'This code has expired. Request a new one.',
      too_many_attempts: 'Too many wrong attempts. Request a new code.',
      no_active_code: 'No active code. Request a new one.',
      wrong_code: 'Incorrect code.',
    }[result.reason] || 'Verification failed.');
  }
  return { verificationToken: O.makeToken({ email, purpose }, TOKEN_SECRET.value()) };
});

// ----------------------------------------------------------------------------- Company registration
exports.registerCompany = onCall({ secrets: [TOKEN_SECRET] }, async (request) => {
  const d = request.data || {};
  const email = V.normalizeEmail(d.email);
  const gstin = String(d.gstin || '').trim().toUpperCase();
  const tok = O.verifyToken(d.verificationToken, TOKEN_SECRET.value());
  if (!tok || tok.purpose !== 'register' || tok.email !== email) fail('permission-denied', 'E-mail is not verified. Request a new code.');
  if (!String(d.ownerName || '').trim()) fail('invalid-argument', 'Owner name is required.');
  if (!String(d.companyName || '').trim()) fail('invalid-argument', 'Company name is required.');
  if (!V.isValidEmail(email)) fail('invalid-argument', 'Enter a valid company e-mail.');
  if (!V.isValidPhone(d.phone)) fail('invalid-argument', 'Enter a valid phone number.');
  if (!V.isValidGstin(gstin)) fail('invalid-argument', 'Invalid GSTIN (format/check digit). Government verification is not performed.');
  const pw = V.passwordProblem(d.password); if (pw) fail('invalid-argument', pw);
  if (d.password !== d.confirmPassword) fail('invalid-argument', 'Passwords do not match.');

  let user;
  try {
    user = await auth.createUser({ email, password: d.password, displayName: String(d.ownerName).trim(), emailVerified: true });
  } catch (e) {
    if (e.code === 'auth/email-already-exists') fail('already-exists', 'This e-mail is already registered.');
    throw e;
  }
  const orgId = `org_${user.uid.slice(0, 12)}`;
  const memberId = `admin_${user.uid.slice(0, 8)}`;
  try {
    await db.runTransaction(async (tx) => {
      const gref = db.doc(`gstinIndex/${gstin}`);
      if ((await tx.get(gref)).exists) fail('already-exists', 'A company with this GSTIN is already registered.');
      tx.set(gref, { orgId, createdAt: FieldValue.serverTimestamp() });
      tx.set(db.doc(`organizations/${orgId}`), {
        id: orgId, name: String(d.companyName).trim(), gstin, gstinVerifiedByGovernment: false,
        email, phone: String(d.phone).trim(), description: String(d.description || '').slice(0, 500),
        address: String(d.address || '').slice(0, 300), logoPath: '', ownerUid: user.uid,
        createdAt: FieldValue.serverTimestamp(),
      });
      tx.set(db.doc(`organizations/${orgId}/members/${memberId}`), {
        id: memberId, userId: user.uid, displayName: String(d.ownerName).trim(), employeeId: 'ADM-001',
        email, role: 'admin', status: 'active', phone: String(d.phone).trim(),
      });
      tx.set(db.doc(`organizations/${orgId}/settings/general`), { createdAt: FieldValue.serverTimestamp(), theme: 'system' });
      tx.set(db.doc(`userDirectory/${user.uid}`), {
        orgId, role: 'admin', status: 'active', memberId, employeeId: 'ADM-001',
        displayName: String(d.ownerName).trim(), orgName: String(d.companyName).trim(), contactEmail: email,
      });
      tx.set(db.doc(`organizations/${orgId}/meta/counters`), { manager: 0, staff: 0 });
    });
    await setClaims(user.uid, orgId, 'admin');
  } catch (e) {
    await auth.deleteUser(user.uid).catch(() => {});          // retry-safe: no half-created company
    throw e;
  }
  await securityEvent(orgId, 'company_registered', user.uid, {});
  return { orgId };
});

// ----------------------------------------------------------------------------- Members
async function provisionMember(adminProfile, { name, role, phone, departmentId, departmentName, contactEmail }) {
  if (!['manager', 'staff'].includes(role)) fail('invalid-argument', 'Role must be manager or staff.');
  if (!String(name || '').trim()) fail('invalid-argument', 'Name is required.');
  const orgId = adminProfile.orgId;
  for (let attempt = 0; attempt < 3; attempt++) {
    const n = await db.runTransaction(async (tx) => {
      const ref = db.doc(`organizations/${orgId}/meta/counters`);
      const s = await tx.get(ref); const next = ((s.exists ? s.get(role) : 0) || 0) + 1;
      tx.set(ref, { [role]: next }, { merge: true }); return next;
    });
    const employeeId = V.makeEmployeeId(role, orgId, n);
    const temp = O.tempPassword();
    let user;
    try {
      user = await auth.createUser({ email: V.employeeAuthEmail(employeeId), password: temp, displayName: String(name).trim() });
    } catch (e) { if (e.code === 'auth/email-already-exists') continue; throw e; }
    const memberId = `mem_${user.uid.slice(0, 10)}`;
    try {
      const batch = db.batch();
      batch.set(db.doc(`organizations/${orgId}/members/${memberId}`), {
        id: memberId, userId: user.uid, displayName: String(name).trim(), employeeId, role, status: 'active',
        email: V.normalizeEmail(contactEmail), phone: String(phone || '').trim(),
        departmentId: departmentId || '', departmentName: departmentName || '', mustChangePassword: true,
      });
      batch.set(db.doc(`userDirectory/${user.uid}`), {
        orgId, role, status: 'active', memberId, employeeId, displayName: String(name).trim(),
        orgName: adminProfile.orgName || '', contactEmail: V.normalizeEmail(contactEmail), mustChangePassword: true,
      });
      await batch.commit();
      await setClaims(user.uid, orgId, role);
    } catch (e) { await auth.deleteUser(user.uid).catch(() => {}); throw e; }
    await audit(orgId, 'MEMBER_CREATED', adminProfile, memberId, { role, employeeId });
    return { employeeId, memberId, uid: user.uid, tempPassword: temp };
  }
  fail('aborted', 'Could not allocate a unique ID. Try again.');
}

exports.createMember = onCall(async (request) => {
  const admin_ = await requireAdmin(request);
  const r = await provisionMember(admin_, request.data || {});
  return { employeeId: r.employeeId, memberId: r.memberId, tempPassword: r.tempPassword }; // shown to the Owner once
});

exports.deactivateMember = onCall(async (request) => {
  const a = await requireAdmin(request);
  const memberId = String(request.data?.memberId || '');
  const m = await db.doc(`organizations/${a.orgId}/members/${memberId}`).get();
  if (!m.exists) fail('not-found', 'Member not found.');
  if (m.get('role') === 'admin') fail('permission-denied', 'The Owner account cannot be deactivated.');
  const uid = m.get('userId');
  await auth.updateUser(uid, { disabled: true });
  await auth.revokeRefreshTokens(uid);
  await db.doc(`userDirectory/${uid}`).update({ status: 'disabled' });
  await m.ref.update({ status: 'inactive' });
  await audit(a.orgId, 'MEMBER_DEACTIVATED', a, memberId);
  return { ok: true };
});

// ----------------------------------------------------------------------------- Approval workflow
exports.decideApproval = onCall(async (request) => {
  const a = await requireAdmin(request);
  const { requestId, decision } = request.data || {};
  const notes = String(request.data?.notes || '').slice(0, 300);
  if (!['approve', 'reject'].includes(decision)) fail('invalid-argument', 'Invalid decision.');
  const ref = db.doc(`organizations/${a.orgId}/permission_requests/${requestId}`);
  const snap = await ref.get();
  if (!snap.exists) fail('not-found', 'Request not found.');
  if (snap.get('status') !== 'pending') fail('failed-precondition', 'Request already decided.');
  let result = {};
  if (decision === 'approve') {
    const type = snap.get('actionType');
    if (type === 'CREATE_STAFF') {
      let p; try { p = JSON.parse(snap.get('payloadJson')); } catch { fail('failed-precondition', 'Request data is invalid.'); }
      const created = await provisionMember(a, {
        name: p.name, role: p.role === 'manager' ? 'manager' : 'staff', phone: p.phone,
        departmentId: p.departmentId, departmentName: p.department, contactEmail: p.email,
      });
      result = { employeeId: created.employeeId, tempPassword: created.tempPassword };
    } else if (type === 'DELETE_STAFF') {
      const mid = snap.get('targetStaffId');
      const m = await db.doc(`organizations/${a.orgId}/members/${mid}`).get();
      if (m.exists && m.get('role') !== 'admin') {
        const uid = m.get('userId');
        await auth.updateUser(uid, { disabled: true }); await auth.revokeRefreshTokens(uid);
        await db.doc(`userDirectory/${uid}`).update({ status: 'disabled' });
        await m.ref.update({ status: 'inactive' });
      }
    }
  }
  await ref.update({ status: decision === 'approve' ? 'approved' : 'rejected', adminNotes: notes, decidedBy: a.uid, decidedAt: FieldValue.serverTimestamp() });
  await audit(a.orgId, decision === 'approve' ? 'APPROVAL_APPROVED' : 'APPROVAL_REJECTED', a, requestId, { type: snap.get('actionType') });
  const mgrMember = snap.get('managerId');
  if (mgrMember) {
    await db.collection(`organizations/${a.orgId}/notifications`).add({
      toMemberId: mgrMember, type: 'approval_result', title: `Request ${decision === 'approve' ? 'approved' : 'rejected'}`,
      body: notes || '', read: false, createdAt: FieldValue.serverTimestamp(),
    });
  }
  return result;
});

// ----------------------------------------------------------------------------- Recovery
exports.requestRecovery = onCall(async (request) => {
  const kind = request.data?.kind;
  if (!['id', 'password'].includes(kind)) fail('invalid-argument', 'Invalid request.');
  await rateLimit(`rec-ip|${request.rawRequest?.ip}`, 10, 3600 * 1000);
  const generic = { ok: true, message: 'If the details match an account, the company Owner has been notified.' };
  try {
    let orgId = null; const data = { kind, status: 'pending', createdAt: FieldValue.serverTimestamp() };
    if (kind === 'password') {
      const id = String(request.data?.employeeId || '').trim();
      if (!V.isValidEmployeeId(id)) return generic;
      await rateLimit(`rec-id|${id.toLowerCase()}`, 3, 24 * 3600 * 1000);
      const u = await auth.getUserByEmail(V.employeeAuthEmail(id));
      const d = await db.doc(`userDirectory/${u.uid}`).get();
      if (!d.exists || d.get('role') === 'admin') return generic;
      orgId = d.get('orgId'); data.employeeId = id.toUpperCase(); data.memberId = d.get('memberId'); data.displayName = d.get('displayName');
    } else {
      const ownerEmail = V.normalizeEmail(request.data?.companyEmail);
      const fullName = String(request.data?.fullName || '').trim().slice(0, 80);
      if (!V.isValidEmail(ownerEmail) || !fullName) return generic;
      await rateLimit(`rec-co|${ownerEmail}`, 5, 24 * 3600 * 1000);
      const u = await auth.getUserByEmail(ownerEmail);
      const d = await db.doc(`userDirectory/${u.uid}`).get();
      if (!d.exists || d.get('role') !== 'admin') return generic;
      orgId = d.get('orgId'); data.fullName = fullName;
    }
    await db.collection(`organizations/${orgId}/recovery_requests`).add(data);
    await securityEvent(orgId, 'recovery_requested', null, { kind });
  } catch (e) {
    if (e instanceof HttpsError) throw e;            // rate limits surface; lookups never do
  }
  return generic;
});

exports.decideRecovery = onCall(async (request) => {
  const a = await requireAdmin(request);
  const { requestId, decision } = request.data || {};
  const ref = db.doc(`organizations/${a.orgId}/recovery_requests/${requestId}`);
  const snap = await ref.get();
  if (!snap.exists || snap.get('status') !== 'pending') fail('failed-precondition', 'Request not pending.');
  if (decision === 'reject') { await ref.update({ status: 'rejected', decidedAt: FieldValue.serverTimestamp() }); return { ok: true }; }
  if (decision !== 'approve') fail('invalid-argument', 'Invalid decision.');
  let out = {};
  if (snap.get('kind') === 'password') {
    const m = await db.doc(`organizations/${a.orgId}/members/${snap.get('memberId')}`).get();
    if (!m.exists) fail('not-found', 'Member not found.');
    const temp = O.tempPassword();
    await auth.updateUser(m.get('userId'), { password: temp });
    await auth.revokeRefreshTokens(m.get('userId'));            // end every existing session
    await db.doc(`userDirectory/${m.get('userId')}`).update({ mustChangePassword: true });
    out = { tempPassword: temp, employeeId: m.get('employeeId') }; // the Owner never sees the OLD password
  } else {
    const q = await db.collection(`organizations/${a.orgId}/members`).where('displayName', '==', snap.get('fullName')).where('role', 'in', ['manager', 'staff']).limit(5).get();
    out = { matches: q.docs.map((x) => ({ employeeId: x.get('employeeId'), role: x.get('role') })) };
  }
  await ref.update({ status: 'resolved', decidedBy: a.uid, decidedAt: FieldValue.serverTimestamp() });
  await audit(a.orgId, 'RECOVERY_RESOLVED', a, requestId, { kind: snap.get('kind') });
  await securityEvent(a.orgId, 'credentials_reissued', a.uid, { kind: snap.get('kind') });
  return out;
});

exports.ownerResetPassword = onCall({ secrets: [TOKEN_SECRET] }, async (request) => {
  const email = V.normalizeEmail(request.data?.email);
  const tok = O.verifyToken(request.data?.verificationToken, TOKEN_SECRET.value());
  if (!tok || tok.purpose !== 'owner_reset' || tok.email !== email) fail('permission-denied', 'Verification required.');
  const pw = V.passwordProblem(request.data?.newPassword); if (pw) fail('invalid-argument', pw);
  let u;
  try { u = await auth.getUserByEmail(email); } catch { fail('permission-denied', 'Verification required.'); }
  const d = await db.doc(`userDirectory/${u.uid}`).get();
  if (!d.exists || d.get('role') !== 'admin') fail('permission-denied', 'Verification required.');
  await auth.updateUser(u.uid, { password: request.data.newPassword });
  await auth.revokeRefreshTokens(u.uid);
  await securityEvent(d.get('orgId'), 'owner_password_reset', u.uid, {});
  return { ok: true };
});

// ----------------------------------------------------------------------------- Push tokens + triggers
exports.registerFcmToken = onCall(async (request) => {
  const p = await profileOf(request);
  const token = String(request.data?.token || '');
  if (token.length < 20 || token.length > 400) fail('invalid-argument', 'Bad token.');
  await db.doc(`userDirectory/${p.uid}`).update({ fcmTokens: FieldValue.arrayUnion(token) });
  return { ok: true };
});

async function notifyMember(orgId, memberId, title, body, type, taskId) {
  const m = await db.doc(`organizations/${orgId}/members/${memberId}`).get();
  if (!m.exists) return;
  await db.collection(`organizations/${orgId}/notifications`).add({
    toMemberId: memberId, type, title, body, taskId: taskId || '', read: false, createdAt: FieldValue.serverTimestamp(),
  });
  const dir = await db.doc(`userDirectory/${m.get('userId')}`).get();
  const tokens = (dir.get('fcmTokens') || []).slice(-5);
  if (tokens.length) {
    // Generic lock-screen text on purpose: no task titles or other confidential data.
    await admin.messaging().sendEachForMulticast({ tokens, notification: { title: 'TaskFlow AI', body: 'You have a new update.' }, data: { type, taskId: taskId || '' } })
      .catch((e) => console.error('fcm', e.message));
  }
}

exports.onTaskCreated = onDocumentCreated('organizations/{orgId}/tasks/{taskId}', async (event) => {
  const t = event.data?.data(); if (!t) return;
  const { orgId, taskId } = event.params;
  await db.collection(`organizations/${orgId}/activity`).add({ type: 'TASK_CREATED', taskId, actorName: t.createdByName || '', summary: `Task created: ${t.title || ''}`.slice(0, 120), at: FieldValue.serverTimestamp() });
  if (t.assignedToMemberId) await notifyMember(orgId, t.assignedToMemberId, 'New task assigned', t.title || '', 'task_assigned', taskId);
});

exports.onTaskUpdated = onDocumentUpdated('organizations/{orgId}/tasks/{taskId}', async (event) => {
  const b = event.data?.before.data(), a = event.data?.after.data(); if (!b || !a) return;
  const { orgId, taskId } = event.params;
  const changes = [];
  if (b.status !== a.status) changes.push(`status ${b.status} -> ${a.status}`);
  if (b.assignedToMemberId !== a.assignedToMemberId) changes.push('reassigned');
  if (String(b.dueDate) !== String(a.dueDate)) changes.push('deadline changed');
  if (!changes.length) return;
  await db.collection(`organizations/${orgId}/activity`).add({ type: 'TASK_UPDATED', taskId, summary: `${a.title || ''}: ${changes.join(', ')}`.slice(0, 160), at: FieldValue.serverTimestamp() });
  if (b.assignedToMemberId !== a.assignedToMemberId && a.assignedToMemberId) await notifyMember(orgId, a.assignedToMemberId, 'Task assigned to you', a.title || '', 'task_assigned', taskId);
  if (b.status !== a.status && a.createdByMemberId) await notifyMember(orgId, a.createdByMemberId, 'Task status changed', a.title || '', 'task_status', taskId);
});

// ----------------------------------------------------------------------------- AI Mode
exports.aiAssist = onCall({ secrets: [AI_API_KEY], timeoutSeconds: 60 }, async (request) => {
  const p = await profileOf(request);
  const key = AI_API_KEY.value();
  if (!key || key === 'unset') return { configured: false, message: 'AI is not configured. Set the AI_API_KEY secret and redeploy.' };
  const question = String(request.data?.question || '').trim().slice(0, 500);
  if (!question) fail('invalid-argument', 'Ask a question.');
  await rateLimit(`ai|${p.uid}`, 30, 3600 * 1000);
  // Server-side scoping: only this company's tasks, and staff only see their own.
  let q = db.collection(`organizations/${p.orgId}/tasks`).limit(100);
  if (p.role === 'staff') q = db.collection(`organizations/${p.orgId}/tasks`).where('assignedToMemberId', '==', p.memberId).limit(100);
  const tasks = (await q.get()).docs.map((d) => {
    const t = d.data(); return { title: t.title, status: t.status, priority: t.priority, due: String(t.dueDate || ''), progress: t.progress };
  });
  const prompt = `You are a task-management assistant. Use ONLY this JSON data (${p.role} view) to answer. If it is not in the data, say so.\nDATA: ${JSON.stringify(tasks)}\nQUESTION: ${question}`;
  const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${AI_MODEL.value()}:generateContent`, {
    method: 'POST', headers: { 'Content-Type': 'application/json', 'x-goog-api-key': key },
    body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] }),
  });
  if (!res.ok) { console.error('ai provider', res.status); fail('unavailable', 'AI service error. Try again later.'); }
  const j = await res.json();
  return { configured: true, answer: j.candidates?.[0]?.content?.parts?.[0]?.text || 'No answer.' };
});

// First-login password change for accounts created with a temporary password.
// The app calls FirebaseAuth.updatePassword() (recent login required), then this clears the server flag.
exports.completePasswordChange = onCall(async (request) => {
  const p = await profileOf(request);
  await db.doc(`userDirectory/${p.uid}`).update({ mustChangePassword: false });
  if (p.memberId) await db.doc(`organizations/${p.orgId}/members/${p.memberId}`).update({ mustChangePassword: false }).catch(() => {});
  await securityEvent(p.orgId, 'password_changed', p.uid, {});
  return { ok: true };
});

module.exports._testOnly = { PURPOSES };
