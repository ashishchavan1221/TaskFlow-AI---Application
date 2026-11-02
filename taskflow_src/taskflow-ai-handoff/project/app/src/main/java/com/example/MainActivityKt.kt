package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.MainAppViewModel
import com.example.ui.MainUiState
import com.example.ui.dashboard.*
import com.example.ui.navigation.AppNavigationHost
import com.example.ui.settings.SettingsScreen
import com.example.ui.team.TeamScreen
import com.example.ui.theme.*

@Composable
fun MainAppContent(viewModel: MainAppViewModel) {
    AppNavigationHost(
        viewModel = viewModel,
        secureSessionManager = viewModel.secureSessionManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppDashboard(
    viewModel: MainAppViewModel,
    uiState: MainUiState,
    onLockApp: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                // Header: Profile & Company Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.currentMember?.displayName?.take(2)?.uppercase() ?: "VS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uiState.currentMember?.displayName ?: "Vikram Sharma (CEO)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.currentOrg?.name ?: "Your Company",
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
                HorizontalDivider(color = Color(0xFF26334D))

                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Executive Dashboard", color = Color.White) },
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF38BDF8)) }
                )
                NavigationDrawerItem(
                    label = { Text("Task Pipeline", color = Color.White) },
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF38BDF8)) }
                )
                NavigationDrawerItem(
                    label = { Text("Team Directory", color = Color.White) },
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF38BDF8)) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings & Security", color = Color.White) },
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF38BDF8)) }
                )

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Lock Session (PIN)", color = Color(0xFFF43F5E)) },
                    selected = false,
                    onClick = { onLockApp(); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF43F5E)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TaskFlow",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = uiState.currentOrg?.name ?: "Your Company",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    actions = {
                        // Interactive Role Pill Badge (matching screenshot)
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                val nextRole = when (uiState.activeRole.lowercase()) {
                                    "admin" -> "manager"
                                    "manager" -> "staff"
                                    else -> "admin"
                                }
                                viewModel.setActiveRole(nextRole)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.activeRole.uppercase(),
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Notification Bell with Red Badge
                        IconButton(onClick = {
                            viewModel.notificationManager.showNotification(
                                (1000..9999).random(),
                                "TaskFlow AI Enterprise Alert",
                                "Operational SLA at 94%. ${uiState.tasks.size} tasks active in pipeline."
                            )
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))

                                val unread = uiState.permissionRequests.count { it.status == "pending" }
                                if (unread > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF43F5E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$unread",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.setShowCreateTaskDialog(true) },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task", modifier = Modifier.size(28.dp))
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF131D33),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                        label = { Text("Tasks", fontSize = 11.sp) },
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_tasks")
                    )
                    if (uiState.activeRole != "staff") {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Groups, contentDescription = "Team") },
                            label = { Text("Team", fontSize = 11.sp) },
                            selected = uiState.selectedTab == 2,
                            onClick = { viewModel.setSelectedTab(2) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF38BDF8),
                                selectedTextColor = Color(0xFF38BDF8),
                                indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("nav_tab_team")
                        )
                    }
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp) },
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.setSelectedTab(3) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (uiState.selectedTab) {
                    0 -> {
                        when (uiState.activeRole.lowercase()) {
                            "admin" -> OwnerDashboard(
                                org = uiState.currentOrg,
                                currentMember = uiState.currentMember,
                                tasks = uiState.tasks,
                                members = uiState.members,
                                permissionRequests = uiState.permissionRequests,
                                onNavigateToTasks = { viewModel.setSelectedTab(1) },
                                onNavigateToTeam = { viewModel.setSelectedTab(2) },
                                onNavigateToAi = { viewModel.setSelectedTab(3) },
                                onApproveRequest = { id -> viewModel.approvePermissionRequest(id) },
                                onRejectRequest = { id -> viewModel.rejectPermissionRequest(id) },
                                onOpenCreateTask = { viewModel.setShowCreateTaskDialog(true) }
                            )
                            "manager" -> ManagerDashboard(
                                org = uiState.currentOrg,
                                currentMember = uiState.currentMember,
                                tasks = uiState.tasks.filter { it.departmentId == uiState.currentMember?.departmentId || it.departmentId == "ops" },
                                members = uiState.members,
                                permissionRequests = uiState.permissionRequests,
                                onOpenCreateTask = { viewModel.setShowCreateTaskDialog(true) },
                                onNavigateToTeam = { viewModel.setSelectedTab(2) }
                            )
                            else -> StaffDashboard(
                                org = uiState.currentOrg,
                                currentMember = uiState.currentMember,
                                tasks = uiState.tasks.filter { it.assignedToMemberId == uiState.currentMember?.id || it.assignedToRole.equals("staff", ignoreCase = true) },
                                onUpdateTaskStatus = { id, status, prog -> viewModel.updateTaskStatus(id, status, prog) }
                            )
                        }
                    }
                    1 -> TaskDashboard(
                        tasks = uiState.tasks,
                        onUpdateStatus = { id, status, prog -> viewModel.updateTaskStatus(id, status, prog) },
                        onUpdatePriority = { id, prio -> viewModel.updateTaskPriority(id, prio) }
                    )
                    2 -> TeamScreen(
                        members = uiState.members,
                        departments = uiState.departments,
                        activeRole = uiState.activeRole,
                        onAddMember = { name, eid, email, pass, role, deptId, deptName, phone ->
                            viewModel.createMemberWithCredentials(name, eid, email, pass, role, deptId, deptName, phone)
                        },
                        onUpdateMember = { id, name, role, deptId, deptName, status ->
                            viewModel.updateMember(id, name, role, deptId, deptName, status)
                        },
                        onDeleteMember = { id, name -> viewModel.deleteMember(id, name) },
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                    3 -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }

                // Create Task Dialog
                if (uiState.showCreateTaskDialog) {
                    CreateTaskModal(
                        viewModel = viewModel,
                        members = uiState.members,
                        onDismiss = { viewModel.setShowCreateTaskDialog(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateTaskModal(
    viewModel: MainAppViewModel,
    members: List<com.example.data.model.Member>,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var selectedMemberIndex by remember { mutableIntStateOf(0) }
    var dueDate by remember { mutableStateOf("In 3 Days") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131D33),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFCBD5E1),
        title = { Text("Create & Delegate Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF26334D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Operational Details") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF26334D)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("low", "medium", "high", "urgent").forEach { p ->
                        Button(
                            onClick = { priority = p },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (priority == p) Color(0xFF4F46E5) else Color(0xFF1E293B),
                                contentColor = if (priority == p) Color.White else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(p.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (members.isNotEmpty()) {
                    Text("Delegate To Member:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                    val assigned = members.getOrNull(selectedMemberIndex)
                    Button(
                        onClick = { selectedMemberIndex = (selectedMemberIndex + 1) % members.size },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = Color.White),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${assigned?.displayName ?: "Unassigned"} (${assigned?.role?.uppercase()})",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val assigned = members.getOrNull(selectedMemberIndex)
                        viewModel.createTask(
                            title = title,
                            description = description,
                            departmentId = assigned?.departmentId ?: "ops",
                            priority = priority,
                            assignedMember = assigned,
                            startDate = "Today",
                            dueDate = dueDate,
                            estimatedHours = 4.0
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text("Create Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
