import Foundation
import FirebaseAuth
import FirebaseFirestore

/// Real Firebase-backed authentication manager matching AuthenticationManager.kt.
/// Enforces three portals:
/// 1. Owner Portal (role: admin)
/// 2. Manager Portal (role: manager, ID: MGR-XXXX)
/// 3. Staff Portal (role: staff, ID: STF-XXXX)
public class AuthenticationManager {
    public static let shared = AuthenticationManager()

    private let auth = Auth.auth()
    private let db = Firestore.firestore()
    private let defaults = UserDefaults.standard

    private let kIsLoggedIn = "taskflow_is_logged_in"
    private let kUserUid = "taskflow_user_uid"
    private let kUserEmail = "taskflow_user_email"
    private let kUserName = "taskflow_user_name"
    private let kUserRole = "taskflow_user_role"
    private let kUserOrg = "taskflow_user_org"
    private let kUserEmpId = "taskflow_user_emp_id"
    private let kUserOrgId = "taskflow_user_org_id"

    public var isSessionActive: Bool {
        return defaults.bool(forKey: kIsLoggedIn) && auth.currentUser != nil
    }

    public var savedUser: AuthUserData? {
        guard defaults.bool(forKey: kIsLoggedIn),
              let uid = defaults.string(forKey: kUserUid) else {
            return nil
        }
        return AuthUserData(
            uid: uid,
            email: defaults.string(forKey: kUserEmail) ?? "",
            displayName: defaults.string(forKey: kUserName) ?? "",
            role: defaults.string(forKey: kUserRole) ?? "",
            orgName: defaults.string(forKey: kUserOrg) ?? "",
            employeeId: defaults.string(forKey: kUserEmpId) ?? "",
            orgId: defaults.string(forKey: kUserOrgId) ?? ""
        )
    }

    private func saveSession(user: AuthUserData) {
        defaults.set(true, forKey: kIsLoggedIn)
        defaults.set(user.uid, forKey: kUserUid)
        defaults.set(user.email, forKey: kUserEmail)
        defaults.set(user.displayName, forKey: kUserName)
        defaults.set(user.role, forKey: kUserRole)
        defaults.set(user.orgName, forKey: kUserOrg)
        defaults.set(user.employeeId, forKey: kUserEmpId)
        defaults.set(user.orgId, forKey: kUserOrgId)
    }

    public func clearSession() {
        defaults.set(false, forKey: kIsLoggedIn)
        defaults.removeObject(forKey: kUserUid)
        defaults.removeObject(forKey: kUserEmail)
        defaults.removeObject(forKey: kUserName)
        defaults.removeObject(forKey: kUserRole)
        defaults.removeObject(forKey: kUserOrg)
        defaults.removeObject(forKey: kUserEmpId)
        defaults.removeObject(forKey: kUserOrgId)
        try? auth.signOut()
    }

    // MARK: - Owner Registration (Atomic via BackendApi)
    public func registerOwner(
        companyName: String,
        ownerName: String,
        email: String,
        pass: String,
        gstin: String,
        industry: String,
        verificationToken: String
    ) async throws -> AuthUserData {
        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()

        // 1. Call atomic Cloud Function
        _ = try await BackendApi.shared.registerCompany(fields: [
            "companyName": companyName.trimmingCharacters(in: .whitespacesAndNewlines),
            "ownerName": ownerName.trimmingCharacters(in: .whitespacesAndNewlines),
            "email": cleanEmail,
            "password": pass,
            "gstin": gstin.trimmingCharacters(in: .whitespacesAndNewlines).uppercased(),
            "industry": industry,
            "verificationToken": verificationToken
        ])

        // 2. Sign in to Firebase Auth
        let authResult = try await auth.signIn(withEmail: cleanEmail, password: pass)
        let uid = authResult.user.uid

        // 3. Fetch userDirectory profile
        let docSnap = try await db.document("userDirectory/\(uid)").getDocument()
        guard docSnap.exists, let data = docSnap.data() else {
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "User directory record missing"])
        }

        let user = AuthUserData(
            uid: uid,
            email: cleanEmail,
            displayName: data["displayName"] as? String ?? ownerName,
            role: "admin",
            orgName: data["orgName"] as? String ?? companyName,
            employeeId: "OWNER-001",
            orgId: data["orgId"] as? String ?? ""
        )
        saveSession(user: user)
        return user
    }

    // MARK: - Owner / Direct Email Login
    public func loginOwner(email: String, pass: String) async throws -> AuthUserData {
        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        let authResult = try await auth.signIn(withEmail: cleanEmail, password: pass)
        let uid = authResult.user.uid

        let docSnap = try await db.document("userDirectory/\(uid)").getDocument()
        guard docSnap.exists, let data = docSnap.data() else {
            try? auth.signOut()
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Account directory entry not found."])
        }

        let status = data["status"] as? String ?? "active"
        guard status == "active" else {
            try? auth.signOut()
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Account is deactivated or suspended."])
        }

        let role = (data["role"] as? String ?? "").lowercased()
        guard role == "admin" else {
            try? auth.signOut()
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Please use the Manager or Staff Portal to sign in."])
        }

        let user = AuthUserData(
            uid: uid,
            email: cleanEmail,
            displayName: data["displayName"] as? String ?? authResult.user.displayName ?? "Administrator",
            role: "admin",
            orgName: data["orgName"] as? String ?? "TaskFlow Workspace",
            employeeId: data["employeeId"] as? String ?? "OWNER-001",
            orgId: data["orgId"] as? String ?? ""
        )
        saveSession(user: user)
        return user
    }

    // MARK: - ID-Based Login (Manager / Staff Portals)
    public func loginWithId(employeeId: String, securityPin: String, expectedRole: String) async throws -> AuthUserData {
        let cleanEmpId = employeeId.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()

        // 1. Query Firestore organizations members for matching employeeId
        let querySnap = try await db.collectionGroup("members")
            .whereField("employeeId", isEqualTo: cleanEmpId)
            .getDocuments()

        guard let memberDoc = querySnap.documents.first else {
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "No member found with ID '\(cleanEmpId)'."])
        }

        let memberData = memberDoc.data()
        let status = memberData["status"] as? String ?? "active"
        guard status == "active" else {
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Account has been suspended. Contact your organization administrator."])
        }

        let actualRole = (memberData["role"] as? String ?? "").lowercased()
        if !expectedRole.isEmpty && actualRole != expectedRole.lowercased() {
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Role mismatch: This ID belongs to the \(actualRole.capitalized) portal."])
        }

        // Verify security PIN / password
        let storedPin = memberData["securityAccessKey"] as? String ?? ""
        let storedPass = memberData["password"] as? String ?? ""
        let validPin = (storedPin == securityPin || storedPass == securityPin)

        guard validPin else {
            throw NSError(domain: "Auth", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid Security PIN or password credentials."])
        }

        // Sign in or link to Firebase Auth session
        let email = memberData["email"] as? String ?? "\(cleanEmpId.lowercased())@staff.taskflow-ai.app"
        let passToUse = storedPass.isEmpty ? securityPin : storedPass

        var uid = memberData["userId"] as? String ?? memberDoc.documentID
        do {
            let res = try await auth.signIn(withEmail: email, password: passToUse)
            uid = res.user.uid
        } catch {
            // If shadow auth user not yet provisioned, authenticate anonymously or reuse uid
            if auth.currentUser == nil {
                let anon = try await auth.signInAnonymously()
                uid = anon.user.uid
            }
        }

        let orgId = memberData["organizationId"] as? String ?? (memberDoc.reference.parent.parent?.documentID ?? "")
        let orgName = memberData["departmentName"] as? String ?? "TaskFlow Enterprise"

        let user = AuthUserData(
            uid: uid,
            email: email,
            displayName: memberData["displayName"] as? String ?? cleanEmpId,
            role: actualRole,
            orgName: orgName,
            employeeId: cleanEmpId,
            orgId: orgId
        )
        saveSession(user: user)
        return user
    }
}
