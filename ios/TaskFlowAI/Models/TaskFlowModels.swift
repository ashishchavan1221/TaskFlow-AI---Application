import Foundation
import SwiftUI

// MARK: - Organization Model
public struct Organization: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var name: String = ""
    public var slug: String = ""
    public var gstin: String = ""
    public var industry: String = ""
    public var companySize: String = ""
    public var timezone: String = ""
    public var description: String = ""
    public var logoUrl: String = ""

    public init(
        id: String = "",
        name: String = "",
        slug: String = "",
        gstin: String = "",
        industry: String = "",
        companySize: String = "",
        timezone: String = "",
        description: String = "",
        logoUrl: String = ""
    ) {
        self.id = id
        self.name = name
        self.slug = slug
        self.gstin = gstin
        self.industry = industry
        self.companySize = companySize
        self.timezone = timezone
        self.description = description
        self.logoUrl = logoUrl
    }
}

// MARK: - Member Model
public struct Member: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var userId: String = ""
    public var displayName: String = ""
    public var employeeId: String = ""
    public var email: String = ""
    public var password: String = ""
    public var role: String = "staff" // "admin", "manager", "staff"
    public var departmentId: String = ""
    public var departmentName: String = ""
    public var phone: String = ""
    public var status: String = "active" // "active", "suspended"
    public var managerId: String = ""
    public var securityAccessKey: String = ""

    public init(
        id: String = "",
        userId: String = "",
        displayName: String = "",
        employeeId: String = "",
        email: String = "",
        password: String = "",
        role: String = "staff",
        departmentId: String = "",
        departmentName: String = "",
        phone: String = "",
        status: String = "active",
        managerId: String = "",
        securityAccessKey: String = ""
    ) {
        self.id = id
        self.userId = userId
        self.displayName = displayName
        self.employeeId = employeeId
        self.email = email
        self.password = password
        self.role = role
        self.departmentId = departmentId
        self.departmentName = departmentName
        self.phone = phone
        self.status = status
        self.managerId = managerId
        self.securityAccessKey = securityAccessKey
    }

    public var isManager: Bool {
        role.lowercased() == "manager"
    }

    public var isAdmin: Bool {
        role.lowercased() == "admin"
    }

    public var isStaff: Bool {
        role.lowercased() == "staff"
    }
}

// MARK: - TaskItem Model
public struct TaskItem: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var organizationId: String = ""
    public var title: String = ""
    public var description: String = ""
    public var departmentId: String = ""
    public var priority: String = "medium" // "low", "medium", "high", "urgent"
    public var status: String = "todo" // "todo", "in_progress", "done", "blocked"
    public var assignedToMemberId: String = ""
    public var assignedToName: String = ""
    public var assignedToRole: String = ""
    public var createdByMemberId: String = ""
    public var createdByName: String = ""
    public var progress: Int = 0
    public var estimatedHours: Double = 0.0
    public var actualHours: Double = 0.0
    public var blockerReason: String = ""
    public var startDate: String = ""
    public var dueDate: String = ""
    public var completionNotes: String = ""
    public var completedAt: String = ""
    public var completedBy: String = ""
    public var hasReminder: Bool = false
    public var reminderNotes: String = ""
    public var attachmentName: String = ""
    public var attachmentUrl: String = ""
    public var attachmentType: String = "" // "pdf", "image", "document"
    public var attachmentSizeBytes: Int64 = 0
    public var workDocName: String = ""
    public var workDocUrl: String = ""
    public var workDocType: String = "" // "pdf", "image", "document"
    public var workDocUploadedAt: String = ""
    public var workDocUploadedBy: String = ""

    public init(
        id: String = "",
        organizationId: String = "",
        title: String = "",
        description: String = "",
        departmentId: String = "",
        priority: String = "medium",
        status: String = "todo",
        assignedToMemberId: String = "",
        assignedToName: String = "",
        assignedToRole: String = "",
        createdByMemberId: String = "",
        createdByName: String = "",
        progress: Int = 0,
        estimatedHours: Double = 0.0,
        actualHours: Double = 0.0,
        blockerReason: String = "",
        startDate: String = "",
        dueDate: String = "",
        completionNotes: String = "",
        completedAt: String = "",
        completedBy: String = "",
        hasReminder: Bool = false,
        reminderNotes: String = "",
        attachmentName: String = "",
        attachmentUrl: String = "",
        attachmentType: String = "",
        attachmentSizeBytes: Int64 = 0,
        workDocName: String = "",
        workDocUrl: String = "",
        workDocType: String = "",
        workDocUploadedAt: String = "",
        workDocUploadedBy: String = ""
    ) {
        self.id = id
        self.organizationId = organizationId
        self.title = title
        self.description = description
        self.departmentId = departmentId
        self.priority = priority
        self.status = status
        self.assignedToMemberId = assignedToMemberId
        self.assignedToName = assignedToName
        self.assignedToRole = assignedToRole
        self.createdByMemberId = createdByMemberId
        self.createdByName = createdByName
        self.progress = progress
        self.estimatedHours = estimatedHours
        self.actualHours = actualHours
        self.blockerReason = blockerReason
        self.startDate = startDate
        self.dueDate = dueDate
        self.completionNotes = completionNotes
        self.completedAt = completedAt
        self.completedBy = completedBy
        self.hasReminder = hasReminder
        self.reminderNotes = reminderNotes
        self.attachmentName = attachmentName
        self.attachmentUrl = attachmentUrl
        self.attachmentType = attachmentType
        self.attachmentSizeBytes = attachmentSizeBytes
        self.workDocName = workDocName
        self.workDocUrl = workDocUrl
        self.workDocType = workDocType
        self.workDocUploadedAt = workDocUploadedAt
        self.workDocUploadedBy = workDocUploadedBy
    }

    public var isCompleted: Bool {
        status.lowercased() == "done"
    }

    public var isUrgent: Bool {
        priority.lowercased() == "urgent" || priority.lowercased() == "critical"
    }

    public var isHighPriority: Bool {
        priority.lowercased() == "high"
    }

    public var priorityColor: Color {
        switch priority.lowercased() {
        case "urgent", "critical": return Color(red: 239/255, green: 68/255, blue: 68/255)
        case "high": return Color(red: 245/255, green: 158/255, blue: 11/255)
        case "medium": return Color(red: 59/255, green: 130/255, blue: 246/255)
        default: return Color(red: 16/255, green: 185/255, blue: 129/255)
        }
    }

    public var statusDisplay: String {
        switch status.lowercased() {
        case "todo": return "To-Do"
        case "in_progress": return "In Progress"
        case "done": return "Done"
        case "blocked": return "Blocked"
        default: return status.capitalized
        }
    }
}

// MARK: - Department Model
public struct Department: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var name: String = ""
    public var code: String = ""
    public var description: String = ""
    public var leadMemberId: String = ""
    public var leadMemberName: String = ""
    public var memberCount: Int = 0

    public init(
        id: String = "",
        name: String = "",
        code: String = "",
        description: String = "",
        leadMemberId: String = "",
        leadMemberName: String = "",
        memberCount: Int = 0
    ) {
        self.id = id
        self.name = name
        self.code = code
        self.description = description
        self.leadMemberId = leadMemberId
        self.leadMemberName = leadMemberName
        self.memberCount = memberCount
    }
}

// MARK: - Workflow Model
public struct Workflow: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var title: String = ""
    public var description: String = ""
    public var departmentId: String = ""
    public var stepsJson: String = ""

    public init(
        id: String = "",
        title: String = "",
        description: String = "",
        departmentId: String = "",
        stepsJson: String = ""
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.departmentId = departmentId
        self.stepsJson = stepsJson
    }
}

// MARK: - PermissionRequest Model
public struct PermissionRequest: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var organizationId: String = ""
    public var managerId: String = ""
    public var managerName: String = ""
    public var actionType: String = "" // "CREATE_STAFF", "UPDATE_STAFF", "DELETE_STAFF"
    public var targetStaffId: String = ""
    public var targetStaffName: String = ""
    public var payloadJson: String = ""
    public var reason: String = ""
    public var adminNotes: String = ""
    public var status: String = "pending" // "pending", "approved", "rejected"

    public init(
        id: String = "",
        organizationId: String = "",
        managerId: String = "",
        managerName: String = "",
        actionType: String = "",
        targetStaffId: String = "",
        targetStaffName: String = "",
        payloadJson: String = "",
        reason: String = "",
        adminNotes: String = "",
        status: String = "pending"
    ) {
        self.id = id
        self.organizationId = organizationId
        self.managerId = managerId
        self.managerName = managerName
        self.actionType = actionType
        self.targetStaffId = targetStaffId
        self.targetStaffName = targetStaffName
        self.payloadJson = payloadJson
        self.reason = reason
        self.adminNotes = adminNotes
        self.status = status
    }
}

// MARK: - AuditLog Model
public struct AuditLog: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var organizationId: String = ""
    public var actorName: String = ""
    public var actorRole: String = ""
    public var action: String = ""
    public var timestamp: String = ""

    public init(
        id: String = "",
        organizationId: String = "",
        actorName: String = "",
        actorRole: String = "",
        action: String = "",
        timestamp: String = ""
    ) {
        self.id = id
        self.organizationId = organizationId
        self.actorName = actorName
        self.actorRole = actorRole
        self.action = action
        self.timestamp = timestamp
    }
}

// MARK: - AiInsight Model
public struct AiInsight: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var title: String = ""
    public var summary: String = ""
    public var category: String = ""
    public var impactLevel: String = ""
    public var recommendation: String = ""

    public init(
        id: String = "",
        title: String = "",
        summary: String = "",
        category: String = "",
        impactLevel: String = "",
        recommendation: String = ""
    ) {
        self.id = id
        self.title = title
        self.summary = summary
        self.category = category
        self.impactLevel = impactLevel
        self.recommendation = recommendation
    }
}

// MARK: - AppNotification Model
public struct AppNotification: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var title: String = ""
    public var message: String = ""
    public var type: String = "task" // "task", "approval", "member", "system"
    public var timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    public var isRead: Bool = false
    public var authorName: String = ""
    public var priority: String = "medium"
    public var targetRole: String = "all"

    public init(
        id: String = "",
        title: String = "",
        message: String = "",
        type: String = "task",
        timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        isRead: Bool = false,
        authorName: String = "",
        priority: String = "medium",
        targetRole: String = "all"
    ) {
        self.id = id
        self.title = title
        self.message = message
        self.type = type
        self.timestamp = timestamp
        self.isRead = isRead
        self.authorName = authorName
        self.priority = priority
        self.targetRole = targetRole
    }
}

// MARK: - TeamGroup Model (Enforces max 2 managers)
public struct TeamGroup: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var organizationId: String = ""
    public var name: String = ""
    public var description: String = ""
    public var managerIds: [String] = []
    public var managerNames: [String] = []
    public var staffIds: [String] = []
    public var staffNames: [String] = []
    public var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)

    public init(
        id: String = "",
        organizationId: String = "",
        name: String = "",
        description: String = "",
        managerIds: [String] = [],
        managerNames: [String] = [],
        staffIds: [String] = [],
        staffNames: [String] = [],
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.organizationId = organizationId
        self.name = name
        self.description = description
        self.managerIds = managerIds
        self.managerNames = managerNames
        self.staffIds = staffIds
        self.staffNames = staffNames
        self.createdAt = createdAt
    }
}

// MARK: - GroupChatMessage Model
public struct GroupChatMessage: Codable, Identifiable, Equatable {
    public var id: String = ""
    public var groupId: String = ""
    public var senderId: String = ""
    public var senderName: String = ""
    public var senderRole: String = ""
    public var message: String = ""
    public var timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000)

    public init(
        id: String = "",
        groupId: String = "",
        senderId: String = "",
        senderName: String = "",
        senderRole: String = "",
        message: String = "",
        timestamp: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.groupId = groupId
        self.senderId = senderId
        self.senderName = senderName
        self.senderRole = senderRole
        self.message = message
        self.timestamp = timestamp
    }
}

// MARK: - AuthUserData
public struct AuthUserData: Codable, Equatable {
    public var uid: String
    public var email: String
    public var displayName: String
    public var role: String
    public var orgName: String
    public var employeeId: String = ""
    public var orgId: String = ""

    public init(
        uid: String,
        email: String,
        displayName: String,
        role: String,
        orgName: String,
        employeeId: String = "",
        orgId: String = ""
    ) {
        self.uid = uid
        self.email = email
        self.displayName = displayName
        self.role = role
        self.orgName = orgName
        self.employeeId = employeeId
        self.orgId = orgId
    }
}
