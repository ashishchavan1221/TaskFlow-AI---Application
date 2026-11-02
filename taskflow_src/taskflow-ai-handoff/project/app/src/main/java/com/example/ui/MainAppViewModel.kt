package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.security.SecureSessionManager
import com.example.data.auth.AuthenticationManager
import org.json.JSONObject
import com.example.data.backend.BackendApi
import com.example.data.auth.AuthUserData
import com.example.data.model.*
import com.example.data.repository.TaskFlowRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val appPin: String = ""
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
            appPin = secureSessionManager.sessionState.value.pin,
            isPinVerified = !secureSessionManager.sessionState.value.isLocked
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var orgJob: Job? = null
    private var membersJob: Job? = null
    private var tasksJob: Job? = null
    private var deptsJob: Job? = null
    private var workflowsJob: Job? = null
    private var permissionReqJob: Job? = null
    private var auditJob: Job? = null
    private var aiJob: Job? = null

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

        // A stored session is only valid while a real Firebase user exists.
        try {
            if (auth.currentUser == null && secureSessionManager.sessionState.value.isAuthenticated) {
                secureSessionManager.endSession(application)
            }
        } catch (_: Exception) {}

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
    }



    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun setActiveRole(role: String) {
        val member = _uiState.value.members.firstOrNull { it.role.equals(role, ignoreCase = true) }
            ?: _uiState.value.currentMember?.copy(role = role)
        _uiState.value = _uiState.value.copy(
            activeRole = role,
            currentMember = member,
            successMessage = "Switched view to ${role.replaceFirstChar { it.uppercase() }} Mode"
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    private fun isValidGstin(g: String): Boolean {
        if (!Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$").matches(g)) return false
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var sum = 0
        for (i in 0 until 14) {
            val p = chars.indexOf(g[i]) * (if (i % 2 == 0) 1 else 2)
            sum += p / 36 + p % 36
        }
        return g[14] == chars[(36 - sum % 36) % 36]
    }

    private fun registrationProblem(
        ownerName: String, companyName: String, email: String, phone: String,
        password: String, confirm: String, gstin: String
    ): String? = when {
        ownerName.isBlank() -> "Enter the Owner's full name."
        companyName.isBlank() -> "Enter the company name."
        !Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$").matches(email.trim()) -> "Enter a valid company e-mail."
        !Regex("^(\\+91|91|0)?[6-9][0-9]{9}$|^\\+[1-9][0-9]{7,14}$").matches(phone.replace(Regex("[\\s\\-()]"), "")) -> "Enter a valid phone number."
        password.length < 8 || password.none { it.isLetter() } || password.none { it.isDigit() } -> "Password must be 8+ characters with letters and digits."
        password != confirm -> "Passwords do not match."
        !isValidGstin(gstin.trim().uppercase()) -> "Enter a valid GSTIN (format and check digit are validated; government verification is not performed)."
        else -> null
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
                if (taskList.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(tasks = taskList)
                }
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
        phone: String
    ) {
        // employeeId and password are generated by the server; the parameters are kept for call-site compatibility.
        if (_uiState.value.currentOrg?.id == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Company data is not loaded yet. Please wait and try again.")
            return
        }
        // Managers never create accounts directly: they submit a request for Owner approval.
        if (_uiState.value.activeRole.equals("manager", ignoreCase = true)) {
            val payload = JSONObject()
                .put("name", name).put("email", email).put("role", role)
                .put("department", departmentName).put("departmentId", departmentId).put("phone", phone).toString()
            submitPermissionRequest(
                actionType = "CREATE_STAFF", targetStaffId = "", targetStaffName = name, payloadJson = payload,
                reason = "Manager request to onboard $role '$name' into $departmentName."
            )
            return
        }
        if (!_uiState.value.activeRole.equals("admin", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(errorMessage = "You are not allowed to create accounts.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val r = backend.createMember(name.trim(), role.lowercase(), phone, departmentId, departmentName, email)
            _uiState.value = if (r.isSuccess) {
                val m = r.getOrThrow()
                _uiState.value.copy(isLoading = false, successMessage =
                    "Created ${name.trim()}. ID: ${m["employeeId"]}  Temporary password: ${m["tempPassword"]}  (shown once - share it securely).")
            } else _uiState.value.copy(isLoading = false, errorMessage = r.exceptionOrNull()?.message ?: "Could not create account.")
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
            _uiState.value = _uiState.value.copy(errorMessage = "Not allowed."); return
        }
        viewModelScope.launch {
            val r = backend.deactivateMember(memberId)
            _uiState.value = if (r.isSuccess) _uiState.value.copy(successMessage = "'$memberName' deactivated. Their sessions were revoked.")
            else _uiState.value.copy(errorMessage = r.exceptionOrNull()?.message)
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



    fun createTask(
        title: String,
        description: String,
        departmentId: String,
        priority: String,
        assignedMember: Member?,
        startDate: String,
        dueDate: String,
        estimatedHours: Double
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
            dueDate = dueDate
        )

        _uiState.value = _uiState.value.copy(
            tasks = listOf(newTask) + _uiState.value.tasks,
            successMessage = "Task '$title' created and assigned successfully."
        )

        notificationManager.showNotification(
            1002,
            "New Task Delegated: $title",
            "Priority: ${priority.uppercase()} • Assigned to: ${assignedMember?.displayName ?: "Team"}"
        )

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
                creator = creator
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
}
