import SwiftUI

/// Task Completion Modal capturing verification notes and completion timestamp.
/// Matches TaskCompletionModal in MainActivityKt.kt 1:1.
public struct TaskCompletionModal: View {
    public var taskTitle: String
    public var onConfirm: (_ notes: String, _ dateStr: String) -> Void
    public var onDismiss: () -> Void

    @State private var notes: String = ""
    @State private var completionDate: String = "Today, Just now"

    public var body: some View {
        ZStack {
            Color.black.opacity(0.6).edgesIgnoringSafeArea(.all)
                .onTapGesture { onDismiss() }

            VStack(alignment: .leading, spacing: 16) {
                // Header
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(Color.successGreen)
                        .font(.system(size: 20))

                    Text("Complete Deliverable")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Spacer()

                    Button(action: onDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(Color.textSecondary)
                    }
                }

                // Task Title Callout
                VStack(alignment: .leading, spacing: 4) {
                    Text("Deliverable")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color.textSecondary)
                    Text(taskTitle)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(Color.textPrimary)
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.darkSurfaceElevated)
                .cornerRadius(8)

                // Notes Field
                VStack(alignment: .leading, spacing: 4) {
                    Text("How did you complete this task? *")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(Color.textPrimary)

                    TextEditor(text: $notes)
                        .frame(height: 70)
                        .padding(4)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(8)
                        .foregroundColor(Color.textPrimary)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.darkBorder, lineWidth: 1)
                        )
                }

                // Timestamp Field
                VStack(alignment: .leading, spacing: 4) {
                    Text("When did you complete it?")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(Color.textPrimary)

                    TextField("Today, Just now", text: $completionDate)
                        .padding(10)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(8)
                        .foregroundColor(Color.textPrimary)
                }

                // Action Buttons
                HStack(spacing: 12) {
                    Button("Cancel") {
                        onDismiss()
                    }
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(Color.textSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(8)

                    Button("Confirm Completion") {
                        let finalNotes = notes.isEmpty ? "Task completed successfully with all operational verification checks passed." : notes
                        onConfirm(finalNotes, completionDate)
                    }
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.successGreen)
                    .cornerRadius(8)
                }
            }
            .padding(20)
            .background(Color.darkSurface)
            .cornerRadius(18)
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(Color.darkBorder, lineWidth: 1)
            )
            .padding(.horizontal, 24)
        }
    }
}
