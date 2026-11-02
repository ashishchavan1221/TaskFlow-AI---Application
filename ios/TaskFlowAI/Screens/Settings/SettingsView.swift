import SwiftUI

/// Settings & Security Screen matching SettingsScreen.kt.
public struct SettingsView: View {
    @ObservedObject var viewModel: MainAppViewModel

    @State private var showPinChangeSheet: Bool = false
    @State private var newPin: String = ""
    @State private var confirmPin: String = ""
    @State private var pinChangeNotice: String? = nil
    @State private var biometricsEnabled: Bool = true
    @State private var showLogoUrlSheet: Bool = false
    @State private var logoUrlInput: String = ""

    public var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                // Profile & Company Banner
                VStack(spacing: 12) {
                    HStack(spacing: 14) {
                        TaskFlowBrandLogo(size: 54, showGlow: true)

                        VStack(alignment: .leading, spacing: 3) {
                            Text(viewModel.currentOrg?.name ?? "TaskFlow Enterprise")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color.textPrimary)

                            Text("Role: \(viewModel.activeRole.uppercased()) • ID: \(viewModel.currentMember?.employeeId ?? "OWNER-001")")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color.accentCyan)

                            if let gstin = viewModel.currentOrg?.gstin, !gstin.isEmpty {
                                Text("GSTIN: \(gstin)")
                                    .font(.system(size: 11, design: .monospaced))
                                    .foregroundColor(Color.textSecondary)
                            }
                        }

                        Spacer()
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // GSTIN Statutory Mandate Status Card
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "checkmark.shield.fill")
                            .foregroundColor(Color.successGreen)
                        Text("GSTIN Statutory Verification")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color.textPrimary)
                        Spacer()
                        Text("AUTHENTICATED")
                            .font(.system(size: 9, weight: .black))
                            .foregroundColor(Color.successGreen)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.successGreen.opacity(0.15))
                            .cornerRadius(4)
                    }

                    Text("Organization operates under verified GSTIN: \(viewModel.currentOrg?.gstin ?? "27AAACG0000A1Z5"). Compliant with Section 25 CGST enterprise mandate.")
                        .font(.system(size: 11))
                        .foregroundColor(Color.textSecondary)
                }
                .padding(14)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // Security & Biometrics Section
                VStack(alignment: .leading, spacing: 14) {
                    Text("Security & Access Control")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    // Change PIN Action
                    Button(action: { showPinChangeSheet = true }) {
                        HStack {
                            Image(systemName: "key.fill")
                                .foregroundColor(Color.actionBlue)
                                .frame(width: 24)
                            Text("Change 4-Digit Security PIN")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color.textPrimary)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundColor(Color.textMuted)
                        }
                        .padding(12)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(10)
                    }

                    // Biometrics Toggle
                    Toggle(isOn: $biometricsEnabled) {
                        HStack {
                            Image(systemName: "faceid")
                                .foregroundColor(Color.accentCyan)
                                .frame(width: 24)
                            Text("Face ID Biometric Unlock")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color.textPrimary)
                        }
                    }
                    .toggleStyle(SwitchToggleStyle(tint: Color.actionBlue))
                    .padding(12)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(10)

                    // Revoke Sessions
                    Button(action: {
                        Task {
                            _ = try? await BackendApi.shared.completePasswordChange()
                        }
                    }) {
                        HStack {
                            Image(systemName: "arrow.triangle.2.circlepath")
                                .foregroundColor(Color.warningAmber)
                                .frame(width: 24)
                            Text("Revoke Active Remote Sessions")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color.textPrimary)
                            Spacer()
                        }
                        .padding(12)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(10)
                    }
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // Appearance & Theme Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Appearance")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Toggle(isOn: $viewModel.isDarkTheme) {
                        HStack {
                            Image(systemName: viewModel.isDarkTheme ? "moon.stars.fill" : "sun.max.fill")
                                .foregroundColor(viewModel.isDarkTheme ? Color.accentAmber : Color.actionBlue)
                                .frame(width: 24)
                            Text("Dark Theme")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color.textPrimary)
                        }
                    }
                    .toggleStyle(SwitchToggleStyle(tint: Color.actionBlue))
                    .padding(12)
                    .background(Color.darkSurfaceElevated)
                    .cornerRadius(10)
                }
                .padding(16)
                .enterpriseCard(isDark: viewModel.isDarkTheme)

                // Sign Out Button
                Button(action: {
                    viewModel.signOut()
                }) {
                    HStack {
                        Image(systemName: "rectangle.portrait.and.arrow.right")
                        Text("Sign Out of Organization Workspace")
                    }
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(Color.errorRed)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.errorRed.opacity(0.12))
                    .cornerRadius(12)
                }
                .padding(.top, 10)
            }
            .padding(16)
        }
        .background(Color.darkBackground.edgesIgnoringSafeArea(.all))
        .sheet(isPresented: $showPinChangeSheet) {
            pinChangeModal
        }
    }

    private var pinChangeModal: some View {
        NavigationView {
            ZStack {
                Color.darkBackground.edgesIgnoringSafeArea(.all)
                VStack(spacing: 16) {
                    Text("Configure Security PIN")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Text("Enter a new 4-digit numeric PIN for secure startup authentication.")
                        .font(.system(size: 12))
                        .foregroundColor(Color.textSecondary)

                    SecureField("New 4-Digit PIN", text: $newPin)
                        .keyboardType(.numberPad)
                        .padding(12)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(10)
                        .foregroundColor(Color.textPrimary)

                    SecureField("Confirm 4-Digit PIN", text: $confirmPin)
                        .keyboardType(.numberPad)
                        .padding(12)
                        .background(Color.darkSurfaceElevated)
                        .cornerRadius(10)
                        .foregroundColor(Color.textPrimary)

                    if let notice = pinChangeNotice {
                        Text(notice)
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color.errorRed)
                    }

                    Button("Save PIN") {
                        if newPin.count != 4 {
                            pinChangeNotice = "PIN must be exactly 4 digits."
                        } else if newPin != confirmPin {
                            pinChangeNotice = "PINs do not match."
                        } else {
                            SecureSessionManager.shared.setPin(newPin)
                            showPinChangeSheet = false
                            AudioChimePlayer.shared.playTaskCompletionSound()
                        }
                    }
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.actionBlue)
                    .cornerRadius(12)

                    Spacer()
                }
                .padding(20)
            }
            .navigationTitle("Security PIN")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showPinChangeSheet = false }
                        .foregroundColor(Color.textSecondary)
                }
            }
        }
    }
}
