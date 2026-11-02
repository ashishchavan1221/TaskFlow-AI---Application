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
import androidx.compose.runtime.*
import org.json.JSONObject
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
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.components.TaskFlowBrandLogo

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
    onOpenCreateTask: () -> Unit,
    onSelectTask: (TaskItem) -> Unit = {},
    onOpenViewDoc: (TaskItem, Boolean) -> Unit = { _, _ -> },
    onOpenUploadDoc: (TaskItem) -> Unit = {},
    onOpenTaskDocuments: (TaskItem) -> Unit = {}
) {
    val pendingRequests = permissionRequests.filter { it.status.equals("pending", ignoreCase = true) }
    val totalTasks = tasks.size
    val doneCount = tasks.count { it.status == "done" || it.status == "completed" }
    val urgentTasks = tasks.filter { it.priority.equals("urgent", ignoreCase = true) || it.priority.equals("high", ignoreCase = true) }
    val velocity = if (totalTasks > 0) ((doneCount.toFloat() / totalTasks) * 100).toInt() else 25

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Executive Command Console Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF4F46E5).copy(alpha = 0.95f),
                                    Color(0xFF7C3AED).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "★  EXECUTIVE COMMAND CONSOLE",
                                    color = Color(0xFFE0E7FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = org?.name ?: "TaskFlow Enterprise",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            if (org?.logoUrl != null && org.logoUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = org.logoUrl,
                                    contentDescription = "Company Logo",
                                    modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                TaskFlowBrandLogo(size = 54.dp, showGlow = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = org?.description ?: "Role-based delegation, real-time audit ledger, and workforce orchestration.",
                            color = Color.White.copy(alpha = 0.85f),
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
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = currentMember?.displayName ?: "Administrator",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "GSTIN: ${org?.gstin?.ifBlank { "Not provided" }}",
                                    color = Color.White,
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

        // 2. 2x2 KPI Metric Cards Grid
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
                        iconBg = Color(0xFF2563EB).copy(alpha = 0.12f),
                        badgeText = "+14%",
                        badgeColor = SuccessGreen,
                        valueText = "$totalTasks",
                        labelText = "Active Workflows",
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        icon = Icons.Default.CheckCircle,
                        iconColor = SuccessGreen,
                        iconBg = SuccessGreen.copy(alpha = 0.12f),
                        badgeText = "$velocity%",
                        badgeColor = SuccessGreen,
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
                        iconBg = Color(0xFFF59E0B).copy(alpha = 0.12f),
                        badgeText = "${pendingRequests.size} Req",
                        badgeColor = Color(0xFFF59E0B),
                        valueText = "${pendingRequests.size}",
                        labelText = "Manager Approvals",
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFF43F5E),
                        iconBg = Color(0xFFF43F5E).copy(alpha = 0.12f),
                        badgeText = "Critical",
                        badgeColor = Color(0xFFF43F5E),
                        valueText = "${urgentTasks.size}",
                        labelText = "Urgent Deliverables",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Pending Manager Requests Section
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                    text = "Team View →",
                    color = ActionBlue,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All manager requests approved! Team operations optimal.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(pendingRequests) { req ->
                PendingRequestCardItem(
                    req = req,
                    onApproveRequest = onApproveRequest,
                    onRejectRequest = onRejectRequest
                )
            }
        }

        // 4. High-Priority Deliverables Section
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All →",
                    color = ActionBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }
        }

        items(urgentTasks) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectTask(task) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFF43F5E).copy(alpha = 0.15f),
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
                            color = ActionBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = task.status.replace("_", " ").uppercase(),
                                color = ActionBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = task.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.description,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Documents Pill Badges (Brief & Work Deliverable)
                    if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (task.attachmentName.isNotBlank()) {
                                Surface(
                                    onClick = { onOpenViewDoc(task, false) },
                                    color = ActionBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = ActionBlue,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Brief: ${task.attachmentName}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ActionBlue
                                        )
                                    }
                                }
                            }
                            if (task.workDocName.isNotBlank()) {
                                Surface(
                                    onClick = { onOpenViewDoc(task, true) },
                                    color = SuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Work Proof: ${task.workDocName}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Document Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenTaskDocuments(task) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = ActionBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Documents", fontSize = 10.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onOpenUploadDoc(task) },
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (task.workDocName.isNotBlank()) "Update Doc" else "Upload Doc", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Assigned: ${task.assignedToName.ifBlank { "Team" }}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${task.progress}%",
                            color = ActionBlue,
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
                        color = ActionBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Delegate new task button removed to prevent duplication.
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = labelText,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PendingRequestCardItem(
    req: PermissionRequest,
    onApproveRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit
) {
    val isCredReset = req.actionType.contains("CREDENTIAL_RESET", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isCredReset) ActionBlue.copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.35f))
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
                    color = if (isCredReset) ActionBlue else Color(0xFFF59E0B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = if (isCredReset) ActionBlue.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isCredReset) "RESET CREDENTIALS" else req.actionType.replace("_", " "),
                        color = if (isCredReset) ActionBlue else Color(0xFFF59E0B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = req.targetStaffName.ifBlank { "Staff Candidate" },
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"${req.reason}\"",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isCredReset) {
                var showRegenModal by remember { mutableStateOf(false) }
                var targetEmail by remember { mutableStateOf("") }
                var oldEmpId by remember { mutableStateOf(req.targetStaffId) }
                var newEmpId by remember { mutableStateOf(if (req.targetStaffId.isNotBlank()) req.targetStaffId else "MGR-002") }
                var newPassword by remember { mutableStateOf("Pass#" + (1000..9999).random()) }

                LaunchedEffect(req.id) {
                    try {
                        val obj = JSONObject(req.payloadJson)
                        targetEmail = obj.optString("email", "")
                        if (obj.optString("employeeId").isNotBlank()) {
                            oldEmpId = obj.optString("employeeId")
                            newEmpId = obj.optString("employeeId")
                        }
                    } catch (_: Exception) {}
                }

                Button(
                    onClick = { showRegenModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🔑 Regenerate ID & Password", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (showRegenModal) {
                    AlertDialog(
                        onDismissRequest = { showRegenModal = false },
                        title = { Text("Regenerate Credentials for ${req.managerName}", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "Admin authorization: Assign new Employee ID and Password for this account.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = targetEmail,
                                    onValueChange = { targetEmail = it },
                                    label = { Text("Registered Email") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newEmpId,
                                    onValueChange = { newEmpId = it.uppercase() },
                                    label = { Text("New Employee ID") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("New Password") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newEmpId.isNotBlank() && newPassword.isNotBlank()) {
                                        onApproveRequest(req.id)
                                        showRegenModal = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                            ) {
                                Text("Apply & Reissue Credentials", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRegenModal = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApproveRequest(req.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
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

