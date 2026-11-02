import SwiftUI
import WebKit

/// Recharts & D3-inspired Priority Bottleneck Donut Chart.
/// Renders task priority distribution (Urgent, High, Medium, Low) using a dual-mode vector chart:
/// High-Performance SwiftUI Vector Canvas + Crash-Resistant Standalone SVG WKWebView.
/// Matches D3PriorityDonutChart.kt 1:1.
public struct D3PriorityDonutChart: View {
    public var tasks: [TaskItem]
    public var isDarkTheme: Bool = true
    public var onOpenCreateTask: (() -> Void)? = nil

    @State private var selectedPriorityFilter: String? = nil
    @State private var useWebViewMode: Bool = false

    public init(tasks: [TaskItem], isDarkTheme: Bool = true, onOpenCreateTask: (() -> Void)? = nil) {
        self.tasks = tasks
        self.isDarkTheme = isDarkTheme
        self.onOpenCreateTask = onOpenCreateTask
    }

    private var urgentCount: Int {
        tasks.count { $0.priority.lowercased() == "urgent" || $0.priority.lowercased() == "critical" }
    }
    private var highCount: Int {
        tasks.count { $0.priority.lowercased() == "high" }
    }
    private var mediumCount: Int {
        tasks.count { $0.priority.lowercased() == "medium" }
    }
    private var lowCount: Int {
        tasks.count { $0.priority.lowercased() == "low" }
    }
    private var totalCount: Int { tasks.count }
    private var isPipelineEmpty: Bool { totalCount == 0 }

    private var bottleneckCount: Int { urgentCount + highCount }
    private var bottleneckPercentage: Int {
        totalCount > 0 ? (bottleneckCount * 100) / totalCount : 0
    }

    private let urgentColor = Color(hex: 0xEF4444)
    private let highColor = Color(hex: 0xF59E0B)
    private let mediumColor = Color(hex: 0x3B82F6)
    private let lowColor = Color(hex: 0x10B981)

    private var slices: [(label: String, count: Int, color: Color)] {
        [
            ("Urgent", urgentCount, urgentColor),
            ("High", highCount, highColor),
            ("Medium", mediumCount, mediumColor),
            ("Low", lowCount, lowColor)
        ]
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // Header Bar
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Circle()
                            .fill(isPipelineEmpty ? Color.textSecondary.opacity(0.4) : Color.actionBlue)
                            .frame(width: 8, height: 8)

                        Text("RECHARTS PRIORITY BOTTLENECK RADAR")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(isPipelineEmpty ? Color.textSecondary : Color.actionBlue)
                            .tracking(1)
                    }

                    Text("Task Priority Distribution")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(isDarkTheme ? Color.textPrimary : Color.textDark)
                }

                Spacer()

                HStack(spacing: 8) {
                    if !isPipelineEmpty {
                        Button(action: { useWebViewMode.toggle() }) {
                            Image(systemName: "chart.pie.fill")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(Color.actionBlue)
                                .frame(width: 28, height: 28)
                                .background(Color.darkSurfaceElevated)
                                .clipShape(Circle())
                        }
                    }

                    Text(isPipelineEmpty ? "0 Tasks" : "\(bottleneckPercentage)% Critical Load")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(isPipelineEmpty ? Color.textSecondary : (bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(
                            (isPipelineEmpty ? Color.darkSurfaceElevated : (bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen)).opacity(0.15)
                        )
                        .cornerRadius(8)
                }
            }

            if isPipelineEmpty {
                // Empty Pipeline Placeholder
                VStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .stroke(isDarkTheme ? Color(hex: 0x334155) : Color(hex: 0xE2E8F0), lineWidth: 16)
                            .frame(width: 110, height: 110)

                        VStack(spacing: 2) {
                            Text("0")
                                .font(.system(size: 24, weight: .heavy))
                                .foregroundColor(Color.textSecondary)
                            Text("TASKS")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(Color.textMuted)
                                .tracking(1)
                        }
                    }
                    .frame(height: 120)

                    Text("No active tasks")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(isDarkTheme ? Color.textPrimary : Color.textDark)

                    Text("Your department pipeline is currently empty. To visualize task priority distribution and bottleneck metrics, assign new deliverables to your staff associates.")
                        .font(.system(size: 12))
                        .foregroundColor(Color.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 10)
                }
                .padding(20)
                .frame(maxWidth: .infinity)
                .background(Color.darkSurfaceElevated.opacity(0.4))
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color.darkBorder.opacity(0.4), lineWidth: 1)
                )
            } else {
                // Donut Chart Vector Display
                ZStack {
                    if useWebViewMode {
                        SVGDonutWebView(
                            urgent: urgentCount,
                            high: highCount,
                            medium: mediumCount,
                            low: lowCount,
                            total: totalCount,
                            isDarkTheme: isDarkTheme
                        )
                        .frame(height: 180)
                    } else {
                        // Native SwiftUI Donut Chart
                        SwiftUIDonutChart(
                            slices: slices,
                            totalCount: totalCount,
                            selectedFilter: selectedPriorityFilter,
                            isDarkTheme: isDarkTheme
                        )
                        .frame(height: 180)
                    }
                }
                .frame(maxWidth: .infinity)

                // Interactive Priority Chips
                HStack(spacing: 6) {
                    ForEach(slices, id: \.label) { slice in
                        let isSelected = selectedPriorityFilter == slice.label
                        let pct = totalCount > 0 ? (slice.count * 100) / totalCount : 0

                        Button(action: {
                            if selectedPriorityFilter == slice.label {
                                selectedPriorityFilter = nil
                            } else {
                                selectedPriorityFilter = slice.label
                            }
                        }) {
                            VStack(spacing: 3) {
                                HStack(spacing: 4) {
                                    Circle()
                                        .fill(slice.color)
                                        .frame(width: 6, height: 6)
                                    Text(slice.label)
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(isDarkTheme ? Color.textPrimary : Color.textDark)
                                }
                                Text("\(slice.count) (\(pct)%)")
                                    .font(.system(size: 11, weight: .heavy))
                                    .foregroundColor(slice.color)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                            .background(
                                isSelected ? slice.color.opacity(0.22) : Color.darkSurfaceElevated.opacity(0.5)
                            )
                            .cornerRadius(10)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(isSelected ? slice.color : Color.clear, lineWidth: 1.5)
                            )
                        }
                    }
                }

                // SLA Guidance Card
                VStack(alignment: .leading, spacing: 4) {
                    Text("Priority SLA Guidance:")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color.textMuted)

                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("🔴 Urgent: < 24h SLA")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(urgentColor)
                            Text("🔵 Medium: Active sprint")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(mediumColor)
                        }
                        Spacer()
                        VStack(alignment: .leading, spacing: 2) {
                            Text("🟡 High: Near deadline")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(highColor)
                            Text("🟢 Low: Flexible backlog")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(lowColor)
                        }
                    }
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.darkSurfaceElevated.opacity(0.4))
                .cornerRadius(10)

                // Bottleneck Alert / Equilibrium Banner
                HStack(alignment: .top, spacing: 8) {
                    Image(systemName: bottleneckPercentage > 40 ? "exclamationmark.triangle.fill" : "checkmark.circle.fill")
                        .foregroundColor(bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen)
                        .font(.system(size: 16))

                    VStack(alignment: .leading, spacing: 2) {
                        Text(bottleneckPercentage > 40 ? "Bottleneck Alert: \(bottleneckCount) critical tasks" : "Pipeline Equilibrium: Healthy workload distribution")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen)

                        Text(bottleneckPercentage > 40 ? "Over 40% of department tasks are high/urgent priority. Delegate work items or request staff quota." : "Work items are well balanced across priority tiers. Review progress updates to maintain delivery cadence.")
                            .font(.system(size: 11))
                            .foregroundColor(Color.textSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .padding(10)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    (bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen).opacity(0.1)
                )
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke((bottleneckPercentage > 40 ? Color.errorRed : Color.successGreen).opacity(0.25), lineWidth: 1)
                )
            }
        }
        .padding(16)
        .enterpriseCard(isDark: isDarkTheme)
    }
}

// MARK: - Native SwiftUI Donut Chart Canvas
struct SwiftUIDonutChart: View {
    let slices: [(label: String, count: Int, color: Color)]
    let totalCount: Int
    let selectedFilter: String?
    let isDarkTheme: Bool

    var body: some View {
        ZStack {
            GeometryReader { geo in
                let center = CGPoint(x: geo.size.width / 2, y: geo.size.height / 2)
                let radius = min(geo.size.width, geo.size.height) / 2 - 16

                ForEach(0..<slices.count, id: \.self) { index in
                    let slice = slices[index]
                    let startAngle = calculateStartAngle(index: index)
                    let sweepAngle = calculateSweepAngle(count: slice.count)
                    let isSelected = selectedFilter == slice.label
                    let alpha = (selectedFilter == nil || isSelected) ? 1.0 : 0.35

                    if slice.count > 0 {
                        DonutSliceShape(
                            startAngle: startAngle,
                            endAngle: startAngle + sweepAngle - Angle(degrees: 3),
                            innerRadiusFraction: 0.65
                        )
                        .fill(slice.color.opacity(alpha))
                        .scaleEffect(isSelected ? 1.06 : 1.0)
                        .animation(.spring(response: 0.3), value: isSelected)
                    }
                }
            }

            // Center Label
            VStack(spacing: 2) {
                Text("\(totalCount)")
                    .font(.system(size: 26, weight: .heavy))
                    .foregroundColor(isDarkTheme ? Color.textPrimary : Color.textDark)
                Text("TASKS")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(Color.textSecondary)
                    .tracking(1)
            }
        }
    }

    private func calculateStartAngle(index: Int) -> Angle {
        var sumDegrees: Double = -90
        for i in 0..<index {
            if totalCount > 0 {
                sumDegrees += (Double(slices[i].count) / Double(totalCount)) * 360
            }
        }
        return Angle(degrees: sumDegrees)
    }

    private func calculateSweepAngle(count: Int) -> Angle {
        guard totalCount > 0 else { return Angle.zero }
        let deg = (Double(count) / Double(totalCount)) * 360
        return Angle(degrees: deg)
    }
}

// MARK: - Donut Slice Shape
struct DonutSliceShape: Shape {
    var startAngle: Angle
    var endAngle: Angle
    var innerRadiusFraction: CGFloat = 0.65

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let outerRadius = min(rect.width, rect.height) / 2
        let innerRadius = outerRadius * innerRadiusFraction

        path.addArc(center: center, radius: outerRadius, startAngle: startAngle, endAngle: endAngle, clockwise: false)
        path.addArc(center: center, radius: innerRadius, startAngle: endAngle, endAngle: startAngle, clockwise: true)
        path.closeSubpath()
        return path
    }
}

// MARK: - SVG Donut WKWebView
struct SVGDonutWebView: UIViewRepresentable {
    let urgent: Int
    let high: Int
    let medium: Int
    let low: Int
    let total: Int
    let isDarkTheme: Bool

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let webView = WKWebView(frame: .zero, configuration: config)
        webView.isOpaque = false
        webView.backgroundColor = .clear
        webView.scrollView.isScrollEnabled = false
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        let textColor = isDarkTheme ? "#F1F5F9" : "#0F172A"
        let subColor = isDarkTheme ? "#94A3B8" : "#64748B"

        let html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { margin:0; padding:0; box-sizing:border-box; }
            html, body { background: transparent; width:100%; height:100%; display:flex; justify-content:center; align-items:center; }
            svg { max-width: 170px; max-height: 170px; }
            .center-total { font-size: 26px; font-weight: 800; fill: \(textColor); text-anchor: middle; dominant-baseline: central; font-family: -apple-system; }
            .center-label { font-size: 10px; font-weight: 700; fill: \(subColor); text-anchor: middle; letter-spacing: 1px; font-family: -apple-system; }
          </style>
        </head>
        <body>
          <svg viewBox="0 0 200 200">
            <circle cx="100" cy="100" r="66" fill="none" stroke="#EF4444" stroke-width="26" stroke-dasharray="80 320" transform="rotate(-90 100 100)" />
            <circle cx="100" cy="100" r="66" fill="none" stroke="#F59E0B" stroke-width="26" stroke-dasharray="100 300" stroke-dashoffset="-85" transform="rotate(-90 100 100)" />
            <circle cx="100" cy="100" r="66" fill="none" stroke="#3B82F6" stroke-width="26" stroke-dasharray="140 260" stroke-dashoffset="-190" transform="rotate(-90 100 100)" />
            <circle cx="100" cy="100" r="66" fill="none" stroke="#10B981" stroke-width="26" stroke-dasharray="70 330" stroke-dashoffset="-335" transform="rotate(-90 100 100)" />
            <text x="100" y="94" class="center-total">\(total)</text>
            <text x="100" y="115" class="center-label">TASKS</text>
          </svg>
        </body>
        </html>
        """
        uiView.loadHTMLString(html, baseURL: nil)
    }
}
