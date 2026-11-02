package com.example.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.Organization
import com.example.data.model.TaskItem
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.components.TaskFlowBrandLogo

@Composable
fun StaffDashboard(
    org: Organization?,
    currentMember: Member?,
    tasks: List<TaskItem>,
    onUpdateTaskStatus: (String, String, Int) -> Unit,
    onOpenCompletionModal: (String, String) -> Unit = { _, _ -> },
    onTriggerReminder: (TaskItem) -> Unit = {},
    onSelectTask: (TaskItem) -> Unit = {},
    onOpenViewDoc: (TaskItem, Boolean) -> Unit = { _, _ -> },
    onOpenUploadDoc: (TaskItem) -> Unit = {},
    onOpenTaskDocuments: (TaskItem) -> Unit = {}
) {
    val isRealStaff = currentMember != null && currentMember.role.equals("staff", ignoreCase = true) && currentMember.displayName != "N/A" && currentMember.displayName.isNotBlank()
    val staffName = if (isRealStaff) currentMember.displayName else "N/A"
    val staffEmpId = if (isRealStaff) currentMember.employeeId.ifBlank { "N/A" } else "N/A"

    // Multi-device sync rule: Strictly show ONLY tasks assigned to this staff member
    val myTasks = if (!isRealStaff) emptyList() else tasks.filter {
        (it.assignedToMemberId.isNotBlank() && it.assignedToMemberId == currentMember.id) ||
        (it.assignedToName.isNotBlank() && it.assignedToName.equals(staffName, ignoreCase = true))
    }
    val myCompleted = myTasks.count { it.status == "done" || it.status == "completed" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Staff Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF059669), Color(0xFF047857))
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
                                    text = "✦ STAFF PERSONAL EXECUTION HUB",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isRealStaff) "Welcome back, $staffName!" else "Welcome! (Staff Profile: N/A)",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            if (org?.logoUrl != null && org.logoUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = org.logoUrl,
                                    contentDescription = "Company Logo",
                                    modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                TaskFlowBrandLogo(size = 50.dp, showGlow = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isRealStaff) "You have ${myTasks.size - myCompleted} active deliverables queued on your device." else "No staff profile currently provisioned. Contact Administrator to assign tasks.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Assigned To: $staffName ($staffEmpId)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "$myCompleted/${myTasks.size} Done",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Donut Priority Chart
        item {
            D3PriorityDonutChart(
                tasks = myTasks,
                isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Direct Deliverables (${myTasks.size})",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Role-Isolated",
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (myTasks.isEmpty()) {
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No tasks assigned to your account yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tasks assigned by Admin or Manager to $staffName will appear here in real-time.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            items(myTasks) { task ->
                val isDone = task.status == "done" || task.status == "completed"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTask(task) }
                        .testTag("staff_task_card_${task.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (task.hasReminder) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = Color(0xFFF43F5E).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = task.priority.uppercase(),
                                        color = Color(0xFFF43F5E),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }

                                if (task.hasReminder) {
                                    Surface(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Reminder Set", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = if (isDone) SuccessGreen.copy(alpha = 0.15f) else ActionBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = task.status.replace("_", " ").uppercase(),
                                    color = if (isDone) SuccessGreen else ActionBlue,
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

                        // Completion Notes Display if Completed
                        if (isDone && task.completionNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Completed by ${task.completedBy.ifBlank { staffName }} (${task.completedAt.ifBlank { "Recorded" }})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Notes: ${task.completionNotes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${task.progress}%", fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { task.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isDone) SuccessGreen else ActionBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Document badges (Brief & Worker Submission)
                        if (task.attachmentName.isNotBlank() || task.workDocName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                                imageVector = if (task.attachmentType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = ActionBlue,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Brief: ${task.attachmentName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
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
                                                imageVector = if (task.workDocType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Work Proof: ${task.workDocName}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Upload Work Document & View Document Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenTaskDocuments(task) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = ActionBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Documents", fontSize = 10.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onOpenUploadDoc(task) },
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.1f).height(34.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (task.workDocName.isNotBlank()) "Update Work Doc" else "Upload Work Doc", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions Row: Complete with Notes + Reminder Chime
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isDone) {
                                Button(
                                    onClick = { onOpenCompletionModal(task.id, task.title) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Complete & Add Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { onTriggerReminder(task) },
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remind", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onUpdateTaskStatus(task.id, "in_progress", 50) },
                                    border = BorderStroke(1.dp, ActionBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text("↺ Re-open Deliverable", color = ActionBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
