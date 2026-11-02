'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const v = require('../lib/validators');
const o = require('../lib/otp');

test('GSTIN: real sample passes, fake/typo fails', () => {
  assert.equal(v.isValidGstin('27AAPFU0939F1ZV'), true);
  assert.equal(v.isValidGstin('27aapfu0939f1zv'), true);          // case-insensitive
  assert.equal(v.isValidGstin('27AAPCA1234F1Z5'), false);         // the old hard-coded fake
  assert.equal(v.isValidGstin('27AAPFU0939F1ZX'), false);         // wrong check digit
  assert.equal(v.isValidGstin('123'), false);
  assert.equal(v.isValidGstin(''), false);
});

test('email / phone / password / employee id', () => {
  assert.equal(v.isValidEmail(' Owner@Acme.IN '), true);
  assert.equal(v.isValidEmail('no-at-sign'), false);
  assert.equal(v.isValidPhone('9876543210'), true);
  assert.equal(v.isValidPhone('+919876543210'), true);
  assert.equal(v.isValidPhone('+14155552671'), true);
  assert.equal(v.isValidPhone('12345'), false);
  assert.equal(v.passwordProblem('short1'), 'Password must be at least 8 characters.');
  assert.ok(v.passwordProblem('onlyletters'));
  assert.ok(v.passwordProblem('12345678'));
  assert.equal(v.passwordProblem('Passw0rdOK'), null);
  assert.equal(v.isValidEmployeeId('MGR-A1B2-0007'), true);
  assert.equal(v.isValidEmployeeId('bad id!'), false);
  assert.equal(v.employeeAuthEmail(' STF-A1B2-0001 '), 'stf-a1b2-0001@staff.taskflow-ai.app');
});

test('employee id generation is <= 20 chars and role-prefixed', () => {
  const id = v.makeEmployeeId('manager', 'org_x7k2m9q1w3ab', 7);
  assert.equal(id, 'MGR-X7K2-0007');
  assert.ok(id.length <= 20 && v.isValidEmployeeId(id));
  assert.ok(v.makeEmployeeId('staff', 'org_abcd', 12345).startsWith('STF-ABCD-'));
});

test('OTP: exactly 6 digits, always', () => {
  for (let i = 0; i < 2000; i++) assert.match(o.generateOtp(), /^\d{6}$/);
});

test('OTP hash: deterministic, bound to email+purpose, never plaintext', () => {
  const h = o.hashOtp('123456', 'a@b.com', 'register', 'pepper');
  assert.equal(h, o.hashOtp('123456', 'a@b.com', 'register', 'pepper'));
  assert.notEqual(h, o.hashOtp('123456', 'a@b.com', 'owner_reset', 'pepper'));
  assert.notEqual(h, o.hashOtp('123456', 'c@d.com', 'register', 'pepper'));
  assert.notEqual(h, o.hashOtp('123456', 'a@b.com', 'register', 'other'));
  assert.ok(!h.includes('123456'));
  assert.equal(o.safeEqualHex(h, h), true);
  assert.equal(o.safeEqualHex(h, o.hashOtp('654321', 'a@b.com', 'register', 'pepper')), false);
  assert.equal(o.safeEqualHex('zz', 'zz'), false);
});

test('send policy: cooldown and hourly cap', () => {
  const now = 1_000_000_000;
  assert.equal(o.evaluateSend(null, now).ok, true);
  const r = o.evaluateSend({ sentAt: now - 10_000, sendsThisHour: 1, hourStart: now - 10_000 }, now);
  assert.deepEqual([r.ok, r.reason], [false, 'cooldown']);
  assert.equal(r.waitSeconds, 50);
  assert.equal(o.evaluateSend({ sentAt: now - 61_000, sendsThisHour: 1, hourStart: now - 61_000 }, now).ok, true);
  const cap = o.evaluateSend({ sentAt: now - 61_000, sendsThisHour: 5, hourStart: now - 600_000 }, now);
  assert.deepEqual([cap.ok, cap.reason], [false, 'hourly_limit']);
  assert.equal(o.evaluateSend({ sentAt: now - 61_000, sendsThisHour: 5, hourStart: now - 3_700_000 }, now).ok, true);
});

test('verify policy: expiry, single use, brute-force lockout', () => {
  const now = 5_000_000;
  const good = { hash: 'x', expiresAt: now + 1000, attempts: 0, used: false };
  assert.equal(o.evaluateVerify(good, now).ok, true);
  assert.equal(o.evaluateVerify({ ...good, expiresAt: now - 1 }, now).reason, 'expired');
  assert.equal(o.evaluateVerify({ ...good, used: true }, now).reason, 'no_active_code');
  assert.equal(o.evaluateVerify({ ...good, attempts: 5 }, now).reason, 'too_many_attempts');
  assert.equal(o.evaluateVerify(null, now).reason, 'no_active_code');
});

test('verification token: valid, tampered, expired, wrong secret', () => {
  const now = 10_000_000;
  const t = o.makeToken({ email: 'a@b.com', purpose: 'register' }, 's3cret', now);
  const p = o.verifyToken(t, 's3cret', now + 1000);
  assert.equal(p.email, 'a@b.com'); assert.equal(p.purpose, 'register');
  assert.equal(o.verifyToken(t, 'wrong', now + 1000), null);
  assert.equal(o.verifyToken(t, 's3cret', now + o.POLICY.tokenTtlMs + 1), null);
  const [body, sig] = t.split('.');
  const forged = Buffer.from(JSON.stringify({ email: 'evil@x.com', purpose: 'register', exp: now + 99999999 })).toString('base64url');
  assert.equal(o.verifyToken(`${forged}.${sig}`, 's3cret', now + 1000), null);
  assert.equal(o.verifyToken('garbage', 's3cret', now), null);
  assert.equal(o.verifyToken('', 's3cret', now), null);
});

test('temp passwords satisfy the password policy', () => {
  for (let i = 0; i < 500; i++) assert.equal(v.passwordProblem(o.tempPassword()), null);
});
