import SwiftUI

/// Security Unlock Gate matching PinEntryScreen.kt.
/// Enforces Biometric Authentication (Face ID / Touch ID) with 4-digit PIN fallback.
public struct PinEntryView: View {
    @ObservedObject var secureSessionManager: SecureSessionManager
    public var onSignOut: () -> Void

    @State private var inputPin: String = ""

    public var body: some View {
        ZStack {
            Color.darkBackground.edgesIgnoringSafeArea(.all)

            VStack(spacing: 28) {
                Spacer()

                // Header & Icon
                VStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.actionBlue.opacity(0.15))
                            .frame(width: 76, height: 76)

                        Image(systemName: "lock.shield.fill")
                            .font(.system(size: 34))
                            .foregroundColor(Color.actionBlue)
                    }

                    Text("TaskFlow Security Gate")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(Color.textPrimary)

                    Text("Enter your 4-digit PIN or authenticate with Face ID to unlock your enterprise workspace.")
                        .font(.system(size: 13))
                        .foregroundColor(Color.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                }

                // PIN Dots
                HStack(spacing: 18) {
                    ForEach(0..<4, id: \.self) { index in
                        Circle()
                            .fill(index < inputPin.count ? Color.actionBlue : Color.darkSurfaceElevated)
                            .frame(width: 16, height: 16)
                            .overlay(
                                Circle()
                                    .stroke(index < inputPin.count ? Color.accentCyan : Color.darkBorder, lineWidth: 1.5)
                            )
                    }
                }
                .padding(.vertical, 8)

                // Security Notice Banner
                if let notice = secureSessionManager.state.securityNotice {
                    Text(notice)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color.errorRed)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                }

                // Biometric Trigger Button
                if secureSessionManager.state.isBiometricAvailable {
                    Button(action: {
                        secureSessionManager.triggerBiometricAuthentication { _ in }
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: "faceid")
                                .font(.system(size: 18))
                            Text("Unlock with Face ID")
                                .font(.system(size: 13, weight: .bold))
                        }
                        .foregroundColor(Color.accentCyan)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(Color.accentCyan.opacity(0.12))
                        .cornerRadius(20)
                    }
                }

                // Numeric Keypad
                VStack(spacing: 16) {
                    ForEach(0..<3) { row in
                        HStack(spacing: 24) {
                            ForEach(1..<4) { col in
                                let num = row * 3 + col
                                keypadButton(label: "\(num)") {
                                    appendPin("\(num)")
                                }
                            }
                        }
                    }
                    HStack(spacing: 24) {
                        Button(action: {
                            secureSessionManager.triggerBiometricAuthentication { _ in }
                        }) {
                            Image(systemName: "faceid")
                                .font(.system(size: 22))
                                .foregroundColor(Color.actionBlue)
                                .frame(width: 72, height: 72)
                                .background(Color.darkSurfaceElevated.opacity(0.6))
                                .clipShape(Circle())
                        }

                        keypadButton(label: "0") {
                            appendPin("0")
                        }

                        Button(action: deletePin) {
                            Image(systemName: "delete.left.fill")
                                .font(.system(size: 20))
                                .foregroundColor(Color.textSecondary)
                                .frame(width: 72, height: 72)
                                .background(Color.darkSurfaceElevated.opacity(0.6))
                                .clipShape(Circle())
                        }
                    }
                }

                Spacer()

                // Sign Out Link
                Button(action: onSignOut) {
                    Text("Sign Out of Session")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(Color.errorRed)
                }
                .padding(.bottom, 24)
            }
        }
        .onAppear {
            if secureSessionManager.state.isBiometricAvailable && !secureSessionManager.state.isLockedOut {
                secureSessionManager.triggerBiometricAuthentication { _ in }
            }
        }
    }

    private func keypadButton(label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(Color.textPrimary)
                .frame(width: 72, height: 72)
                .background(Color.darkSurfaceElevated)
                .clipShape(Circle())
                .overlay(
                    Circle()
                        .stroke(Color.darkBorder, lineWidth: 1)
                )
        }
        .disabled(secureSessionManager.state.isLockedOut)
    }

    private func appendPin(_ digit: String) {
        guard inputPin.count < 4 else { return }
        inputPin.append(digit)
        if inputPin.count == 4 {
            let success = secureSessionManager.verifyPin(inputPin)
            if !success {
                inputPin = ""
                AudioChimePlayer.shared.playUrgentAlertSound()
            }
        }
    }

    private func deletePin() {
        if !inputPin.isEmpty {
            inputPin.removeLast()
        }
    }
}
