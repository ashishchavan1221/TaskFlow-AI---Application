package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskFlowRepository(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    fun observeOrganization(orgId: String): Flow<Organization?> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val org = snapshot?.toObject(Organization::class.java)
                trySend(org)
            }
        awaitClose { listener.remove() }
    }

    fun observeMembers(orgId: String): Flow<List<Member>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { it.toObject(Member::class.java) } ?: emptyList()
                trySend(members)
            }
        awaitClose { listener.remove() }
    }

    fun observeTasks(orgId: String): Flow<List<TaskItem>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { it.toObject(TaskItem::class.java) } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    fun observeDepartments(orgId: String): Flow<List<Department>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("departments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val depts = snapshot?.documents?.mapNotNull { it.toObject(Department::class.java) } ?: emptyList()
                trySend(depts)
            }
        awaitClose { listener.remove() }
    }

    fun observeWorkflows(orgId: String): Flow<List<Workflow>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("workflows")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val workflows = snapshot?.documents?.mapNotNull { it.toObject(Workflow::class.java) } ?: emptyList()
                trySend(workflows)
            }
        awaitClose { listener.remove() }
    }

    fun observePermissionRequests(orgId: String): Flow<List<PermissionRequest>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("permission_requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reqs = snapshot?.documents?.mapNotNull { it.toObject(PermissionRequest::class.java) } ?: emptyList()
                trySend(reqs)
            }
        awaitClose { listener.remove() }
    }

    fun observeAuditLogs(orgId: String): Flow<List<AuditLog>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("audit_logs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { it.toObject(AuditLog::class.java) } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    fun observeAiInsights(orgId: String): Flow<List<AiInsight>> = callbackFlow {
        val listener = firestore.collection("organizations").document(orgId)
            .collection("ai_insights")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val insights = snapshot?.documents?.mapNotNull { it.toObject(AiInsight::class.java) } ?: emptyList()
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
        creator: Member?
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
                estimatedHours = estimatedHours
            )
            ref.set(task).await()
        } catch (_: Exception) {}
    }

    suspend fun updateTaskStatus(
        orgId: String,
        taskId: String,
        newStatus: String,
        progress: Int,
        actualHours: Double,
        blockerReason: String
    ) {
        try {
            firestore.collection("organizations").document(orgId)
                .collection("tasks").document(taskId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "progress" to progress,
                        "actualHours" to actualHours,
                        "blockerReason" to blockerReason
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
}
