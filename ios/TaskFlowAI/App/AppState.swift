import SwiftUI
import Combine

/// Root Session and Navigation Router matching NavigationHost.kt.
public class AppState: ObservableObject {
    public static let shared = AppState()

    @Published public var currentScreen: AppScreen = .auth

    public enum AppScreen {
        case auth
        case securityUnlock
        case dashboard
    }
}
