package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.Organization
import com.example.data.model.PermissionRequest
import com.example.data.model.TaskItem
import com.example.ui.theme.*

@Composable
fun OwnerDashboard(
    org: Organization?,
    currentMember: Member?,
    tasks: List<TaskItem>,
    members: List<Member>,
    permissionRequests: List<PermissionRequest>,
    onNavigateToTasks: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToAi: () -> Unit,
    onApproveRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onOpenCreateTask: () -> Unit
) {
    val pendingRequests = permissionRequests.filter { it.status.equals("pending", ignoreCase = true) }
    val totalTasks = tasks.size
    val doneCount = tasks.count { it.status == "done" }
    val urgentTasks = tasks.filter { it.priority.equals("urgent", ignoreCase = true) || it.priority.equals("high", ignoreCase = true) }
    val velocity = if (totalTasks > 0) ((doneCount.toFloat() / totalTasks) * 100).toInt() else 25

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Executive Command Console Card (Matching Screenshot)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "★  EXECUTIVE COMMAND CONSOLE",
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = org?.name ?: "Your Company",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = org?.description ?: "Cloud automation and workflow delegation infrastructure.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Active Leader: ",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = currentMember?.displayName?.split(" ")?.firstOrNull() ?: "Vikram",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "GSTIN: ${org?.gstin?.ifBlank { "Not provided" }}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. 2x2 KPI Metric Cards Grid (Matching Screenshot)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Active Workflows & Completion Velocity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        icon = Icons.Default.GridView,
                        iconColor = Color(0xFF3B82F6),
                        iconBg = Color(0xFF2563EB).copy(alpha = 0.15f),
                        badgeText = "+14%",
                        badgeColor = Color(0xFF10B981),
                        valueText = "$totalTasks",
                        labelText = "Active Workflows",
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF10B981),
                        iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                        badgeText = "$velocity%",
                        badgeColor = Color(0xFF10B981),
                        valueText = "$velocity%",
                        labelText = "Completion Velocity",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Manager Approvals & Urgent Deliverables
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFFF59E0B),
                        iconBg = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        badgeText = "${pendingRequests.size} Req",
                        badgeColor = Color(0xFFF59E0B),
                        valueText = "${pendingRequests.size}",
                        labelText = "Manager Approvals",
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFF43F5E),
                        iconBg = Color(0xFFF43F5E).copy(alpha = 0.15f),
                        badgeText = "Critical",
                        badgeColor = Color(0xFFF43F5E),
                        valueText = "${urgentTasks.size}",
                        labelText = "Urgent Deliverables",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Pending Manager Requests Section (Matching Screenshot)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pending Manager Requests",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF43F5E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pendingRequests.size}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "SLA Queue",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTeam() }
                )
            }
        }

        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
                    border = BorderStroke(1.dp, Color(0xFF26334D))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All manager requests approved! Team operations optimal.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(pendingRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.managerName,
                                color = Color(0xFFF59E0B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = req.actionType.replace("_", " "),
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = req.targetStaffName.ifBlank { "Staff Candidate" },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"${req.reason}\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApproveRequest(req.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(38.dp)
                            ) {
                                Text(
                                    text = "✓ Approve Request",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { onRejectRequest(req.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
                                border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Text(
                                    text = "Reject",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. High-Priority Deliverables Section (Matching Screenshot)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "High-Priority Task Pipeline",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All →",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }
        }

        items(urgentTasks) { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
                border = BorderStroke(1.dp, Color(0xFF26334D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF43F5E).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = task.priority.uppercase(),
                                color = Color(0xFFF43F5E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = task.status.replace("_", " ").uppercase(),
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = task.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Assigned: ${task.assignedToName.ifBlank { "Team" }}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${task.progress}%",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { task.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Delegate new task button
        item {
            Button(
                onClick = onOpenCreateTask,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delegate New Executive Task", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun KpiCard(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    badgeText: String,
    badgeColor: Color,
    valueText: String,
    labelText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
        border = BorderStroke(1.dp, Color(0xFF26334D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = valueText,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = labelText,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}
