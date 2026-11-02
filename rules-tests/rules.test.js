// Security tests for firestore.rules. NOT RUN in the authoring environment (no emulator download access).
// Run: cd rules-tests && npm i && npm run test:rules   (needs firebase-tools + Java)
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, updateDoc } = require('firebase/firestore');
const fs = require('fs'); const test = require('node:test');

let env;
const dir = (o, r, s = 'active', mid = 'm1') => ({ orgId: o, role: r, status: s, memberId: mid });
test.before(async () => {
  env = await initializeTestEnvironment({ projectId: 'demo-taskflow', firestore: { rules: fs.readFileSync('../firestore.rules', 'utf8') } });
  await env.withSecurityRulesDisabled(async (c) => {
    const d = c.firestore();
    await setDoc(doc(d, 'userDirectory/ownerA'), dir('A', 'admin', 'active', 'oA'));
    await setDoc(doc(d, 'userDirectory/mgrA'), dir('A', 'manager', 'active', 'mA'));
    await setDoc(doc(d, 'userDirectory/stfA'), dir('A', 'staff', 'active', 'sA'));
    await setDoc(doc(d, 'userDirectory/stfOff'), dir('A', 'staff', 'disabled', 'sOff'));
    await setDoc(doc(d, 'userDirectory/ownerB'), dir('B', 'admin', 'active', 'oB'));
    await setDoc(doc(d, 'organizations/A'), { id: 'A', name: 'A Co' });
    await setDoc(doc(d, 'organizations/A/tasks/t1'), { organizationId: 'A', assignedToMemberId: 'sA', status: 'TODO', progress: 0, title: 'x' });
    await setDoc(doc(d, 'organizations/A/tasks/t2'), { organizationId: 'A', assignedToMemberId: 'other', status: 'TODO', title: 'y' });
  });
});
test.after(() => env.cleanup());
const as = (uid) => env.authenticatedContext(uid).firestore();

test('cross-tenant read is denied', async () => assertFails(getDoc(doc(as('ownerB'), 'organizations/A/tasks/t1'))));
test('inactive account is denied', async () => assertFails(getDoc(doc(as('stfOff'), 'organizations/A/tasks/t1'))));
test('staff reads own task, not others', async () => {
  await assertSucceeds(getDoc(doc(as('stfA'), 'organizations/A/tasks/t1')));
  await assertFails(getDoc(doc(as('stfA'), 'organizations/A/tasks/t2')));
});
test('staff cannot create tasks', async () => assertFails(setDoc(doc(as('stfA'), 'organizations/A/tasks/n'), { organizationId: 'A', assignedToMemberId: 'sA' })));
test('manager can create tasks in own org only', async () => {
  await assertSucceeds(setDoc(doc(as('mgrA'), 'organizations/A/tasks/n1'), { organizationId: 'A', assignedToMemberId: 'sA' }));
  await assertFails(setDoc(doc(as('mgrA'), 'organizations/B/tasks/n2'), { organizationId: 'B' }));
});
test('staff may update only progress fields of own task', async () => {
  await assertSucceeds(updateDoc(doc(as('stfA'), 'organizations/A/tasks/t1'), { progress: 50 }));
  await assertFails(updateDoc(doc(as('stfA'), 'organizations/A/tasks/t1'), { assignedToMemberId: 'sA2' }));
});
test('nobody can write userDirectory (role escalation)', async () => {
  await assertFails(setDoc(doc(as('stfA'), 'userDirectory/stfA'), dir('A', 'admin', 'active', 'sA')));
  await assertFails(updateDoc(doc(as('ownerA'), 'userDirectory/stfA'), { role: 'admin' }));
  await assertFails(setDoc(doc(as('stfA'), 'userDirectory/stfA'), dir('B', 'staff', 'active', 'sA'))); // company hop
});
test('clients cannot create organizations or members', async () => {
  await assertFails(setDoc(doc(as('ownerA'), 'organizations/Z'), { id: 'Z' }));
  await assertFails(setDoc(doc(as('ownerA'), 'organizations/A/members/x'), { role: 'admin' }));
});
test('manager cannot decide approvals; OTP/rate-limit collections are closed', async () => {
  await assertFails(updateDoc(doc(as('mgrA'), 'organizations/A/permission_requests/r1'), { status: 'approved' }));
  await assertFails(getDoc(doc(as('ownerA'), 'otpRequests/x')));
  await assertFails(getDoc(doc(as('ownerA'), 'securityEvents/x')));
});
test('owner cannot change GSTIN/ownerUid directly', async () => {
  await assertSucceeds(updateDoc(doc(as('ownerA'), 'organizations/A'), { name: 'New' }));
  await assertFails(updateDoc(doc(as('ownerA'), 'organizations/A'), { gstin: 'X' }));
});
