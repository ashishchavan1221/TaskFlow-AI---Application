# MASTER PROMPT — TaskFlow AI (hand this file + the whole zip to your AI developer)

You are a **principal mobile engineer + Firebase backend engineer + security engineer + QA engineer**.
This zip is an **existing project that has already been audited and partly repaired**. Your job is to **finish it, verify it, and ship it** —
not to start over and not to produce a demo. Read this file fully, then `docs/STATUS.md`, `docs/ARCHITECTURE.md`, `docs/TASKS.md`.

---
## 0. Honesty contract (highest priority)
1. **Never claim something works unless you ran it** and can show the command and its output. Use these exact labels in every report:
   `VERIFIED` (you ran it, passed) · `IMPLEMENTED-UNTESTED` · `NOT DONE` · `BLOCKED (reason + exact steps to unblock)`.
2. The previous engineer (an AI with **no Android SDK, no network to Google/Maven, no emulator**) edited Kotlin **without compiling it**.
   **Step one is always `./gradlew assembleDebug` and fixing real compile errors.** Do not suppress errors, lint or tests to get green.
3. Do not fabricate Firebase connectivity, OTP delivery, test results, APKs, demo logins or iOS support.
   An APK exists only if `ls -la app/build/outputs/apk/...` shows it.
4. If a credential/service/device is unavailable, stop that item, document the blocker, continue with the rest.
5. Do not weaken security to make a feature easier. Never commit secrets, keystores or service-account JSON.

## 1. What this app is
Multi-tenant task management for companies: **Company → Owner/Admin → Managers → Staff**. Native Android (Kotlin, Jetpack Compose, Material 3) on **Firebase**
(Auth, Firestore, Storage, FCM, Cloud Functions, App Check). Region **asia-south1**; Indian business context (GSTIN, +91 phones, IST).
Package id: `com.aistudio.taskflow.qrvkty` (keep it). Existing Firebase project config: `app/google-services.json` (never replace with invented values).

## 2. Ground truth about the code you received
Original upload was an AI-Studio prototype with **critical security holes** (any-credentials login, plaintext passwords on device and in Firestore,
default PIN 1234, errors swallowed, seeded demo users with known passwords, no rules). See `docs/AUDIT.md`. Since then:

**Done in `project/`** (IMPLEMENTED-UNTESTED — never compiled):
- Firebase-only login; no bypass; no local password store; role/company from `userDirectory/{uid}`; portal isolation (email→admin only, ID→manager/staff only).
- Manager/Staff accounts are real Firebase accounts (synthetic e-mail `<id>@staff.taskflow-ai.app`), created **only by Cloud Functions**.
- 2-step company registration UI with 6-digit e-mail OTP (client side), calling `BackendApi` → Cloud Functions.
- PIN stored as PBKDF2 hash + salt, 5-try lockout with backoff, no default PIN, biometric off by default, sessions default logged-out, `allowBackup=false`.
- Demo data / one-tap demo logins / fake GSTIN removed. GSTIN checksum validation.
- Approvals, member creation, deactivation executed server-side.

**Done in `functions/`** (13 callable/trigger functions; **syntax-checked, loads, 9 unit tests pass** for validators/OTP/token logic; **never run against a real Firebase project**):
`sendEmailOtp, verifyEmailOtp, registerCompany, createMember, deactivateMember, decideApproval, requestRecovery, decideRecovery, ownerResetPassword, completePasswordChange, registerFcmToken, onTaskCreated, onTaskUpdated, aiAssist`.

**Done as files, never executed:** `firestore.rules`, `storage.rules`, `firestore.indexes.json`, `rules-tests/` (emulator tests), `scripts/provision-demo.js`.

**Not done:** see `docs/TASKS.md` (work packages WP2–WP16): recovery UIs, logo/profile upload, branches/warehouses, calendar, subtasks/checklists/dependencies,
notification inbox + FCM client, AI Mode screen, drawer/bottom-nav audit, KPI audit, design-system colours, App Check/Crashlytics, tests, iOS, release signing.
Screens never read line-by-line by the previous engineer: `ui/dashboard/*`, `ui/settings/SettingsScreen.kt`, `ui/team/*` (partly), `ui/navigation/NavigationHost.kt` (partly), `MainActivityKt.kt`.

## 3. Non-negotiable architecture decisions (do not change without written approval)
- **Backend = Firebase only.** No Supabase/Mongo/SQL.
- **Trusted identity = `userDirectory/{uid}` + custom claims `{orgId, role}`**, written only by Admin SDK (Cloud Functions). Clients can never write it.
  Rules and functions re-check role on every privileged call. Hiding a button is never security.
- **Manager/Staff ID login** = deterministic server-created Firebase account, email `<id lower>@staff.taskflow-ai.app`. Employee IDs look like `MGR-A1B2-0007` (globally unique via org code).
  Passwords are never stored or readable by anyone; Owner can only **reset** (temporary password, shown once) — never read.
- **OTP**: 6 digits (CSPRNG), HMAC-SHA256 hashed with a server secret, 10-min TTL, single use, 60 s resend cooldown, 5 wrong tries burn it, 5 sends/hour,
  rate-limited per IP. Delivered through a transactional mail API (Resend) from a function. Firebase's built-in e-mail verification is **not** a 6-digit OTP — do not pretend it is.
- **GSTIN**: format + check-digit validation only; always stored with `gstinVerifiedByGovernment:false`. Never claim government verification.
- **Overdue** is derived (due date passed and status ≠ COMPLETED), never a stored status. Status model `TODO → IN_PROGRESS → COMPLETED` (+ BLOCKED flag).
- **Staff cannot create/assign tasks.** Managers cannot create Owners, change their own role, or perform restricted actions without Owner approval (`permission_requests` → `decideApproval`).
- **Notifications**: lock-screen text is generic (“You have a new update.”); details only in-app.
- **AI**: server-side only (`aiAssist`), key in Secret Manager, data scoped by role+tenant; shows “not configured” when no key. Never invent answers/analytics.
- **iOS** = separate native SwiftUI app (optionally sharing Kotlin Multiplatform domain code). Do not claim iOS until it builds in Xcode on macOS.

## 4. Design system (default)
Dark: bg `#050817`, secondary `#0B1024`, card `#101936`, elevated `#16213D`, primary `#6366F1`, secondary `#8B5CF6`, cyan `#22D3EE`, text `#F8FAFC`, text-2 `#A8B3CC`.
Light: bg `#F7F8FC`, surface/card `#FFFFFF`, primary `#4F46E5`, secondary `#7C3AED`, highlight `#0891B2`, text `#111827`, text-2 `#5B6475`, border `#E5E7EB`.
Restrained blue→violet gradients only on primary actions/selection/progress. WCAG AA contrast, dynamic type, empty/skeleton/error/retry states, persisted theme (light/dark/system).
Company logo (Owner upload → Firebase Storage `companies/{orgId}/logo/…`, path in Firestore) shows in drawer, dashboards, company screens; default is the TaskFlow AI icon. Validate type/size/decode (png/jpeg/webp, ≤2 MB).

## 5. Functional spec (condensed — full traceability in `docs/STATUS.md`)
- **Registration (Owner)**: name, company name, company e-mail, password+confirm, phone, GSTIN mandatory; optional description, logo, address, branches, warehouses (structured, many). E-mail OTP before completion; duplicate-company and duplicate-submission safe; atomic create.
- **Three portals**: Owner (e-mail+password), Manager (ID+password), Staff (ID+password). Server verifies role+tenant+active after auth; wrong-portal attempts rejected generically and logged in `securityEvents`.
- **Recovery**: Manager/Staff Forgot ID / Forgot Password → request → Owner reviews → reissue ID / temp password (sessions revoked, event logged). Owner password recovery via OTP (`owner_reset`) → `ownerResetPassword`.
- **App lock**: 4-digit MPIN (set/confirm/unlock/change/disable, lockout, re-auth for changes), biometrics via BiometricPrompt, local lock only — never grants roles.
- **Dashboards**: Owner (oversight/delegation/KPIs/team/approvals), Manager (execute+supervise, approvals requests, escalation), Staff (my/today/upcoming, checklists, blockers, submit for review). KPIs from backend data with precise counting rules (no duplicates/deleted).
- **Tasks**: pipeline with search + filters (All/To Do/In Progress/Completed/Not Completed/Overdue/High/Medium/Low), cards (title, desc, priority, status, progress %, creator + assignee real names, dates, team, overdue badge), detail (subtasks, checklist, dependencies, comments, history, attachments if enabled), transactional updates.
- **Calendar** (drawer): month/week/day, filters, open task, create task if role allows, reminders (WorkManager local + FCM), timezone-safe.
- **Team Directory**: manager sees team name, manager, staff names + IDs + status; owner sees company; staff sees only permitted; never passwords/private contact.
- **Navigation**: drawer (logo, company, user+role, Dashboard, AI Mode, Activity, Task Pipeline, Team Directory, Calendar, Settings, Security, Logout) + bottom bar (Home, Tasks, Settings), no overlap with system bars.
- **Profile/Settings/Company/Security**: edit profile + photo; theme; MPIN/biometric; password change; owner-only company settings, branches, warehouses, GSTIN (validated).
- **Activity/Notifications/Audit**: events with actor, time, target, tenant, type; paginated; FCM + in-app inbox with read state.
- **Offline/performance**: Firestore persistence, visible pending/failed/synced states, no silent stale data, bounded/paginated queries, image resizing.

## 6. Execution order (do not skip steps; commit after each)
1. **Compile**: open `project/` in Android Studio (JDK 17+), sync, `./gradlew assembleDebug`. Fix errors truthfully. Record them in `docs/BUILD_LOG.md`.
2. **Backend up**: follow `docs/SETUP_FIREBASE.md` (dev project first): enable Auth(Email/Password), Firestore, Storage, Functions (Blaze), set secrets, `firebase deploy`.
   Run `cd functions && npm test`, then emulator tests: `cd rules-tests && npm i && npm run test:rules`. Fix failures in rules/functions.
3. **Smoke test on a device** (see `docs/TEST_PLAN.md` §A): register → OTP → login → create manager/staff → portal isolation → approvals → deactivate.
4. Work packages **WP2…WP16** in `docs/TASKS.md`, each with its acceptance criteria and tests.
5. Provision the **single** dev demo account with `scripts/provision-demo.js` (dev project only), document the credentials it printed, verify login. No other dummy users.
6. Release: signing config via `keystore.properties` (outside git), `assembleRelease`/`bundleRelease`, verify files exist, document ADB install (`scripts/install-android.*`).
7. Final report `docs/FINAL_REPORT.md`: every requirement ID from `docs/STATUS.md` with VERIFIED / IMPLEMENTED-UNTESTED / NOT DONE / BLOCKED, real test output, known limitations.

## 7. Security checklist you must re-verify before declaring done
No plaintext passwords/OTP/PIN anywhere · no secrets in app or git · no client write path to `userDirectory`, `organizations/*/members`, roles, `gstin`, `ownerUid` ·
cross-tenant reads/writes denied (rules tests) · inactive/revoked accounts lose access (token revoke + `status`) · staff cannot create/assign tasks · manager cannot self-escalate ·
approval cannot be bypassed · OTP brute force/expiry/replay tests · logs contain no PII/secrets · App Check enforced on Firestore/Storage/Functions · `allowBackup=false` ·
release build minified with signing outside repo.

## 8. Reporting format for each work package
`WPx — status label — what changed (files) — commands run + output excerpts — tests added — remaining risks`.
Stop and ask the owner only for: Firebase console actions, mail/AI provider keys, signing keystore, physical-device/macOS access.
