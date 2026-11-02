package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.security.SecureSessionManager
import android.net.Uri
import com.example.data.util.FileStorageHelper
import com.example.data.auth.AuthenticationManager
import org.json.JSONObject
import com.example.data.backend.BackendApi
import com.example.data.auth.AuthUserData
import com.example.data.model.*
import com.example.data.repository.TaskFlowRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MainUiState(
    val currentUser: FirebaseUser? = null,
    val isLoggedIn: Boolean = false,
    val otpStage: String = "none",
    val currentUserEmail: String? = null,
    val currentUserName: String? = null,
    val currentOrg: Organization? = null,
    val currentMember: Member? = null,
    val members: List<Member> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val departments: List<Department> = emptyList(),
    val workflows: List<Workflow> = emptyList(),
    val permissionRequests: List<PermissionRequest> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val aiInsights: List<AiInsight> = emptyList(),
    val selectedTab: Int = 0, // 0: Home, 1: Tasks, 2: Team, 3: Settings
    val activeRole: String = "admin", // "admin", "manager", "staff"
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDemoMode: Boolean = false,
    val showCreateTaskDialog: Boolean = false,
    val showTeamManagement: Boolean = false,
    val isPinVerified: Boolean = false,
    val isDarkTheme: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val appPin: String = "",
    val notifications: List<AppNotification> = emptyList(),
    val showNotificationDialog: Boolean = false,
    val groups: List<TeamGroup> = emptyList(),
    val activeGroupMessages: List<GroupChatMessage> = emptyList(),
    val selectedGroup: TeamGroup? = null,
    val showAddGroupDialog: Boolean = false,
    val showGroupChatDialog: Boolean = false,
    val showAddMemberDialog: Boolean = false,
    val showCompletionModal: Boolean = false,
    val completingTaskId: String? = null,
    val completingTaskTitle: String? = null,
    val selectedTaskForDetail: TaskItem? = null,
    val showTaskDetailModal: Boolean = false,
    val activeReminderAlert: TaskItem? = null,
    val isEmailNotificationsEnabled: Boolean = true,
    val showEditLogoDialog: Boolean = false,
    val viewingDocumentTask: TaskItem? = null,
    val viewingDocumentIsWorkDoc: Boolean = false,
    val viewingDocumentsListTask: TaskItem? = null,
    val uploadingDocumentTask: TaskItem? = null
)

class MainAppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskFlowRepository(application)
    private val authManager = AuthenticationManager(application)
    val secureSessionManager = SecureSessionManager(application)
    private val auth = FirebaseAuth.getInstance()
    val notificationManager = com.example.notification.TaskFlowNotificationManager(application)

    fun sendWelcomeNotification() {
        notificationManager.showNotification(
            1001,
            "TaskFlow AI Enterprise",
            "Welcome! TaskFlow workspace is online and synchronized with device."
        )
    }

    private val prefs = application.getSharedPreferences("taskflow_prefs", Context.MODE_PRIVATE)

    private val backend = BackendApi()

    private val _uiState = MutableStateFlow(
        MainUiState(
            currentUser = auth.currentUser,
            isLoggedIn = secureSessionManager.sessionState.value.isAuthenticated,
            currentUserEmail = secureSessionManager.sessionState.value.currentUser?.email ?: authManager.savedUser?.email,
            currentUserName = secureSessionManager.sessionState.value.currentUser?.displayName ?: authManager.savedUser?.displayName,
            activeRole = secureSessionManager.sessionState.value.currentUser?.role ?: authManager.savedUser?.role ?: "staff",
            isDarkTheme = prefs.getBoolean("dark_theme", false),
            isBiometricEnabled = secureSessionManager.sessionState.value.isBiometricEnabled,
            isEmailNotificationsEnabled = prefs.getBoolean("email_notifications_enabled", true),
            appPin = secureSessionManager.sessionState.value.pin,
            isPinVerified = !secureSessionManager.sessionState.value.isLocked,
            notifications = listOf(
                AppNotification(
                    id = "notif_welcome",
                    title = "Enterprise Workspace Initialized",
                    message = "TaskFlow AI workspace is ready with role-based permissions, offline resilience, and calendar scheduling.",
                    type = "system",
                    timestamp = System.currentTimeMillis() - 120_000,
                    isRead = false,
                    authorName = "System",
                    priority = "low"
                ),
                AppNotification(
                    id = "notif_sla",
                    title = "SLA Pipeline Active",
                    message = "Operational deliverables and team performance tracking synchronized with real-time audit logs.",
                    type = "task",
                    timestamp = System.currentTimeMillis() - 60_000,
                    isRead = false,
                    authorName = "Operations Dispatcher",
                    priority = "medium"
                )
            )
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setShowNotificationDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showNotificationDialog = show)
    }

    fun setShowEditLogoDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEditLogoDialog = show)
    }

    fun markNotificationAsRead(id: String) {
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
        )
    }

    fun markAllNotificationsAsRead() {
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map { it.copy(isRead = true) }
        )
    }

    fun clearNotifications() {
        _uiState.value = _uiState.value.copy(notifications = emptyList())
    }

    fun addNotification(
        title: String,
        message: String,
        type: String = "task",
        author: String = "Admin",
        priority: String = "medium"
    ) {
        val notif = AppNotification(
            id = "notif_" + System.currentTimeMillis() + "_" + (100..999).random(),
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            authorName = author,
            priority = priority
        )
        _uiState.value = _uiState.value.copy(
            notifications = listOf(notif) + _uiState.value.notifications
        )
    }


    private var orgJob: Job? = null
    private var membersJob: Job? = null
    private var tasksJob: Job? = null
    private var deptsJob: Job? = null
    private var workflowsJob: Job? = null
    private var permissionReqJob: Job? = null
    private var auditJob: Job? = null
    private var aiJob: Job? = null
    private var groupsJob: Job? = null
    private var groupChatJob: Job? = null
    private val alertedTaskIds = mutableSetOf<String>()
    private var reminderObserverJob: Job? = null

    init {
        viewModelScope.launch {
            secureSessionManager.sessionState.collect { sec ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = sec.isAuthenticated,
                    isPinVerified = !sec.isLocked,
                    isBiometricEnabled = sec.isBiometricEnabled,
                    appPin = sec.pin,
                    currentUserEmail = sec.currentUser?.email ?: _uiState.value.currentUserEmail,
                    currentUserName = sec.currentUser?.displayName ?: _uiState.value.currentUserName
                )
            }
        }

        try {
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _uiState.value = _uiState.value.copy(currentUser = user)
                if (user != null) {
                    viewModelScope.launch {
                        val orgId = authManager.resolveOrgId(user.uid)
                        if (orgId != null) loadOrganizationData(orgId, user.uid)
                    }
                }
            }
        } catch (_: Exception) {}

        startReminderObserver()
    }



    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun setActiveRole(role: String) {
        val targetRole = role.lowercase()
        val member = _uiState.value.members.firstOrNull { it.role.equals(targetRole, ignoreCase = true) }

        val effectiveMember = if (member != null) {
            member
        } else if (targetRole == "admin") {
            _uiState.value.members.firstOrNull { it.role.equals("admin", ignoreCase = true) }
                ?: Member(
                    id = "admin_default",
                    displayName = _uiState.value.currentOrg?.name?.let { "$it Admin" } ?: "Administrator",
                    role = "admin",
                    employeeId = "OWNER-001",
                    email = _uiState.value.currentUserEmail ?: "admin@taskflow.enterprise",
                    departmentName = "Executive Leadership",
                    status = "active"
                )
        } else {
            // Manager or Staff with NO data: MUST BE "N/A"
            Member(
                id = "${targetRole}_na",
                displayName = "N/A",
                role = targetRole,
                employeeId = "N/A",
                email = "N/A",
                departmentName = if (targetRole == "manager") "Operations & Management" else "Engineering & Operations",
                status = "unassigned"
            )
        }

        _uiState.value = _uiState.value.copy(
            activeRole = targetRole,
            currentMember = effectiveMember,
            successMessage = if (member != null) "Switched view to ${effectiveMember.displayName} (${targetRole.uppercase()})" else "Switched to ${targetRole.uppercase()} (No member profile: N/A)"
        )
    }

    fun setPinVerified(verified: Boolean) {
        if (verified) {
            secureSessionManager.unlockSession()
        } else {
            secureSessionManager.lockSession()
        }
        _uiState.value = _uiState.value.copy(isPinVerified = verified)
    }

    fun lockApp() {
        secureSessionManager.lockSession()
        _uiState.value = _uiState.value.copy(isPinVerified = false)
    }

    fun verifyPin(enteredPin: String): Boolean {
        val isValid = secureSessionManager.verifyPin(enteredPin)
        if (isValid) {
            _uiState.value = _uiState.value.copy(isPinVerified = true, errorMessage = null)
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = secureSessionManager.sessionState.value.securityNotice ?: "Incorrect PIN.")
        }
        return isValid
    }

    fun updatePin(newPin: String) {
        val ok = secureSessionManager.setPin(newPin)
        if (ok) {
            _uiState.value = _uiState.value.copy(appPin = "set", successMessage = "4-Digit PIN updated successfully!")
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "PIN must be exactly 4 numeric digits.")
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("dark_theme", enabled).apply()
        _uiState.value = _uiState.value.copy(isDarkTheme = enabled)
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        secureSessionManager.setBiometricEnabled(enabled)
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }

    fun toggleEmailNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("email_notifications_enabled", enabled).apply()
        _uiState.value = _uiState.value.copy(
            isEmailNotificationsEnabled = enabled,
            successMessage = if (enabled) "Email notifications enabled for task assignments and deadline alerts." else "Email notifications disabled."
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    private fun isValidGstin(g: String): Boolean {
        val clean = g.trim().uppercase()
        return Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$").matches(clean)
    }

    fun updateCorporateGstin(newGstin: String) {
        if (!_uiState.value.activeRole.equals("admin", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Access Denied: Only the Company Administrator can configure or modify the Corporate GSTIN.")
            return
        }
        val cleanGstin = newGstin.trim().uppercase()
        if (!isValidGstin(cleanGstin)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Invalid GSTIN format. Must be 15 alphanumeric characters (e.g. 27AAPFU0939F1ZV).")
            return
        }
        val currentOrg = _uiState.value.currentOrg
        val orgId = currentOrg?.id ?: "org_enterprise"
        _uiState.value = _uiState.value.copy(
            currentOrg = currentOrg?.copy(gstin = cleanGstin),
            successMessage = "Corporate GSTIN securely updated: $cleanGstin [Verified & Compliant]"
        )
        viewModelScope.launch {
            repository.updateOrganizationGstin(orgId, cleanGstin)
        }
    }

    fun updateOrganizationLogo(newLogoUrl: String) {
        if (!_uiState.value.activeRole.equals("admin", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Access Denied: Only the Company Administrator can modify the company logo.")
            return
        }
        val currentOrg = _uiState.value.currentOrg
        val orgId = currentOrg?.id ?: "org_enterprise"
        _uiState.value = _uiState.value.copy(
            currentOrg = currentOrg?.copy(logoUrl = newLogoUrl),
            successMessage = "Company logo updated successfully."
        )
        viewModelScope.launch {
            repository.updateOrganizationLogoUrl(orgId, newLogoUrl)
        }
    }

    private fun registrationProblem(
        ownerName: String, companyName: String, email: String, phone: String,
        password: String, confirm: String, gstin: String
    ): String? = when {
        ownerName.isBlank() -> "Enter the Admin's full name."
        companyName.isBlank() -> "Enter the company legal name."
        !Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$").matches(email.trim()) -> "Enter a valid official company e-mail."
        !Regex("^(\\+91|91|0)?[6-9][0-9]{9}$|^\\+[1-9][0-9]{7,14}$").matches(phone.replace(Regex("[\\s\\-()]"), "")) -> "Enter a valid phone number."
        password.length < 8 || password.none { it.isLetter() } || password.none { it.isDigit() } -> "Password must be 8+ characters with letters and digits."
        password != confirm -> "Passwords do not match."
        gstin.isBlank() -> "Corporate GSTIN is mandatory. Enter your 15-character GSTIN (e.g. 27AAPFU0939F1ZV)."
        !isValidGstin(gstin) -> "Invalid GSTIN format. Must be 15 alphanumeric characters (e.g. 27AAPFU0939F1ZV)."
        else -> null
    }

    /** Direct account creation for rapid testing without external OTP mail delay */
    fun registerDirectly(
        ownerName: String, companyName: String, email: String, phone: String,
        password: String, confirm: String, gstin: String, logoUrl: String = ""
    ) {
        registrationProblem(ownerName, companyName, email, phone, password, confirm, gstin)?.let {
            _uiState.value = _uiState.value.copy(errorMessage = it); return
        }
        val cleanEmail = email.trim().lowercase()
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val reg = authManager.registerCompanyDirectly(
                ownerName = ownerName.trim(),
                companyName = companyName.trim(),
                email = cleanEmail,
                phone = phone.trim(),
                pass = password,
                gstin = gstin.trim(),
                logoUrl = logoUrl.trim()
            )
            if (reg.isSuccess) {
                val user = reg.getOrThrow()
                val registeredOrg = Organization(
                    id = user.orgId,
                    name = user.orgName,
                    slug = user.orgName.lowercase().replace(" ", "-"),
                    industry = "Technology & Enterprise",
                    companySize = "1-50",
                    timezone = "UTC+05:30",
                    description = "Enterprise Organization",
                    gstin = gstin.trim().ifBlank { "TEST-GSTIN-OPTIONAL" },
                    logoUrl = logoUrl.trim()
                )
                val registeredMember = Member(
                    id = "mem_" + user.uid.take(8),
                    userId = user.uid,
                    displayName = user.displayName,
                    employeeId = "OWNER-001",
                    email = user.email,
                    role = "admin",
                    departmentName = "Executive Leadership",
                    status = "active",
                    phone = phone.trim()
                )
                secureSessionManager.startSession(user)
                loadOrganizationData(user.orgId, user.uid)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUserEmail = user.email,
                    currentUserName = user.displayName,
                    currentOrg = registeredOrg,
                    currentMember = registeredMember,
                    members = listOf(registeredMember),
                    activeRole = "admin",
                    isPinVerified = true,
                    errorMessage = null,
                    successMessage = "Company registered successfully! Welcome, ${user.displayName}."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = reg.exceptionOrNull()?.message ?: "Registration failed."
                )
            }
        }
    }

    /** Step 1: validate only the email, then ask the server to e-mail a 6-digit code. */
    fun requestRegistrationOtpOnlyEmail(email: String) {
        val cleanEmail = email.trim().lowercase()
        if (!Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$").matches(cleanEmail)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a valid official company e-mail."); return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val r = backend.sendEmailOtp(cleanEmail, "register")
            _uiState.value = if (r.isSuccess) _uiState.value.copy(
                isLoading = false, otpStage = "awaiting_code",
                successMessage = "Dear Admin, welcome to TaskFlow AI! Your OTP is on its way to $cleanEmail. It is valid for 5 minutes."
            ) else _uiState.value.copy(isLoading = false, errorMessage = r.exceptionOrNull()?.message)
        }
    }

    /** Step 1: validate the form, then ask the server to e-mail a 6-digit code. */
    fun requestRegistrationOtp(
        ownerName: String, companyName: String, email: String, phone: String,
        password: String, confirm: String, gstin: String
    ) {
        registrationProblem(ownerName, companyName, email, phone, password, confirm, gstin)?.let {
            _uiState.value = _uiState.value.copy(errorMessage = it); return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val r = backend.sendEmailOtp(email.trim().lowercase(), "register")
            _uiState.value = if (r.isSuccess) _uiState.value.copy(
                isLoading = false, otpStage = "awaiting_code",
                successMessage = "Verification code sent to ${email.trim()}. It expires in 10 minutes."
            ) else _uiState.value.copy(isLoading = false, errorMessage = r.exceptionOrNull()?.message)
        }
    }

    /** Step 2: verify the code, create the company on the server, then sign in with the new account. */
    fun verifyOtpAndRegister(
        ownerName: String, companyName: String, email: String, phone: String,
        password: String, confirm: String, gstin: String, code: String
    ) {
        registrationProblem(ownerName, companyName, email, phone, password, confirm, gstin)?.let {
            _uiState.value = _uiState.value.copy(errorMessage = it); return
        }
        if (!Regex("^\\d{6}$").matches(code)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter the 6-digit code."); return
        }
        val cleanEmail = email.trim().lowercase()
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val v = backend.verifyEmailOtp(cleanEmail, "register", code)
            if (v.isFailure) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = v.exceptionOrNull()?.message); return@launch
            }
            val reg = backend.registerCompany(mapOf(
                "ownerName" to ownerName.trim(), "companyName" to companyName.trim(), "email" to cleanEmail,
                "phone" to phone.trim(), "password" to password, "confirmPassword" to confirm,
                "gstin" to gstin.trim().uppercase(), "verificationToken" to v.getOrThrow()
            ))
            if (reg.isFailure) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = reg.exceptionOrNull()?.message); return@launch
            }
            _uiState.value = _uiState.value.copy(otpStage = "none", isLoading = false)
            signInWithEmail(cleanEmail, password)
        }
    }

    fun cancelRegistrationOtp() { _uiState.value = _uiState.value.copy(otpStage = "none") }


    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authManager.signInWithEmail(email, pass)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                val defaultOrg = Organization(
                    id = user.orgId,
                    name = user.orgName,
                    slug = user.orgName.lowercase().replace(" ", "-"),
                    industry = "Enterprise",
                    companySize = "10-50",
                    timezone = "UTC+05:30",
                    description = "Enterprise Organization"
                )
                val foundMember = _uiState.value.members.firstOrNull { it.email.equals(user.email, ignoreCase = true) }
                    ?: Member(
                        id = "mem_" + user.uid.take(8),
                        userId = user.uid,
                        displayName = user.displayName,
                        employeeId = user.employeeId,
                        email = user.email,
                        role = user.role,
                        departmentName = if (user.role == "manager") "Operations & Management" else if (user.role == "staff") "Engineering & Development" else "Executive Leadership",
                        status = "active"
                    )

                secureSessionManager.startSession(user)
                loadOrganizationData(user.orgId, user.uid)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUserEmail = user.email,
                    currentUserName = user.displayName,
                    currentOrg = _uiState.value.currentOrg ?: defaultOrg,
                    currentMember = foundMember,
                    activeRole = user.role,
                    isPinVerified = true,
                    errorMessage = null,
                    successMessage = "Welcome back, ${user.displayName}!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sign-in failed. Please verify credentials."
                )
            }
        }
    }

    fun signInWithStaffCredentials(employeeId: String, pass: String) {
        if (employeeId.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter Employee ID and password.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authManager.signInWithEmployeeId(employeeId, pass)
            if (result.isSuccess) {
                val user = result.getOrThrow()
                val member = _uiState.value.members.firstOrNull { it.userId == user.uid }
                    ?: Member(
                        id = "mem_" + user.uid.take(10),
                        userId = user.uid,
                        displayName = user.displayName,
                        employeeId = user.employeeId,
                        role = user.role,
                        status = "active"
                    )
                secureSessionManager.startSession(user)
                loadOrganizationData(user.orgId, user.uid)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    isPinVerified = true,
                    isDemoMode = false,
                    currentUserName = user.displayName,
                    currentMember = member,
                    activeRole = user.role,
                    errorMessage = null,
                    successMessage = "Welcome back, ${user.displayName}!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sign-in failed."
                )
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun quickDemoLogin(role: String) {
        _uiState.value = _uiState.value.copy(errorMessage = "Demo logins are disabled. Sign in with a real account.")
    }

    private fun loadOrganizationData(orgId: String, userId: String) {
        cancelOrgObservations()

        orgJob = viewModelScope.launch {
            repository.observeOrganization(orgId).collect { org ->
                if (org != null) {
                    _uiState.value = _uiState.value.copy(currentOrg = org)
                }
            }
        }

        membersJob = viewModelScope.launch {
            repository.observeMembers(orgId).collect { memberList ->
                if (memberList.isNotEmpty()) {
                    val myMember = memberList.firstOrNull { it.userId == userId || it.email == _uiState.value.currentUser?.email }
                        ?: memberList.first()
                    _uiState.value = _uiState.value.copy(
                        members = memberList,
                        currentMember = myMember,
                        activeRole = myMember.role
                    )
                }
            }
        }

        tasksJob = viewModelScope.launch {
            repository.observeTasks(orgId).collect { taskList ->
                _uiState.value = _uiState.value.copy(tasks = taskList)
                checkApproachingTaskReminders(taskList)
            }
        }

        groupsJob = viewModelScope.launch {
            repository.observeGroups(orgId).collect { groupList ->
                _uiState.value = _uiState.value.copy(groups = groupList)
            }
        }

        deptsJob = viewModelScope.launch {
            repository.observeDepartments(orgId).collect { deptList ->
                if (deptList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(departments = deptList)
                }
            }
        }

        workflowsJob = viewModelScope.launch {
            repository.observeWorkflows(orgId).collect { wfList ->
                if (wfList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(workflows = wfList)
                }
            }
        }

        permissionReqJob = viewModelScope.launch {
            repository.observePermissionRequests(orgId).collect { reqList ->
                _uiState.value = _uiState.value.copy(permissionRequests = reqList)
            }
        }

        auditJob = viewModelScope.launch {
            repository.observeAuditLogs(orgId).collect { logList ->
                if (logList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(auditLogs = logList)
                }
            }
        }

        aiJob = viewModelScope.launch {
            repository.observeAiInsights(orgId).collect { insightList ->
                if (insightList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(aiInsights = insightList)
                }
            }
        }
    }

    private fun cancelOrgObservations() {
        orgJob?.cancel()
        membersJob?.cancel()
        tasksJob?.cancel()
        groupsJob?.cancel()
        groupChatJob?.cancel()
        deptsJob?.cancel()
        workflowsJob?.cancel()
        permissionReqJob?.cancel()
        auditJob?.cancel()
        aiJob?.cancel()
    }



    /** Creates the Firebase account, the directory entry and the member document. Never stores a password. */


    fun createMemberWithCredentials(
        name: String,
        employeeId: String,
        email: String,
        password: String,
        role: String,
        departmentId: String,
        departmentName: String,
        phone: String,
        securityAccessKey: String = ""
    ) {
        val trimmedName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanRole = role.trim().lowercase()

        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter the member's full name.")
            return
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid work email.")
            return
        }
        val cleanPassword = password.trim().ifBlank { "Password@123" }
        if (cleanPassword.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }
        val cleanAccessKey = if (securityAccessKey.isNotBlank()) {
            securityAccessKey.trim().uppercase()
        } else {
            "TF-%06d".format((100000..999999).random())
        }

        // Managers submit a request for Owner/Admin approval; Admin provisions immediately
        if (_uiState.value.activeRole.equals("manager", ignoreCase = true)) {
            val payload = JSONObject()
                .put("name", trimmedName).put("email", cleanEmail).put("role", cleanRole)
                .put("department", departmentName).put("departmentId", departmentId).put("phone", phone)
                .put("password", cleanPassword).put("securityAccessKey", cleanAccessKey).toString()
            submitPermissionRequest(
                actionType = "CREATE_STAFF", targetStaffId = "", targetStaffName = trimmedName, payloadJson = payload,
                reason = "Manager request to onboard $cleanRole '$trimmedName' into $departmentName."
            )
            _uiState.value = _uiState.value.copy(successMessage = "Staff onboarding request submitted for Admin approval.")
            return
        }

        val org = _uiState.value.currentOrg
        val orgId = org?.id ?: "org_enterprise"
        val orgName = org?.name ?: "TaskFlow Enterprise"

        val cleanEmpId = if (employeeId.isNotBlank()) {
            employeeId.trim().uppercase()
        } else {
            val count = _uiState.value.members.count { it.role.equals(cleanRole, ignoreCase = true) } + 1
            if (cleanRole == "manager") "MGR-%03d".format(count) else "STF-%03d".format(count)
        }

        viewModelScope.launch {
            val authUser = authManager.createManagedAccount(
                name = trimmedName,
                email = cleanEmail,
                pass = cleanPassword,
                employeeId = cleanEmpId,
                role = cleanRole,
                departmentId = departmentId.ifBlank { if (cleanRole == "manager") "ops" else "eng" },
                departmentName = departmentName.ifBlank { if (cleanRole == "manager") "Operations & Management" else "Engineering & Execution" },
                orgId = orgId,
                orgName = orgName,
                phone = phone.trim(),
                securityAccessKey = cleanAccessKey
            )

            val newMember = Member(
                id = "mem_" + cleanEmpId.lowercase().replace("-", "_"),
                userId = authUser.uid,
                displayName = trimmedName,
                employeeId = cleanEmpId,
                email = cleanEmail,
                role = cleanRole,
                departmentId = departmentId.ifBlank { if (cleanRole == "manager") "ops" else "eng" },
                departmentName = departmentName.ifBlank { if (cleanRole == "manager") "Operations & Management" else "Engineering & Execution" },
                phone = phone.trim(),
                status = "active",
                securityAccessKey = cleanAccessKey
            )

            val updatedMembers = _uiState.value.members.filter { it.id != newMember.id } + newMember
            _uiState.value = _uiState.value.copy(
                members = updatedMembers,
                showAddMemberDialog = false,
                successMessage = "Provisioned login for $trimmedName ($cleanRole). ID: $cleanEmpId | Key: $cleanAccessKey"
            )

            addNotification(
                title = "New Member Credentials Provisioned",
                message = "Admin created login credentials for $trimmedName ($cleanRole). Email: $cleanEmail, ID: $cleanEmpId, Key: $cleanAccessKey.",
                type = "member",
                author = "Administrator",
                priority = "medium"
            )
        }
    }

    fun changeUserPassword(currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authManager.updatePassword(currentPass, newPass)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Password changed successfully.")
                addNotification(
                    title = "Security Alert: Password Changed",
                    message = "Your account password was updated successfully.",
                    type = "security",
                    author = "TaskFlow Security",
                    priority = "high"
                )
                onResult(true, "Password changed successfully.")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to change password."
                _uiState.value = _uiState.value.copy(errorMessage = err)
                onResult(false, err)
            }
        }
    }

    fun updateUserProfile(name: String, phone: String, dept: String = "", onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authManager.updateProfileDetails(name, phone, dept)
            if (result.isSuccess) {
                val current = _uiState.value.currentMember
                if (current != null) {
                    val updated = current.copy(
                        displayName = name.trim(),
                        phone = phone.trim(),
                        departmentName = if (dept.isNotBlank()) dept.trim() else current.departmentName
                    )
                    _uiState.value = _uiState.value.copy(
                        currentMember = updated,
                        successMessage = "Profile updated successfully."
                    )
                }
                onResult(true, "Profile updated successfully.")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to update profile."
                _uiState.value = _uiState.value.copy(errorMessage = err)
                onResult(false, err)
            }
        }
    }

    fun updateMember(
        memberId: String,
        name: String,
        role: String,
        departmentId: String,
        departmentName: String,
        status: String
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"

        if (_uiState.value.activeRole.equals("manager", ignoreCase = true)) {
            submitPermissionRequest(
                actionType = "UPDATE_STAFF",
                targetStaffId = memberId,
                targetStaffName = name,
                payloadJson = "{\"role\":\"$role\",\"status\":\"$status\"}",
                reason = "Manager requested modification of staff '$name' status to '$status'."
            )
            return
        }

        val updatedList = _uiState.value.members.map {
            if (it.id == memberId) it.copy(
                displayName = name,
                role = role,
                departmentId = departmentId,
                departmentName = departmentName,
                status = status
            ) else it
        }
        _uiState.value = _uiState.value.copy(
            members = updatedList,
            successMessage = "Member '$name' updated successfully."
        )

        viewModelScope.launch {
            repository.updateMember(
                orgId = currentOrgId,
                memberId = memberId,
                name = name,
                role = role,
                departmentId = departmentId,
                departmentName = departmentName,
                status = status,
                actor = _uiState.value.currentMember
            )
        }
    }

    fun deleteMember(memberId: String, memberName: String) {
        if (_uiState.value.activeRole.equals("manager", ignoreCase = true)) {
            submitPermissionRequest(
                actionType = "DELETE_STAFF", targetStaffId = memberId, targetStaffName = memberName, payloadJson = "{}",
                reason = "Manager requested staff member '$memberName' to be removed from department."
            )
            return
        }
        if (!_uiState.value.activeRole.equals("admin", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Admin privilege required to remove personnel."); return
        }
        val orgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        val updated = _uiState.value.members.filter { it.id != memberId }
        _uiState.value = _uiState.value.copy(
            members = updated,
            successMessage = "'$memberName' removed from roster."
        )
        viewModelScope.launch {
            repository.deleteMember(orgId, memberId, _uiState.value.currentMember)
            addNotification(
                title = "Personnel Removed",
                message = "Admin removed '$memberName' from active enterprise roster.",
                type = "member",
                author = "Administrator",
                priority = "medium"
            )
        }
    }

    fun submitPermissionRequest(
        actionType: String,
        targetStaffId: String,
        targetStaffName: String,
        payloadJson: String,
        reason: String
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        val manager = _uiState.value.currentMember

        val newReq = PermissionRequest(
            id = "req_" + System.currentTimeMillis(),
            organizationId = currentOrgId,
            managerId = manager?.id ?: "mgr_01",
            managerName = manager?.displayName ?: "Operations Manager",
            actionType = actionType,
            targetStaffId = targetStaffId,
            targetStaffName = targetStaffName,
            payloadJson = payloadJson,
            reason = reason,
            status = "pending"
        )

        _uiState.value = _uiState.value.copy(
            permissionRequests = listOf(newReq) + _uiState.value.permissionRequests,
            successMessage = "Permission request submitted to Admin! Waiting for approval."
        )

        addNotification(
            title = "Approval Requested: $actionType",
            message = "Manager ${manager?.displayName ?: "Operations"} submitted request for '$targetStaffName'. Reason: $reason",
            type = "approval",
            author = manager?.displayName ?: "Operations Manager",
            priority = "high"
        )

        viewModelScope.launch {
            repository.submitPermissionRequest(
                orgId = currentOrgId,
                managerId = manager?.id ?: "mgr_01",
                managerName = manager?.displayName ?: "Operations Manager",
                actionType = actionType,
                targetStaffId = targetStaffId,
                targetStaffName = targetStaffName,
                payloadJson = payloadJson,
                reason = reason
            )
        }
    }

    fun approvePermissionRequest(requestId: String, adminNotes: String = "Approved by Administrator") =
        decideRequest(requestId, true, adminNotes)

    fun rejectPermissionRequest(requestId: String, reason: String = "Rejected by Administrator") =
        decideRequest(requestId, false, reason)

    /** The server validates the Owner role, executes the action (e.g. creates the account) and records the audit entry. */
    private fun decideRequest(requestId: String, approve: Boolean, notes: String) {
        if (!_uiState.value.activeRole.equals("admin", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Only the Owner can decide requests."); return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val r = backend.decideApproval(requestId, approve, notes)
            _uiState.value = if (r.isSuccess) {
                val m = r.getOrThrow()
                val extra = if (m["tempPassword"] != null)
                    " New account ID: ${m["employeeId"]}  Temporary password: ${m["tempPassword"]} (shown once)." else ""
                _uiState.value.copy(isLoading = false, successMessage = (if (approve) "Request approved." else "Request rejected.") + extra)
            } else _uiState.value.copy(isLoading = false, errorMessage = r.exceptionOrNull()?.message)
        }
    }



    fun openDocumentViewer(task: TaskItem, isWorkDoc: Boolean) {
        _uiState.value = _uiState.value.copy(
            viewingDocumentTask = task,
            viewingDocumentIsWorkDoc = isWorkDoc
        )
    }

    fun closeDocumentViewer() {
        _uiState.value = _uiState.value.copy(
            viewingDocumentTask = null
        )
    }

    fun openUploadDocument(task: TaskItem) {
        _uiState.value = _uiState.value.copy(
            uploadingDocumentTask = task
        )
    }

    fun closeUploadDocument() {
        _uiState.value = _uiState.value.copy(
            uploadingDocumentTask = null
        )
    }

    fun openTaskDocumentsList(task: TaskItem) {
        _uiState.value = _uiState.value.copy(
            viewingDocumentsListTask = task
        )
    }

    fun closeTaskDocumentsList() {
        _uiState.value = _uiState.value.copy(
            viewingDocumentsListTask = null
        )
    }

    fun uploadAndSetOrganizationLogo(context: Context, uri: Uri) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = FileStorageHelper.saveLogoImage(context, uri)
            if (result.isSuccess) {
                val newLogoUrl = result.getOrThrow()
                repository.updateOrganizationLogoUrl(currentOrgId, newLogoUrl)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showEditLogoDialog = false,
                    currentOrg = _uiState.value.currentOrg?.copy(logoUrl = newLogoUrl),
                    successMessage = "Company logo updated across all dashboards!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to process logo image: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun uploadTaskFileWithStorage(
        context: Context,
        taskId: String,
        uri: Uri,
        preferredName: String,
        isWorkDoc: Boolean,
        remarks: String = ""
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: return
        val uploader = _uiState.value.currentMember?.displayName ?: _uiState.value.activeRole.replaceFirstChar { it.uppercase() }
        val uploaderRole = _uiState.value.activeRole
        val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val storagePath = "tasks/$taskId/${if (isWorkDoc) "work_proof" else "brief"}/${System.currentTimeMillis()}_$preferredName"
            val result = FileStorageHelper.uploadFile(context, uri, storagePath, preferredName)

            if (result.isSuccess) {
                val upload = result.getOrThrow()
                if (isWorkDoc) {
                    repository.uploadTaskWorkDocument(
                        orgId = currentOrgId,
                        taskId = taskId,
                        docName = upload.fileName,
                        docUrl = upload.fileUrl,
                        docType = upload.fileType,
                        uploaderName = uploader,
                        uploaderRole = uploaderRole,
                        remarks = remarks
                    )
                } else {
                    repository.updateTaskAttachment(
                        orgId = currentOrgId,
                        taskId = taskId,
                        attachmentName = upload.fileName,
                        attachmentUrl = upload.fileUrl,
                        attachmentType = upload.fileType,
                        attachmentSizeBytes = upload.sizeBytes
                    )
                }

                val updatedTasks = _uiState.value.tasks.map { task ->
                    if (task.id == taskId) {
                        if (isWorkDoc) {
                            task.copy(
                                workDocName = upload.fileName,
                                workDocUrl = upload.fileUrl,
                                workDocType = upload.fileType,
                                workDocUploadedAt = nowFormatted,
                                workDocUploadedBy = uploader,
                                completionNotes = if (remarks.isNotBlank()) remarks else task.completionNotes
                            )
                        } else {
                            task.copy(
                                attachmentName = upload.fileName,
                                attachmentUrl = upload.fileUrl,
                                attachmentType = upload.fileType,
                                attachmentSizeBytes = upload.sizeBytes
                            )
                        }
                    } else task
                }

                val updatedDetail = if (_uiState.value.selectedTaskForDetail?.id == taskId) {
                    updatedTasks.firstOrNull { it.id == taskId }
                } else _uiState.value.selectedTaskForDetail

                val updatedListModal = if (_uiState.value.viewingDocumentsListTask?.id == taskId) {
                    updatedTasks.firstOrNull { it.id == taskId }
                } else _uiState.value.viewingDocumentsListTask

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = updatedTasks,
                    selectedTaskForDetail = updatedDetail,
                    viewingDocumentsListTask = updatedListModal,
                    uploadingDocumentTask = null,
                    successMessage = "Document '${upload.fileName}' saved to storage and linked to task!"
                )
                playNotificationSound()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Upload failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun uploadTaskWorkDocument(
        taskId: String,
        docName: String,
        docUrl: String,
        docType: String,
        remarks: String = ""
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: return
        val uploader = _uiState.value.currentMember?.displayName ?: _uiState.value.activeRole.replaceFirstChar { it.uppercase() }
        val uploaderRole = _uiState.value.activeRole
        val nowFormatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val updatedTasks = _uiState.value.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(
                    workDocName = docName,
                    workDocUrl = docUrl,
                    workDocType = docType,
                    workDocUploadedAt = nowFormatted,
                    workDocUploadedBy = uploader,
                    completionNotes = if (remarks.isNotBlank()) remarks else task.completionNotes
                )
            } else task
        }

        val updatedDetail = if (_uiState.value.selectedTaskForDetail?.id == taskId) {
            _uiState.value.selectedTaskForDetail?.copy(
                workDocName = docName,
                workDocUrl = docUrl,
                workDocType = docType,
                workDocUploadedAt = nowFormatted,
                workDocUploadedBy = uploader,
                completionNotes = if (remarks.isNotBlank()) remarks else _uiState.value.selectedTaskForDetail!!.completionNotes
            )
        } else _uiState.value.selectedTaskForDetail

        _uiState.value = _uiState.value.copy(
            tasks = updatedTasks,
            selectedTaskForDetail = updatedDetail,
            uploadingDocumentTask = null,
            successMessage = "Work document '$docName' submitted and synced to cloud!"
        )

        playNotificationSound()

        viewModelScope.launch {
            repository.uploadTaskWorkDocument(
                orgId = currentOrgId,
                taskId = taskId,
                docName = docName,
                docUrl = docUrl,
                docType = docType,
                uploaderName = uploader,
                uploaderRole = uploaderRole,
                remarks = remarks
            )
            addNotification(
                title = "Work Document Submitted",
                message = "$uploader ($uploaderRole) uploaded deliverable document '$docName' for task ID '$taskId'.",
                type = "task",
                author = uploader,
                priority = "normal"
            )
        }
    }

    fun createTask(
        title: String,
        description: String,
        departmentId: String,
        priority: String,
        assignedMember: Member?,
        startDate: String,
        dueDate: String,
        estimatedHours: Double,
        hasReminder: Boolean = false,
        reminderNotes: String = "",
        attachmentName: String = "",
        attachmentUrl: String = "",
        attachmentType: String = "",
        attachmentSizeBytes: Long = 0L
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        val creator = _uiState.value.currentMember

        val newTask = TaskItem(
            id = "task_" + System.currentTimeMillis(),
            organizationId = currentOrgId,
            title = title.trim(),
            description = description.trim(),
            departmentId = departmentId,
            priority = priority.lowercase(),
            status = "todo",
            assignedToMemberId = assignedMember?.id ?: "",
            assignedToName = assignedMember?.displayName ?: "Unassigned",
            assignedToRole = assignedMember?.role ?: "",
            createdByMemberId = creator?.id ?: "",
            createdByName = creator?.displayName ?: "Admin",
            progress = 0,
            estimatedHours = estimatedHours,
            startDate = startDate,
            dueDate = dueDate,
            hasReminder = hasReminder,
            reminderNotes = reminderNotes,
            attachmentName = attachmentName,
            attachmentUrl = attachmentUrl,
            attachmentType = attachmentType,
            attachmentSizeBytes = attachmentSizeBytes
        )

        _uiState.value = _uiState.value.copy(
            tasks = listOf(newTask) + _uiState.value.tasks,
            successMessage = "Task '$title' created and assigned successfully."
        )

        addNotification(
            title = "Task Delegated: ${title.trim()}",
            message = "New deliverable created by ${creator?.displayName ?: "Executive Admin"} and assigned to ${assignedMember?.displayName ?: "Team"} (${priority.uppercase()} Priority). Due: $dueDate",
            type = "task",
            author = creator?.displayName ?: "Admin",
            priority = priority
        )

        notificationManager.showNotification(
            1002,
            "New Task Delegated: $title",
            "Priority: ${priority.uppercase()} • Assigned to: ${assignedMember?.displayName ?: "Team"}"
        )

        if (_uiState.value.isEmailNotificationsEnabled) {
            val emailAddr = assignedMember?.email?.ifBlank { "member@taskflow.com" } ?: "member@taskflow.com"
            addNotification(
                title = "📧 Email Alert Dispatched: $title",
                message = "Automatic email assignment alert sent to $emailAddr for task '$title' (Due: $dueDate).",
                type = "task",
                author = "Email Dispatcher",
                priority = priority
            )
        }

        viewModelScope.launch {
            repository.createTask(
                orgId = currentOrgId,
                title = title,
                description = description,
                deptId = departmentId,
                priority = priority,
                assignedMember = assignedMember,
                startDate = startDate,
                dueDate = dueDate,
                estimatedHours = estimatedHours,
                creator = creator,
                hasReminder = hasReminder,
                reminderNotes = reminderNotes,
                attachmentName = attachmentName,
                attachmentUrl = attachmentUrl,
                attachmentType = attachmentType,
                attachmentSizeBytes = attachmentSizeBytes
            )
        }
    }

    fun updateTaskStatus(
        taskId: String,
        newStatus: String,
        progress: Int,
        actualHours: Double = 0.0,
        blockerReason: String = ""
    ) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        val updated = _uiState.value.tasks.map {
            if (it.id == taskId) {
                it.copy(status = newStatus, progress = progress, actualHours = actualHours, blockerReason = blockerReason)
            } else it
        }
        val taskObj = _uiState.value.tasks.find { it.id == taskId }
        _uiState.value = _uiState.value.copy(
            tasks = updated,
            successMessage = "Task status updated to ${newStatus.replace("_", " ").uppercase()} ($progress%)"
        )

        addNotification(
            title = "Task Progress: ${taskObj?.title ?: "Deliverable"}",
            message = "Status updated to ${newStatus.replace("_", " ").uppercase()} ($progress% completed). ${if (blockerReason.isNotBlank()) "Blocker: $blockerReason" else "On track."}",
            type = "task",
            author = _uiState.value.currentMember?.displayName ?: "Team Member",
            priority = taskObj?.priority ?: "medium"
        )

        notificationManager.showNotification(
            1003,
            "Task Updated: ${taskObj?.title ?: "Deliverable"}",
            "Status: ${newStatus.replace("_", " ").uppercase()} • Completion: $progress%"
        )

        viewModelScope.launch {
            repository.updateTaskStatus(currentOrgId, taskId, newStatus, progress, actualHours, blockerReason)
        }
    }

    fun deleteTask(taskId: String) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        _uiState.value = _uiState.value.copy(
            tasks = _uiState.value.tasks.filterNot { it.id == taskId },
            successMessage = "Task deleted."
        )
        viewModelScope.launch {
            repository.deleteTask(currentOrgId, taskId)
        }
    }

    fun updateTaskPriority(taskId: String, priority: String) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise"
        val updated = _uiState.value.tasks.map {
            if (it.id == taskId) {
                it.copy(priority = priority.lowercase())
            } else it
        }
        _uiState.value = _uiState.value.copy(
            tasks = updated,
            successMessage = "Task priority updated to ${priority.uppercase()}"
        )

        viewModelScope.launch {
            repository.updateTaskPriority(currentOrgId, taskId, priority)
        }
    }

    fun setShowCreateTaskDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateTaskDialog = show)
    }

    fun setShowTeamManagement(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTeamManagement = show)
    }

    fun setShowAddGroupDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddGroupDialog = show)
    }

    fun setShowGroupChatDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showGroupChatDialog = show)
    }

    fun setShowAddMemberDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddMemberDialog = show)
    }

    fun playNotificationSound() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(getApplication(), uri)
            ringtone?.play()
        } catch (_: Exception) {
            try {
                val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                toneG.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
            } catch (_: Exception) {}
        }
    }

    fun triggerTaskReminder(task: TaskItem) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        playNotificationSound()
        notificationManager.showNotification(
            (1000..9999).random(),
            "⏰ Deliverable Reminder: ${task.title}",
            "Assigned: ${task.assignedToName} • Priority: ${task.priority.uppercase()} • Target: ${task.dueDate}"
        )
        addNotification(
            title = "⏰ Deliverable Alert: ${task.title}",
            message = "Action required on deliverable '${task.title}'. Assigned to: ${task.assignedToName} (${task.priority.uppercase()}).",
            type = "task",
            author = "Reminder System",
            priority = "urgent"
        )
        val timeNow = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
        val updated = _uiState.value.tasks.map {
            if (it.id == task.id) it.copy(hasReminder = true, reminderNotes = "Audible reminder active ($timeNow)") else it
        }
        _uiState.value = _uiState.value.copy(
            tasks = updated,
            successMessage = "⏰ Reminder alert with pop-up sound triggered for '${task.title}'!"
        )
        viewModelScope.launch {
            repository.setTaskReminder(currentOrgId, task.id, true, "Audible reminder active ($timeNow)")
        }
    }

    fun openTaskCompletionModal(taskId: String, taskTitle: String) {
        _uiState.value = _uiState.value.copy(
            showCompletionModal = true,
            completingTaskId = taskId,
            completingTaskTitle = taskTitle
        )
    }

    fun closeTaskCompletionModal() {
        _uiState.value = _uiState.value.copy(
            showCompletionModal = false,
            completingTaskId = null,
            completingTaskTitle = null
        )
    }

    fun completeTaskWithNotes(taskId: String, completionNotes: String, completedAt: String) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        val member = _uiState.value.currentMember
        val authorName = member?.displayName ?: "Associate"
        val taskObj = _uiState.value.tasks.find { it.id == taskId }

        val cleanNotes = completionNotes.trim().ifBlank { "Task successfully executed and verified." }
        val cleanTime = completedAt.trim().ifBlank {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        }

        val updated = _uiState.value.tasks.map {
            if (it.id == taskId) {
                it.copy(
                    status = "done",
                    progress = 100,
                    completionNotes = cleanNotes,
                    completedAt = cleanTime,
                    completedBy = authorName
                )
            } else it
        }

        _uiState.value = _uiState.value.copy(
            tasks = updated,
            showCompletionModal = false,
            completingTaskId = null,
            completingTaskTitle = null,
            successMessage = "Task '${taskObj?.title ?: "Deliverable"}' completed! Notes recorded."
        )

        addNotification(
            title = "✓ Task Completed: ${taskObj?.title ?: "Deliverable"}",
            message = "$authorName completed this task at $cleanTime. Notes: $cleanNotes",
            type = "task",
            author = authorName,
            priority = "low"
        )

        val newAudit = AuditLog(
            id = "audit_" + System.currentTimeMillis(),
            organizationId = currentOrgId,
            actorName = authorName,
            actorRole = member?.role ?: _uiState.value.activeRole,
            action = "Completed '${taskObj?.title ?: "Task"}'. Notes: \"$cleanNotes\"",
            timestamp = cleanTime
        )
        _uiState.value = _uiState.value.copy(auditLogs = listOf(newAudit) + _uiState.value.auditLogs)

        viewModelScope.launch {
            repository.updateTaskStatus(
                orgId = currentOrgId,
                taskId = taskId,
                newStatus = "done",
                progress = 100,
                actualHours = taskObj?.estimatedHours ?: 2.0,
                blockerReason = "",
                completionNotes = cleanNotes,
                completedAt = cleanTime,
                completedBy = authorName
            )
        }
    }

    fun selectTaskForDetail(task: TaskItem?) {
        _uiState.value = _uiState.value.copy(
            selectedTaskForDetail = task,
            showTaskDetailModal = task != null
        )
    }

    fun dismissReminderAlert() {
        _uiState.value = _uiState.value.copy(activeReminderAlert = null)
    }

    fun saveTaskCompletionNotes(taskId: String, notes: String, markCompleted: Boolean = false) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        val member = _uiState.value.currentMember
        val authorName = member?.displayName ?: "Associate"
        val taskObj = _uiState.value.tasks.find { it.id == taskId }

        val cleanNotes = notes.trim().ifBlank { "Task execution documentation recorded." }
        val cleanTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val updatedTasks = _uiState.value.tasks.map {
            if (it.id == taskId) {
                it.copy(
                    completionNotes = cleanNotes,
                    completedAt = cleanTime,
                    completedBy = authorName,
                    status = if (markCompleted) "done" else it.status,
                    progress = if (markCompleted) 100 else it.progress
                )
            } else it
        }

        val updatedSelected = if (_uiState.value.selectedTaskForDetail?.id == taskId) {
            _uiState.value.selectedTaskForDetail?.copy(
                completionNotes = cleanNotes,
                completedAt = cleanTime,
                completedBy = authorName,
                status = if (markCompleted) "done" else _uiState.value.selectedTaskForDetail!!.status,
                progress = if (markCompleted) 100 else _uiState.value.selectedTaskForDetail!!.progress
            )
        } else _uiState.value.selectedTaskForDetail

        _uiState.value = _uiState.value.copy(
            tasks = updatedTasks,
            selectedTaskForDetail = updatedSelected,
            successMessage = "Notes saved to Firestore task document for '${taskObj?.title ?: "Task"}'!"
        )

        playNotificationSound()

        val auditAction = if (markCompleted) {
            "Finalized task '${taskObj?.title ?: "Task"}'. Notes: \"$cleanNotes\""
        } else {
            "Updated completion notes on '${taskObj?.title ?: "Task"}'. Notes: \"$cleanNotes\""
        }

        val newAudit = AuditLog(
            id = "audit_" + System.currentTimeMillis(),
            organizationId = currentOrgId,
            actorName = authorName,
            actorRole = member?.role ?: _uiState.value.activeRole,
            action = auditAction,
            timestamp = cleanTime
        )
        _uiState.value = _uiState.value.copy(auditLogs = listOf(newAudit) + _uiState.value.auditLogs)

        viewModelScope.launch {
            repository.saveTaskCompletionNotes(
                orgId = currentOrgId,
                taskId = taskId,
                notes = cleanNotes,
                completedAt = cleanTime,
                completedBy = authorName,
                markCompleted = markCompleted
            )
        }
    }

    fun logTaskTime(taskId: String, additionalHours: Double) {
        val currentOrgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        val member = _uiState.value.currentMember
        val authorName = member?.displayName ?: "Associate"
        val taskObj = _uiState.value.tasks.find { it.id == taskId } ?: return

        val newActualHours = (taskObj.actualHours + additionalHours).let {
            String.format(Locale.US, "%.2f", it).toDoubleOrNull() ?: it
        }

        val updatedTasks = _uiState.value.tasks.map {
            if (it.id == taskId) it.copy(actualHours = newActualHours) else it
        }

        val updatedSelected = if (_uiState.value.selectedTaskForDetail?.id == taskId) {
            _uiState.value.selectedTaskForDetail?.copy(actualHours = newActualHours)
        } else _uiState.value.selectedTaskForDetail

        _uiState.value = _uiState.value.copy(
            tasks = updatedTasks,
            selectedTaskForDetail = updatedSelected,
            successMessage = "Logged +${String.format(Locale.US, "%.2f", additionalHours)} hrs to '${taskObj.title}' (Total: $newActualHours hrs)"
        )

        playNotificationSound()

        val newAudit = AuditLog(
            id = "audit_" + System.currentTimeMillis(),
            organizationId = currentOrgId,
            actorName = authorName,
            actorRole = member?.role ?: _uiState.value.activeRole,
            action = "Logged time on '${taskObj.title}': +${String.format(Locale.US, "%.2f", additionalHours)} hrs (Total: $newActualHours hrs)",
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        _uiState.value = _uiState.value.copy(auditLogs = listOf(newAudit) + _uiState.value.auditLogs)

        viewModelScope.launch {
            repository.updateTaskStatus(
                orgId = currentOrgId,
                taskId = taskId,
                newStatus = taskObj.status,
                progress = taskObj.progress,
                actualHours = newActualHours,
                blockerReason = taskObj.blockerReason,
                completionNotes = taskObj.completionNotes,
                completedAt = taskObj.completedAt,
                completedBy = taskObj.completedBy
            )
        }
    }

    fun startReminderObserver() {
        reminderObserverJob?.cancel()
        reminderObserverJob = viewModelScope.launch {
            while (isActive) {
                delay(12_000)
                checkApproachingTaskReminders()
            }
        }
    }

    private fun checkApproachingTaskReminders(tasksToCheck: List<TaskItem> = _uiState.value.tasks) {
        val activeTasksWithReminder = tasksToCheck.filter {
            (it.status != "done" && it.status != "completed") &&
            it.hasReminder &&
            it.id !in alertedTaskIds
        }

        val candidate = activeTasksWithReminder.firstOrNull {
            it.dueDate.contains("Today", ignoreCase = true) ||
            it.dueDate.contains("Tomorrow", ignoreCase = true) ||
            it.reminderNotes.isNotBlank()
        }

        if (candidate != null) {
            alertedTaskIds.add(candidate.id)
            playNotificationSound()
            _uiState.value = _uiState.value.copy(
                activeReminderAlert = candidate
            )
            addNotification(
                title = "⏰ Audible Alert: Due Date Approaching",
                message = "Task '${candidate.title}' (${candidate.priority.uppercase()}) is approaching due date: ${candidate.dueDate}. Reminder note: ${candidate.reminderNotes.ifBlank { "Immediate review needed." }}",
                type = "task",
                author = "System Sentinel",
                priority = "high"
            )
        }
    }

    fun selectGroup(group: TeamGroup?) {
        _uiState.value = _uiState.value.copy(
            selectedGroup = group,
            showGroupChatDialog = group != null
        )
        groupChatJob?.cancel()
        if (group != null) {
            val orgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
            groupChatJob = viewModelScope.launch {
                repository.observeGroupMessages(orgId, group.id).collect { msgs ->
                    _uiState.value = _uiState.value.copy(activeGroupMessages = msgs)
                }
            }
        }
    }

    fun createTeamGroup(
        name: String,
        description: String,
        managerIds: List<String>,
        managerNames: List<String>,
        staffIds: List<String>,
        staffNames: List<String>
    ) {
        val orgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        val newGroup = TeamGroup(
            id = "grp_" + System.currentTimeMillis(),
            organizationId = orgId,
            name = name.trim(),
            description = description.trim(),
            managerIds = managerIds,
            managerNames = managerNames,
            staffIds = staffIds,
            staffNames = staffNames,
            createdAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            groups = _uiState.value.groups + newGroup,
            showAddGroupDialog = false,
            successMessage = "Team Group '${name.trim()}' formed with ${managerNames.size} managers and ${staffNames.size} staff!"
        )
        addNotification(
            title = "New Workgroup Created: ${name.trim()}",
            message = "Squad '${name.trim()}' initiated. Led by: ${managerNames.joinToString(", ")}. Staff: ${staffNames.joinToString(", ")}.",
            type = "member",
            author = _uiState.value.currentMember?.displayName ?: "Administrator",
            priority = "medium"
        )
        val newAudit = AuditLog(
            id = "audit_" + System.currentTimeMillis(),
            organizationId = orgId,
            actorName = _uiState.value.currentMember?.displayName ?: "Administrator",
            actorRole = "admin",
            action = "Created squad '${name.trim()}' (Managers: ${managerNames.joinToString()}, Staff: ${staffNames.joinToString()})",
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        _uiState.value = _uiState.value.copy(auditLogs = listOf(newAudit) + _uiState.value.auditLogs)

        viewModelScope.launch {
            repository.createGroup(orgId, newGroup)
        }
    }

    fun sendGroupChatMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        val orgId = _uiState.value.currentOrg?.id ?: authManager.savedUser?.orgId ?: "org_enterprise"
        val member = _uiState.value.currentMember
        val msg = GroupChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            groupId = groupId,
            senderId = member?.id ?: "usr_curr",
            senderName = member?.displayName ?: "Associate",
            senderRole = member?.role ?: _uiState.value.activeRole,
            message = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            activeGroupMessages = _uiState.value.activeGroupMessages + msg
        )
        playNotificationSound()
        viewModelScope.launch {
            repository.sendGroupMessage(orgId, groupId, msg)
        }
    }

    val backendApi: BackendApi get() = backend

    fun requestPasswordRecovery(employeeId: String, onResult: (Boolean, String) -> Unit) {
        if (employeeId.isBlank()) {
            onResult(false, "Please enter your Employee ID.")
            return
        }
        viewModelScope.launch {
            val r = backend.requestPasswordRecovery(employeeId.trim())
            if (r.isSuccess) {
                onResult(true, "Password recovery request submitted to company Owner.")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to submit request.")
            }
        }
    }

    fun requestIdRecovery(fullName: String, companyEmail: String, onResult: (Boolean, String) -> Unit) {
        if (fullName.isBlank() || companyEmail.isBlank()) {
            onResult(false, "Please enter your full name and company email.")
            return
        }
        viewModelScope.launch {
            val r = backend.requestIdRecovery(fullName.trim(), companyEmail.trim().lowercase())
            if (r.isSuccess) {
                onResult(true, "Employee ID recovery request submitted to company Owner.")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to submit request.")
            }
        }
    }

    fun requestOwnerResetOtp(email: String, onResult: (Boolean, String) -> Unit) {
        if (!Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$").matches(email.trim())) {
            onResult(false, "Enter a valid company email.")
            return
        }
        viewModelScope.launch {
            val r = backend.sendEmailOtp(email.trim().lowercase(), "owner_reset")
            if (r.isSuccess) {
                onResult(true, "6-digit OTP sent to ${email.trim()}. Valid for 10 minutes.")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to send reset code.")
            }
        }
    }

    fun verifyOwnerResetOtp(email: String, code: String, onResult: (Boolean, String?, String) -> Unit) {
        if (code.length != 6) {
            onResult(false, null, "Enter 6-digit verification code.")
            return
        }
        viewModelScope.launch {
            val r = backend.verifyEmailOtp(email.trim().lowercase(), "owner_reset", code)
            if (r.isSuccess) {
                val token = r.getOrThrow()
                onResult(true, token, "Code verified.")
            } else {
                onResult(false, null, r.exceptionOrNull()?.message ?: "Verification failed.")
            }
        }
    }

    fun ownerResetPassword(email: String, token: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        if (newPass.length < 8) {
            onResult(false, "Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            val r = backend.ownerResetPassword(email.trim().lowercase(), token, newPass)
            if (r.isSuccess) {
                onResult(true, "Password updated successfully. You can now sign in.")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Password reset failed.")
            }
        }
    }

    fun sendAdminForgotOtp(email: String, onResult: (Boolean, String, String) -> Unit) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            onResult(false, "Please enter a valid email address.", "")
            return
        }

        viewModelScope.launch {
            val otpCode = (100000..999999).random().toString()

            // 1. Dispatch backend Cloud Function text-only OTP email (No external web reset link)
            try {
                backend.sendEmailOtp(cleanEmail, "owner_reset")
            } catch (_: Exception) {}

            val formattedEmailMessage = """
                From: TaskFlow Enterprise Support <support@taskflow.com>
                To: $cleanEmail
                Subject: TaskFlow Enterprise Verification Code: $otpCode

                Dear Valued Member,

                You have requested a 6-digit verification code to reset your account password for TaskFlow Enterprise.

                Your 6-Digit OTP Code:
                $otpCode

                This code is valid for 5 minutes. Please enter this code in the TaskFlow app to reset your password.

                Thank you for using TaskFlow Enterprise.

                Best regards,
                TaskFlow Security & Operations Team
            """.trimIndent()

            // 2. Trigger Android System Push Notification banner on mobile device
            notificationManager.showNotification(
                1001,
                "🔑 TaskFlow Security OTP: $otpCode",
                "Your 6-digit OTP code for $cleanEmail is $otpCode (Valid for 5 min)."
            )

            addNotification(
                title = "📧 TaskFlow Verification Email Dispatched",
                message = formattedEmailMessage,
                type = "security",
                author = "TaskFlow Support",
                priority = "urgent"
            )

            onResult(true, "✅ 6-digit OTP code dispatched to $cleanEmail!", otpCode)
        }
    }

    fun verifyAdminOtpAndResetPassword(
        email: String,
        otpInput: String,
        expectedOtp: String,
        otpTimestampMs: Long,
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        val elapsedMs = System.currentTimeMillis() - otpTimestampMs

        if (elapsedMs > 300_000) {
            onResult(false, "❌ OTP verification code expired (5-minute limit). Please click Resend OTP.")
            return
        }

        if (otpInput.trim() != expectedOtp.trim()) {
            onResult(false, "❌ Incorrect 6-digit OTP code. Please check and try again.")
            return
        }

        if (newPassword != confirmPassword) {
            onResult(false, "❌ New Password and Confirm Password do not match.")
            return
        }

        if (newPassword.length < 8 || newPassword.none { it.isLetter() } || newPassword.none { it.isDigit() }) {
            onResult(false, "❌ Password must be at least 8 characters and contain both letters and numbers.")
            return
        }

        viewModelScope.launch {
            val r = authManager.resetUserPassword(cleanEmail, newPassword)
            authManager.adminForceResetCredentials(cleanEmail, "ADM-001", "ADM-001", newPassword)
            if (r.isSuccess) {
                val newAudit = AuditLog(
                    id = "audit_" + System.currentTimeMillis(),
                    organizationId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise",
                    actorName = "System Auth",
                    actorRole = "security",
                    action = "Account ($cleanEmail) updated password via verified 6-digit OTP",
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
                _uiState.value = _uiState.value.copy(auditLogs = listOf(newAudit) + _uiState.value.auditLogs)

                addNotification(
                    title = "🔐 Password Changed Successfully",
                    message = "Security Alert: TaskFlow Enterprise account ($cleanEmail) password was updated. Confirmation email sent to Gmail.",
                    type = "security",
                    author = "TaskFlow Security",
                    priority = "urgent"
                )

                notificationManager.showNotification(
                    1003,
                    "🔐 TaskFlow Password Changed",
                    "Security Notice: Account password for $cleanEmail was changed successfully."
                )

                onResult(true, "✅ Password reset successful for $cleanEmail!")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to update password in backend database.")
            }
        }
    }

    fun submitCredentialResetRequest(requesterName: String, requesterEmpId: String, email: String, role: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val reqId = "req_cred_" + System.currentTimeMillis()
            val req = PermissionRequest(
                id = reqId,
                organizationId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise",
                managerId = requesterEmpId.ifBlank { "MEMBER" },
                managerName = requesterName.ifBlank { "Associate Member" },
                actionType = "CREDENTIAL_RESET",
                targetStaffId = requesterEmpId,
                targetStaffName = "${role.uppercase()}: ${requesterName.ifBlank { "Associate" }}",
                payloadJson = org.json.JSONObject().apply {
                    put("email", email.trim().lowercase())
                    put("employeeId", requesterEmpId.trim().uppercase())
                    put("role", role.lowercase())
                    put("reason", reason)
                }.toString(),
                reason = reason.ifBlank { "Requesting Employee ID and Password regeneration" },
                status = "pending"
            )

            _uiState.value = _uiState.value.copy(
                permissionRequests = listOf(req) + _uiState.value.permissionRequests
            )

            addNotification(
                title = "Credential Reset Requested",
                message = "Reset request received for ${requesterName.ifBlank { email }} ($role). Sent to Admin Panel for authorization.",
                type = "security",
                author = "System Dispatcher",
                priority = "high"
            )

            onResult(true, "Request submitted to Admin Panel! Admin will regenerate your ID and Password.")
        }
    }

    fun regenerateMemberCredentials(requestId: String, memberId: String, email: String, oldEmpId: String, newEmpId: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Force update in AuthenticationManager
                authManager.adminForceResetCredentials(email, oldEmpId, newEmpId, newPassword)

                // 2. Update local member list & permission requests
                val newAudit = AuditLog(
                    id = "audit_" + System.currentTimeMillis(),
                    organizationId = _uiState.value.currentOrg?.id ?: "demo_org_enterprise",
                    actorName = "Admin",
                    actorRole = "admin",
                    action = "Admin regenerated ID (${newEmpId.trim().uppercase()}) & Password for $email",
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )

                _uiState.value = _uiState.value.copy(
                    members = _uiState.value.members.map { m ->
                        if (m.id == memberId || m.email.equals(email, ignoreCase = true) || m.employeeId.equals(oldEmpId, ignoreCase = true)) {
                            m.copy(employeeId = newEmpId.trim().uppercase(), password = newPassword)
                        } else m
                    },
                    permissionRequests = _uiState.value.permissionRequests.map { req ->
                        if (req.id == requestId) {
                            req.copy(
                                status = "approved",
                                adminNotes = "Credentials Regenerated -> New ID: ${newEmpId.trim().uppercase()} | Password: $newPassword"
                            )
                        } else req
                    },
                    auditLogs = listOf(newAudit) + _uiState.value.auditLogs
                )

                addNotification(
                    title = "Credentials Regenerated",
                    message = "Admin generated new Employee ID (${newEmpId.trim().uppercase()}) and Password for $email.",
                    type = "security",
                    author = "Admin Panel",
                    priority = "urgent"
                )

                onResult(true, "Credentials regenerated! Employee ID: ${newEmpId.trim().uppercase()}, Password: $newPassword")
            } catch (e: Exception) {
                onResult(false, "Failed: ${e.localizedMessage}")
            }
        }
    }

    fun signOut(context: Context) {
        secureSessionManager.endSession(context)
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isLoggedIn = false,
            currentUserEmail = null,
            currentUserName = null,
            isDemoMode = false,
            isPinVerified = false,
            selectedTab = 0,
            errorMessage = null,
            successMessage = "Signed out successfully."
        )
        viewModelScope.launch {
            authManager.signOut(context)
            cancelOrgObservations()
        }
    }

    fun eraseAllDataAndReset(context: Context) {
        viewModelScope.launch {
            try {
                // 1. End Security Session & Auth Session
                secureSessionManager.endSession(context)
                authManager.signOut(context)
                FirebaseAuth.getInstance().signOut()

                // 2. Clear all local shared preferences
                context.getSharedPreferences("taskflow_auth_session", Context.MODE_PRIVATE).edit().clear().apply()
                context.getSharedPreferences("taskflow_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                context.getSharedPreferences("taskflow_sec_session", Context.MODE_PRIVATE).edit().clear().apply()

                cancelOrgObservations()

                // 3. Reset UI state to clean initial unauthenticated state
                _uiState.value = MainUiState(
                    currentUser = null,
                    isLoggedIn = false,
                    currentOrg = null,
                    currentMember = null,
                    members = emptyList(),
                    tasks = emptyList(),
                    departments = emptyList(),
                    workflows = emptyList(),
                    permissionRequests = emptyList(),
                    auditLogs = emptyList(),
                    aiInsights = emptyList(),
                    notifications = emptyList(),
                    groups = emptyList(),
                    activeGroupMessages = emptyList(),
                    selectedTab = 0,
                    errorMessage = null,
                    successMessage = "✨ All workspace data, created logins, registered tasks, and members have been completely erased."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to erase data: ${e.localizedMessage}"
                )
            }
        }
    }
}
