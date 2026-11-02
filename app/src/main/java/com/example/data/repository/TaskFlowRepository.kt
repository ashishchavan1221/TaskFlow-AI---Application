package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskFlowRepository(private val context: Context) {
    private val firestore: FirebaseFirestore by lazy {
        val dbId = try {
            context.getString(com.example.R.string.firestore_database_id)
        } catch (_: Exception) { "" }
        if (dbId.isNotBlank()) {
            com.google.firebase.FirebaseApp.getInstance()?.let { app ->
                FirebaseFirestore.getInstance(app, dbId)
            } ?: FirebaseFirestore.getInstance()
        } else {
            FirebaseFirestore.getInstance()
        }
    }


    fun observeOrganization(orgId: String): Flow<Organization?> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val org = try {
                    snapshot.toObject(Organization::class.java)?.let {
                        if (it.id.isBlank()) it.copy(id = snapshot.id) else it
                    }
                } catch (_: Exception) {
                    val data = snapshot.data
                    if (data != null) {
                        Organization(
                            id = snapshot.id,
                            name = data["name"] as? String ?: "",
                            slug = data["slug"] as? String ?: "",
                            gstin = data["gstin"] as? String ?: "",
                            industry = data["industry"] as? String ?: "",
                            companySize = data["companySize"] as? String ?: "",
                            timezone = data["timezone"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            logoUrl = data["logoUrl"] as? String ?: ""
                        )
                    } else null
                }
                trySend(org)
            }
        awaitClose { listener.remove() }
    }

    fun observeMembers(orgId: String): Flow<List<Member>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Member::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) {
                        val d = doc.data ?: return@mapNotNull null
                        Member(
                            id = doc.id,
                            userId = d["userId"] as? String ?: "",
                            displayName = d["displayName"] as? String ?: "",
                            employeeId = d["employeeId"] as? String ?: "",
                            email = d["email"] as? String ?: "",
                            role = d["role"] as? String ?: "staff",
                            departmentId = d["departmentId"] as? String ?: "",
                            departmentName = d["departmentName"] as? String ?: "",
                            phone = d["phone"] as? String ?: "",
                            status = d["status"] as? String ?: "active"
                        )
                    }
                }
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    fun observeTasks(orgId: String): Flow<List<TaskItem>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(TaskItem::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) {
                        val d = doc.data ?: return@mapNotNull null
                        TaskItem(
                            id = doc.id,
                            organizationId = d["organizationId"] as? String ?: orgId,
                            title = d["title"] as? String ?: "",
                            description = d["description"] as? String ?: "",
                            departmentId = d["departmentId"] as? String ?: "",
                            priority = d["priority"] as? String ?: "medium",
                            status = d["status"] as? String ?: "todo",
                            assignedToMemberId = d["assignedToMemberId"] as? String ?: "",
                            assignedToName = d["assignedToName"] as? String ?: "",
                            assignedToRole = d["assignedToRole"] as? String ?: "",
                            createdByMemberId = d["createdByMemberId"] as? String ?: "",
                            createdByName = d["createdByName"] as? String ?: "",
                            progress = (d["progress"] as? Number)?.toInt() ?: 0,
                            estimatedHours = (d["estimatedHours"] as? Number)?.toDouble() ?: 0.0,
                            actualHours = (d["actualHours"] as? Number)?.toDouble() ?: 0.0,
                            blockerReason = d["blockerReason"] as? String ?: "",
                            startDate = d["startDate"] as? String ?: "",
                            dueDate = d["dueDate"] as? String ?: "",
                            completionNotes = d["completionNotes"] as? String ?: "",
                            completedAt = d["completedAt"] as? String ?: "",
                            completedBy = d["completedBy"] as? String ?: "",
                            hasReminder = d["hasReminder"] as? Boolean ?: false,
                            reminderNotes = d["reminderNotes"] as? String ?: ""
                        )
                    }
                }
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    fun observeDepartments(orgId: String): Flow<List<Department>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("departments")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val depts = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Department::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) { null }
                }
                trySend(depts)
            }
        awaitClose { listener.remove() }
    }

    fun observeWorkflows(orgId: String): Flow<List<Workflow>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("workflows")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val workflows = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Workflow::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) { null }
                }
                trySend(workflows)
            }
        awaitClose { listener.remove() }
    }

    fun observePermissionRequests(orgId: String): Flow<List<PermissionRequest>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("permission_requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reqs = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(PermissionRequest::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) { null }
                }
                trySend(reqs)
            }
        awaitClose { listener.remove() }
    }

    fun observeAuditLogs(orgId: String): Flow<List<AuditLog>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("audit_logs")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(AuditLog::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) { null }
                }
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    fun observeAiInsights(orgId: String): Flow<List<AiInsight>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("ai_insights")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val insights = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(AiInsight::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) { null }
                }
                trySend(insights)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createOrganization(
        name: String,
        slug: String,
        industry: String,
        companySize: String,
        timezone: String,
        description: String,
        gstin: String,
        adminUserId: String,
        adminEmail: String,
        adminName: String,
        orgId: String? = null,
        memberId: String? = null
    ): Result<String> {
        return try {
            val orgRef = if (orgId != null) firestore.collection("organizations").document(orgId)
                         else firestore.collection("organizations").document()
            val org = Organization(
                id = orgRef.id,
                name = name,
                slug = slug,
                industry = industry,
                companySize = companySize,
                timezone = timezone,
                description = description,
                gstin = gstin
            )

            val adminMember = Member(
                id = memberId ?: ("admin_" + orgRef.id.take(8)),
                userId = adminUserId,
                displayName = adminName,
                employeeId = "ADM-001",
                email = adminEmail,
                role = "admin",
                departmentName = "Executive Leadership",
                status = "active"
            )
            // Atomic: either both the company and its Owner member exist, or neither does.
            val batch = firestore.batch()
            batch.set(orgRef, org)
            batch.set(orgRef.collection("members").document(adminMember.id), adminMember)
            batch.commit().await()

            Result.success(orgRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMemberWithCredentials(
        orgId: String,
        name: String,
        employeeId: String,
        email: String,
        password: String,
        role: String,
        departmentId: String,
        departmentName: String,
        phone: String,
        actor: Member?,
        memberId: String? = null,
        userId: String? = null
    ) {
        run {
            val memberRef = firestore.collection("organizations").document(orgId).collection("members").document(memberId ?: firestore.collection("organizations").document().id)
            val newMember = Member(
                id = memberRef.id,
                userId = userId ?: ("usr_" + memberRef.id.take(8)),
                displayName = name,
                employeeId = employeeId,
                email = email,
                password = "", // passwords are never stored
                role = role,
                departmentId = departmentId,
                departmentName = departmentName,
                phone = phone,
                status = "active"
            )
            memberRef.set(newMember).await() // errors propagate to the caller
        }
    }

    suspend fun updateMember(
        orgId: String,
        memberId: String,
        name: String,
        role: String,
        departmentId: String,
        departmentName: String,
        status: String,
        actor: Member?
    ) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("members").document(memberId)
                .update(
                    mapOf(
                        "displayName" to name,
                        "role" to role,
                        "departmentId" to departmentId,
                        "departmentName" to departmentName,
                        "status" to status
                    )
                ).await()
        } catch (_: Exception) {}
    }

    suspend fun deleteMember(orgId: String, memberId: String, actor: Member?) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("members").document(memberId)
                .delete().await()
        } catch (_: Exception) {}
    }

    suspend fun submitPermissionRequest(
        orgId: String,
        managerId: String,
        managerName: String,
        actionType: String,
        targetStaffId: String,
        targetStaffName: String,
        payloadJson: String,
        reason: String
    ) {
        try {
            val ref = firestore.collection("organizations").document(orgId).collection("permission_requests").document()
            val req = PermissionRequest(
                id = ref.id,
                organizationId = orgId,
                managerId = managerId,
                managerName = managerName,
                actionType = actionType,
                targetStaffId = targetStaffId,
                targetStaffName = targetStaffName,
                payloadJson = payloadJson,
                reason = reason,
                status = "pending"
            )
            ref.set(req).await()
        } catch (_: Exception) {}
    }

    suspend fun approvePermissionRequest(
        orgId: String,
        requestId: String,
        adminId: String,
        adminName: String,
        adminNotes: String
    ) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("permission_requests").document(requestId)
                .update(mapOf("status" to "approved", "adminNotes" to adminNotes)).await()
        } catch (_: Exception) {}
    }

    suspend fun rejectPermissionRequest(
        orgId: String,
        requestId: String,
        adminId: String,
        adminName: String,
        adminNotes: String
    ) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("permission_requests").document(requestId)
                .update(mapOf("status" to "rejected", "adminNotes" to adminNotes)).await()
        } catch (_: Exception) {}
    }

    suspend fun createTask(
        orgId: String,
        title: String,
        description: String,
        deptId: String,
        priority: String,
        assignedMember: Member?,
        startDate: String,
        dueDate: String,
        estimatedHours: Double,
        creator: Member?,
        hasReminder: Boolean = false,
        reminderNotes: String = "",
        attachmentName: String = "",
        attachmentUrl: String = "",
        attachmentType: String = "",
        attachmentSizeBytes: Long = 0L
    ) {
        try {
            val ref = firestore.collection("organizations").document(orgId).collection("tasks").document()
            val task = TaskItem(
                id = ref.id,
                organizationId = orgId,
                title = title,
                description = description,
                departmentId = deptId,
                priority = priority,
                status = "todo",
                assignedToMemberId = assignedMember?.id ?: "",
                assignedToName = assignedMember?.displayName ?: "Unassigned",
                assignedToRole = assignedMember?.role ?: "",
                createdByMemberId = creator?.id ?: "",
                createdByName = creator?.displayName ?: "Admin",
                startDate = startDate,
                dueDate = dueDate,
                estimatedHours = estimatedHours,
                hasReminder = hasReminder,
                reminderNotes = reminderNotes,
                attachmentName = attachmentName,
                attachmentUrl = attachmentUrl,
                attachmentType = attachmentType,
                attachmentSizeBytes = attachmentSizeBytes
            )
            ref.set(task).await()
        } catch (_: Exception) {}
    }

    suspend fun saveTaskCompletionNotes(
        orgId: String,
        taskId: String,
        notes: String,
        completedAt: String,
        completedBy: String,
        markCompleted: Boolean = false
    ) {
        try {
            val updates = mutableMapOf<String, Any>(
                "completionNotes" to notes,
                "completedAt" to completedAt,
                "completedBy" to completedBy
            )
            if (markCompleted) {
                updates["status"] = "done"
                updates["progress"] = 100
            }
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .update(updates).await()
        } catch (_: Exception) {}
    }

    suspend fun updateTaskStatus(
        orgId: String,
        taskId: String,
        newStatus: String,
        progress: Int,
        actualHours: Double,
        blockerReason: String,
        completionNotes: String = "",
        completedAt: String = "",
        completedBy: String = ""
    ) {
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "progress" to progress,
                "actualHours" to actualHours,
                "blockerReason" to blockerReason
            )
            if (completionNotes.isNotBlank()) updates["completionNotes"] = completionNotes
            if (completedAt.isNotBlank()) updates["completedAt"] = completedAt
            if (completedBy.isNotBlank()) updates["completedBy"] = completedBy

            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .update(updates).await()
        } catch (_: Exception) {}
    }

    suspend fun setTaskReminder(
        orgId: String,
        taskId: String,
        hasReminder: Boolean,
        reminderNotes: String
    ) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "hasReminder" to hasReminder,
                        "reminderNotes" to reminderNotes
                    )
                ).await()
        } catch (_: Exception) {}
    }

    suspend fun deleteTask(orgId: String, taskId: String) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .delete().await()
        } catch (_: Exception) {}
    }

    suspend fun updateTaskPriority(orgId: String, taskId: String, priority: String) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .update("priority", priority.lowercase()).await()
        } catch (_: Exception) {}
    }

    suspend fun updateOrganizationGstin(orgId: String, newGstin: String) {
        try {
            firestore.collection("organizations").document(orgId)
                .update("gstin", newGstin.uppercase()).await()
        } catch (_: Exception) {}
    }

    suspend fun updateOrganizationLogoUrl(orgId: String, logoUrl: String) {
        try {
            firestore.collection("organizations").document(orgId)
                .set(mapOf("logoUrl" to logoUrl), com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (_: Exception) {}
    }

    suspend fun uploadTaskWorkDocument(
        orgId: String,
        taskId: String,
        docName: String,
        docUrl: String,
        docType: String,
        uploaderName: String,
        uploaderRole: String,
        remarks: String = ""
    ) {
        try {
            val nowFormatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val updates = mutableMapOf<String, Any>(
                "workDocName" to docName,
                "workDocUrl" to docUrl,
                "workDocType" to docType,
                "workDocUploadedAt" to nowFormatted,
                "workDocUploadedBy" to uploaderName
            )
            if (remarks.isNotBlank()) {
                updates["completionNotes"] = remarks
            }
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (_: Exception) {}
    }

    suspend fun updateTaskAttachment(
        orgId: String,
        taskId: String,
        attachmentName: String,
        attachmentUrl: String,
        attachmentType: String,
        attachmentSizeBytes: Long
    ) {
        try {
            val updates = mapOf<String, Any>(
                "attachmentName" to attachmentName,
                "attachmentUrl" to attachmentUrl,
                "attachmentType" to attachmentType,
                "attachmentSizeBytes" to attachmentSizeBytes
            )
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (_: Exception) {}
    }

    fun observeGroups(orgId: String): Flow<List<TeamGroup>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val groups = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(TeamGroup::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) {
                        val d = doc.data ?: return@mapNotNull null
                        @Suppress("UNCHECKED_CAST")
                        TeamGroup(
                            id = doc.id,
                            organizationId = d["organizationId"] as? String ?: orgId,
                            name = d["name"] as? String ?: "",
                            description = d["description"] as? String ?: "",
                            managerIds = (d["managerIds"] as? List<String>) ?: emptyList(),
                            managerNames = (d["managerNames"] as? List<String>) ?: emptyList(),
                            staffIds = (d["staffIds"] as? List<String>) ?: emptyList(),
                            staffNames = (d["staffNames"] as? List<String>) ?: emptyList(),
                            createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    }
                }
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createGroup(orgId: String, group: TeamGroup) {
        try {
            val docRef = if (group.id.isNotBlank()) {
                firestore.collection("organizations").document(orgId).collection("groups").document(group.id)
            } else {
                firestore.collection("organizations").document(orgId).collection("groups").document()
            }
            val finalGroup = if (group.id.isBlank()) group.copy(id = docRef.id, organizationId = orgId) else group
            docRef.set(finalGroup).await()
        } catch (_: Exception) {}
    }

    fun observeGroupMessages(orgId: String, groupId: String): Flow<List<GroupChatMessage>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("groups").document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val msgs = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(GroupChatMessage::class.java)?.let {
                            if (it.id.isBlank()) it.copy(id = doc.id) else it
                        }
                    } catch (_: Exception) {
                        val d = doc.data ?: return@mapNotNull null
                        GroupChatMessage(
                            id = doc.id,
                            groupId = d["groupId"] as? String ?: groupId,
                            senderId = d["senderId"] as? String ?: "",
                            senderName = d["senderName"] as? String ?: "",
                            senderRole = d["senderRole"] as? String ?: "",
                            message = d["message"] as? String ?: "",
                            timestamp = (d["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                    }
                }
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendGroupMessage(orgId: String, groupId: String, message: GroupChatMessage) {
        try {
            val docRef = firestore.collection("organizations").document(orgId)
                .collection("groups").document(groupId)
                .collection("messages").document()
            val finalMsg = message.copy(id = docRef.id, groupId = groupId)
            docRef.set(finalMsg).await()
        } catch (_: Exception) {}
    }
}
