package com.example.ui.team

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.Member
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    members: List<Member>,
    departments: List<Department>,
    activeRole: String,
    onAddMember: (String, String, String, String, String, String, String, String) -> Unit,
    onUpdateMember: (String, String, String, String, String, String) -> Unit,
    onDeleteMember: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf("staff") }
    var newMemberEmpId by remember { mutableStateOf("") }
    var newMemberPassword by remember { mutableStateOf("") }
    var newMemberDept by remember { mutableStateOf("Operations & Management") }

    val isAdmin = activeRole.equals("admin", ignoreCase = true)
    val isManager = activeRole.equals("manager", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Roster (${members.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
        },
        floatingActionButton = {
            if (isAdmin || isManager) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ActionBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_member")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                MemberCard(
                    member = member,
                    onUpdate = onUpdateMember,
                    onDelete = onDeleteMember,
                    canDelete = isAdmin
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(if (isManager) "Submit Staff Request to Admin" else "Add Team Member")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Employee ID and a temporary password are generated securely by the server after you tap Create.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("Email *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("staff", "manager").forEach { role ->
                            Button(
                                onClick = { newMemberRole = role },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (newMemberRole == role) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (newMemberRole == role) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(role.replaceFirstChar { it.uppercase() }, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            onAddMember(
                                newMemberName,
                                newMemberEmpId,
                                newMemberEmail,
                                "",
                                newMemberRole,
                                "ops",
                                newMemberDept,
                                ""
                            )
                            showAddDialog = false
                            newMemberName = ""
                            newMemberEmpId = ""
                            newMemberEmail = ""
                            newMemberPassword = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text(if (isManager) "Submit Request" else "Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberCard(
    member: Member,
    onUpdate: (String, String, String, String, String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = when (member.role.lowercase()) {
                    "admin" -> NavyPrimary
                    "manager" -> ActionBlue
                    else -> SuccessGreen
                }.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = member.displayName.take(1).uppercase(),
                        color = when (member.role.lowercase()) {
                            "admin" -> ActionBlue
                            "manager" -> ActionBlue
                            else -> SuccessGreen
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${member.role.uppercase()} • ${member.departmentName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "ID: ${member.employeeId} | ${member.email}",
                    fontSize = 11.sp,
                    color = ActionBlue
                )
            }

            if (canDelete && member.role != "admin") {
                IconButton(onClick = { onDelete(member.id, member.displayName) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}
