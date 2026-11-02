# iOS SPEC (NOT STARTED — do not claim support until it builds in Xcode on macOS)
Separate native app: **SwiftUI**, iOS 16+, Firebase iOS SDK (Auth, Firestore, Storage, Messaging, Functions, AppCheck), same project, region `asia-south1`.
Reuse backend and rules unchanged; call the same callable functions (names/payloads in `functions/index.js` and `BackendApi.kt`).
Must match: three portals (ID→`<id>@staff.taskflow-ai.app`), OTP registration, role dashboards, task pipeline/detail, calendar (EventKit not required), team directory, theme (palette in MASTER_PROMPT §4), MPIN (Keychain + PBKDF2/CryptoKit salted hash, lockout), Face ID/Touch ID via `LocalAuthentication` (`NSFaceIDUsageDescription`), APNs via FCM, App Check (App Attest).
Optional: share domain models/validators via Kotlin Multiplatform. Deliver Xcode build + signing instructions (team ID, bundle id, provisioning) and real simulator/device test results.
