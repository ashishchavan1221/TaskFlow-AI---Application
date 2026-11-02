import SwiftUI

/// Create Task Deliverable Modal matching CreateTaskModal in MainActivityKt.kt.
public struct CreateTaskModal: View {
    @ObservedObject var viewModel: MainAppViewModel
    public var onDismiss: () -> Void

    @State private var title: String = ""
    @State private var description: String = ""
    @State private var priority: String = "medium"
    @State private var departmentId: String = "ops"
    @State private var selectedMemberIndex: Int = 0
    @State private var estimatedHours: Double = 4.0
    @State private var dueDate: Date = Date().addingTimeInterval(86400 * 3)
    @State private var hasReminder: Bool = false
    @State private var reminderNotes: String = ""

    private let priorities = ["urgent", "high", "medium", "low"]
    private let departments = [("ops", "Operations"), ("eng", "Engineering"), ("fin", "Finance & GST"), ("sales", "Sales")]

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // Title
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Deliverable Title *")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            TextField("e.g. Audit GSTR-1 Invoices & File e-Way Bills", text: $title)
                                .padding(10)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        // Description
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Operational Scope & Instructions")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)
                            TextEditor(text: $description)
                                .frame(height: 70)
                                .padding(4)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                                .foregroundColor(Color.textPrimary)
                        }

                        // Priority Selector
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Priority Level")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            HStack(spacing: 8) {
                                ForEach(priorities, id: \.self) { prio in
                                    let isSelected = priority == prio
                                    Button(action: { priority = prio }) {
                                        Text(prio.capitalized)
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(isSelected ? .white : Color.textSecondary)
                                            .frame(maxWidth: .infinity)
                                            .padding(.vertical, 8)
                                            .background(isSelected ? priorityColor(prio) : Color.darkSurfaceElevated)
                                            .cornerRadius(8)
                                    }
                                }
                            }
                        }

                        // Department
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Department")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Picker("Department", selection: $departmentId) {
                                ForEach(departments, id: \.0) { dept in
                                    Text(dept.1).tag(dept.0)
                                }
                            }
                            .pickerStyle(SegmentedPickerStyle())
                        }

                        // Assignee Picker
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Assign Personnel")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            if !viewModel.members.isEmpty {
                                Picker("Assignee", selection: $selectedMemberIndex) {
                                    ForEach(0..<viewModel.members.count, id: \.self) { idx in
                                        let mem = viewModel.members[idx]
                                        Text("\(mem.displayName) (\(mem.role.capitalized))").tag(idx)
                                    }
                                }
                                .pickerStyle(MenuPickerStyle())
                                .padding(6)
                                .background(Color.darkSurfaceElevated)
                                .cornerRadius(8)
                            } else {
                                Text("No members available in organization.")
                                    .font(.system(size: 12))
                                    .foregroundColor(Color.textSecondary)
                            }
                        }

                        // Estimated Hours & Due Date
                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Estimated Hours: \(String(format: "%.1f", estimatedHours))h")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                Slider(value: $estimatedHours, in: 1...40, step: 0.5)
                                    .accentColor(Color.actionBlue)
                            }

                            VStack(alignment: .leading, spacing: 6) {
                                Text("Deadline Date")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                DatePicker("", selection: $dueDate, displayedComponents: .date)
                                    .labelsHidden()
                                    .colorScheme(.dark)
                            }
                        }

                        // Audible Reminder Switch
                        Toggle(isOn: $hasReminder) {
                            Label("Enable Audible Reminders", systemImage: "bell.badge.fill")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color.textPrimary)
                        }
                        .toggleStyle(SwitchToggleStyle(tint: Color.actionBlue))

                        // Submit Button
                        Button(action: createDeliverable) {
                            Text("Create & Delegate Deliverable")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(title.isEmpty ? Color.gray : Color.actionBlue)
                                .cornerRadius(12)
                        }
                        .disabled(title.isEmpty)
                        .padding(.top, 10)
                    }
                    .padding(16)
                }
            }
            .navigationTitle("New Task Deliverable")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { onDismiss() }
                        .foregroundColor(Color.textSecondary)
                }
            }
        }
    }

    private func priorityColor(_ prio: String) -> Color {
        switch prio {
        case "urgent": return Color.errorRed
        case "high": return Color.warningAmber
        case "medium": return Color.actionBlue
        default: return Color.successGreen
        }
    }

    private func createDeliverable() {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let dueStr = formatter.string(from: dueDate)
        let startStr = formatter.string(from: Date())

        let assignedMem = viewModel.members.indices.contains(selectedMemberIndex) ? viewModel.members[selectedMemberIndex] : nil

        viewModel.createTask(
            title: title,
            description: description,
            departmentId: departmentId,
            priority: priority,
            assignedMember: assignedMem,
            startDate: startStr,
            dueDate: dueStr,
            estimatedHours: estimatedHours,
            hasReminder: hasReminder,
            reminderNotes: reminderNotes.isEmpty ? "Approaching task deadline" : reminderNotes
        )
        onDismiss()
    }
}
