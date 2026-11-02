import SwiftUI

/// Main Enterprise Dashboard Scaffold matching MainActivityKt.kt.
/// Incorporates side drawer navigation, top bar with role switcher, floating action button,
/// 5-destination bottom navigation bar, and active role view dispatcher.
public struct MainDashboardView: View {
    @ObservedObject var viewModel: MainAppViewModel
    public var onLockApp: () -> Void

    @State private var isDrawerOpen: Bool = false

    public var body: some View {
        ZStack {
            Color.darkBackground.edgesIgnoringSafeArea(.all)

            // Main Content Area
            VStack(spacing: 0) {
                // Top Application Bar
                topAppBar

                // Main Dynamic Content
                ZStack {
                    switch viewModel.selectedTab {
                    case 0:
                        // Role-based Dashboard Dispatch
                        switch viewModel.activeRole.lowercased() {
                        case "admin":
                            OwnerDashboardView(viewModel: viewModel)
                        case "manager":
                            ManagerDashboardView(viewModel: viewModel)
                        default:
                            StaffDashboardView(viewModel: viewModel)
                        }
                    case 1:
                        TaskPipelineView(viewModel: viewModel)
                    case 2:
                        TeamDirectoryView(viewModel: viewModel)
                    case 3:
                        SettingsView(viewModel: viewModel)
                    case 4:
                        OperationalCalendarView(viewModel: viewModel)
                    case 5:
                        AiAssistantView(viewModel: viewModel)
                    case 6:
                        ActivityAuditView(viewModel: viewModel)
                    default:
                        OwnerDashboardView(viewModel: viewModel)
                    }

                    // Floating Action Button (+ Task)
                    if viewModel.activeRole != "staff" && (viewModel.selectedTab == 0 || viewModel.selectedTab == 1) {
                        VStack {
                            Spacer()
                            HStack {
                                Spacer()
                                Button(action: { viewModel.showCreateTaskDialog = true }) {
                                    HStack(spacing: 6) {
                                        Image(systemName: "plus")
                                            .font(.system(size: 16, weight: .bold))
                                        Text("New Task")
                                            .font(.system(size: 13, weight: .bold))
                                    }
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                                    .background(Color.actionBlue)
                                    .cornerRadius(24)
                                    .shadow(color: Color.actionBlue.opacity(0.4), radius: 8, x: 0, y: 4)
                                }
                                .padding(.trailing, 20)
                                .padding(.bottom, 16)
                            }
                        }
                    }
                }

                // Bottom 5-Destination Navigation Bar
                bottomNavigationBar
            }

            // Side Drawer Overlay
            if isDrawerOpen {
                Color.black.opacity(0.5)
                    .edgesIgnoringSafeArea(.all)
                    .onTapGesture {
                        withAnimation { isDrawerOpen = false }
                    }

                HStack(spacing: 0) {
                    sideDrawerContent
                        .frame(width: UIScreen.main.bounds.width * 0.78)
                        .background(Color.darkSurface)
                        .edgesIgnoringSafeArea(.all)
                        .transition(.move(edge: .leading))

                    Spacer()
                }
            }
        }
        // Attached Sheets & Overlays
        .sheet(isPresented: $viewModel.showCreateTaskDialog) {
            CreateTaskModal(viewModel: viewModel, onDismiss: { viewModel.showCreateTaskDialog = false })
        }
        .sheet(isPresented: $viewModel.showAddGroupDialog) {
            CreateWorkgroupModal(viewModel: viewModel, onDismiss: { viewModel.showAddGroupDialog = false })
        }
        .sheet(isPresented: $viewModel.showGroupChatDialog) {
            if let grp = viewModel.selectedGroup {
                GroupChatModal(group: grp, viewModel: viewModel, onDismiss: { viewModel.selectGroup(nil) })
            }
        }
        .sheet(isPresented: $viewModel.showTaskDetailModal) {
            if let task = viewModel.selectedTaskForDetail {
                TaskDetailModal(task: task, viewModel: viewModel, onDismiss: { viewModel.selectTaskForDetail(nil) })
            }
        }
        .sheet(isPresented: $viewModel.showNotificationDialog) {
            NotificationCenterModal(
                notifications: viewModel.notifications,
                onMarkAllAsRead: {
                    for i in 0..<viewModel.notifications.count { viewModel.notifications[i].isRead = true }
                },
                onDismiss: { viewModel.showNotificationDialog = false }
            )
        }
        .sheet(item: $viewModel.viewingDocumentTask) { task in
            DocumentPreviewModal(task: task, isWorkDoc: viewModel.viewingDocumentIsWorkDoc, onDismiss: { viewModel.closeDocumentViewer() })
        }
        .sheet(item: $viewModel.uploadingDocumentTask) { task in
            UploadWorkDocumentModal(task: task, onUpload: { name, url, type, remarks in
                viewModel.uploadTaskWorkDocument(taskId: task.id, docName: name, docUrl: url, docType: type, remarks: remarks)
            }, onDismiss: { viewModel.closeUploadDocument() })
        }
        .overlay(
            Group {
                if viewModel.showCompletionModal, let taskId = viewModel.completingTaskId {
                    TaskCompletionModal(
                        taskTitle: viewModel.completingTaskTitle ?? "Deliverable",
                        onConfirm: { notes, dateStr in
                            viewModel.completeTaskWithNotes(taskId: taskId, notes: notes, dateStr: dateStr)
                        },
                        onDismiss: { viewModel.closeTaskCompletionModal() }
                    )
                }
            }
        )
    }

    // MARK: - Top App Bar
    private var topAppBar: some View {
        HStack(spacing: 12) {
            Button(action: { withAnimation { isDrawerOpen = true } }) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color.textPrimary)
                    .frame(width: 36, height: 36)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(8)
            }

            TaskFlowBrandLogo(size: 32, showGlow: false)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Text("TaskFlow")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color.textPrimary)
                    Text("AI")
                        .font(.system(size: 9, weight: .black))
                        .foregroundColor(.white)
                        .padding(.horizontal, 5)
                        .padding(.vertical, 1)
                        .background(Color.primaryGradient)
                        .cornerRadius(4)
                }
                Text(viewModel.currentOrg?.name ?? "TaskFlow Workspace")
                    .font(.system(size: 10))
                    .foregroundColor(Color.textSecondary)
            }

            Spacer()

            // Interactive Role Switcher Pill Badge
            Button(action: cycleRole) {
                HStack(spacing: 5) {
                    Circle()
                        .fill(Color.actionBlue)
                        .frame(width: 6, height: 6)
                    Text(viewModel.activeRole.uppercased())
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color.actionBlue)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.darkSurfaceElevated)
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.actionBlue.opacity(0.4), lineWidth: 1)
                )
            }

            // Notification Bell
            Button(action: { viewModel.showNotificationDialog = true }) {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell.fill")
                        .font(.system(size: 16))
                        .foregroundColor(Color.textPrimary)
                        .frame(width: 36, height: 36)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(8)

                    let unread = viewModel.notifications.count { !$0.isRead }
                    if unread > 0 {
                        Text("\(unread)")
                            .font(.system(size: 8, weight: .black))
                            .foregroundColor(.white)
                            .frame(width: 14, height: 14)
                            .background(Color.errorRed)
                            .clipShape(Circle())
                            .offset(x: 2, y: -2)
                    }
                }
            }

            // Theme Toggle
            Button(action: { viewModel.toggleDarkTheme(!viewModel.isDarkTheme) }) {
                Image(systemName: viewModel.isDarkTheme ? "sun.max.fill" : "moon.stars.fill")
                    .font(.system(size: 15))
                    .foregroundColor(viewModel.isDarkTheme ? Color.accentAmber : Color.actionBlue)
                    .frame(width: 36, height: 36)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(8)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color.darkSurface)
    }

    private func cycleRole() {
        let next: String
        switch viewModel.activeRole.lowercased() {
        case "admin": next = "manager"
        case "manager": next = "staff"
        default: next = "admin"
        }
        viewModel.setActiveRole(next)
        AudioChimePlayer.shared.playNotificationSound()
    }

    // MARK: - Side Drawer Content
    private var sideDrawerContent: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header Profile
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    TaskFlowBrandLogo(size: 44, showGlow: true)
                    Spacer()
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.2))
                            .frame(width: 36, height: 36)
                        Text(viewModel.activeRole.prefix(2).uppercased())
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
                Text(viewModel.currentMember?.displayName ?? (viewModel.activeRole == "admin" ? "Organization Owner" : "Workspace Member"))
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                HStack(spacing: 6) {
                    Text(viewModel.currentOrg?.name ?? "TaskFlow Workspace")
                        .font(.system(size: 11))
                        .foregroundColor(.white.opacity(0.8))
                    Text("ID: \(viewModel.currentMember?.employeeId ?? "OWNER-001")")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 5)
                        .padding(.vertical, 2)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(4)
                }
            }
            .padding(20)
            .padding(.top, 40)
            .background(Color.drawerHeaderGradient)

            ScrollView {
                VStack(spacing: 4) {
                    drawerItem(title: "Executive Dashboard", icon: "square.grid.2x2.fill", tab: 0)
                    drawerItem(title: "Task Pipeline", icon: "checklist", tab: 1)
                    drawerItem(title: "Team & Workgroups", icon: "person.3.fill", tab: 2)
                    drawerItem(title: "Operational Calendar", icon: "calendar", tab: 4)
                    drawerItem(title: "AI Assistant", icon: "sparkles", tab: 5)
                    drawerItem(title: "Activity & Audit", icon: "clock.arrow.circlepath", tab: 6)
                    drawerItem(title: "Settings & Security", icon: "gearshape.fill", tab: 3)

                    Divider().background(Color.darkBorder).padding(.vertical, 8)

                    Button(action: {
                        isDrawerOpen = false
                        onLockApp()
                    }) {
                        HStack(spacing: 12) {
                            Image(systemName: "lock.fill")
                                .foregroundColor(Color.errorRed)
                                .frame(width: 24)
                            Text("Lock Session (PIN)")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(Color.errorRed)
                            Spacer()
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }
                }
                .padding(.vertical, 10)
            }
        }
    }

    private func drawerItem(title: String, icon: String, tab: Int) -> some View {
        Button(action: {
            viewModel.setSelectedTab(tab)
            withAnimation { isDrawerOpen = false }
        }) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 15))
                    .foregroundColor(viewModel.selectedTab == tab ? Color.actionBlue : Color.textSecondary)
                    .frame(width: 24)
                Text(title)
                    .font(.system(size: 13, weight: viewModel.selectedTab == tab ? .bold : .medium))
                    .foregroundColor(viewModel.selectedTab == tab ? Color.textPrimary : Color.textSecondary)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(viewModel.selectedTab == tab ? Color.actionBlue.opacity(0.15) : Color.clear)
            .cornerRadius(8)
        }
    }

    // MARK: - Bottom Navigation Bar (5 Primary Destinations)
    private var bottomNavigationBar: some View {
        HStack {
            navButton(title: "Home", icon: "house.fill", tab: 0)
            navButton(title: "Tasks", icon: "checklist", tab: 1)

            // AI Chatbot highlighted tab
            Button(action: { viewModel.setSelectedTab(5) }) {
                VStack(spacing: 3) {
                    ZStack {
                        Circle()
                            .fill(Color.primaryGradient)
                            .frame(width: 32, height: 32)
                        Image(systemName: "sparkles")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                    }
                    Text("AI Chat")
                        .font(.system(size: 10, weight: .heavy))
                        .foregroundColor(viewModel.selectedTab == 5 ? Color.actionBlue : Color.textSecondary)
                }
                .frame(maxWidth: .infinity)
            }

            navButton(title: "Team", icon: "person.3.fill", tab: 2)
            navButton(title: "Settings", icon: "gearshape.fill", tab: 3)
        }
        .padding(.vertical, 8)
        .padding(.bottom, 4)
        .background(Color.darkSurface)
        .overlay(
            Rectangle()
                .frame(height: 1)
                .foregroundColor(Color.darkBorder),
            alignment: .top
        )
    }

    private func navButton(title: String, icon: String, tab: Int) -> some View {
        Button(action: { viewModel.setSelectedTab(tab) }) {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                Text(title)
                    .font(.system(size: 10, weight: viewModel.selectedTab == tab ? .bold : .medium))
            }
            .foregroundColor(viewModel.selectedTab == tab ? Color.actionBlue : Color.textSecondary)
            .frame(maxWidth: .infinity)
        }
    }
}
