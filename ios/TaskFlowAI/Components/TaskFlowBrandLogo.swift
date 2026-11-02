import SwiftUI

/// Premium cyber-neon TaskFlow emblem badge rendering the official TF hexagonal mark.
/// Matches TaskFlowBrandLogo.kt 1:1.
public struct TaskFlowBrandLogo: View {
    public var size: CGFloat = 40
    public var showGlow: Bool = true

    public init(size: CGFloat = 40, showGlow: Bool = true) {
        self.size = size
        self.showGlow = showGlow
    }

    public var body: some View {
        let cornerRadius = size * 0.24
        let shape = RoundedRectangle(cornerRadius: cornerRadius)

        ZStack {
            shape
                .fill(Color(hex: 0x101726))
                .overlay(
                    shape
                        .stroke(Color.cyberNeonGradient, lineWidth: 1.5)
                )
                .shadow(
                    color: showGlow ? Color(hex: 0x00D2FF, alpha: 0.5) : Color.clear,
                    radius: showGlow ? 6 : 0,
                    x: 0,
                    y: 2
                )

            Image("logo", bundle: Bundle.main)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: size * 0.92, height: size * 0.92)
                .clipShape(shape)
        }
        .frame(width: size, height: size)
    }
}
