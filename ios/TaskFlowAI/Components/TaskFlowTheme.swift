import SwiftUI

// MARK: - TaskFlow Color Palette (Exact Parity with Android Color.kt & Theme.kt)
public extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }

    // Modern Dark Palette matching TaskFlow AI Android Design
    static let darkBackground = Color(hex: 0x0B1120)
    static let darkSurface = Color(hex: 0x131D33)
    static let darkSurfaceElevated = Color(hex: 0x1A2744)
    static let darkCardBg = Color(hex: 0x152238)
    static let darkBorder = Color(hex: 0x26334D)
    static let darkBorderHighlight = Color(hex: 0x6366F1, alpha: 0.2)

    // Vibrant Accent Colors
    static let brandIndigo = Color(hex: 0x6366F1)
    static let brandIndigoDeep = Color(hex: 0x4F46E5)
    static let brandPurple = Color(hex: 0x7C3AED)
    static let accentCyan = Color(hex: 0x38BDF8)
    static let accentEmerald = Color(hex: 0x10B981)
    static let accentAmber = Color(hex: 0xF59E0B)
    static let accentRose = Color(hex: 0xF43F5E)

    // Text Tokens
    static let textPrimary = Color(hex: 0xF8FAFC)
    static let textSecondary = Color(hex: 0x94A3B8)
    static let textMuted = Color(hex: 0x64748B)

    // Functional & Compatibility Tokens
    static let navyPrimary = Color(hex: 0x0F172A)
    static let navyDeep = Color(hex: 0x020617)
    static let actionBlue = Color(hex: 0x4F46E5)
    static let secondaryBlue = Color(hex: 0x38BDF8)
    static let successGreen = Color(hex: 0x10B981)
    static let warningAmber = Color(hex: 0xF59E0B)
    static let errorRed = Color(hex: 0xF43F5E)
    static let surfaceLight = Color(hex: 0xF8FAFC)
    static let cardLight = Color.white
    static let textDark = Color(hex: 0x0F172A)

    // Gradients
    static let primaryGradient = LinearGradient(
        colors: [Color(hex: 0x4F46E5), Color(hex: 0x7C3AED)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let cyberNeonGradient = LinearGradient(
        colors: [Color(hex: 0x00D2FF), Color(hex: 0x7C3AED), Color(hex: 0xD500F9)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let drawerHeaderGradient = LinearGradient(
        colors: [Color(hex: 0x1E1B4B), Color(hex: 0x312E81), Color(hex: 0x4F46E5)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

// MARK: - Reusable Enterprise Card View Modifier
public struct EnterpriseCardModifier: ViewModifier {
    var isDark: Bool = true

    public func body(content: Content) -> some View {
        content
            .background(isDark ? Color.darkSurface : Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isDark ? Color.darkBorder.opacity(0.8) : Color(hex: 0xCBD5E1), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(isDark ? 0.3 : 0.06), radius: 8, x: 0, y: 3)
    }
}

public extension View {
    func enterpriseCard(isDark: Bool = true) -> some View {
        self.modifier(EnterpriseCardModifier(isDark: isDark))
    }
}
