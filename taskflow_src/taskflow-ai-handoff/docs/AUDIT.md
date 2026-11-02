# TaskFlow AI — Audit Report (Phase 1)

Scope: archive `taskflow-ai__1_.rar` (RAR5, 1,498 files). Read: build config, manifest, `AuthenticationManager`,
models, repository, `MainAppViewModel` (auth/registration/login sections), `SecureSessionManager` (storage/PIN).
Not yet read line-by-line: dashboards, `AuthScreen`, `SettingsScreen`, `TeamScreens`, `NavigationHost`, `MainActivityKt`.
**Nothing was compiled or run** (no Android SDK / network in the audit environment).

## What the project actually is
A Kotlin + Compose Android prototype (package `com.aistudio.taskflow.qrvkty`, ~6,000 lines) generated in Google AI Studio,
with a Node/HTML browser mock (`server.js`, `web/`). Firebase Auth + Firestore dependencies exist, but the app is built to
**work without them**, which is the opposite of the security requirements.

## Critical findings (verified in source)

| # | Finding | Where |
|---|---------|-------|
| 1 | **Authentication bypass.** If Firebase sign-in fails, *any* email/password logs in. Role is guessed from the email text ("manager"/"staff" in the address). | `AuthenticationManager.signInWithEmail` L139-148 |
| 2 | **Plaintext passwords stored on the device** in SharedPreferences (`registered_accounts`). | `saveAccountLocally` L167-183 |
| 3 | **Plaintext passwords stored in Firestore** (`Member.password`) and placed in the approval payload JSON. | `TaskFlowModels.kt` L22; `ViewModel` L679, L696 |
| 4 | **Manager/Staff login compares passwords client-side** against the Firestore member record. No Firebase identity exists for them. | `ViewModel` L498-508 |
| 5 | **Session defaults to "logged in"** (`is_logged_in` default `true`) and role defaults to `admin`. | `AuthenticationManager` L35, L42; `SecureSessionManager` L44 |
| 6 | **Default MPIN `1234`**, stored plaintext in SharedPreferences, no lockout. Biometric defaults on. | `SecureSessionManager` L22, L46, L220 |
| 7 | **Every repository write swallows exceptions** (`catch (_: Exception) {}`): the UI reports success even when the write failed. | `TaskFlowRepository` throughout |
| 8 | **Role/company supplied by the client**; no Firestore Security Rules in the project; no server-side authorization. | whole project |
| 9 | **Hard-coded demo data in production code**: 35 occurrences (e.g. `Nexus Enterprise Ltd`, `Manager@123`, `Staff@123`, fake GSTIN `27AAPCA1234F1Z5`, auto-filled when GSTIN is blank). | `ViewModel`, `AuthenticationManager` |
| 10 | **No email OTP, no Storage/FCM/App Check/Crashlytics, no Cloud Functions**, no GSTIN validation, no logo upload, no calendar, no recovery flows. | absent |
| 11 | `android:allowBackup="true"` (backs up the plaintext session data); release build has `isMinifyEnabled=false`; no release signing config. | manifest, `app/build.gradle.kts` |
| 12 | Registration writes the org **after** flipping the UI to "logged in" and ignores failure → inconsistent/partial company records. | `registerCompanyWithAdmin` |

## Feature inventory (honest status)
- **Works as UI only / local-state**: three dashboards, task list, team screen, settings, theme, PIN screen, biometric prompt wrapper.
- **Partially real**: Firestore listeners for org/members/tasks/etc. (read path); writes exist but errors are hidden.
- **Missing**: items 10 above, iOS app, Storage logo, calendar, activity feed backed by real events, AI Mode backend, tests.
- **Broken / unsafe**: items 1-9, 12.

## Misc
- `AGP 9.1.1`, Kotlin `2.2.10`, `compileSdk 35`, Firebase BoM `34.17.0`, Compose BoM `2024.09.00` (old vs. other deps), Gradle `9.3.1`.
- Committed build output (`app/build`, `.gradle`, `.kotlin`, ~120 MB) and a `local.properties` with a Windows path should not be in source control.
- `google-services.json` is present (client config, not a secret) for project id `task…mo`; **Firebase Auth/Firestore/Storage console settings were not verifiable from here**.

## What a correct fix requires (not done — cannot be verified here)
1. Cloud Functions (Admin SDK): `registerCompany` (transactional), `sendEmailOtp`/`verifyEmailOtp` (hashed OTP, TTL, attempt limits, email provider key in Secret Manager), `createMember` (generates Manager/Staff ID, creates Auth user, sets custom claims `{orgId, role}`), `resolveLogin` (ID → Auth email mapping), `decideApproval`, `resetCredentials`.
2. App: delete the bypass + local password store; sign in with Firebase only; read role from custom claims; remove `password` from models; Keystore-backed MPIN (salted hash + lockout); `allowBackup=false`.
3. Deploy Firestore/Storage rules (drafts included; they assume `userDirectory/{uid}` written by functions).
4. Emulator-based rules tests, then device tests.

## UPDATE (after hand-over work)
Findings 1-9 and 12 were addressed in `project/` (untested build); item 10 is partly addressed in `functions/` (OTP, Functions, rules). See STATUS.md for the live checklist.

## (Original) deliverables note
- `taskflow-ai-debug-EXISTING-PROTOTYPE.apk` — the debug APK that was already in your upload (not rebuilt by me; **has the security problems above**).
- `install-android.bat` / `install-android.sh` — `adb install -r` + launch.
- `firestore.rules`, `storage.rules` — drafts; **do not deploy until the app/functions are migrated** or the prototype's writes will be blocked.
