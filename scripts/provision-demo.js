'use strict';
/**
 * DEV-ONLY: creates exactly ONE documented demo company + owner + 3 sample tasks in your DEV Firebase project.
 * Never run against production. Needs a service-account key kept OUTSIDE the repo:
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/dev-service-account.json
 *   node scripts/provision-demo.js
 * Prints the credentials it actually created. Delete the account when done.
 */
const admin = require('../functions/node_modules/firebase-admin');
admin.initializeApp();
const db = admin.firestore();
const EMAIL = 'demo.owner@taskflow-demo.example';
const PASSWORD = 'DemoOwner#2026';
const GSTIN = '27AAPFU0939F1ZV'; // checksum-valid sample, NOT a real registration

(async () => {
  if (process.env.NODE_ENV === 'production') throw new Error('Refusing to run in production.');
  let user;
  try { user = await admin.auth().getUserByEmail(EMAIL); console.log('Demo owner already exists.'); }
  catch { user = await admin.auth().createUser({ email: EMAIL, password: PASSWORD, displayName: 'Demo Owner (TEST)', emailVerified: true }); }
  const orgId = `org_${user.uid.slice(0, 12)}`, memberId = `admin_${user.uid.slice(0, 8)}`;
  const batch = db.batch();
  batch.set(db.doc(`gstinIndex/${GSTIN}`), { orgId });
  batch.set(db.doc(`organizations/${orgId}`), { id: orgId, name: 'DEMO Company (test data)', gstin: GSTIN, gstinVerifiedByGovernment: false, email: EMAIL, phone: '+919999999999', ownerUid: user.uid });
  batch.set(db.doc(`organizations/${orgId}/members/${memberId}`), { id: memberId, userId: user.uid, displayName: 'Demo Owner (TEST)', employeeId: 'ADM-001', email: EMAIL, role: 'admin', status: 'active' });
  batch.set(db.doc(`organizations/${orgId}/meta/counters`), { manager: 0, staff: 0 });
  batch.set(db.doc(`userDirectory/${user.uid}`), { orgId, role: 'admin', status: 'active', memberId, employeeId: 'ADM-001', displayName: 'Demo Owner (TEST)', orgName: 'DEMO Company (test data)', contactEmail: EMAIL });
  const day = 86400000, now = Date.now();
  [['Sample: prepare weekly report', 'TODO', 'HIGH', 2], ['Sample: review vendor contract', 'IN_PROGRESS', 'MEDIUM', 5], ['Sample: overdue example', 'TODO', 'LOW', -2]].forEach(([title, status, priority, d], i) =>
    batch.set(db.doc(`organizations/${orgId}/tasks/demo_${i + 1}`), { id: `demo_${i + 1}`, organizationId: orgId, title, status, priority, progress: 0, dueDate: now + d * day, createdByMemberId: memberId, createdByName: 'Demo Owner (TEST)', assignedToMemberId: memberId }));
  await batch.commit();
  await admin.auth().setCustomUserClaims(user.uid, { orgId, role: 'admin' });
  console.log(`DEMO LOGIN (Owner portal)  email: ${EMAIL}   password: ${PASSWORD}`);
})().catch((e) => { console.error(e); process.exit(1); });
