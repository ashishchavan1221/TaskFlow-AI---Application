import SwiftUI

/// Owner / Administrator Dashboard matching OwnerDashboard.kt.
/// Full enterprise visibility into company pipelines, manager approvals, and member rosters.
public struct OwnerDashboardView: View {
    @ObservedObject var viewModel: MainAppViewModel

    private var totalTasks: Int { viewModel.tasks.count }
    private var completedTasks: Int { viewModel.tasks.count { $0.isCompleted } }
    private var healthRate: Int {
        totalTasks > 0 ? (completedTasks * 100) / totalTasks : 100
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Enterprise Banner Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("EXECUTIVE HEADQUARTERS")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.accentCyan)
                                .tracking(1)

                            Text(viewModel.currentOrg?.name ?? "TaskFlow Enterprise")
                                .font(.system(size: 20, weight: .heavy))
                                .foregroundColor(Color.textPrimary)

                            if let gstin = viewModel.currentOrg?.gstin, !gstin.isEmpty {
                                Text("GSTIN Mandate: \(gstin)")
                                    .font(.system(size: 11, weight: .semibold, design: .monospaced))
                                    .foregroundColor(Color.textSecondary)
                            }
                        }

                        Spacer()

                        VStack(spacing: 4) {
                            Text("\(healthRate)%")
                                .font(.system(size: 22, weight: .black))
                                .foregroundColor(Color.successGreen)
                            Text("HEALTH SCORE")
                                .font(.system(size: 8, weight: .bold))
                                .foregroundColor(Color.textSecondary)
                        }
                        .padding(10)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(12)
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // 4 Core Metrics
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    metricCard(title: "Deliverables", value: "\(totalTasks)", icon: "checklist", color: Color.actionBlue)
                    metricCard(title: "Personnel", value: "\(viewModel.members.count)", icon: "person.3.fill", color: Color.brandPurple)
                    metricCard(title: "Approvals", value: "\(viewModel.permissionRequests.count)", icon: "bell.badge.fill", color: Color.warningAmber)
                    metricCard(title: "Completed", value: "\(completedTasks)", icon: "checkmark.seal.fill", color: Color.successGreen)
                }

                // Pending Manager Approval Requests
                if !viewModel.permissionRequests.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(Color.warningAmber)
                            Text("Manager Permission Requests")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            Spacer()
                            Text("\(viewModel.permissionRequests.count) Pending")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.warningAmber)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color.warningAmber.opacity(0.15))
                                .cornerRadius(6)
                        }

                        ForEach(viewModel.permissionRequests) { req in
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text(req.actionType)
                                        .font(.system(size: 11, weight: .heavy))
                                        .foregroundColor(Color.actionBlue)
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color.actionBlue.opacity(0.15))
                                        .cornerRadius(4)

                                    Spacer()
                                    Text("From: \(req.managerName)")
                                        .font(.system(size: 11))
                                        .foregroundColor(Color.textSecondary)
                                }

                                Text(req.reason)
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(Color.textPrimary)

                                HStack(spacing: 12) {
                                    Button(action: { viewModel.approvePermissionRequest(id: req.id) }) {
                                        Text("Approve Request")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(.white)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(Color.successGreen)
                                            .cornerRadius(8)
                                    }

                                    Button(action: { viewModel.rejectPermissionRequest(id: req.id) }) {
                                        Text("Reject")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(Color.errorRed)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(Color.errorRed.opacity(0.15))
                                            .cornerRadius(8)
                                    }
                                }
                            }
                            .padding(12)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(10)
                        }
                    }
                    .padding(16)
                    .enterpriseCard(isDark: viewModel.isDarkTheme)
                }

                // Company-wide Deliverable Pipeline
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("Active Organizational Tasks")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Spacer()
                        Button("View All") {
                            viewModel.setSelectedTab(1)
                        }
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color.actionBlue)
                    }

                    ForEach(viewModel.tasks.prefix(5)) { task in
                        Button(action: { viewModel.selectTaskForDetail(task) }) {
                            HStack(spacing: 12) {
                                Circle()
                                    .fill(task.priorityColor)
                                    .frame(width: 8, height: 8)

                                VStack(alignment: .leading, spacing: 3) {
                                    Text(task.title)
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(Color.textPrimary)
                                    Text("Assigned to: \(task.assignedToName) • Due \(task.dueDate)")
                                        .font(.system(size: 11))
                                        .foregroundColor(Color.textSecondary)
                                }

                                Spacer()

                                Text(task.statusDisplay)
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(task.isCompleted ? Color.successGreen : Color.actionBlue)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background((task.isCompleted ? Color.successGreen : Color.actionBlue).opacity(0.15))
                                    .cornerRadius(6)
                            }
                            .padding(12)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(10)
                        }
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)
            }
            .padding(16)
        }
    }

    private func metricCard(title: String, value: String, icon: String, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(color)
                Spacer()
            }
            Text(value)
                .font(.system(size: 24, weight: .black))
                .foregroundColor(Color.textPrimary)
            Text(title)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(Color.textSecondary)
        }
        .padding(14)
        .enterpriseCard(isDark: viewModel.isDarkTheme)
    }
}
