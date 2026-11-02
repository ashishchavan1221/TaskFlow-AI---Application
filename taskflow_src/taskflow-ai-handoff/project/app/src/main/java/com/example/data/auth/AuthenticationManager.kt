package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AuthUserData(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: String,
    val orgName: String,
    val employeeId: String = "",
    val orgId: String = ""
)

/**
 * Real Firebase-backed authentication.
 *
 *  - There is NO offline / "accept any credentials" fallback.
 *  - No password is ever stored on the device or in Firestore.
 *  - Role, company and active-status come from userDirectory/{uid} in Firestore,
 *    never from the email text or from local preferences.
 *  - Owner portal accepts only role "admin"; ID portal accepts only "manager"/"staff".
 *
 * NOTE: userDirectory is written from the Owner's device here because the project has no
 * Cloud Functions yet. It MUST be locked down by Firestore rules (see firestore.rules) and
 * moved to a Cloud Function before production use.
 */
class AuthenticationManager(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("taskflow_auth_session", Context.MODE_PRIVATE)

    val isSessionActive: Boolean
        get() = auth.currentUser != null && prefs.getBoolean("is_logged_in", false)

    /** Cached profile for the currently signed-in Firebase user, or null. */
    val savedUser: AuthUserData?
        get() {
            val fbUser = auth.currentUser ?: return null
            if (!prefs.getBoolean("is_logged_in", false)) return null
            if (prefs.getString("user_uid", null) != fbUser.uid) return null
            return AuthUserData(
                uid = fbUser.uid,
                email = prefs.getString("user_email", "") ?: "",
                displayName = prefs.getString("user_name", "") ?: "",
                role = prefs.getString("user_role", "") ?: "",
                orgName = prefs.getString("user_org", "") ?: "",
                employeeId = prefs.getString("user_emp_id", "") ?: "",
                orgId = prefs.getString("user_org_id", "") ?: ""
            )
        }

    // ---------------------------------------------------------------- Owner registration



    // ---------------------------------------------------------------- Logins

    /** Owner / Admin portal: company email + password. */
    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUserData> {
        val cleanEmail = email.trim().lowercase()
        if (!EMAIL_REGEX.matches(cleanEmail) || pass.isEmpty()) {
            return Result.failure(Exception("Enter a valid email and password."))
        }
        return signInAndResolve(cleanEmail, pass, allowedRoles = setOf("admin"))
    }

    /** Manager / Staff portal: Employee ID + password. */
    suspend fun signInWithEmployeeId(employeeId: String, pass: String): Result<AuthUserData> {
        val id = employeeId.trim()
        if (!ID_REGEX.matches(id) || pass.isEmpty()) {
            return Result.failure(Exception("Enter your Employee ID and password."))
        }
        return signInAndResolve(emailForEmployeeId(id), pass, allowedRoles = setOf("manager", "staff"))
    }

    private suspend fun signInAndResolve(
        authEmail: String,
        pass: String,
        allowedRoles: Set<String>
    ): Result<AuthUserData> {
        return try {
            val res = auth.signInWithEmailAndPassword(authEmail, pass).await()
            val fbUser = res.user ?: return Result.failure(Exception("Sign-in failed."))
            val snap = db.collection("userDirectory").document(fbUser.uid).get().await()

            val role = snap.getString("role")
            val status = snap.getString("status") ?: "active"
            val orgId = snap.getString("orgId")
            if (!snap.exists() || role.isNullOrBlank() || orgId.isNullOrBlank()) {
                auth.signOut()
                return Result.failure(Exception("This account is not linked to a company. Contact your company Owner."))
            }
            if (status != "active") {
                auth.signOut()
                return Result.failure(Exception("This account is disabled. Contact your company Owner."))
            }
            if (role !in allowedRoles) {
                // Wrong portal: do not reveal what the account actually is.
                auth.signOut()
                return Result.failure(Exception("Incorrect credentials for this login portal."))
            }
            val user = AuthUserData(
                uid = fbUser.uid,
                email = if (role == "admin") authEmail else (snap.getString("contactEmail") ?: ""),
                displayName = snap.getString("displayName") ?: fbUser.displayName ?: "",
                role = role,
                orgName = snap.getString("orgName") ?: "",
                employeeId = snap.getString("employeeId") ?: "",
                orgId = orgId
            )
            saveActiveSession(user)
            Result.success(user)
        } catch (e: Exception) {
            try { auth.signOut() } catch (_: Exception) {}
            Result.failure(Exception(friendly(e)))
        }
    }

    // ---------------------------------------------------------------- Manager / Staff accounts

    /**
     * Creates a real Firebase Auth account for a Manager/Staff member WITHOUT signing the Owner out,
     * by using a secondary FirebaseApp instance. Returns the new uid.
     */




 catch (_: Exception) { false }

    suspend fun resolveOrgId(uid: String): String? = try {
        db.collection("userDirectory").document(uid).get().await().getString("orgId")
    } catch (_: Exception) { null }

    // ---------------------------------------------------------------- Session

    fun saveActiveSession(user: AuthUserData) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_uid", user.uid)
            .putString("user_email", user.email)
            .putString("user_name", user.displayName)
            .putString("user_role", user.role)
            .putString("user_org", user.orgName)
            .putString("user_emp_id", user.employeeId)
            .putString("user_org_id", user.orgId)
            .apply()
    }

    suspend fun signOut(context: Context) {
        prefs.edit().clear().apply()
        try { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) } catch (_: Exception) {}
        try { auth.signOut() } catch (_: Exception) {}
    }

    // ---------------------------------------------------------------- Helpers

    private fun emailForEmployeeId(id: String) = "${id.trim().lowercase()}@staff.taskflow-ai.app"

    private fun validatePassword(p: String): String? = when {
        p.length < 8 -> "Password must be at least 8 characters."
        p.none { it.isLetter() } || p.none { it.isDigit() } -> "Password must contain letters and digits."
        else -> null
    }

    /** Generic messages on purpose: never reveal whether an account exists. */
    private fun friendly(e: Exception): String = when (e) {
        is FirebaseNetworkException -> "No internet connection. Please try again."
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Incorrect ID/email or password."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
        is FirebaseAuthWeakPasswordException -> "Password is too weak."
        else -> e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }

    companion object {
        private const val MANAGED_APP = "taskflow-managed-accounts"
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        private val ID_REGEX = Regex("^[A-Za-z0-9\\-]{3,20}$")
    }
}
