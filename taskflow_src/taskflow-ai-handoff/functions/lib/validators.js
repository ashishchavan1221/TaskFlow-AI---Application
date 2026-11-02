'use strict';
// Pure helpers (no Firebase imports) so they can be unit-tested without a project.

const EMAIL_RE = /^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$/;
const GSTIN_RE = /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$/;
const GST_CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
const EMPLOYEE_ID_RE = /^[A-Za-z0-9\-]{3,20}$/;

function normalizeEmail(e) { return String(e || '').trim().toLowerCase(); }
function isValidEmail(e) { return EMAIL_RE.test(normalizeEmail(e)); }

/** Format + check-digit validation only. This is NOT government verification. */
function isValidGstin(raw) {
  const g = String(raw || '').trim().toUpperCase();
  if (!GSTIN_RE.test(g)) return false;
  let sum = 0;
  for (let i = 0; i < 14; i++) {
    const p = GST_CHARS.indexOf(g[i]) * (i % 2 === 0 ? 1 : 2);
    sum += Math.floor(p / 36) + (p % 36);
  }
  return g[14] === GST_CHARS[(36 - (sum % 36)) % 36];
}

/** Indian mobile (+91 optional) or generic E.164. */
function isValidPhone(raw) {
  const p = String(raw || '').replace(/[\s\-()]/g, '');
  return /^(\+91|91|0)?[6-9][0-9]{9}$/.test(p) || /^\+[1-9][0-9]{7,14}$/.test(p);
}

/** Returns an error string, or null when acceptable. */
function passwordProblem(p) {
  p = String(p || '');
  if (p.length < 8) return 'Password must be at least 8 characters.';
  if (p.length > 128) return 'Password is too long.';
  if (!/[A-Za-z]/.test(p) || !/[0-9]/.test(p)) return 'Password must contain letters and digits.';
  return null;
}

function isValidEmployeeId(id) { return EMPLOYEE_ID_RE.test(String(id || '')); }

/** Synthetic Firebase Auth email for Manager/Staff IDs. Must match the Android app. */
function employeeAuthEmail(id) { return `${String(id).trim().toLowerCase()}@staff.taskflow-ai.app`; }

/** e.g. MGR-A1B2-0007: prefix + 4-char company code + zero-padded counter (<= 20 chars). */
function makeEmployeeId(role, orgId, n) {
  const prefix = role === 'manager' ? 'MGR' : 'STF';
  const code = String(orgId).replace(/^org_/, '').replace(/[^A-Za-z0-9]/g, '').slice(0, 4).toUpperCase();
  return `${prefix}-${code}-${String(n).padStart(4, '0')}`;
}

module.exports = {
  normalizeEmail, isValidEmail, isValidGstin, isValidPhone, passwordProblem,
  isValidEmployeeId, employeeAuthEmail, makeEmployeeId,
};
