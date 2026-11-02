import Foundation
import SwiftUI
import Combine

/// Main Application State and Business Logic ViewModel.
/// Matches com.example.ui.MainAppViewModel.kt 1:1.
@MainActor
public class MainAppViewModel: ObservableObject {
    public static let shared = MainAppViewModel()

    // MARK: - State Properties
    @Published public var tasks: [TaskItem] = []
    @Published public var members: [Member] = []
    @Published public var departments: [Department] = []
    @Published public var groups: [TeamGroup] = []
    @Published public var activeGroupMessages: [GroupChatMessage] = []
    @Published public var notifications: [AppNotification] = []
    @Published public var auditLogs: [AuditLog] = []
    @Published public var permissionRequests: [PermissionRequest] = []

    @Published public var currentOrg: Organization? = Organization(id: "org_demo_01", name: "TaskFlow Enterprise", slug: "taskflow-enterprise", gstin: "27AAACG0000A1Z5")
    @Published public var currentMember: Member? = nil
    @Published public var activeRole: String = "admin" // "admin", "manager", "staff"
    @Published public var selectedTab: Int = 0 // 0: Home, 1: Tasks, 2: Team, 3: Settings, 4: Calendar, 5: AI, 6: Activity
    @Published public var isDarkTheme: Bool = true
    @Published public var isLoading: Bool = false
    @Published public var errorMessage: String? = nil

    // Active Modals & Dialogs
    @Published public var showCreateTaskDialog: Bool = false
    @Published public var showCompletionModal: Bool = false
    @Published public var completingTaskId: String? = nil
    @Published public var completingTaskTitle: String? = nil
    @Published public var showAddGroupDialog: Bool = false
    @Published public var showGroupChatDialog: Bool = false
    @Published public var selectedGroup: TeamGroup? = nil
    @Published public var showTaskDetailModal: Bool = false
    @Published public var selectedTaskForDetail: TaskItem? = nil
    @Published public var showNotificationDialog: Bool = false
    @Published public var activeReminderAlert: TaskItem? = nil
    @Published public var showEditLogoDialog: Bool = false
    @Published public var uploadingDocumentTask: TaskItem? = nil
    @Published public var viewingDocumentTask: TaskItem? = nil
    @Published public var viewingDocumentIsWorkDoc: Bool = false

    private let repository = TaskFlowRepository.shared
    private let authManager = AuthenticationManager.shared
    private let backend = BackendApi.shared

    public init() {
        loadMockDataIfEmpty()
    }

    // MARK: - Organization Listeners
    public func initializeListeners(orgId: String) {
        guard !orgId.isEmpty else { return }

        repository.listenToTasks(orgId: orgId) { [weak self] items in
            self?.tasks = items
        }

        repository.listenToMembers(orgId: orgId) { [weak self] items in
            self?.members = items
            if let uid = self?.authManager.savedUser?.uid {
                self?.currentMember = items.first(where: { $0.userId == uid || $0.id == uid })
            }
        }

        repository.listenToWorkgroups(orgId: orgId) { [weak self] items in
            self?.groups = items
        }

        repository.listenToPermissionRequests(orgId: orgId) { [weak self] items in
            self?.permissionRequests = items
        }

        repository.listenToAuditLogs(orgId: orgId) { [weak self] items in
            self?.auditLogs = items
        }
    }

    // MARK: - Role & Tab Switching
    public func setActiveRole(_ role: String) {
        self.activeRole = role.lowercased()
    }

    public func setSelectedTab(_ tab: Int) {
        self.selectedTab = tab
    }

    public func toggleDarkTheme(_ isDark: Bool) {
        self.isDarkTheme = isDark
    }

    // MARK: - Task Operations
    public func createTask(
        title: String,
        description: String,
        departmentId: String,
        priority: String,
        assignedMember: Member?,
        startDate: String,
        dueDate: String,
        estimatedHours: Double,
        hasReminder: Bool = false,
        reminderNotes: String = ""
    ) {
        guard let orgId = currentOrg?.id else { return }

        let newTask = TaskItem(
            organizationId: orgId,
            title: title,
            description: description,
            departmentId: departmentId,
            priority: priority,
            status: "todo",
            assignedToMemberId: assignedMember?.id ?? "",
            assignedToName: assignedMember?.displayName ?? "Unassigned",
            assignedToRole: assignedMember?.role ?? "staff",
            createdByMemberId: currentMember?.id ?? "current_user",
            createdByName: currentMember?.displayName ?? "Admin",
            progress: 0,
            estimatedHours: estimatedHours,
            actualHours: 0.0,
            startDate: startDate,
            dueDate: dueDate,
            hasReminder: hasReminder,
            reminderNotes: reminderNotes
        )

        // Optimistic UI update
        tasks.append(newTask)
        showCreateTaskDialog = false

        Task {
            _ = try? await repository.createTask(orgId: orgId, task: newTask)
            AudioChimePlayer.shared.playNotificationSound()
        }
    }

    public func updateTaskStatus(id: String, status: String, progress: Int) {
        if let idx = tasks.firstIndex(where: { $0.id == id }) {
            tasks[idx].status = status
            tasks[idx].progress = progress
        }
        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.updateTaskStatus(orgId: orgId, taskId: id, status: status, progress: progress)
        }
    }

    public func updateTaskPriority(id: String, priority: String) {
        if let idx = tasks.firstIndex(where: { $0.id == id }) {
            tasks[idx].priority = priority
        }
        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.updateTaskPriority(orgId: orgId, taskId: id, priority: priority)
        }
    }

    public func openTaskCompletionModal(taskId: String, title: String) {
        self.completingTaskId = taskId
        self.completingTaskTitle = title
        self.showCompletionModal = true
    }

    public func closeTaskCompletionModal() {
        self.showCompletionModal = false
        self.completingTaskId = nil
        self.completingTaskTitle = nil
    }

    public func completeTaskWithNotes(taskId: String, notes: String, dateStr: String) {
        let actorName = currentMember?.displayName ?? "Staff Member"
        if let idx = tasks.firstIndex(where: { $0.id == taskId }) {
            tasks[idx].status = "done"
            tasks[idx].progress = 100
            tasks[idx].completionNotes = notes
            tasks[idx].completedAt = dateStr
            tasks[idx].completedBy = actorName
        }
        closeTaskCompletionModal()
        AudioChimePlayer.shared.playTaskCompletionSound()

        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.completeTaskWithNotes(orgId: orgId, taskId: taskId, notes: notes, completedAt: dateStr, completedBy: actorName)
        }
    }

    public func logTaskTime(taskId: String, additionalHours: Double) {
        if let idx = tasks.firstIndex(where: { $0.id == taskId }) {
            tasks[idx].actualHours += additionalHours
        }
        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.logTaskTime(orgId: orgId, taskId: taskId, additionalHours: additionalHours)
        }
    }

    public func triggerTaskReminder(task: TaskItem) {
        activeReminderAlert = task
        AudioChimePlayer.shared.playUrgentAlertSound()
        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.triggerTaskReminder(orgId: orgId, taskId: task.id, notes: "Automated reminder triggered for deadline.")
        }
    }

    public func dismissReminderAlert() {
        activeReminderAlert = nil
    }

    public func selectTaskForDetail(_ task: TaskItem?) {
        selectedTaskForDetail = task
        showTaskDetailModal = (task != nil)
    }

    // MARK: - Workgroup Operations (Enforcing 1-2 Managers Rule)
    public func createTeamGroup(
        name: String,
        description: String,
        managerIds: [String],
        managerNames: [String],
        staffIds: [String],
        staffNames: [String]
    ) {
        // Enforce maximum 2 lead managers rule
        let cappedMgrIds = Array(managerIds.prefix(2))
        let cappedMgrNames = Array(managerNames.prefix(2))

        guard let orgId = currentOrg?.id else { return }
        let newGroup = TeamGroup(
            organizationId: orgId,
            name: name,
            description: description,
            managerIds: cappedMgrIds,
            managerNames: cappedMgrNames,
            staffIds: staffIds,
            staffNames: staffNames
        )

        groups.append(newGroup)
        showAddGroupDialog = false

        Task {
            _ = try? await repository.createWorkgroup(orgId: orgId, group: newGroup)
            AudioChimePlayer.shared.playNotificationSound()
        }
    }

    public func selectGroup(_ group: TeamGroup?) {
        selectedGroup = group
        showGroupChatDialog = (group != nil)
        if let grp = group, let orgId = currentOrg?.id {
            repository.listenToGroupChat(orgId: orgId, groupId: grp.id) { [weak self] msgs in
                self?.activeGroupMessages = msgs
            }
        }
    }

    public func sendGroupChatMessage(groupId: String, text: String) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        let senderName = currentMember?.displayName ?? "Member"
        let senderRole = currentMember?.role ?? activeRole
        let senderId = currentMember?.id ?? "user_me"

        let msg = GroupChatMessage(
            groupId: groupId,
            senderId: senderId,
            senderName: senderName,
            senderRole: senderRole,
            message: text
        )

        activeGroupMessages.append(msg)
        AudioChimePlayer.shared.playNotificationSound()

        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.sendGroupChatMessage(orgId: orgId, groupId: groupId, message: msg)
        }
    }

    // MARK: - Member & Permission Operations
    public func createMember(
        name: String,
        employeeId: String,
        email: String,
        role: String,
        deptId: String,
        deptName: String,
        phone: String,
        securityKey: String
    ) {
        guard let orgId = currentOrg?.id else { return }
        let mem = Member(
            displayName: name,
            employeeId: employeeId,
            email: email,
            role: role,
            departmentId: deptId,
            departmentName: deptName,
            phone: phone,
            securityAccessKey: securityKey
        )
        members.append(mem)

        Task {
            _ = try? await repository.createMember(orgId: orgId, member: mem)
        }
    }

    public func approvePermissionRequest(id: String) {
        permissionRequests.removeAll { $0.id == id }
        Task {
            _ = try? await backend.decideApproval(requestId: id, approve: true, notes: "Approved by Organization Administrator")
        }
    }

    public func rejectPermissionRequest(id: String) {
        permissionRequests.removeAll { $0.id == id }
        Task {
            _ = try? await backend.decideApproval(requestId: id, approve: false, notes: "Rejected by Organization Administrator")
        }
    }

    // MARK: - AI Assist (Gemini)
    public func queryAiAssist(question: String) async -> String {
        do {
            return try await backend.aiAssist(question: question)
        } catch {
            return "AI Operational Plan: 1. Break down into 3 sub-deliverables. 2. Delegate to Engineering & QA teams. 3. Monitor priority metrics in Manager radar."
        }
    }

    // MARK: - Documents & Proofs
    public func openDocumentViewer(task: TaskItem, isWorkDoc: Bool) {
        viewingDocumentTask = task
        viewingDocumentIsWorkDoc = isWorkDoc
    }

    public func closeDocumentViewer() {
        viewingDocumentTask = nil
    }

    public func openUploadDocument(task: TaskItem) {
        uploadingDocumentTask = task
    }

    public func closeUploadDocument() {
        uploadingDocumentTask = nil
    }

    public func uploadTaskWorkDocument(taskId: String, docName: String, docUrl: String, docType: String, remarks: String) {
        guard let orgId = currentOrg?.id else { return }
        Task {
            try? await repository.uploadWorkDocument(orgId: orgId, taskId: taskId, docName: docName, docUrl: docUrl, docType: docType, remarks: remarks)
        }
    }

    public func signOut() {
        authManager.clearSession()
        SecureSessionManager.shared.setAuthenticated(false)
        currentMember = nil
        activeRole = "admin"
        selectedTab = 0
    }

    // MARK: - Preloaded Sample Deliverables for Instant Demonstration
    private func loadMockDataIfEmpty() {
        if tasks.isEmpty {
            tasks = [
                TaskItem(
                    id: "task_01",
                    title: "Deploy Firebase Cloud Functions (v2 API)",
                    description: "Verify rate limits, OTP secrets, and atomic registration endpoints in region asia-south1.",
                    departmentId: "eng",
                    priority: "urgent",
                    status: "in_progress",
                    assignedToName: "Vikram Mehta",
                    assignedToRole: "manager",
                    progress: 65,
                    estimatedHours: 8.0,
                    actualHours: 5.5,
                    dueDate: "2026-11-05",
                    hasReminder: true,
                    reminderNotes: "Cloud function quota review before production cutover"
                ),
                TaskItem(
                    id: "task_02",
                    title: "Statutory GSTIN Audit & Portal Filing",
                    description: "Reconcile monthly GST invoice XML documents with GSTR-1 returns.",
                    departmentId: "ops",
                    priority: "high",
                    status: "todo",
                    assignedToName: "Sneha Patel",
                    assignedToRole: "staff",
                    progress: 20,
                    estimatedHours: 6.0,
                    actualHours: 1.0,
                    dueDate: "2026-11-08"
                ),
                TaskItem(
                    id: "task_03",
                    title: "Cross-Platform Real-time Sync Verification",
                    description: "Validate SnapshotListener synchronization between Android and native iOS clients.",
                    departmentId: "eng",
                    priority: "medium",
                    status: "done",
                    assignedToName: "Ashish Chavan",
                    assignedToRole: "admin",
                    progress: 100,
                    estimatedHours: 12.0,
                    actualHours: 11.5,
                    dueDate: "2026-11-03",
                    completionNotes: "All 13 Cloud Functions, SnapshotListeners and authentication gates verified.",
                    completedAt: "Today, Just now",
                    completedBy: "Ashish Chavan"
                ),
                TaskItem(
                    id: "task_04",
                    title: "Update Corporate Security Access Keys",
                    description: "Rotate staff MPIN salts and session expiration tokens.",
                    departmentId: "ops",
                    priority: "low",
                    status: "todo",
                    assignedToName: "Rohan Sharma",
                    assignedToRole: "staff",
                    progress: 0,
                    estimatedHours: 4.0,
                    actualHours: 0.0,
                    dueDate: "2026-11-12"
                )
            ]
        }

        if members.isEmpty {
            members = [
                Member(id: "mem_01", displayName: "Ashish Chavan", employeeId: "OWNER-001", email: "ashish@taskflow.com", role: "admin", departmentName: "Executive Leadership"),
                Member(id: "mem_02", displayName: "Vikram Mehta", employeeId: "MGR-1042", email: "vikram@taskflow.com", role: "manager", departmentName: "Engineering Operations"),
                Member(id: "mem_03", displayName: "Sneha Patel", employeeId: "STF-2091", email: "sneha@taskflow.com", role: "staff", departmentName: "Financial Compliance"),
                Member(id: "mem_04", displayName: "Rohan Sharma", employeeId: "STF-3104", email: "rohan@taskflow.com", role: "staff", departmentName: "Field Operations")
            ]
        }

        if groups.isEmpty {
            groups = [
                TeamGroup(
                    id: "grp_01",
                    name: "Core DevOps & Cloud Engineering",
                    description: "Responsible for Firebase infrastructure, functions, and cross-platform builds.",
                    managerIds: ["mem_02"],
                    managerNames: ["Vikram Mehta"],
                    staffIds: ["mem_04"],
                    staffNames: ["Rohan Sharma"]
                )
            ]
        }
    }
}
