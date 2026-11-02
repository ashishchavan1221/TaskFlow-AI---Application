# STATUS & REQUIREMENT TRACEABILITY
Labels: **VERIFIED** (executed, passed) · **IMPL-UNTESTED** (code written, never compiled/run) · **NOT DONE** · **BLOCKED**.
Only two things have been *executed so far*: `functions/` unit tests (9/9 pass), `node --check`/module load of functions, JSON validity of config files, bracket-balance of edited Kotlin.

| ID | Requirement (original spec phase) | Status | Where / note |
|---|---|---|---|
| P1 | Audit existing project | VERIFIED (read-only audit) | docs/AUDIT.md |
| P2.1 | Native Android, Compose, M3 | IMPL-UNTESTED | project/ (must compile first) |
| P2.2 | Native iOS | NOT DONE | docs/IOS_SPEC.md |
| P2.3 | Firebase backend | IMPL-UNTESTED | functions/, rules, firebase.json — never deployed |
| P3.1-3.2 | Design system colours/UX | NOT DONE (audit needed) | WP12 |
| P3.3 | Company logo upload/show | NOT DONE (rules exist) | WP4 |
| P4 | Roles, hierarchy, authorization | IMPL-UNTESTED | rules + functions + app |
| P5.1-5.2 | Registration fields/validation | IMPL-UNTESTED | phone/email/password/GSTIN-checksum validated (logic VERIFIED by unit tests) |
| P5.1b | Branches/warehouses mgmt | NOT DONE | WP5 (rules exist) |
| P5.3 | 6-digit e-mail OTP | IMPL-UNTESTED | policy logic VERIFIED (unit tests); delivery needs Resend key |
| P5.4 | Atomic registration | IMPL-UNTESTED | `registerCompany` (txn + rollback) |
| P6 | Three login portals + isolation | IMPL-UNTESTED | AuthenticationManager + functions |
| P7.1 | Manager/Staff Forgot ID/Password | Backend IMPL-UNTESTED, UI NOT DONE | WP2 |
| P7.2 | Owner recovery via OTP | Backend IMPL-UNTESTED, UI NOT DONE | WP2 |
| P8.1 | MPIN | IMPL-UNTESTED | hash+lockout done; setup prompt after first login, change/disable w/ re-auth: WP13 |
| P8.2 | Biometrics | PARTIAL (existing wrapper) | Keystore binding: WP13 |
| P8.3 | Sessions/revocation | IMPL-UNTESTED | token revoke in functions; client re-check: WP13 |
| P9 | Owner dashboard | NOT AUDITED | WP11 |
| P10 | Manager dashboard + approvals | Approvals IMPL-UNTESTED; dashboard NOT AUDITED | WP11 |
| P11 | Staff dashboard | NOT AUDITED | WP11 |
| P12 | Task pipeline/detail | NOT AUDITED; subtasks/checklists/deps NOT DONE | WP7 |
| P13 | Calendar + reminders | NOT DONE | WP6, WP8 |
| P14 | Team directory | PARTIAL (screen exists) | WP10 |
| P15 | Drawer + bottom nav | NOT AUDITED | WP10 |
| P16 | Profile/settings/company mgmt | NOT AUDITED | WP4, WP5, WP13 |
| P17 | Activity/notifications/audit | Backend IMPL-UNTESTED (triggers, audit_logs, securityEvents); client NOT DONE | WP8 |
| P18 | AI Mode | Backend IMPL-UNTESTED (`aiAssist`); screen NOT DONE | WP9 |
| P19 | Firestore model + indexes | IMPL-UNTESTED | docs/ARCHITECTURE.md, firestore.indexes.json |
| P20 | Firestore/Storage rules + tests | IMPL-UNTESTED (tests written, emulator not run) | rules-tests/ |
| P21 | Exactly one demo account | IMPL-UNTESTED | scripts/provision-demo.js (dev only) |
| P22 | Performance/offline | NOT DONE | WP14 |
| P23 | Test suite | PARTIAL: functions unit tests VERIFIED; rest NOT DONE | docs/TEST_PLAN.md |
| P24 | APK/AAB + USB install | NOT DONE (no SDK in authoring env). Only the ORIGINAL prototype APK existed. | scripts/install-android.* |
| P25 | Final deliverables/report | NOT DONE | docs/FINAL_REPORT.md (you write it) |
