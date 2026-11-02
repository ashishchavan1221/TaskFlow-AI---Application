package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import com.example.data.auth.AuthUserData
import com.example.ui.auth.BiometricPromptHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * State representing current security & session status.
 */
data class SecureSessionState(
    val isAuthenticated: Boolean,
    val isLocked: Boolean,
    val isBiometricEnabled: Boolean,
    val isBiometricAvailable: Boolean,
    val pin: String = "", // never holds a real PIN; the PIN is stored only as a salted hash
    val currentUser: AuthUserData? = null,
    val securityNotice: String? = null
)

/**
 * SecureSessionManager manages:
 * 1. Persistent session state across app restarts.
 * 2. Immediate app startup security checks via BiometricPrompt.
 * 3. Enforced 4-digit PIN fallback when biometrics are unavailable, cancelled, or fail.
 * 4. Reactive session locking and unlocking for enterprise-grade security.
 */
class SecureSessionManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("taskflow_secure_session", Context.MODE_PRIVATE)

    private val _sessionState: MutableStateFlow<SecureSessionState>

    val sessionState: StateFlow<SecureSessionState>

    init {
        val isAuth = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        val isBioEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        prefs.edit().remove(KEY_APP_PIN).apply() // purge legacy plaintext PIN
        val hasPin = prefs.getString(KEY_PIN_HASH, null) != null
        val bioAvailable = checkBiometricAvailability(context)

        val savedUser = readSavedUser()

        // If authenticated on startup, app locks by default to enforce immediate biometric/PIN check
        val initialLock = isAuth && (hasPin || isBioEnabled)

        _sessionState = MutableStateFlow(
            SecureSessionState(
                isAuthenticated = isAuth,
                isLocked = initialLock,
                isBiometricEnabled = isBioEnabled,
                isBiometricAvailable = bioAvailable,
                pin = "",
                currentUser = savedUser,
                securityNotice = null
            )
        )
        sessionState = _sessionState.asStateFlow()
    }

    /**
     * Checks if biometric hardware is present and enrolled.
     */
    fun checkBiometricAvailability(ctx: Context = context): Boolean {
        return BiometricPromptHelper.isBiometricAvailable(ctx)
    }

    /**
     * Enforces the immediate startup security check.
     * If user is authenticated and app is locked:
     * - Triggers BiometricPrompt immediately if biometric is available and enabled.
     * - If biometric fails, is cancelled, or is disabled, enforces the 4-digit PIN fallback.
     */
    fun triggerStartupSecurityCheck(
        activity: FragmentActivity,
        onUnlockResult: (Boolean) -> Unit = {}
    ) {
        val current = _sessionState.value
        // If not authenticated, no lock check required
        if (!current.isAuthenticated) {
            _sessionState.value = _sessionState.value.copy(isLocked = false)
            onUnlockResult(true)
            return
        }

        // If already unlocked, allow access
        if (!current.isLocked) {
            onUnlockResult(true)
            return
        }

        val bioAvailable = checkBiometricAvailability(activity)
        _sessionState.value = _sessionState.value.copy(isBiometricAvailable = bioAvailable)

        if (current.isBiometricEnabled && bioAvailable) {
            BiometricPromptHelper.showBiometricPrompt(
                activity = activity,
                title = "TaskFlow Secure Access",
                subtitle = "Authenticate via fingerprint or face recognition",
                negativeButtonText = "Use 4-Digit PIN",
                onSuccess = {
                    unlockSession()
                    onUnlockResult(true)
                },
                onError = { errMsg ->
                    // Fallback directly to 4-digit PIN keypad
                    _sessionState.value = _sessionState.value.copy(
                        isLocked = true,
                        securityNotice = "Biometrics unavailable. Enter 4-digit PIN."
                    )
                    onUnlockResult(false)
                },
                onFailed = {
                    // Fingerprint unrecognized - keep 4-digit PIN fallback ready
                    _sessionState.value = _sessionState.value.copy(
                        isLocked = true,
                        securityNotice = "Authentication failed. Enter 4-digit PIN."
                    )
                    onUnlockResult(false)
                }
            )
        } else {
            // Biometric not enabled or not available on device - enforce 4-digit PIN fallback immediately
            _sessionState.value = _sessionState.value.copy(
                isLocked = true,
                securityNotice = "Enter your 4-digit security PIN to unlock."
            )
            onUnlockResult(false)
        }
    }

    /**
     * Verifies the 4-digit PIN. Unlocks session on match.
     */
    private var failedAttempts = prefs.getInt(KEY_FAILS, 0)
    private var lockedUntil = prefs.getLong(KEY_LOCK_UNTIL, 0L)

    fun hasPin(): Boolean = prefs.getString(KEY_PIN_HASH, null) != null

    private fun hashPin(pin: String, salt: ByteArray): String {
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 120_000, 256)
        val bytes = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    fun verifyPin(inputPin: String): Boolean {
        val now = System.currentTimeMillis()
        if (now < lockedUntil) {
            _sessionState.value = _sessionState.value.copy(
                securityNotice = "Too many attempts. Try again in ${(lockedUntil - now) / 1000 + 1}s."
            )
            return false
        }
        val saltB64 = prefs.getString(KEY_PIN_SALT, null)
        val stored = prefs.getString(KEY_PIN_HASH, null)
        if (saltB64 == null || stored == null) return false
        val salt = android.util.Base64.decode(saltB64, android.util.Base64.NO_WRAP)
        val ok = java.security.MessageDigest.isEqual(
            hashPin(inputPin.trim(), salt).toByteArray(), stored.toByteArray()
        )
        if (ok) {
            failedAttempts = 0; lockedUntil = 0L
            prefs.edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCK_UNTIL, 0L).apply()
            unlockSession()
            return true
        }
        failedAttempts++
        if (failedAttempts >= 5) {
            lockedUntil = now + 30_000L * (1L shl minOf(failedAttempts - 5, 4))
            _sessionState.value = _sessionState.value.copy(
                securityNotice = "Too many attempts. Locked for ${(lockedUntil - now) / 1000}s."
            )
        } else {
            _sessionState.value = _sessionState.value.copy(
                securityNotice = "Incorrect PIN. ${5 - failedAttempts} attempt(s) left."
            )
        }
        prefs.edit().putInt(KEY_FAILS, failedAttempts).putLong(KEY_LOCK_UNTIL, lockedUntil).apply()
        return false
    }

    /**
     * Unlocks the current session.
     */
    fun unlockSession() {
        _sessionState.value = _sessionState.value.copy(
            isLocked = false,
            securityNotice = null
        )
    }

    /**
     * Manually locks the session (e.g. from Settings or background pause).
     */
    fun lockSession() {
        _sessionState.value = _sessionState.value.copy(
            isLocked = true,
            securityNotice = null
        )
    }

    /**
     * Starts a new authenticated session (after login or registration).
     * Marked as unlocked so user directly accesses the Dashboard.
     */
    fun startSession(user: AuthUserData) {
        saveUser(user)
        prefs.edit().putBoolean(KEY_IS_AUTHENTICATED, true).apply()
        _sessionState.value = _sessionState.value.copy(
            isAuthenticated = true,
            isLocked = false,
            currentUser = user,
            securityNotice = null
        )
    }

    /**
     * Ends the session (Sign Out). Clears session data and locks navigation to Auth screen.
     */
    fun endSession(ctx: Context = context) {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, false)
            .remove(KEY_USER_JSON)
            .apply()

        // Also clear legacy taskflow_auth_session for seamless consistency
        val legacyPrefs = ctx.getSharedPreferences("taskflow_auth_session", Context.MODE_PRIVATE)
        legacyPrefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_email")
            .remove("user_uid")
            .remove("user_name")
            .remove("user_role")
            .apply()

        _sessionState.value = _sessionState.value.copy(
            isAuthenticated = false,
            isLocked = true,
            currentUser = null,
            securityNotice = null
        )
    }

    /**
     * Updates the 4-digit security PIN.
     * Enforces exactly 4 numeric digits.
     */
    fun setPin(newPin: String): Boolean {
        val cleanPin = newPin.trim()
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) return false
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PIN_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hashPin(cleanPin, salt))
            .remove(KEY_APP_PIN)
            .apply()
        return true
    }

    /**
     * Toggles biometric unlock preference.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _sessionState.value = _sessionState.value.copy(isBiometricEnabled = enabled)
    }

    private fun saveUser(user: AuthUserData) {
        try {
            val json = JSONObject().apply {
                put("uid", user.uid)
                put("email", user.email)
                put("displayName", user.displayName)
                put("role", user.role)
                put("orgName", user.orgName)
                put("employeeId", user.employeeId)
            }
            prefs.edit().putString(KEY_USER_JSON, json.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun readSavedUser(): AuthUserData? {
        val jsonStr = prefs.getString(KEY_USER_JSON, null)
        if (jsonStr != null) {
            try {
                val json = JSONObject(jsonStr)
                return AuthUserData(
                    uid = json.optString("uid", "usr_default"),
                    email = json.optString("email", "admin@nexusenterprise.com"),
                    displayName = json.optString("displayName", "Vikram Sharma"),
                    role = json.optString("role", "admin"),
                    orgName = json.optString("orgName", ""),
                    employeeId = json.optString("employeeId", "ADM-001")
                )
            } catch (_: Exception) {}
        }
        // Fallback to legacy auth session if present
        val legacyPrefs = context.getSharedPreferences("taskflow_auth_session", Context.MODE_PRIVATE)
        val email = legacyPrefs.getString("user_email", null)
        if (email != null) {
            return AuthUserData(
                uid = legacyPrefs.getString("user_uid", "usr_${email.hashCode()}") ?: "usr_default",
                email = email,
                displayName = legacyPrefs.getString("user_name", "Enterprise User") ?: "Enterprise User",
                role = legacyPrefs.getString("user_role", "admin") ?: "admin",
                orgName = legacyPrefs.getString("user_org", "") ?: "",
                employeeId = legacyPrefs.getString("user_emp_id", "ADM-001") ?: "ADM-001"
            )
        }
        return null
    }

    companion object {
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_PIN = "app_pin"
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_FAILS = "pin_fail_count"
        private const val KEY_LOCK_UNTIL = "pin_lock_until"
    }
}
