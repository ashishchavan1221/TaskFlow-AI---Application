'use strict';
const crypto = require('crypto');

const POLICY = Object.freeze({
  ttlMs: 10 * 60 * 1000,        // code lifetime
  cooldownMs: 60 * 1000,        // min gap between sends
  maxAttempts: 5,               // wrong guesses before the code is burned
  maxSendsPerHour: 5,
  tokenTtlMs: 15 * 60 * 1000,   // lifetime of the post-verification token
});

/** Exactly six digits, uniform, from a CSPRNG. */
function generateOtp() { return String(crypto.randomInt(0, 1000000)).padStart(6, '0'); }

/** HMAC so the stored value is useless without the server-side pepper (no plaintext OTP anywhere). */
function hashOtp(otp, email, purpose, pepper) {
  return crypto.createHmac('sha256', pepper).update(`${purpose}|${email}|${otp}`).digest('hex');
}

function safeEqualHex(a, b) {
  const x = Buffer.from(String(a), 'hex'), y = Buffer.from(String(b), 'hex');
  return x.length === y.length && x.length > 0 && crypto.timingSafeEqual(x, y);
}

/** rec: {sentAt, sendsThisHour, hourStart} or null. */
function evaluateSend(rec, now) {
  if (!rec) return { ok: true };
  if (now - rec.sentAt < POLICY.cooldownMs) {
    return { ok: false, reason: 'cooldown', waitSeconds: Math.ceil((POLICY.cooldownMs - (now - rec.sentAt)) / 1000) };
  }
  const inWindow = now - (rec.hourStart || 0) < 3600 * 1000;
  if (inWindow && (rec.sendsThisHour || 0) >= POLICY.maxSendsPerHour) return { ok: false, reason: 'hourly_limit' };
  return { ok: true };
}

/** rec: {hash, expiresAt, attempts, used}. */
function evaluateVerify(rec, now) {
  if (!rec || rec.used) return { ok: false, reason: 'no_active_code' };
  if (now > rec.expiresAt) return { ok: false, reason: 'expired' };
  if ((rec.attempts || 0) >= POLICY.maxAttempts) return { ok: false, reason: 'too_many_attempts' };
  return { ok: true };
}

const b64u = (b) => Buffer.from(b).toString('base64url');

/** Signed, expiring proof that an e-mail passed OTP verification for a given purpose. */
function makeToken(payload, secret, now = Date.now()) {
  const body = b64u(JSON.stringify({ ...payload, exp: now + POLICY.tokenTtlMs }));
  const sig = crypto.createHmac('sha256', secret).update(body).digest('base64url');
  return `${body}.${sig}`;
}

function verifyToken(token, secret, now = Date.now()) {
  const [body, sig] = String(token || '').split('.');
  if (!body || !sig) return null;
  const expect = crypto.createHmac('sha256', secret).update(body).digest('base64url');
  const a = Buffer.from(sig), b = Buffer.from(expect);
  if (a.length !== b.length || !crypto.timingSafeEqual(a, b)) return null;
  try {
    const p = JSON.parse(Buffer.from(body, 'base64url').toString('utf8'));
    return p.exp > now ? p : null;
  } catch { return null; }
}

function tempPassword() {
  const alpha = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789';
  let s = 'T';
  for (let i = 0; i < 9; i++) s += alpha[crypto.randomInt(alpha.length)];
  return s + crypto.randomInt(10);
}

const sha256 = (s) => crypto.createHash('sha256').update(String(s)).digest('hex');

module.exports = { POLICY, generateOtp, hashOtp, safeEqualHex, evaluateSend, evaluateVerify, makeToken, verifyToken, tempPassword, sha256 };
