import SwiftUI

/// Staff Associate Dashboard matching StaffDashboard.kt.
/// Strictly isolates deliverables: only displays tasks assigned to the authenticated staff member.
public struct StaffDashboardView: View {
    @ObservedObject var viewModel: MainAppViewModel

    private var myAssignedTasks: [TaskItem] {
        guard let myMem = viewModel.currentMember else {
            return viewModel.tasks.filter { $0.assignedToRole.lowercased() == "staff" }
        }
        return viewModel.tasks.filter {
            $0.assignedToMemberId == myMem.id ||
            $0.assignedToName.lowercased() == myMem.displayName.lowercased() ||
            $0.assignedToMemberId == myMem.userId
        }
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Personal Staff Roster Card
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("STAFF WORKSPACE")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.accentCyan)
                                .tracking(1)

                            Text(viewModel.currentMember?.displayName ?? "Staff Associate")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            HStack(spacing: 8) {
                                Text("ID: \(viewModel.currentMember?.employeeId ?? "STF-2091")")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(Color.actionBlue)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(Color.actionBlue.opacity(0.15))
                                    .cornerRadius(4)

                                Text(viewModel.currentMember?.departmentName ?? "Operations")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color.textSecondary)
                            }
                        }

                        Spacer()

                        ZStack {
                            Circle()
                                .fill(Color.brandPurple.opacity(0.15))
                                .frame(width: 48, height: 48)

                            Image(systemName: "person.crop.circle.fill")
                                .font(.system(size: 26))
                                .foregroundColor(Color.brandPurple)
                        }
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // Deliverables List
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("My Assigned Deliverables")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)

                        Spacer()

                        Text("\(myAssignedTasks.count) Active")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color.textSecondary)
                    }

                    if myAssignedTasks.isEmpty {
                        VStack(spacing: 8) {
                            Image(systemName: "checkmark.seal")
                                .font(.system(size: 32))
                                .foregroundColor(Color.successGreen.opacity(0.6))
                            Text("No deliverables pending")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            Text("You currently have no tasks assigned to your employee queue.")
                                .font(.system(size: 12))
                                .foregroundColor(Color.textSecondary)
                        }
                        .padding(24)
                        .frame(maxWidth: .infinity)
                    } else {
                        ForEach(myAssignedTasks) { task in
                            VStack(alignment: .leading, spacing: 10) {
                                HStack(alignment: .top) {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(task.title)
                                            .font(.system(size: 15, weight: .bold))
                                            .foregroundColor(Color.textPrimary)

                                        Text(task.description)
                                            .font(.system(size: 12))
                                            .foregroundColor(Color.textSecondary)
                                            .lineLimit(2)
                                    }

                                    Spacer()

                                    Text(task.priority.uppercased())
                                        .font(.system(size: 9, weight: .black))
                                        .foregroundColor(task.priorityColor)
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(task.priorityColor.opacity(0.15))
                                        .cornerRadius(4)
                                }

                                // Quick Status Action Buttons
                                HStack(spacing: 8) {
                                    if task.status != "in_progress" && !task.isCompleted {
                                        Button(action: {
                                            viewModel.updateTaskStatus(id: task.id, status: "in_progress", progress: 50)
                                            AudioChimePlayer.shared.playNotificationSound()
                                        }) {
                                            Label("Start Work", systemImage: "play.fill")
                                                .font(.system(size: 11, weight: .bold))
                                                .foregroundColor(Color.actionBlue)
                                                .padding(.horizontal, 10)
                                                .padding(.vertical, 6)
                                                .background(Color.actionBlue.opacity(0.15))
                                                .cornerRadius(6)
                                        }
                                    }

                                    if !task.isCompleted {
                                        Button(action: {
                                            viewModel.openTaskCompletionModal(taskId: task.id, title: task.title)
                                        }) {
                                            Label("Complete", systemImage: "checkmark.circle.fill")
                                                .font(.system(size: 11, weight: .bold))
                                                .foregroundColor(.white)
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 6)
                                                .background(Color.successGreen)
                                                .cornerRadius(6)
                                        }
                                    } else {
                                        Label("Verified Complete", systemImage: "checkmark.seal.fill")
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(Color.successGreen)
                                    }

                                    Spacer()

                                    Button(action: {
                                        viewModel.selectTaskForDetail(task)
                                    }) {
                                        Image(systemName: "timer")
                                            .font(.system(size: 13, weight: .semibold))
                                            .foregroundColor(Color.accentCyan)
                                            .padding(6)
                                            .background(Color.accentCyan.opacity(0.15))
                                            .clipShape(Circle())
                                    }
                                }

                                // Persistent Completion Notes Display
                                if !task.completionNotes.isEmpty {
                                    HStack(spacing: 6) {
                                        Image(systemName: "note.text")
                                            .foregroundColor(Color.successGreen)
                                            .font(.system(size: 11))
                                        Text("Completed: \"\(task.completionNotes)\"")
                                            .font(.system(size: 11, weight: .medium))
                                            .foregroundColor(Color.successGreen)
                                            .lineLimit(1)
                                    }
                                    .padding(8)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color.successGreen.opacity(0.1))
                                    .cornerRadius(6)
                                }
                            }
                            .padding(14)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(12)
                        }
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)
            }
            .padding(16)
        }
    }
}
