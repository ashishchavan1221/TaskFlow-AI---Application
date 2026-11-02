import SwiftUI

/// Real-Time Workgroup Chat Modal matching GroupChatModal in MainActivityKt.kt.
public struct GroupChatModal: View {
    public var group: TeamGroup
    @ObservedObject var viewModel: MainAppViewModel
    public var onDismiss: () -> Void

    @State private var messageText: String = ""

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                VStack(spacing: 0) {
                    // Group Header Banner
                    HStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(Color.primaryGradient)
                                .frame(width: 40, height: 40)
                            Image(systemName: "bubble.left.and.bubble.right.fill")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text(group.name)
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Text("Leads: \(group.managerNames.joined(separator: ", ")) • \(group.staffNames.count) Staff")
                                .font(.system(size: 11))
                                .foregroundColor(Color.textSecondary)
                        }

                        Spacer()
                    }
                    .padding(14)
                    .background(Color.darkSurface)

                    Divider().background(Color.darkBorder)

                    // Message Stream
                    ScrollViewReader { proxy in
                        ScrollView {
                            LazyVStack(spacing: 12) {
                                if viewModel.activeGroupMessages.isEmpty {
                                    VStack(spacing: 10) {
                                        Image(systemName: "message.fill")
                                            .font(.system(size: 32))
                                            .foregroundColor(Color.textSecondary.opacity(0.3))
                                        Text("Workgroup Channel Initialized")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(Color.textPrimary)
                                        Text("Send a message to start collaborative communications with your team managers and associates.")
                                            .font(.system(size: 12))
                                            .foregroundColor(Color.textSecondary)
                                            .multilineTextAlignment(.center)
                                            .padding(.horizontal, 30)
                                    }
                                    .padding(.top, 40)
                                } else {
                                    ForEach(viewModel.activeGroupMessages) { msg in
                                        let isMe = msg.senderId == (viewModel.currentMember?.id ?? "user_me")
                                        chatBubble(msg: msg, isMe: isMe)
                                            .id(msg.id)
                                    }
                                }
                            }
                            .padding(14)
                        }
                        .onChange(of: viewModel.activeGroupMessages.count) { _ in
                            if let last = viewModel.activeGroupMessages.last {
                                withAnimation {
                                    proxy.scrollTo(last.id, anchor: .bottom)
                                }
                            }
                        }
                    }

                    // Input Bar
                    HStack(spacing: 10) {
                        TextField("Type team message...", text: $messageText)
                            .padding(10)
                            .background(Color.darkSurfaceElevated)
                            .cornerRadius(20)
                            .foregroundColor(Color.textPrimary)

                        Button(action: sendMessage) {
                            Image(systemName: "paperplane.fill")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 40, height: 40)
                                .background(Color.actionBlue)
                                .clipShape(Circle())
                        }
                        .disabled(messageText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                    .padding(12)
                    .background(Color.darkSurface)
                }
            }
            .navigationTitle("Workgroup Chat")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") { onDismiss() }
                        .foregroundColor(Color.actionBlue)
                }
            }
        }
    }

    private func chatBubble(msg: GroupChatMessage, isMe: Bool) -> some View {
        HStack(alignment: .bottom, spacing: 8) {
            if !isMe {
                Circle()
                    .fill(Color.actionBlue.opacity(0.3))
                    .frame(width: 28, height: 28)
                    .overlay(
                        Text(msg.senderName.prefix(2).uppercased())
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(Color.actionBlue)
                    )
            } else {
                Spacer()
            }

            VStack(alignment: isMe ? .trailing : .leading, spacing: 3) {
                if !isMe {
                    HStack(spacing: 6) {
                        Text(msg.senderName)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color.textSecondary)

                        Text(msg.senderRole.uppercased())
                            .font(.system(size: 8, weight: .heavy))
                            .foregroundColor(Color.brandPurple)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(Color.brandPurple.opacity(0.15))
                            .cornerRadius(3)
                    }
                }

                Text(msg.message)
                    .font(.system(size: 13))
                    .foregroundColor(.white)
                    .padding(10)
                    .background(isMe ? Color.actionBlue : Color.darkSurfaceElevated)
                    .cornerRadius(14)
            }

            if isMe {
                Circle()
                    .fill(Color.brandPurple.opacity(0.3))
                    .frame(width: 28, height: 28)
                    .overlay(
                        Text("ME")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(Color.brandPurple)
                    )
            } else {
                Spacer()
            }
        }
    }

    private func sendMessage() {
        let text = messageText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        viewModel.sendGroupChatMessage(groupId: group.id, text: text)
        messageText = ""
    }
}
