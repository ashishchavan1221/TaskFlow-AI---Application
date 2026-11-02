import Foundation
import LocalAuthentication
import CryptoKit
import Combine

public struct SessionSecurityState: Equatable {
    public var isAuthenticated: Bool = false
    public var isLocked: Bool = false
    public var isBiometricAvailable: Bool = false
    public var failedAttempts: Int = 0
    public var lockoutUntil: Date? = nil
    public var securityNotice: String? = nil

    public var isLockedOut: Bool {
        if let lockoutUntil = lockoutUntil {
            return Date() < lockoutUntil
        }
        return false
    }
}

/// Robust LocalAuthentication (Face ID / Touch ID) and 4-digit PIN session gatekeeper.
/// Matches com.example.security.SecureSessionManager.kt 1:1.
public class SecureSessionManager: ObservableObject {
    public static let shared = SecureSessionManager()

    @Published public var state: SessionSecurityState = SessionSecurityState()

    private let defaults = UserDefaults.standard
    private let kPinHash = "taskflow_secure_pin_hash"
    private let kPinSalt = "taskflow_secure_pin_salt"
    private let kBiometricsEnabled = "taskflow_biometrics_enabled"
    private let kFailedAttempts = "taskflow_failed_pin_attempts"
    private let kLockoutTime = "taskflow_lockout_timestamp"

    public init() {
        checkBiometricAvailability()
        loadPersistedSecurityState()
    }

    public func checkBiometricAvailability() {
        let context = LAContext()
        var error: NSError?
        let available = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        state.isBiometricAvailable = available
    }

    private func loadPersistedSecurityState() {
        let attempts = defaults.integer(forKey: kFailedAttempts)
        state.failedAttempts = attempts

        if let lockoutTimestamp = defaults.object(forKey: kLockoutTime) as? Date {
            if Date() < lockoutTimestamp {
                state.lockoutUntil = lockoutTimestamp
            } else {
                state.lockoutUntil = nil
                defaults.removeObject(forKey: kLockoutTime)
            }
        }
    }

    // MARK: - Biometric Prompt (Face ID / Touch ID)
    public func triggerBiometricAuthentication(completion: @escaping (Bool) -> Void) {
        guard !state.isLockedOut else {
            state.securityNotice = "Too many failed attempts. Device locked temporarily."
            completion(false)
            return
        }

        let context = LAContext()
        context.localizedCancelTitle = "Use PIN"

        let reason = "Unlock TaskFlow AI to access enterprise workspace"
        context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { [weak self] success, error in
            DispatchQueue.main.async {
                if success {
                    self?.unlockSession()
                    completion(true)
                } else {
                    self?.state.securityNotice = error?.localizedDescription ?? "Biometric recognition unconfirmed"
                    completion(false)
                }
            }
        }
    }

    // MARK: - 4-Digit PIN Hash & Verification
    public func verifyPin(_ inputPin: String) -> Bool {
        guard !state.isLockedOut else {
            state.securityNotice = "Security Lockout active. Please wait."
            return false
        }

        guard let storedHash = defaults.string(forKey: kPinHash),
              let storedSalt = defaults.string(forKey: kPinSalt) else {
            // Default demo pin fallback: 1234
            if inputPin == "1234" {
                unlockSession()
                return true
            }
            recordFailedAttempt()
            return false
        }

        let computedHash = hashPin(pin: inputPin, salt: storedSalt)
        if computedHash == storedHash {
            unlockSession()
            return true
        } else {
            recordFailedAttempt()
            return false
        }
    }

    public func setPin(_ newPin: String) {
        let salt = UUID().uuidString
        let hash = hashPin(pin: newPin, salt: salt)
        defaults.set(hash, forKey: kPinHash)
        defaults.set(salt, forKey: kPinSalt)
        defaults.set(0, forKey: kFailedAttempts)
        state.failedAttempts = 0
        state.securityNotice = "New 4-digit PIN configured securely."
    }

    private func hashPin(pin: String, salt: String) -> String {
        let input = "\(salt):\(pin):TASKFLOW_PEPPER"
        let digest = SHA256.hash(data: Data(input.utf8))
        return digest.map { String(format: "%02hhx", $0) }.joined()
    }

    private func recordFailedAttempt() {
        state.failedAttempts += 1
        defaults.set(state.failedAttempts, forKey: kFailedAttempts)

        if state.failedAttempts >= 5 {
            let lockout = Date().addingTimeInterval(60) // 1 minute lockout
            state.lockoutUntil = lockout
            defaults.set(lockout, forKey: kLockoutTime)
            state.securityNotice = "5 incorrect attempts. Locked for 60 seconds."
        } else {
            state.securityNotice = "Incorrect PIN. Attempt \(state.failedAttempts) of 5."
        }
    }

    public func lockSession() {
        state.isLocked = true
        state.securityNotice = nil
    }

    public func unlockSession() {
        state.isLocked = false
        state.failedAttempts = 0
        state.lockoutUntil = nil
        defaults.set(0, forKey: kFailedAttempts)
        defaults.removeObject(forKey: kLockoutTime)
        state.securityNotice = nil
    }

    public func setAuthenticated(_ auth: Bool) {
        state.isAuthenticated = auth
        if auth {
            state.isLocked = false
        }
    }
}
