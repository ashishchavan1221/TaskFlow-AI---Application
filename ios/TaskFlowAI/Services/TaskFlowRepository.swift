import Foundation
import FirebaseFirestore
import FirebaseStorage

/// Cloud Firestore and Cloud Storage data repository.
/// Matches TaskFlowRepository.kt 1:1.
public class TaskFlowRepository {
    public static let shared = TaskFlowRepository()

    private let db = Firestore.firestore()
    private let storage = Storage.storage().reference()

    private var tasksListener: ListenerRegistration?
    private var membersListener: ListenerRegistration?
    private var groupsListener: ListenerRegistration?
    private var requestsListener: ListenerRegistration?
    private var auditLogsListener: ListenerRegistration?
    private var groupChatListener: ListenerRegistration?

    public init() {}

    // MARK: - Real-Time Listeners

    public func listenToTasks(orgId: String, onUpdate: @escaping ([TaskItem]) -> Void) {
        tasksListener?.remove()
        guard !orgId.isEmpty else { return }

        tasksListener = db.collection("organizations/\(orgId)/tasks")
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [TaskItem] = documents.compactMap { doc -> TaskItem? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(TaskItem.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func listenToMembers(orgId: String, onUpdate: @escaping ([Member]) -> Void) {
        membersListener?.remove()
        guard !orgId.isEmpty else { return }

        membersListener = db.collection("organizations/\(orgId)/members")
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [Member] = documents.compactMap { doc -> Member? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(Member.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func listenToWorkgroups(orgId: String, onUpdate: @escaping ([TeamGroup]) -> Void) {
        groupsListener?.remove()
        guard !orgId.isEmpty else { return }

        groupsListener = db.collection("organizations/\(orgId)/workgroups")
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [TeamGroup] = documents.compactMap { doc -> TeamGroup? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(TeamGroup.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func listenToGroupChat(orgId: String, groupId: String, onUpdate: @escaping ([GroupChatMessage]) -> Void) {
        groupChatListener?.remove()
        guard !orgId.isEmpty, !groupId.isEmpty else { return }

        groupChatListener = db.collection("organizations/\(orgId)/workgroups/\(groupId)/messages")
            .order(by: "timestamp", descending: false)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [GroupChatMessage] = documents.compactMap { doc -> GroupChatMessage? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(GroupChatMessage.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func listenToPermissionRequests(orgId: String, onUpdate: @escaping ([PermissionRequest]) -> Void) {
        requestsListener?.remove()
        guard !orgId.isEmpty else { return }

        requestsListener = db.collection("organizations/\(orgId)/permission_requests")
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [PermissionRequest] = documents.compactMap { doc -> PermissionRequest? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(PermissionRequest.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func listenToAuditLogs(orgId: String, onUpdate: @escaping ([AuditLog]) -> Void) {
        auditLogsListener?.remove()
        guard !orgId.isEmpty else { return }

        auditLogsListener = db.collection("organizations/\(orgId)/audit_logs")
            .limit(to: 50)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents else { return }
                let items: [AuditLog] = documents.compactMap { doc -> AuditLog? in
                    var data = doc.data()
                    data["id"] = doc.documentID
                    guard let jsonData = try? JSONSerialization.data(withJSONObject: data) else { return nil }
                    return try? JSONDecoder().decode(AuditLog.self, from: jsonData)
                }
                onUpdate(items)
            }
    }

    public func removeAllListeners() {
        tasksListener?.remove()
        membersListener?.remove()
        groupsListener?.remove()
        requestsListener?.remove()
        auditLogsListener?.remove()
        groupChatListener?.remove()
    }

    // MARK: - Task Operations

    public func createTask(orgId: String, task: TaskItem) async throws -> String {
        let colRef = db.collection("organizations/\(orgId)/tasks")
        let docRef = colRef.document()
        var newTask = task
        newTask.id = docRef.documentID
        newTask.organizationId = orgId

        let encoder = JSONEncoder()
        let data = try encoder.encode(newTask)
        let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        try await docRef.setData(dict)
        return docRef.documentID
    }

    public func updateTaskStatus(orgId: String, taskId: String, status: String, progress: Int) async throws {
        try await db.document("organizations/\(orgId)/tasks/\(taskId)").updateData([
            "status": status,
            "progress": progress
        ])
    }

    public func updateTaskPriority(orgId: String, taskId: String, priority: String) async throws {
        try await db.document("organizations/\(orgId)/tasks/\(taskId)").updateData([
            "priority": priority
        ])
    }

    public func completeTaskWithNotes(orgId: String, taskId: String, notes: String, completedAt: String, completedBy: String) async throws {
        try await db.document("organizations/\(orgId)/tasks/\(taskId)").updateData([
            "status": "done",
            "progress": 100,
            "completionNotes": notes,
            "completedAt": completedAt,
            "completedBy": completedBy
        ])
    }

    public func logTaskTime(orgId: String, taskId: String, additionalHours: Double) async throws {
        let docRef = db.document("organizations/\(orgId)/tasks/\(taskId)")
        let snap = try await docRef.getDocument()
        let currentActual = snap.data()?["actualHours"] as? Double ?? 0.0
        try await docRef.updateData([
            "actualHours": currentActual + additionalHours
        ])
    }

    public func triggerTaskReminder(orgId: String, taskId: String, notes: String) async throws {
        try await db.document("organizations/\(orgId)/tasks/\(taskId)").updateData([
            "hasReminder": true,
            "reminderNotes": notes
        ])
    }

    public func uploadWorkDocument(orgId: String, taskId: String, docName: String, docUrl: String, docType: String, remarks: String) async throws {
        let dateStr = ISO8601DateFormatter().string(from: Date())
        try await db.document("organizations/\(orgId)/tasks/\(taskId)").updateData([
            "workDocName": docName,
            "workDocUrl": docUrl,
            "workDocType": docType,
            "workDocUploadedAt": dateStr,
            "workDocUploadedBy": remarks
        ])
    }

    // MARK: - Team & Workgroups

    public func createWorkgroup(orgId: String, group: TeamGroup) async throws -> String {
        // Enforce maximum 2 managers constraint
        var boundedGroup = group
        if boundedGroup.managerIds.count > 2 {
            boundedGroup.managerIds = Array(boundedGroup.managerIds.prefix(2))
            boundedGroup.managerNames = Array(boundedGroup.managerNames.prefix(2))
        }

        let docRef = db.collection("organizations/\(orgId)/workgroups").document()
        boundedGroup.id = docRef.documentID
        boundedGroup.organizationId = orgId

        let encoder = JSONEncoder()
        let data = try encoder.encode(boundedGroup)
        let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        try await docRef.setData(dict)
        return docRef.documentID
    }

    public func sendGroupChatMessage(orgId: String, groupId: String, message: GroupChatMessage) async throws {
        let docRef = db.collection("organizations/\(orgId)/workgroups/\(groupId)/messages").document()
        var newMsg = message
        newMsg.id = docRef.documentID

        let encoder = JSONEncoder()
        let data = try encoder.encode(newMsg)
        let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        try await docRef.setData(dict)
    }

    public func createMember(orgId: String, member: Member) async throws -> String {
        let docRef = db.collection("organizations/\(orgId)/members").document()
        var newMember = member
        newMember.id = docRef.documentID

        let encoder = JSONEncoder()
        let data = try encoder.encode(newMember)
        let dict = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        try await docRef.setData(dict)
        return docRef.documentID
    }

    public func updateMember(orgId: String, memberId: String, fields: [String: Any]) async throws {
        try await db.document("organizations/\(orgId)/members/\(memberId)").updateData(fields)
    }

    public func deleteMember(orgId: String, memberId: String) async throws {
        try await db.document("organizations/\(orgId)/members/\(memberId)").delete()
    }

    // MARK: - Organization & Logo

    public func updateOrganizationLogo(orgId: String, logoUrl: String) async throws {
        try await db.document("organizations/\(orgId)").updateData([
            "logoUrl": logoUrl
        ])
    }

    public func uploadLogoImage(orgId: String, imageData: Data) async throws -> String {
        let fileRef = storage.child("companies/\(orgId)/logo/logo_\(Int(Date().timeIntervalSince1970)).png")
        let metadata = StorageMetadata()
        metadata.contentType = "image/png"

        _ = try await fileRef.putDataAsync(imageData, metadata: metadata)
        let downloadUrl = try await fileRef.downloadURL()
        return downloadUrl.absoluteString
    }
}
