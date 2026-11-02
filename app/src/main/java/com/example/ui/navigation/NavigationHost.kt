package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.MainAppDashboard
import com.example.security.SecureSessionManager
import com.example.ui.MainAppViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.PinEntryScreen
import com.example.ui.theme.TaskFlowTheme

/**
 * Route definitions for type-safe and robust session navigation.
 */
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object SecurityUnlock : Screen("security_unlock")
    object Dashboard : Screen("dashboard")
}

/**
 * Robust NavigationHost implementation that handles user sessions within a single-activity architecture:
 * - Unauthenticated users are strictly routed to the Login/Registration Auth screen.
 * - Authenticated users are directed to the Enterprise Dashboard upon successful unlock.
 * - Startup security checks leverage BiometricPrompt for app unlocking with enforced 4-digit PIN fallback.
 */
@Composable
fun AppNavigationHost(
    viewModel: MainAppViewModel,
    secureSessionManager: SecureSessionManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val sessionState by secureSessionManager.sessionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Determine initial startup route
    val initialRoute = remember {
        when {
            !sessionState.isAuthenticated -> Screen.Auth.route
            sessionState.isLocked -> Screen.SecurityUnlock.route
            else -> Screen.Dashboard.route
        }
    }

    // Reactively guide navigation based on session security state
    LaunchedEffect(sessionState.isAuthenticated, sessionState.isLocked) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (!sessionState.isAuthenticated) {
            // User signed out or unauthenticated -> route to Auth screen
            if (currentRoute != Screen.Auth.route) {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (sessionState.isLocked) {
            // User authenticated but session is locked (startup or manual lock) -> route to SecurityUnlock
            if (currentRoute != Screen.SecurityUnlock.route) {
                navController.navigate(Screen.SecurityUnlock.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else {
            // User authenticated and unlocked -> route to Dashboard
            if (currentRoute == Screen.Auth.route || currentRoute == Screen.SecurityUnlock.route || currentRoute == null) {
                navController.navigate(Screen.Dashboard.route) {
                    val popTarget = currentRoute ?: Screen.Auth.route
                    popUpTo(popTarget) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    TaskFlowTheme(darkTheme = uiState.isDarkTheme) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = initialRoute,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Unified Authentication Screen (Login / Registration)
                composable(Screen.Auth.route) {
                    BackHandler {
                        activity?.finish()
                    }

                    AuthScreen(
                        viewModel = viewModel,
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage
                    )
                }

                // 2. Biometric & 4-Digit PIN Security Unlock Gate
                composable(Screen.SecurityUnlock.route) {
                    BackHandler {
                        // Prevent bypassing lock screen via back gesture
                        activity?.finish()
                    }

                    PinEntryScreen(
                        onPinVerified = {
                            secureSessionManager.unlockSession()
                        },
                        onTriggerBiometric = {
                            if (activity != null) {
                                secureSessionManager.triggerStartupSecurityCheck(activity) { success ->
                                    // Callback updates sessionState reactively
                                }
                            }
                        },
                        isBiometricAvailable = sessionState.isBiometricAvailable,
                        onVerify = { secureSessionManager.verifyPin(it) },
                        securityNotice = sessionState.securityNotice,
                        onSignOut = {
                            viewModel.signOut(context)
                        }
                    )
                }

                // 3. Enterprise Dashboard with Single-Activity Architecture
                composable(Screen.Dashboard.route) {
                    MainAppDashboard(
                        viewModel = viewModel,
                        uiState = uiState,
                        onLockApp = {
                            secureSessionManager.lockSession()
                        }
                    )
                }
            }
        }
    }
}
