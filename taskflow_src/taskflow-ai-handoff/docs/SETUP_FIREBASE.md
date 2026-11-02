# SETUP — Firebase backend (do on a DEV project first)
Prereqs: Node 20+, `npm i -g firebase-tools`, Java 17 (emulators), a Firebase project on the **Blaze** plan (Functions need it).
1. Console → Authentication → Sign-in method → enable **Email/Password**.
2. Console → Firestore → Create database (production mode, region asia-south1). Console → Storage → get started.
3. `firebase login && firebase use <your-project-id>` (id is inside `app/google-services.json` → `project_info.project_id`).
4. Mail provider: create a Resend account, verify a sender domain, copy the API key.
5. Secrets (random 32+ chars for the first two; `unset` is fine for AI until you have a key):
   ```
   firebase functions:secrets:set OTP_PEPPER
   firebase functions:secrets:set TOKEN_SECRET
   firebase functions:secrets:set MAIL_API_KEY
   firebase functions:secrets:set AI_API_KEY
   ```
   Params: create `functions/.env.<projectId>` with `MAIL_FROM="TaskFlow AI <no-reply@yourdomain>"` and optional `AI_MODEL=...`.
6. `cd functions && npm ci && npm test` (must show 9 pass), then `cd .. && firebase deploy --only functions,firestore,storage`.
7. Rules tests: `cd rules-tests && npm i && npm run test:rules` — fix any failure before trusting the rules.
8. App Check (console → App Check): register Android app with Play Integrity (debug provider for dev), then **enforce** for Firestore, Storage, Functions after the app sends tokens (WP13).
9. Demo account (dev only): `export GOOGLE_APPLICATION_CREDENTIALS=<key outside repo> && node scripts/provision-demo.js`.
10. Android: put your SHA-1/SHA-256 in the Firebase Android app settings, re-download `google-services.json` only if the console says so (never hand-edit).
Cost/abuse: functions use `maxInstances:10`; OTP & recovery are rate-limited in Firestore.
