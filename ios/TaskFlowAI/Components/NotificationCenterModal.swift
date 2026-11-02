import SwiftUI

/// In-App Notification Center modal.
/// Matches NotificationCenterModal.kt 1:1.
public struct NotificationCenterModal: View {
    public var notifications: [AppNotification]
    public var onMarkAllAsRead: () -> Void
    public var onDismiss: () -> Void

    public init(notifications: [AppNotification], onMarkAllAsRead: @escaping () -> Void, onDismiss: @escaping () -> Void) {
        self.notifications = notifications
        self.onMarkAllAsRead = onMarkAllAsRead
        self.onDismiss = onDismiss
    }

    public var body: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                if notifications.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bell.slash.fill")
                            .font(.system(size: 38))
                            .foregroundColor(Color.textSecondary.opacity(0.4))
                        Text("No notifications")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Text("You're all caught up with your enterprise tasks and operational alerts.")
                            .font(.system(size: 13))
                            .foregroundColor(Color.textSecondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 30)
                    }
                } else {
                    List {
                        ForEach(notifications) { notif in
                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    HStack(spacing: 6) {
                                        Circle()
                                            .fill(notif.isRead ? Color.textSecondary.opacity(0.3) : Color.errorRed)
                                            .frame(width: 8, height: 8)
                                        Text(notif.title)
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(Color.textPrimary)
                                    }
                                    Spacer()
                                    Text(notif.type.uppercased())
                                        .font(.system(size: 9, weight: .bold))
                                        .foregroundColor(Color.actionBlue)
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color.actionBlue.opacity(0.15))
                                        .cornerRadius(4)
                                }

                                Text(notif.message)
                                    .font(.system(size: 12))
                                    .foregroundColor(Color.textSecondary)

                                if !notif.authorName.isEmpty {
                                    Text("From: \(notif.authorName)")
                                        .font(.system(size: 10, weight: .semibold))
                                        .foregroundColor(Color.textMuted)
                                }
                            }
                            .padding(.vertical, 6)
                            .listRowBackground(Color.darkSurface)
                        }
                    }
                    .listStyle(PlainListStyle())
                }
            }
            .navigationTitle("Notifications")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") { onDismiss() }
                        .foregroundColor(Color.textSecondary)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    if !notifications.isEmpty {
                        Button("Mark Read") { onMarkAllAsRead() }
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(Color.actionBlue)
                    }
                }
            }
        }
    }
}
