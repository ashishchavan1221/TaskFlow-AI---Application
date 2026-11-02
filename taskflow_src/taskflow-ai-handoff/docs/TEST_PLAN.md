# TEST PLAN (record real results in docs/FINAL_REPORT.md)
## A. Manual smoke test (device, dev Firebase) — run after WP1
1 Register with a real inbox: wrong OTP ×5 → locked; expired code; resend <60 s refused; correct OTP → dashboard.
2 Re-register same e-mail / same GSTIN → refused. Bad GSTIN/phone/password-mismatch → inline errors.
3 Owner creates Manager and Staff → ID + temp password shown once → logs in via correct portal; same creds in wrong portal → rejected; Owner e-mail in ID portal → rejected.
4 Staff cannot see create-task UI; direct Firestore write attempt (emulator/REST) denied. Manager requests staff creation → nothing created until Owner approves.
5 Owner deactivates staff → staff app loses access within one token refresh; ID login refused.
6 Kill/reopen app: logged-in session persists; PIN prompts if set; 5 wrong PINs → lockout; fresh install starts logged out.
## B. Automated
- `cd functions && npm test` (validators/OTP/token: **9 pass at hand-over**).
- `cd rules-tests && npm run test:rules` (written, never run).
- To add: functions emulator tests; `./gradlew test`; `./gradlew connectedDebugAndroidTest`.
## C. Security regression list
Cross-tenant read/write · role escalation via directory/members · company hop · inactive account · task assignment by staff · approval bypass · OTP replay/brute-force/enumeration · recovery enumeration · plaintext secret grep (`grep -rIn "password" app/src | review`) · lock-screen notification content.
