package com.example.data.backend

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

/**
 * Thin client for the Cloud Functions in /functions. All identity, role and credential changes go through here;
 * the app itself can no longer write those records (see firestore.rules). Region must match setGlobalOptions().
 */
class BackendApi {
    private val functions by lazy { FirebaseFunctions.getInstance("asia-south1") }

    private suspend fun call(name: String, data: Map<String, Any?>): Result<Map<String, Any?>> = try {
        val res = functions.getHttpsCallable(name).call(data).await()
        @Suppress("UNCHECKED_CAST")
        Result.success((res.data as? Map<String, Any?>) ?: emptyMap())
    } catch (e: FirebaseFunctionsException) {
        Result.failure(Exception(e.message?.takeIf { it.isNotBlank() } ?: "Request failed (${e.code})."))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Network error. Please try again."))
    }

    suspend fun sendEmailOtp(email: String, purpose: String) =
        call("sendEmailOtp", mapOf("email" to email, "purpose" to purpose))

    /** Returns the short-lived verification token on success. */
    suspend fun verifyEmailOtp(email: String, purpose: String, code: String): Result<String> =
        call("verifyEmailOtp", mapOf("email" to email, "purpose" to purpose, "code" to code))
            .mapCatching { it["verificationToken"] as? String ?: error("Verification failed.") }

    suspend fun registerCompany(fields: Map<String, Any?>) = call("registerCompany", fields)

    suspend fun createMember(name: String, role: String, phone: String, departmentId: String, departmentName: String, contactEmail: String) =
        call("createMember", mapOf(
            "name" to name, "role" to role, "phone" to phone,
            "departmentId" to departmentId, "departmentName" to departmentName, "contactEmail" to contactEmail
        ))

    suspend fun deactivateMember(memberId: String) = call("deactivateMember", mapOf("memberId" to memberId))

    suspend fun decideApproval(requestId: String, approve: Boolean, notes: String) =
        call("decideApproval", mapOf("requestId" to requestId, "decision" to if (approve) "approve" else "reject", "notes" to notes))

    suspend fun requestPasswordRecovery(employeeId: String) =
        call("requestRecovery", mapOf("kind" to "password", "employeeId" to employeeId))

    suspend fun requestIdRecovery(fullName: String, companyEmail: String) =
        call("requestRecovery", mapOf("kind" to "id", "fullName" to fullName, "companyEmail" to companyEmail))

    suspend fun decideRecovery(requestId: String, approve: Boolean) =
        call("decideRecovery", mapOf("requestId" to requestId, "decision" to if (approve) "approve" else "reject"))

    suspend fun ownerResetPassword(email: String, verificationToken: String, newPassword: String) =
        call("ownerResetPassword", mapOf("email" to email, "verificationToken" to verificationToken, "newPassword" to newPassword))

    suspend fun registerFcmToken(token: String) = call("registerFcmToken", mapOf("token" to token))

    suspend fun completePasswordChange() = call("completePasswordChange", emptyMap())

    suspend fun aiAssist(question: String) = call("aiAssist", mapOf("question" to question))
}
