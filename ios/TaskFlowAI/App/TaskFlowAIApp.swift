import SwiftUI
import FirebaseCore

@main
struct TaskFlowAIApp: App {
    @StateObject private var viewModel = MainAppViewModel.shared
    @StateObject private var secureSessionManager = SecureSessionManager.shared

    init() {
        // Configure Firebase with GoogleService-Info.plist
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }

        // Check if prior session exists
        if AuthenticationManager.shared.isSessionActive {
            SecureSessionManager.shared.setAuthenticated(true)
            SecureSessionManager.shared.lockSession() // Enforce startup Face ID / PIN check
            if let saved = AuthenticationManager.shared.savedUser {
                MainAppViewModel.shared.setActiveRole(saved.role)
                MainAppViewModel.shared.currentOrg = Organization(id: saved.orgId, name: saved.orgName)
                MainAppViewModel.shared.initializeListeners(orgId: saved.orgId)
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)

                if !secureSessionManager.state.isAuthenticated {
                    AuthView(viewModel: viewModel)
                        .transition(.opacity)
                } else if secureSessionManager.state.isLocked {
                    PinEntryView(
                        secureSessionManager: secureSessionManager,
                        onSignOut: {
                            viewModel.signOut()
                        }
                    )
                    .transition(.opacity)
                } else {
                    MainDashboardView(
                        viewModel: viewModel,
                        onLockApp: {
                            secureSessionManager.lockSession()
                        }
                    )
                    .transition(.opacity)
                }
            }
            .preferredColorScheme(viewModel.isDarkTheme ? .dark : .light)
            .animation(.easeInOut(duration: 0.25), value: secureSessionManager.state.isAuthenticated)
            .animation(.easeInOut(duration: 0.25), value: secureSessionManager.state.isLocked)
        }
    }
}
