import SwiftUI

/// Dynamic Time Log Tracker with interactive stopwatch.
/// Matches the live stopwatch in TaskDetailModal of the Android application.
public struct StopwatchTimerView: View {
    public var currentActualHours: Double
    public var estimatedHours: Double
    public var onLogHours: (Double) -> Void

    @State private var isTimerRunning: Bool = false
    @State private var elapsedSeconds: Int = 0
    @State private var timerSubscription: Timer? = nil

    public init(currentActualHours: Double, estimatedHours: Double, onLogHours: @escaping (Double) -> Void) {
        self.currentActualHours = currentActualHours
        self.estimatedHours = estimatedHours
        self.onLogHours = onLogHours
    }

    private var formattedTime: String {
        let hrs = elapsedSeconds / 3600
        let mins = (elapsedSeconds % 3600) / 60
        let secs = elapsedSeconds % 60
        return String(format: "%02d:%02d:%02d", hrs, mins, secs)
    }

    public var body: some View {
        VStack(spacing: 12) {
            // Header
            HStack {
                Label("Dynamic Work Stopwatch", systemImage: "timer")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(Color.textPrimary)

                Spacer()

                Text("Logged: \(String(format: "%.1f", currentActualHours))h / \(String(format: "%.1f", estimatedHours))h")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(Color.accentCyan)
            }

            // Big Digital Display
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color(hex: 0x070D18))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(isTimerRunning ? Color.actionBlue : Color.darkBorder, lineWidth: 1.5)
                    )

                HStack(spacing: 16) {
                    Text(formattedTime)
                        .font(.system(size: 28, weight: .heavy, design: .monospaced))
                        .foregroundColor(isTimerRunning ? Color.accentCyan : Color.textPrimary)

                    if isTimerRunning {
                        Circle()
                            .fill(Color.errorRed)
                            .frame(width: 10, height: 10)
                            .opacity(0.8)
                    }
                }
                .padding(.vertical, 14)
            }

            // Stopwatch Controls
            HStack(spacing: 10) {
                if !isTimerRunning {
                    Button(action: startTimer) {
                        HStack(spacing: 6) {
                            Image(systemName: "play.fill")
                            Text("Start Timer")
                        }
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(Color.actionBlue)
                        .cornerRadius(10)
                    }
                } else {
                    Button(action: stopAndLog) {
                        HStack(spacing: 6) {
                            Image(systemName: "stop.fill")
                            Text("Stop & Log")
                        }
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(Color.errorRed)
                        .cornerRadius(10)
                    }
                }
            }

            // Quick Log Increment Buttons
            VStack(alignment: .leading, spacing: 6) {
                Text("Quick Add Hours:")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(Color.textSecondary)

                HStack(spacing: 8) {
                    ForEach([0.25, 0.5, 1.0, 2.0], id: \.self) { addH in
                        let label = addH == 0.25 ? "+15m" : (addH == 0.5 ? "+30m" : "+\(Int(addH))h")
                        Button(action: {
                            onLogHours(addH)
                            AudioChimePlayer.shared.playNotificationSound()
                        }) {
                            Text(label)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color.actionBlue)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 7)
                                .background(Color.actionBlue.opacity(0.15))
                                .cornerRadius(8)
                        }
                    }
                }
            }
        }
        .padding(14)
        .background(Color.darkSurfaceElevated.opacity(0.5))
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color.darkBorder.opacity(0.6), lineWidth: 1)
        )
    }

    private func startTimer() {
        isTimerRunning = true
        timerSubscription = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            elapsedSeconds += 1
        }
    }

    private func stopAndLog() {
        timerSubscription?.invalidate()
        timerSubscription = nil
        isTimerRunning = false

        if elapsedSeconds > 0 {
            let loggedHours = Double(elapsedSeconds) / 3600.0
            let roundedHours = (loggedHours * 100).rounded() / 100
            onLogHours(max(0.05, roundedHours))
            elapsedSeconds = 0
            AudioChimePlayer.shared.playTaskCompletionSound()
        }
    }
}
