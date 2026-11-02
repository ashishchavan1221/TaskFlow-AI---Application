import SwiftUI

/// Operational Calendar Screen matching CalendarScreen.kt.
/// Maps deliverables to deadlines with month and day agenda views.
public struct OperationalCalendarView: View {
    @ObservedObject var viewModel: MainAppViewModel

    @State private var selectedDate: Date = Date()

    private var tasksForSelectedDate: [TaskItem] {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let selectedStr = formatter.string(from: selectedDate)
        return viewModel.tasks.filter { $0.dueDate == selectedStr }
    }

    public var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Calendar Date Picker Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Label("Operational Calendar", systemImage: "calendar")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Spacer()
                    }

                    DatePicker("", selection: $selectedDate, displayedComponents: .date)
                        .datePickerStyle(GraphicalDatePickerStyle())
                        .accentColor(Color.actionBlue)
                        .colorScheme(.dark)
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // Selected Date Agenda
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        let formatter = DateFormatter()
                        let _ = formatter.dateStyle = .medium
                        Text("Agenda: \(formatter.string(from: selectedDate))")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Spacer()
                        Text("\(tasksForSelectedDate.count) Items")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color.textSecondary)
                    }

                    if tasksForSelectedDate.isEmpty {
                        VStack(spacing: 8) {
                            Image(systemName: "calendar.badge.clock")
                                .font(.system(size: 28))
                                .foregroundColor(Color.textSecondary.opacity(0.4))
                            Text("No deliverables due on this date")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                        }
                        .padding(24)
                        .frame(maxWidth: .infinity)
                    } else {
                        ForEach(tasksForSelectedDate) { task in
                            Button(action: { viewModel.selectTaskForDetail(task) }) {
                                HStack(spacing: 10) {
                                    Circle()
                                        .fill(task.priorityColor)
                                        .frame(width: 8, height: 8)

                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(task.title)
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(Color.textPrimary)
                                        Text("Assignee: \(task.assignedToName) • \(task.statusDisplay)")
                                            .font(.system(size: 11))
                                            .foregroundColor(Color.textSecondary)
                                    }

                                    Spacer()

                                    Image(systemName: "chevron.right")
                                        .foregroundColor(Color.textMuted)
                                        .font(.system(size: 12))
                                }
                                .padding(12)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(10)
                            }
                        }
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)
            }
            .padding(16)
        }
        .background(Color.darkBackground.edgesIgnoringSafeArea(.all))
    }
}
