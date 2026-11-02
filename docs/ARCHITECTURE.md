# ARCHITECTURE & DATA MODEL

## Trust boundaries
```
Android app ──(Firebase Auth ID token)──► Cloud Functions (Admin SDK) ──► Firestore / Auth / FCM / Mail / AI
     │  reads/limited writes allowed by firestore.rules (tenant + role + active)
     └──► Firestore / Storage (rules enforce company + role)
```
Only Cloud Functions write: `userDirectory`, `organizations/*` (create), `members` (create/delete), `permission_requests` (decisions), `recovery_requests`, `notifications`, `activity`, `audit_logs`, `securityEvents`, `otpRequests`, `rateLimits`, `gstinIndex`, `meta/counters`, and Auth users/claims.

## Collections
- `userDirectory/{uid}`: `orgId, role(admin|manager|staff), status(active|disabled), memberId, employeeId, displayName, orgName, contactEmail, mustChangePassword, fcmTokens[≤5]`
- `organizations/{orgId}`: `name, gstin, gstinVerifiedByGovernment=false, email, phone, description, address, logoPath, ownerUid, createdAt`
  - `members/{memberId}`: `userId, displayName, employeeId, role, status(active|inactive), email(contact), phone, departmentId/Name, mustChangePassword`
  - `tasks/{taskId}`: `title, description, status(TODO|IN_PROGRESS|COMPLETED), priority(LOW|MEDIUM|HIGH), progress, startDate, dueDate, createdByMemberId, createdByName, assignedToMemberId, teamId, blocked, blockerReason, checklist[], subtasks[], dependsOn[]` (+ `comments/{id}`)
  - `permission_requests/{id}`: `actionType(CREATE_STAFF|DELETE_STAFF|…), managerId, targetStaffId, payloadJson(no secrets), reason, status(pending|approved|rejected), adminNotes, decidedBy, decidedAt`
  - `recovery_requests/{id}`: `kind(id|password), employeeId|fullName, memberId, status(pending|resolved|rejected)`
  - `notifications/{id}`: `toMemberId, type, title, body, taskId, read, createdAt`
  - `activity/{id}`: `type, taskId, summary, at`   ·   `audit_logs/{id}`: `action, actorId, actorRole, targetId, at`
  - `branches/{id}`, `warehouses/{id}`, `settings/general`, `departments/{id}`, `meta/counters` (server only)
- Server-only top level: `otpRequests/{sha256(purpose|email)}` (HMAC hash, never the code), `rateLimits/*`, `gstinIndex/{GSTIN}` (uniqueness), `securityEvents/*`.

## Flows
**Register:** form → `sendEmailOtp(register)` → user types code → `verifyEmailOtp` → signed token (15 min) → `registerCompany` (verifies token+email, GSTIN, password; creates Auth user; Firestore txn creates gstinIndex+org+owner member+settings+directory+counters; sets claims; rolls back Auth user on failure) → app signs in.
**ID login:** app maps ID → `<id>@staff.taskflow-ai.app` → Firebase sign-in → reads own `userDirectory` → requires role∈{manager,staff} & status=active, else sign-out + generic error.
**Create member:** Owner → `createMember` → counter txn → ID `MGR|STF-<ORG4>-<NNNN>` → Auth user with temp password → member+directory (batch) → claims → returns ID+temp password once; `mustChangePassword=true`.
**Approval:** Manager writes `permission_requests` (pending) → Owner `decideApproval` → server executes action → status + audit + notification.
**Recovery:** `requestRecovery` (rate-limited, generic response) → Owner `decideRecovery` → temp password set + tokens revoked, or list of matching IDs.
**Deactivate:** `deactivateMember` → Auth disabled + tokens revoked + directory status disabled.

## Known design limits (be aware)
- Manager→team scoping is not enforced in rules yet (managers can read all org tasks/members). Add `teamId` + `managerId` scoping (WP7/WP10) before claiming the manager-scope requirement.
- Rules read `userDirectory` per request (1 extra read). Consider switching to custom claims in rules for cost.
- `completePasswordChange` clears the flag on trust after the client's `updatePassword`; acceptable for v1, document it.
