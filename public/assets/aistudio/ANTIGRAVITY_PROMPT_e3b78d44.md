# PASTE THIS INTO ANTIGRAVITY (Agent panel) — after opening the folder `taskflow-ai-handoff` as the workspace

You are the lead engineer finishing an EXISTING project in this workspace (`taskflow-ai-handoff`). You have a terminal, the Android SDK/Gradle on this machine, and (with my approval) `adb` for my USB-connected Android phone. Generate, build, test and install the complete app **from this workspace**.

## Read first (in this order, fully)
1. `docs/MASTER_PROMPT.md` — rules, architecture decisions, security rules, order of work. These are binding.
2. `docs/STATUS.md`, `docs/ARCHITECTURE.md`, `docs/TASKS.md`, `docs/SETUP_FIREBASE.md`, `docs/TEST_PLAN.md`, `docs/AUDIT.md`.
3. Then the code: `project/` (Android, Kotlin/Compose), `functions/` (Cloud Functions), `firestore.rules`, `storage.rules`, `rules-tests/`.
Do not rewrite working code. Do not switch backend (Firebase only). Do not weaken any security decision.

## Honesty contract
Label every claim VERIFIED (you ran it; show command + output) / IMPLEMENTED-UNTESTED / NOT DONE / BLOCKED (exact unblock steps). Never claim an APK, OTP e-mail, Firebase connection, test pass or iOS build that you did not actually produce. Never hide or suppress errors to get a green build.

## Work plan — execute fully, do not stop after planning
First produce a short Implementation Plan artifact, then proceed without waiting unless a gate below says STOP.

**Gate 0 — Compile.** In `project/`: run `./gradlew assembleDebug` (Windows: `gradlew.bat`). The previous code was edited WITHOUT a compiler: fix every real compile error (BackendApi, AuthenticationManager, MainAppViewModel, AuthScreen, TeamScreens, SecureSessionManager, PinEntryScreen). Log to `docs/BUILD_LOG.md`. Confirm `app/build/outputs/apk/debug/app-debug.apk` exists with `ls -la`.
**Gate 1 — Backend.** `cd functions && npm ci && npm test` (expect 9 pass). `cd rules-tests && npm i && npm run test:rules` — fix rules/tests until they pass (needs Java + firebase-tools).
  **STOP and ask me** for: Firebase login (`firebase login`), project id, Resend API key, and creating secrets (`OTP_PEPPER`, `TOKEN_SECRET`, `MAIL_API_KEY`, `AI_API_KEY`). Never invent them or put them in files. Then `firebase deploy --only functions,firestore,storage` on my DEV project and report the real output.
**Gate 2 — Implement the remaining work packages** from `docs/TASKS.md`, in order, each with tests: WP2 recovery UIs + forced first-login password change · WP3 session revocation checks · WP4 logo/profile upload · WP5 branches/warehouses · WP6 calendar (month/week/day) · WP7 subtasks/checklists/dependencies/comments + manager→team scoping (model + rules + rules tests) · WP8 FCM client, notification inbox, activity screen, WorkManager reminders · WP9 AI Mode screen (`aiAssist`, honest "not configured" state) · WP10 drawer + bottom-nav exactly per spec, Team Directory per role · WP11 audit and fix all dashboards (real Firestore-derived KPIs, documented counting rules, overdue derived) · WP12 design system colours + light/dark/system persistence · WP13 App Check, Crashlytics (no PII), MPIN setup/change/disable with re-auth, Keystore biometric, R8.
  Read every screen under `project/app/src/main/java/com/example/ui/**` line by line before changing it.
**Gate 3 — Test.** Unit tests (`./gradlew test`), Compose/instrumented tests on an emulator or my phone (`./gradlew connectedDebugAndroidTest`), plus the manual smoke test in `docs/TEST_PLAN.md` §A. Fix real failures.
**Gate 4 — Demo account.** With my approval and a DEV project only: `node scripts/provision-demo.js` (service-account key stays OUTSIDE the repo). Verify the printed login really works. Create no other dummy users.
**Gate 5 — Install on my phone.** Run `adb devices`. **STOP and ask me to confirm** the listed device is mine and to approve installing. Then `./gradlew assembleDebug` and `adb install -r app/build/outputs/apk/debug/app-debug.apk`, launch it, and walk through: register → e-mail OTP → login → create Manager and Staff → log in via each portal → wrong-portal rejection → approval flow → deactivation. Capture screenshots as artifacts. If no device is authorized, tell me the exact steps (USB debugging, data cable, tap Allow).
**Gate 6 — Release.** Signing via `keystore.properties` (gitignored; ask me before generating a keystore, never commit it). Build `assembleRelease` and `bundleRelease`; verify the files exist before reporting paths.
**Gate 7 — iOS.** Create the separate native SwiftUI project per `docs/IOS_SPEC.md`. If this machine is not macOS with Xcode, write the code and docs but mark it IMPLEMENTED-UNTESTED / BLOCKED — never claim iOS support.
**Gate 8 — Report.** Write `docs/FINAL_REPORT.md`: every requirement ID in `docs/STATUS.md` with its label, real command output for tests/builds, remaining configuration, known limitations.

## Hard rules
- No plaintext passwords/OTPs/PINs anywhere; no secrets or service-account/keystore files in the repo; no client write path to `userDirectory`, members, roles, `gstin`, `ownerUid`.
- Staff cannot create/assign tasks; managers cannot self-escalate or bypass Owner approval; tenant isolation proven by tests.
- GSTIN is format+checksum validated only — never call it government-verified.
- Commit to git after each gate with a clear message. Ask me only for: Firebase console/CLI login, provider keys, keystore, device approval.

Start now with Gate 0.
