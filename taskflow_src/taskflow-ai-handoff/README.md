# TaskFlow AI — hand-off pack
Give this **whole zip** to an AI developer and tell it: **"Read docs/MASTER_PROMPT.md and execute it."**

| Path | What |
|---|---|
| `docs/MASTER_PROMPT.md` | The instruction set for the AI (rules, decisions, order of work) |
| `docs/ANTIGRAVITY_PROMPT.md` | Paste-ready prompt for Google Antigravity |
| `docs/STATUS.md` | Requirement-by-requirement status (honest) |
| `docs/TASKS.md` | Work packages WP0–WP16 with acceptance criteria |
| `docs/ARCHITECTURE.md` | Trust model, data model, flows |
| `docs/SETUP_FIREBASE.md` | Exact backend setup + deploy |
| `docs/TEST_PLAN.md`, `docs/IOS_SPEC.md`, `docs/AUDIT.md` | Tests, iOS spec, original audit |
| `project/` | Android app (Kotlin/Compose) — **edited without a compiler; build it first** |
| `functions/` | Cloud Functions (13) + unit tests (9 pass) |
| `firestore.rules`, `storage.rules`, `firestore.indexes.json`, `firebase.json`, `rules-tests/` | Security + config (rules tests not yet run) |
| `scripts/` | `provision-demo.js` (dev only), `install-android.bat/.sh` |

## You (non-developer) — 5 steps
1. Install **Android Studio** and **Node 20**. 2. Open `project/` in Android Studio, let it sync, press **Build ▸ Build APK(s)**.
3. Follow `docs/SETUP_FIREBASE.md` (Firebase console + deploy). 4. Phone: USB debugging ON, plug in, run `scripts\install-android.bat`.
5. If anything fails, paste the exact error to your AI together with this zip.

**What is NOT in this pack:** a built APK of the new code (the authoring environment could not build), an iOS app, and any verification against a real Firebase project.
