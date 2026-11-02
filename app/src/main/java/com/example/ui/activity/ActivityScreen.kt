package com.example.ui.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.PermissionRequest
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    auditLogs: List<AuditLog>,
    permissionRequests: List<PermissionRequest>,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Activities, 1 = Audits

    // Concrete enterprise examples to demonstrate real operational workflows
    val sampleActivities = remember {
        listOf(
            AuditLog(
                id = "act-01",
                organizationId = "org-main",
                actorName = "Aarav Sharma",
                actorRole = "Staff",
                action = "Completed Deliverable: 'Generate GST Billing Invoice #2026-08' [Notes: Verified e-Way bill & signed XML invoice via portal]",
                timestamp = "Today, 14:32"
            ),
            AuditLog(
                id = "act-02",
                organizationId = "org-main",
                actorName = "System Sentinel",
                actorRole = "Chime Alert",
                action = "Triggered Audio Reminder Alert for overdue inventory audit task",
                timestamp = "Today, 12:15"
            ),
            AuditLog(
                id = "act-03",
                organizationId = "org-main",
                actorName = "Admin Executive",
                actorRole = "Admin",
                action = "Formed Workgroup 'Billing & Invoicing Squad' leading 2 Managers & 3 Staff",
                timestamp = "Yesterday, 17:40"
            ),
            AuditLog(
                id = "act-04",
                organizationId = "org-main",
                actorName = "Priya Patel",
                actorRole = "Manager",
                action = "Dispatched 4 tasks to Operations Alpha workgroup",
                timestamp = "Yesterday, 11:20"
            )
        )
    }

    val sampleAudits = remember {
        listOf(
            AuditLog(
                id = "aud-01",
                organizationId = "org-main",
                actorName = "Admin Owner",
                actorRole = "Admin",
                action = "GSTIN Statutory Mandate Verified: 27AAACG0000A1Z5 (Maharashtra State Jurisdiction, Tax Class A)",
                timestamp = "2026-10-20 09:30"
            ),
            AuditLog(
                id = "aud-02",
                organizationId = "org-main",
                actorName = "Admin Owner",
                actorRole = "Admin",
                action = "PROVISION_CREDENTIALS: Generated manager login credentials for Priya Patel (MGR-001)",
                timestamp = "2026-10-20 10:15"
            ),
            AuditLog(
                id = "aud-03",
                organizationId = "org-main",
                actorName = "Security Gateway",
                actorRole = "System",
                action = "MULTI_DEVICE_SYNC_ESTABLISHED: Firestore cloud channel verified across 2 physical device terminals",
                timestamp = "2026-10-21 08:00"
            )
        )
    }

    // Merge live logs with sample reference data
    val displayLogs = remember(selectedTab, auditLogs, permissionRequests) {
        if (selectedTab == 0) {
            // Activities tab: Show task completions, notes, delegations
            val taskEvents = auditLogs.filter { it.action.contains("TASK", ignoreCase = true) || it.action.contains("Group", ignoreCase = true) || it.action.contains("Completed", ignoreCase = true) }
            taskEvents + sampleActivities
        } else {
            // Audits tab: Show security, logins, approvals, GSTIN audits
            val securityEvents = auditLogs.filter { it.action.contains("AUTH", ignoreCase = true) || it.action.contains("LOGIN", ignoreCase = true) || it.action.contains("GST", ignoreCase = true) || it.action.contains("CREATE", ignoreCase = true) }
            val permEvents = permissionRequests.map { req ->
                AuditLog(
                    id = req.id,
                    organizationId = req.organizationId,
                    actorName = req.managerName,
                    actorRole = "Manager",
                    action = "POLICY_DECISION: ${req.actionType} - ${req.reason} [${req.status.uppercase()}]",
                    timestamp = "Reviewed"
                )
            }
            securityEvents + permEvents + sampleAudits
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enterprise Operations Ledger", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Concrete activities & compliance audit trail", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Activities vs Audits Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ActionBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Operational Activities", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compliance & Audits", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            // Concrete Example Banner
            Surface(
                color = if (selectedTab == 0) ActionBlue.copy(alpha = 0.08f) else SuccessGreen.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, if (selectedTab == 0) ActionBlue.copy(alpha = 0.25f) else SuccessGreen.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Info else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (selectedTab == 0) ActionBlue else SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Live Activity Stream (Example: Invoicing & Completion Notes)" else "Compliance Audit Ledger (Example: Mandatory GSTIN & Credentials)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedTab == 0)
                                "Logs task completions with custom notes, reminder audio chimes, and group communications."
                            else
                                "Records tamper-evident security actions, Admin GSTIN statutory enforcement, and role credentials.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            log.action.contains("DELETE", true) -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                            log.action.contains("GST", true) -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                            log.action.contains("Completed", true) || log.action.contains("CREATE", true) -> SuccessGreen.copy(alpha = 0.15f)
                                            log.action.contains("Reminder", true) -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            else -> ActionBlue.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        log.action.contains("DELETE", true) -> Icons.Default.Delete
                                        log.action.contains("GST", true) -> Icons.Default.Verified
                                        log.action.contains("Completed", true) -> Icons.Default.CheckCircle
                                        log.action.contains("Reminder", true) -> Icons.Default.Alarm
                                        log.action.contains("CREATE", true) -> Icons.Default.Add
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        log.action.contains("DELETE", true) -> Color(0xFFEF4444)
                                        log.action.contains("GST", true) -> Color(0xFF8B5CF6)
                                        log.action.contains("Completed", true) || log.action.contains("CREATE", true) -> SuccessGreen
                                        log.action.contains("Reminder", true) -> Color(0xFFF59E0B)
                                        else -> ActionBlue
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = log.timestamp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Actor: ${log.actorName} (${log.actorRole})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ActionBlue
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
