import SwiftUI

/// Gemini AI Assistant Chat Screen matching AiScreen.kt.
/// Offers operational assistance, deliverable breakdown, and one-click task creation.
public struct AiAssistantView: View {
    @ObservedObject var viewModel: MainAppViewModel

    @State private var inputPrompt: String = ""
    @State private var messages: [(isUser: Bool, text: String)] = [
        (isUser: false, text: "Hello! I am your TaskFlow AI Operational Assistant. I can analyze team workloads, decompose complex deliverables into subtasks, or summarize enterprise project status.")
    ]
    @State private var isThinking: Bool = false

    private let suggestions = [
        "Break down GST audit into 3 subtasks",
        "Identify priority bottlenecks in operations",
        "Generate executive briefing of active pipeline",
        "Draft operational checklist for deployment"
    ]

    public var body: some View {
        VStack(spacing: 0) {
            // Header Prompt Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(suggestions, id: \.self) { suggestion in
                        Button(action: {
                            inputPrompt = suggestion
                            sendQuery()
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: "sparkles")
                                    .font(.system(size: 10))
                                Text(suggestion)
                                    .font(.system(size: 11, weight: .semibold))
                            }
                            .foregroundColor(Color.accentCyan)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.darkBorder, lineWidth: 1)
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }
            .background(Color.darkSurface)

            Divider().background(Color.darkBorder)

            // Message Stream
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 14) {
                        ForEach(0..<messages.count, id: \.self) { idx in
                            let msg = messages[idx]
                            aiChatBubble(isUser: msg.isUser, text: msg.text)
                                .id(idx)
                        }

                        if isThinking {
                            HStack(spacing: 8) {
                                ProgressView()
                                    .scaleEffect(0.8)
                                    .progressViewStyle(CircularProgressViewStyle(tint: Color.actionBlue))
                                Text("TaskFlow AI is analyzing operational data...")
                                    .font(.system(size: 12))
                                    .foregroundColor(Color.textSecondary)
                                Spacer()
                            }
                            .padding(12)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(12)
                        }
                    }
                    .padding(16)
                }
                .onChange(of: messages.count) { count in
                    withAnimation {
                        proxy.scrollTo(count - 1, anchor: .bottom)
                    }
                }
            }

            // Input Bar
            HStack(spacing: 10) {
                TextField("Ask TaskFlow AI operational question...", text: $inputPrompt)
                    .padding(10)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(20)
                    .foregroundColor(Color.textPrimary)

                Button(action: sendQuery) {
                    ZStack {
                        Circle()
                            .fill(Color.primaryGradient)
                            .frame(width: 40, height: 40)
                        Image(systemName: "arrow.up")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                .disabled(inputPrompt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || isThinking)
            }
            .padding(12)
            .background(Color.darkSurface)
        }
        .background(Color.darkBackground.edgesIgnoringSafeArea(.all))
    }

    private func aiChatBubble(isUser: Bool, text: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            if !isUser {
                ZStack {
                    Circle()
                        .fill(Color.primaryGradient)
                        .frame(width: 32, height: 32)
                    Image(systemName: "sparkles")
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                }
            } else {
                Spacer()
            }

            VStack(alignment: isUser ? .trailing : .leading, spacing: 6) {
                Text(text)
                    .font(.system(size: 13))
                    .foregroundColor(.white)
                    .padding(12)
                    .background(isUser ? Color.actionBlue : Color.darkSurfaceElevated)
                    .cornerRadius(14)

                // One-click "Add to Tasks" action if AI provided a recommendation
                if !isUser && text.contains("subtask") || text.contains("1.") {
                    Button(action: {
                        viewModel.createTask(
                            title: "AI Action Item: Operational Deliverable",
                            description: text,
                            departmentId: "ops",
                            priority: "high",
                            assignedMember: viewModel.members.first,
                            startDate: "Today",
                            dueDate: "In 3 Days",
                            estimatedHours: 4.0,
                            hasReminder: true,
                            reminderNotes: "Generated by TaskFlow AI Assistant"
                        )
                        AudioChimePlayer.shared.playTaskCompletionSound()
                    }) {
                        Label("Add to Tasks Pipeline", systemImage: "plus.circle.fill")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color.accentCyan)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(Color.accentCyan.opacity(0.15))
                            .cornerRadius(8)
                    }
                }
            }

            if isUser {
                ZStack {
                    Circle()
                        .fill(Color.brandPurple.opacity(0.3))
                        .frame(width: 32, height: 32)
                    Text("ME")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color.brandPurple)
                }
            } else {
                Spacer()
            }
        }
    }

    private func sendQuery() {
        let query = inputPrompt.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return }
        messages.append((isUser: true, text: query))
        inputPrompt = ""
        isThinking = true

        Task {
            let answer = await viewModel.queryAiAssist(question: query)
            messages.append((isUser: false, text: answer))
            isThinking = false
            AudioChimePlayer.shared.playNotificationSound()
        }
    }
}
