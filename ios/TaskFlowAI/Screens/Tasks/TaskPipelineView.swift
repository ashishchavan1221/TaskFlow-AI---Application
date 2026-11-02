import SwiftUI

/// Task Pipeline & Kanban Dashboard matching TaskDashboard.kt.
public struct TaskPipelineView: View {
    @ObservedObject var viewModel: MainAppViewModel

    @State private var selectedStatus: String = "all"
    @State private var selectedPriority: String = "all"
    @State private var searchQuery: String = ""

    private let statusOptions = [
        ("all", "All"),
        ("todo", "To-Do"),
        ("in_progress", "In Progress"),
        ("done", "Done"),
        ("blocked", "Blocked")
    ]

    private let priorityOptions = [
        ("all", "All"),
        ("urgent", "Urgent"),
        ("high", "High"),
        ("medium", "Medium"),
        ("low", "Low")
    ]

    private var filteredTasks: [TaskItem] {
        viewModel.tasks.filter { task in
            let matchesStatus = (selectedStatus == "all" || task.status.lowercased() == selectedStatus)
            let matchesPriority = (selectedPriority == "all" || task.priority.lowercased() == selectedPriority)
            let matchesSearch = searchQuery.isEmpty ||
                task.title.localizedCaseInsensitiveContains(searchQuery) ||
                task.description.localizedCaseInsensitiveContains(searchQuery) ||
                task.assignedToName.localizedCaseInsensitiveContains(searchQuery)
            return matchesStatus && matchesPriority && matchesSearch
        }
    }

    public var body: some View {
        VStack(spacing: 0) {
            // Search Bar & Filter Controls
            VStack(spacing: 10) {
                // Search Field
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(Color.textSecondary)
                    TextField("Search tasks, assignees, or keywords...", text: $searchQuery)
                        .font(.system(size: 13))
                        .foregroundColor(Color.textPrimary)
                    if !searchQuery.isEmpty {
                        Button(action: { searchQuery = "" }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(Color.textSecondary)
                        }
                    }
                }
                .padding(10)
                .background(Color.darkSurfaceElevated)
                .cornerRadius(10)

                // Status Horizontal Filter
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(statusOptions, id: \.0) { item in
                            let isSelected = selectedStatus == item.0
                            Button(action: { selectedStatus = item.0 }) {
                                Text(item.1)
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(isSelected ? .white : Color.textSecondary)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(isSelected ? Color.actionBlue : Color.darkSurfaceElevated)
                                    .cornerRadius(8)
                            }
                        }
                    }
                }

                // Priority Horizontal Filter
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(priorityOptions, id: \.0) { item in
                            let isSelected = selectedPriority == item.0
                            Button(action: { selectedPriority = item.0 }) {
                                Text(item.1)
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(isSelected ? .white : Color.textSecondary)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(isSelected ? Color.brandPurple : Color.darkSurfaceElevated.opacity(0.6))
                                    .cornerRadius(6)
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.darkSurface)

            // Tasks List
            ScrollView {
                LazyVStack(spacing: 12) {
                    if filteredTasks.isEmpty {
                        VStack(spacing: 10) {
                            Image(systemName: "tray.fill")
                                .font(.system(size: 36))
                                .foregroundColor(Color.textSecondary.opacity(0.4))
                            Text("No tasks match criteria")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            Text("Try adjusting your filters or search keywords.")
                                .font(.system(size: 12))
                                .foregroundColor(Color.textSecondary)
                        }
                        .padding(.top, 60)
                    } else {
                        ForEach(filteredTasks) { task in
                            taskCard(task: task)
                        }
                    }
                }
                .padding(16)
            }
        }
        .background(Color.darkBackground.edgesIgnoringSafeArea(.all))
    }

    private func taskCard(task: TaskItem) -> some View {
        Button(action: { viewModel.selectTaskForDetail(task) }) {
            VStack(alignment: .leading, spacing: 10) {
                // Top Meta: Priority & Status
                HStack {
                    HStack(spacing: 4) {
                        Circle()
                            .fill(task.priorityColor)
                            .frame(width: 6, height: 6)
                        Text(task.priority.uppercased())
                            .font(.system(size: 9, weight: .black))
                            .foregroundColor(task.priorityColor)
                    }
                    .padding(.horizontal, 6)
                    .padding(.vertical, 3)
                    .background(task.priorityColor.opacity(0.15))
                    .cornerRadius(4)

                    if task.hasReminder {
                        Image(systemName: "bell.fill")
                            .font(.system(size: 11))
                            .foregroundColor(Color.warningAmber)
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

                // Title & Description
                Text(task.title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(Color.textPrimary)
                    .multilineTextAlignment(.leading)

                if !task.description.isEmpty {
                    Text(task.description)
                        .font(.system(size: 12))
                        .foregroundColor(Color.textSecondary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }

                // Progress Bar
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(Color.darkBorder)
                            .frame(height: 6)

                        RoundedRectangle(cornerRadius: 3)
                            .fill(task.isCompleted ? Color.successGreen : Color.actionBlue)
                            .frame(width: max(0, min(geo.size.width, geo.size.width * CGFloat(task.progress) / 100)), height: 6)
                    }
                }
                .frame(height: 6)

                // Bottom Row: Assignee, Hours, Due Date
                HStack {
                    Label(task.assignedToName, systemImage: "person.circle.fill")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(Color.textSecondary)

                    Spacer()

                    if task.actualHours > 0 {
                        Text("\(String(format: "%.1f", task.actualHours))h spent")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(Color.accentCyan)
                    }

                    HStack(spacing: 3) {
                        Image(systemName: "calendar")
                            .font(.system(size: 10))
                        Text(task.dueDate)
                            .font(.system(size: 10, weight: .semibold))
                    }
                    .foregroundColor(Color.textMuted)
                }
            }
            .padding(14)
            .enterpriseCard(isDark: viewModel.isDarkTheme)
        }
    }
}
