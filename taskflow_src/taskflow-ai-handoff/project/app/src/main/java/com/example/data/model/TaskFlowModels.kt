package com.example.data.model

import com.google.firebase.firestore.DocumentId

data class Organization(
    @DocumentId val id: String = "",
    val name: String = "",
    val slug: String = "",
    val gstin: String = "",
    val industry: String = "",
    val companySize: String = "",
    val timezone: String = "",
    val description: String = ""
)

data class Member(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val employeeId: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "staff", // "admin", "manager", "staff"
    val departmentId: String = "",
    val departmentName: String = "",
    val phone: String = "",
    val status: String = "active", // "active", "suspended"
    val managerId: String = "" // Added to support manager-staff hierarchy
)

data class TaskItem(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val title: String = "",
    val description: String = "",
    val departmentId: String = "",
    val priority: String = "medium", // "low", "medium", "high", "urgent"
    val status: String = "todo", // "todo", "in_progress", "done", "blocked"
    val assignedToMemberId: String = "",
    val assignedToName: String = "",
    val assignedToRole: String = "",
    val createdByMemberId: String = "",
    val createdByName: String = "",
    val progress: Int = 0,
    val estimatedHours: Double = 0.0,
    val actualHours: Double = 0.0,
    val blockerReason: String = "",
    val startDate: String = "",
    val dueDate: String = ""
)

data class Department(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val leadMemberId: String = "",
    val leadMemberName: String = "",
    val memberCount: Int = 0
)

data class Workflow(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val departmentId: String = "",
    val stepsJson: String = ""
)

data class PermissionRequest(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val managerId: String = "",
    val managerName: String = "",
    val actionType: String = "", // "CREATE_STAFF", "UPDATE_STAFF", "DELETE_STAFF"
    val targetStaffId: String = "",
    val targetStaffName: String = "",
    val payloadJson: String = "",
    val reason: String = "",
    val adminNotes: String = "",
    val status: String = "pending" // "pending", "approved", "rejected"
)

data class AuditLog(
    @DocumentId val id: String = "",
    val organizationId: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val action: String = "",
    val timestamp: String = ""
)

data class AiInsight(
    @DocumentId val id: String = "",
    val title: String = "",
    val summary: String = "",
    val category: String = "",
    val impactLevel: String = "",
    val recommendation: String = ""
)
