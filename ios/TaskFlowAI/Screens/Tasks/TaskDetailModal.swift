import SwiftUI

/// Detailed Task Breakdown Modal with live stopwatch time tracking, completion audit card, and document proofs.
/// Matches TaskDetailModal in MainActivityKt.kt 1:1.
public struct TaskDetailModal: View {
    public var task: TaskItem
    @ObservedObject var viewModel: MainAppViewModel
    public var onDismiss: () -> Void

    @State private var notesText: String = ""

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                ScrollView {
                    VStack(alignment: .leading, spacing: 18) {
                        // Title & Status Badge
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text(task.priority.uppercased())
                                    .font(.system(size: 10, weight: .black))
                                    .foregroundColor(task.priorityColor)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(task.priorityColor.opacity(0.15))
                                    .cornerRadius(6)

                                Spacer()

                                Text(task.statusDisplay)
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(task.isCompleted ? Color.successGreen : Color.actionBlue)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background((task.isCompleted ? Color.successGreen : Color.actionBlue).opacity(0.15))
                                    .cornerRadius(6)
                            }

                            Text(task.title)
                                .font(.system(size: 18, weight: .heavy))
                                .foregroundColor(Color.textPrimary)

                            Text(task.description)
                                .font(.system(size: 13))
                                .foregroundColor(Color.textSecondary)
                        }
                        .padding(16)
                        .enterpriseCard(isDark: true)

                        // Personnel & Timeline Row
                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 4) {
                                Label("Assignee", systemImage: "person.fill")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(Color.textSecondary)
                                Text(task.assignedToName)
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(8)

                            VStack(alignment: .leading, spacing: 4) {
                                Label("Deadline", systemImage: "calendar")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(Color.textSecondary)
                                Text(task.dueDate)
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(8)
                        }

                        // Dynamic Time Tracker & Stopwatch
                        StopwatchTimerView(
                            currentActualHours: task.actualHours,
                            estimatedHours: task.estimatedHours,
                            onLogHours: { addH in
                                viewModel.logTaskTime(taskId: task.id, additionalHours: addH)
                            }
                        )

                        // Persistent Completion Audit Card (if completed)
                        if task.isCompleted || !task.completionNotes.isEmpty {
                            VStack(alignment: .leading, spacing: 6) {
                                HStack(spacing: 6) {
                                    Image(systemName: "checkmark.seal.fill")
                                        .foregroundColor(Color.successGreen)
                                    Text("Statutory Completion & Verification Record")
                                        .font(.system(size: 12, weight: .bold))
                                        .foregroundColor(Color.successGreen)
                                }

                                Text("\"\(task.completionNotes)\"")
                                    .font(.system(size: 13, weight: .medium))
                                    .foregroundColor(Color.textPrimary)
                                    .padding(.vertical, 2)

                                HStack {
                                    Text("Logged by: \(task.completedBy.isEmpty ? task.assignedToName : task.completedBy)")
                                        .font(.system(size: 11))
                                        .foregroundColor(Color.textSecondary)
                                    Spacer()
                                    Text(task.completedAt.isEmpty ? "Completed" : task.completedAt)
                                        .font(.system(size: 11, weight: .semibold))
                                        .foregroundColor(Color.textSecondary)
                                }
                            }
                            .padding(14)
                            .background(Color.successGreen.opacity(0.12))
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.successGreen.opacity(0.3), lineWidth: 1)
                            )
                        }

                        // Attachments & Work Proof Section
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Documents & Verification Proofs")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            HStack(spacing: 12) {
                                Button(action: {
                                    viewModel.openDocumentViewer(task: task, isWorkDoc: false)
                                }) {
                                    Label("View Brief", systemImage: "doc.text.fill")
                                        .font(.system(size: 12, weight: .semibold))
                                        .foregroundColor(Color.actionBlue)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 10)
                                        .background(Color.actionBlue.opacity(0.15))
                                        .cornerRadius(8)
                                }

                                Button(action: {
                                    viewModel.openUploadDocument(task: task)
                                }) {
                                    Label("Upload Proof", systemImage: "arrow.up.doc.fill")
                                        .font(.system(size: 12, weight: .semibold))
                                        .foregroundColor(Color.accentCyan)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 10)
                                        .background(Color.accentCyan.opacity(0.15))
                                        .cornerRadius(8)
                                }
                            }
                        }
                        .padding(16)
                        .enterpriseCard(isDark: true)

                        // Audible Reminder Trigger Button
                        Button(action: {
                            viewModel.triggerTaskReminder(task: task)
                        }) {
                            HStack {
                                Image(systemName: "bell.badge.fill")
                                    .foregroundColor(Color.warningAmber)
                                Text("Trigger Audible Due Date Reminder")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(Color.textPrimary)
                                Spacer()
                                Image(systemName: "chevron.right")
                                    .foregroundColor(Color.textSecondary)
                            }
                            .padding(14)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(10)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("Deliverable Detail")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { onDismiss() }
                        .foregroundColor(Color.actionBlue)
                }
            }
        }
    }
}
