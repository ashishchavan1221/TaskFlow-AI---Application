import SwiftUI

/// Department Manager Dashboard matching ManagerDashboard.kt.
/// Features department-scoped deliverables and the D3.js Priority Donut Chart.
public struct ManagerDashboardView: View {
    @ObservedObject var viewModel: MainAppViewModel

    private var departmentTasks: [TaskItem] {
        guard let myDept = viewModel.currentMember?.departmentId, !myDept.isEmpty else {
            return viewModel.tasks
        }
        return viewModel.tasks.filter { $0.departmentId == myDept || $0.departmentId == "ops" }
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header Card
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("DEPARTMENT OPERATIONS")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.accentCyan)
                                .tracking(1)

                            Text(viewModel.currentMember?.departmentName ?? "Operations & Logistics")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Text("Manager Lead: \(viewModel.currentMember?.displayName ?? "Department Lead") (\(viewModel.currentMember?.employeeId ?? "MGR-1042"))")
                                .font(.system(size: 12))
                                .foregroundColor(Color.textSecondary)
                        }

                        Spacer()

                        Button(action: { viewModel.showCreateTaskDialog = true }) {
                            Image(systemName: "plus")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(Color.actionBlue)
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // D3.js / Recharts Priority Donut Chart
                D3PriorityDonutChart(
                    tasks: departmentTasks,
                    isDarkTheme: viewModel.isDarkTheme,
                    onOpenCreateTask: { viewModel.showCreateTaskDialog = true }
                )

                // Department Deliverables List
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("Department Deliverables")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Spacer()
                        Text("\(departmentTasks.count) Items")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color.textSecondary)
                    }

                    ForEach(departmentTasks) { task in
                        Button(action: { viewModel.selectTaskForDetail(task) }) {
                            HStack(spacing: 12) {
                                Circle()
                                    .fill(task.priorityColor)
                                    .frame(width: 8, height: 8)

                                VStack(alignment: .leading, spacing: 3) {
                                    Text(task.title)
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(Color.textPrimary)
                                    Text("Assignee: \(task.assignedToName) • Due: \(task.dueDate)")
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
}
