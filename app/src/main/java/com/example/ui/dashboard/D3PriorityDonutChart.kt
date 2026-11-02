package com.example.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.Paint
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.TaskItem
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Recharts & D3-inspired Priority Bottleneck Donut Chart.
 *
 * Renders task priority distribution (Urgent, High, Medium, Low) using a dual-mode vector chart
 * (High-Performance Compose Canvas + Crash-Resistant Standalone SVG WebView).
 * Handles empty states with an explicit 'No active tasks' placeholder card and guidance on creating tasks.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3PriorityDonutChart(
    tasks: List<TaskItem>,
    isDarkTheme: Boolean = true,
    onOpenCreateTask: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Priority breakdown calculation
    val actualUrgent = tasks.count { it.priority.equals("urgent", ignoreCase = true) || it.priority.equals("critical", ignoreCase = true) }
    val actualHigh = tasks.count { it.priority.equals("high", ignoreCase = true) }
    val actualMedium = tasks.count { it.priority.equals("medium", ignoreCase = true) }
    val actualLow = tasks.count { it.priority.equals("low", ignoreCase = true) }
    val totalCount = tasks.size

    val isPipelineEmpty = totalCount == 0

    val bottleneckCount = actualUrgent + actualHigh
    val bottleneckPercentage = if (totalCount > 0) (bottleneckCount * 100) / totalCount else 0

    var selectedPriorityFilter by remember { mutableStateOf<String?>(null) }
    var useWebViewMode by remember { mutableStateOf(false) } // Default to instant Compose Canvas, with optional SVG WebView toggle

    // Recharts / Material 3 Palette
    val urgentColor = Color(0xFFEF4444)
    val highColor = Color(0xFFF59E0B)
    val mediumColor = Color(0xFF3B82F6)
    val lowColor = Color(0xFF10B981)
    val emptyRingColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    val textColorHex = if (isDarkTheme) "#F1F5F9" else "#0F172A"
    val subTextColorHex = if (isDarkTheme) "#94A3B8" else "#64748B"

    val slices = listOf(
        Triple("Urgent", actualUrgent, urgentColor),
        Triple("High", actualHigh, highColor),
        Triple("Medium", actualMedium, mediumColor),
        Triple("Low", actualLow, lowColor)
    )

    // Pure Standalone SVG generator (No external HTTP / JS library required, zero network dependencies)
    val standaloneSvgHtml = remember(actualUrgent, actualHigh, actualMedium, actualLow, totalCount, isDarkTheme) {
        val activeSlices = slices.filter { it.second > 0 }
        val cx = 100.0
        val cy = 100.0
        val outerRadius = 80.0
        val innerRadius = 52.0

        var currentAngle = -90.0
        val pathElements = StringBuilder()

        if (totalCount > 0) {
            for (slice in activeSlices) {
                val value = slice.second
                val hexColor = when (slice.first) {
                    "Urgent" -> "#EF4444"
                    "High" -> "#F59E0B"
                    "Medium" -> "#3B82F6"
                    else -> "#10B981"
                }

                val angleExtent = (value.toDouble() / totalCount.toDouble()) * 360.0
                val gap = if (activeSlices.size > 1) 3.0 else 0.0
                val drawAngle = maxOf(1.0, angleExtent - gap)
                val startRad = Math.toRadians(currentAngle + (gap / 2.0))
                val endRad = Math.toRadians(currentAngle + (gap / 2.0) + drawAngle)

                val x1Outer = cx + outerRadius * cos(startRad)
                val y1Outer = cy + outerRadius * sin(startRad)
                val x2Outer = cx + outerRadius * cos(endRad)
                val y2Outer = cy + outerRadius * sin(endRad)

                val x1Inner = cx + innerRadius * cos(endRad)
                val y1Inner = cy + innerRadius * sin(endRad)
                val x2Inner = cx + innerRadius * cos(startRad)
                val y2Inner = cy + innerRadius * sin(startRad)

                val largeArc = if (drawAngle > 180.0) 1 else 0

                val svgPath = "M $x1Outer $y1Outer " +
                        "A $outerRadius $outerRadius 0 $largeArc 1 $x2Outer $y2Outer " +
                        "L $x1Inner $y1Inner " +
                        "A $innerRadius $innerRadius 0 $largeArc 0 $x2Inner $y2Inner Z"

                pathElements.append("<path d=\"$svgPath\" fill=\"$hexColor\" />\n")
                currentAngle += angleExtent
            }
        }

        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; }
            html, body {
              background: transparent;
              width: 100%;
              height: 100%;
              overflow: hidden;
              display: flex;
              justify-content: center;
              align-items: center;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            }
            #chart-wrap { width: 100%; height: 180px; display: flex; justify-content: center; align-items: center; }
            svg { max-width: 180px; max-height: 180px; }
            .center-total { font-size: 26px; font-weight: 800; fill: $textColorHex; text-anchor: middle; dominant-baseline: central; }
            .center-label { font-size: 10px; font-weight: 700; fill: $subTextColorHex; text-anchor: middle; letter-spacing: 1px; }
          </style>
        </head>
        <body>
          <div id="chart-wrap">
            <svg viewBox="0 0 200 200">
              $pathElements
              <text x="100" y="93" class="center-total">$totalCount</text>
              <text x="100" y="115" class="center-label">TASKS</text>
            </svg>
          </div>
        </body>
        </html>
        """.trimIndent()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_priority_donut_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPipelineEmpty) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else ActionBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECHARTS PRIORITY BOTTLENECK RADAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPipelineEmpty) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else ActionBlue,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Task Priority Distribution",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isPipelineEmpty) {
                        IconButton(
                            onClick = { useWebViewMode = !useWebViewMode },
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 4.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = "Toggle Chart Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        color = when {
                            isPipelineEmpty -> MaterialTheme.colorScheme.surfaceVariant
                            bottleneckPercentage > 40 -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            else -> SuccessGreen.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when {
                                isPipelineEmpty -> "0 Tasks"
                                else -> "$bottleneckPercentage% Critical Load"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isPipelineEmpty -> MaterialTheme.colorScheme.onSurfaceVariant
                                bottleneckPercentage > 40 -> Color(0xFFEF4444)
                                else -> SuccessGreen
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isPipelineEmpty) {
                // Explicit 'No active tasks' Placeholder Card with Descriptive Instructions
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_chart_placeholder")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Empty Ring Vector Illustration
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidthPx = 18.dp.toPx()
                                val arcSizePx = size.width - strokeWidthPx
                                val topLeftPx = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                                drawArc(
                                    color = emptyRingColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeftPx,
                                    size = Size(arcSizePx, arcSizePx),
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "0",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "TASKS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No active tasks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Your department pipeline is currently empty. To visualize task priority distribution and bottleneck metrics, assign new deliverables to your staff associates.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            } else {
                // Interactive Donut Chart Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (useWebViewMode) {
                        // Crash-Proof SVG WebView Renderer with onRenderProcessGone recovery
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                    settings.javaScriptEnabled = false // Pure SVG XML requires no JS execution!
                                    settings.domStorageEnabled = false
                                    settings.allowFileAccess = false
                                    settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                    webViewClient = object : WebViewClient() {
                                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                            // Handle Chromium renderer process crash gracefully without crashing the app
                                            useWebViewMode = false // Auto-switch to Compose Canvas mode
                                            return true
                                        }
                                    }
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(null, standaloneSvgHtml, "text/html", "UTF-8", null)
                            }
                        )
                    } else {
                        // High-Performance Jetpack Compose Vector Canvas Donut Chart
                        val onSurfaceTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
                        val subTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f).toArgb()

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .testTag("compose_donut_canvas")
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                            val strokeWidth = 28.dp.toPx()
                            val outerRadius = (minOf(canvasWidth, canvasHeight) - strokeWidth) / 2f

                            var startAngle = -90f
                            val activeSlices = slices.filter { it.second > 0 }

                            for (slice in activeSlices) {
                                val label = slice.first
                                val count = slice.second
                                val color = slice.third

                                val sweepAngle = (count.toFloat() / totalCount.toFloat()) * 360f
                                val isSelected = selectedPriorityFilter == label
                                val gap = if (activeSlices.size > 1) 4f else 0f
                                val drawSweep = maxOf(1f, sweepAngle - gap)

                                val currentStrokeWidth = if (isSelected) strokeWidth + 8.dp.toPx() else strokeWidth
                                val currentAlpha = if (selectedPriorityFilter == null || isSelected) 1f else 0.35f

                                drawArc(
                                    color = color.copy(alpha = currentAlpha),
                                    startAngle = startAngle + (gap / 2f),
                                    sweepAngle = drawSweep,
                                    useCenter = false,
                                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                                    size = Size(outerRadius * 2f, outerRadius * 2f),
                                    style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Butt)
                                )

                                startAngle += sweepAngle
                            }

                            // Center Text: Total Count and TASKS label
                            val totalTextPaint = Paint().apply {
                                color = onSurfaceTextColor
                                textSize = 26.sp.toPx()
                                isFakeBoldText = true
                                textAlign = Paint.Align.CENTER
                            }

                            val labelTextPaint = Paint().apply {
                                color = subTextColor
                                textSize = 10.sp.toPx()
                                isFakeBoldText = true
                                textAlign = Paint.Align.CENTER
                                letterSpacing = 0.1f
                            }

                            drawContext.canvas.nativeCanvas.drawText(
                                totalCount.toString(),
                                center.x,
                                center.y + 4.dp.toPx(),
                                totalTextPaint
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                "TASKS",
                                center.x,
                                center.y + 20.dp.toPx(),
                                labelTextPaint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Legend & Breakdown Interactive Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEach { (label, count, color) ->
                        val isSelected = selectedPriorityFilter == label
                        val pct = if (totalCount > 0) (count * 100) / totalCount else 0

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedPriorityFilter = if (isSelected) null else label
                                }
                                .testTag("chip_priority_${label.lowercase()}"),
                            color = if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) BorderStroke(1.5.dp, color) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$count ($pct%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = color
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Tier SLA Indicators Guide
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Priority SLA Guidance:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🔴 Urgent: < 24h SLA", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = urgentColor)
                                Text("🔵 Medium: Active sprint", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = mediumColor)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🟡 High: Near deadline", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = highColor)
                                Text("🟢 Low: Flexible backlog", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = lowColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actionable Operational Bottleneck Insight Banner
                Surface(
                    color = if (bottleneckPercentage > 40) Color(0xFFEF4444).copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (bottleneckPercentage > 40) Color(0xFFEF4444).copy(alpha = 0.3f) else SuccessGreen.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (bottleneckPercentage > 40) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (bottleneckPercentage > 40) Color(0xFFEF4444) else SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (bottleneckPercentage > 40)
                                    "Bottleneck Alert: $bottleneckCount critical tasks"
                                else
                                    "Pipeline Equilibrium: Healthy workload distribution",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (bottleneckPercentage > 40) Color(0xFFEF4444) else SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (bottleneckPercentage > 40)
                                    "Over 40% of department tasks are high/urgent priority. Delegate work items or request staff quota."
                                else
                                    "Work items are well balanced across priority tiers. Review progress updates to maintain delivery cadence.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
