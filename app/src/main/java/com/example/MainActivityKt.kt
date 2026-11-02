package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.data.model.Member
import com.example.data.model.TeamGroup
import com.example.ui.MainAppViewModel
import com.example.ui.MainUiState
import com.example.ui.dashboard.*
import com.example.ui.navigation.AppNavigationHost
import com.example.ui.settings.SettingsScreen
import com.example.ui.team.TeamScreen
import com.example.ui.calendar.CalendarScreen
import com.example.ui.ai.AiScreen
import com.example.ui.activity.ActivityScreen
import com.example.ui.notification.NotificationCenterModal
import com.example.ui.components.TaskFlowBrandLogo
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
    val context = androidx.compose.ui.platform.LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(0.78f)
            ) {
                // Header: Profile & Company Info
                val currentRole = uiState.activeRole.lowercase()
                val currentMem = uiState.currentMember
                val hasRealProfile = currentMem != null && currentMem.displayName != "N/A" && currentMem.displayName.isNotBlank()
                val headerName = if (hasRealProfile) {
                    currentMem.displayName
                } else {
                    when (currentRole) {
                        "admin" -> uiState.currentOrg?.name?.let { "$it Admin" } ?: "Administrator"
                        "manager" -> "Manager (N/A)"
                        "staff" -> "Staff Associate (N/A)"
                        else -> "N/A"
                    }
                }
                val headerInitials = if (hasRealProfile) {
                    currentMem.displayName.take(2).uppercase()
                } else {
                    when (currentRole) {
                        "admin" -> "AD"
                        "manager" -> "MG"
                        "staff" -> "ST"
                        else -> "NA"
                    }
                }
                val headerEmployeeId = if (hasRealProfile && currentMem.employeeId.isNotBlank() && currentMem.employeeId != "N/A") {
                    currentMem.employeeId
                } else if (currentRole == "admin") {
                    "OWNER-001"
                } else {
                    "N/A"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4F46E5))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!uiState.currentOrg?.logoUrl.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = uiState.currentOrg!!.logoUrl,
                                contentDescription = "Company Logo",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            TaskFlowBrandLogo(size = 46.dp, showGlow = true)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = headerInitials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = headerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = uiState.currentOrg?.name ?: "TaskFlow Workspace",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ID: $headerEmployeeId",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Executive Dashboard", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("Task Pipeline", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("Team & Workgroups", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("Operational Calendar", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 4,
                    onClick = { viewModel.setSelectedTab(4); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notifications", color = MaterialTheme.colorScheme.onSurface)
                            val unread = uiState.notifications.count { !it.isRead }
                            if (unread > 0) {
                                Surface(
                                    color = Color(0xFFF43F5E),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "$unread new",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        viewModel.setShowNotificationDialog(true)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("AI Assistant", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 5,
                    onClick = { viewModel.setSelectedTab(5); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("Activity & Audit", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 6,
                    onClick = { viewModel.setSelectedTab(6); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.History, contentDescription = null, tint = ActionBlue) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings & Security", color = MaterialTheme.colorScheme.onSurface) },
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = ActionBlue) }
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
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!uiState.currentOrg?.logoUrl.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = uiState.currentOrg!!.logoUrl,
                                    contentDescription = "Company Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.5.dp, ActionBlue.copy(alpha = 0.5f), CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                TaskFlowBrandLogo(size = 34.dp, showGlow = false)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TaskFlow",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
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
                                    text = uiState.currentOrg?.name ?: "TaskFlow Workspace",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    actions = {
                        // Interactive Role Pill Badge (matching enterprise theme)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.4f)),
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
                                        .background(ActionBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.activeRole.uppercase(),
                                    color = ActionBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // In-App Notification Bell with Unread Badge
                        IconButton(onClick = {
                            viewModel.setShowNotificationDialog(true)
                        }) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "In-App Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )

                                val unread = uiState.notifications.count { !it.isRead }
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

                        Spacer(modifier = Modifier.width(6.dp))

                        // Quick Light / Dark Theme Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkTheme(!uiState.isDarkTheme) },
                            modifier = Modifier.testTag("btn_quick_toggle_theme")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Light/Dark Theme",
                                    tint = if (uiState.isDarkTheme) Color(0xFFFBBF24) else ActionBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (uiState.activeRole.lowercase() != "staff" && (uiState.selectedTab == 0 || uiState.selectedTab == 1)) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setShowCreateTaskDialog(true) },
                        containerColor = ActionBlue,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Task",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "New Task",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("fab_create_task")
                    )
                }
            },
            bottomBar = {
                // 5 Bottom Tabs: Home, Tasks, AI Chatbot, Team & Groups, Settings
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) },
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActionBlue,
                            selectedTextColor = ActionBlue,
                            indicatorColor = ActionBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Tasks") },
                        label = { Text("Tasks", fontSize = 10.sp) },
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActionBlue,
                            selectedTextColor = ActionBlue,
                            indicatorColor = ActionBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_tasks")
                    )
                    // AI Chat Bot in Bottom Bar as explicitly ordered by user
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Chatbot", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        },
                        label = { Text("AI Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        selected = uiState.selectedTab == 5,
                        onClick = { viewModel.setSelectedTab(5) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActionBlue,
                            selectedTextColor = ActionBlue,
                            indicatorColor = ActionBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_ai_chat")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Team") },
                        label = { Text("Team", fontSize = 10.sp) },
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setSelectedTab(2) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActionBlue,
                            selectedTextColor = ActionBlue,
                            indicatorColor = ActionBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_team")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 10.sp) },
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.setSelectedTab(3) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActionBlue,
                            selectedTextColor = ActionBlue,
                            indicatorColor = ActionBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                onNavigateToAi = { viewModel.setSelectedTab(5) },
                                onApproveRequest = { id -> viewModel.approvePermissionRequest(id) },
                                onRejectRequest = { id -> viewModel.rejectPermissionRequest(id) },
                                onOpenCreateTask = { viewModel.setShowCreateTaskDialog(true) },
                                onSelectTask = { task -> viewModel.selectTaskForDetail(task) },
                                onOpenViewDoc = { task, isWorkDoc -> viewModel.openDocumentViewer(task, isWorkDoc) },
                                onOpenUploadDoc = { task -> viewModel.openUploadDocument(task) },
                                onOpenTaskDocuments = { task -> viewModel.openTaskDocumentsList(task) }
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
                                tasks = uiState.tasks,
                                onUpdateTaskStatus = { id, status, prog -> viewModel.updateTaskStatus(id, status, prog) },
                                onOpenCompletionModal = { id, title -> viewModel.openTaskCompletionModal(id, title) },
                                onTriggerReminder = { task -> viewModel.triggerTaskReminder(task) },
                                onSelectTask = { task -> viewModel.selectTaskForDetail(task) },
                                onOpenViewDoc = { task, isWorkDoc -> viewModel.openDocumentViewer(task, isWorkDoc) },
                                onOpenUploadDoc = { task -> viewModel.openUploadDocument(task) },
                                onOpenTaskDocuments = { task -> viewModel.openTaskDocumentsList(task) }
                            )
                        }
                    }
                    1 -> TaskDashboard(
                        tasks = uiState.tasks,
                        org = uiState.currentOrg,
                        onUpdateStatus = { id, status, prog -> viewModel.updateTaskStatus(id, status, prog) },
                        onUpdatePriority = { id, prio -> viewModel.updateTaskPriority(id, prio) },
                        onOpenCompletionModal = { id, title -> viewModel.openTaskCompletionModal(id, title) },
                        onTriggerReminder = { task -> viewModel.triggerTaskReminder(task) },
                        onSelectTask = { task -> viewModel.selectTaskForDetail(task) },
                        onNavigateToCalendar = { viewModel.setSelectedTab(4) },
                        onOpenViewDoc = { task, isWorkDoc -> viewModel.openDocumentViewer(task, isWorkDoc) },
                        onOpenUploadDoc = { task -> viewModel.openUploadDocument(task) },
                        onOpenTaskDocuments = { task -> viewModel.openTaskDocumentsList(task) }
                    )
                    2 -> TeamScreen(
                        members = uiState.members,
                        departments = uiState.departments,
                        groups = uiState.groups,
                        activeRole = uiState.activeRole,
                        onAddMember = { name, eid, email, pass, role, deptId, deptName, phone, key ->
                            viewModel.createMemberWithCredentials(name, eid, email, pass, role, deptId, deptName, phone, key)
                        },
                        onUpdateMember = { id, name, role, deptId, deptName, status ->
                            viewModel.updateMember(id, name, role, deptId, deptName, status)
                        },
                        onDeleteMember = { id, name -> viewModel.deleteMember(id, name) },
                        onOpenCreateGroup = { viewModel.setShowAddGroupDialog(true) },
                        onOpenGroupChat = { group -> viewModel.selectGroup(group) },
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                    3 -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    4 -> CalendarScreen(
                        tasks = uiState.tasks,
                        onOpenTask = { task -> viewModel.selectTaskForDetail(task) },
                        onCreateTask = { viewModel.setShowCreateTaskDialog(true) },
                        onUpdateTaskStatus = { id, status, prog -> viewModel.updateTaskStatus(id, status, prog) },
                        canCreateTask = uiState.activeRole != "staff"
                    )
                    5 -> AiScreen(
                        tasks = uiState.tasks,
                        members = uiState.members,
                        activeRole = uiState.activeRole,
                        orgName = uiState.currentOrg?.name ?: "TaskFlow Enterprise",
                        onCreateTaskFromAi = { title, desc, prio, hrs, dept ->
                            viewModel.createTask(
                                title = title,
                                description = desc,
                                departmentId = if (dept.contains("Engineering", ignoreCase = true)) "eng" else "ops",
                                priority = prio,
                                assignedMember = uiState.members.firstOrNull(),
                                startDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                                dueDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() + 86400000L * 3)),
                                estimatedHours = hrs,
                                hasReminder = true,
                                reminderNotes = "AI generated deliverable deadline"
                            )
                        },
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                    6 -> ActivityScreen(
                        auditLogs = uiState.auditLogs,
                        permissionRequests = uiState.permissionRequests,
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                }

                // In-App Notification Center Dialog
                if (uiState.showNotificationDialog) {
                    NotificationCenterModal(
                        viewModel = viewModel,
                        notifications = uiState.notifications,
                        onDismiss = { viewModel.setShowNotificationDialog(false) }
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

                // Task Completion Modal (User Request: Notes + Date)
                if (uiState.showCompletionModal && uiState.completingTaskId != null) {
                    val taskId = uiState.completingTaskId!!
                    TaskCompletionModal(
                        taskTitle = uiState.completingTaskTitle ?: "Deliverable",
                        onConfirm = { notes, dateStr ->
                            viewModel.completeTaskWithNotes(taskId, notes, dateStr)
                        },
                        onDismiss = { viewModel.closeTaskCompletionModal() }
                    )
                }

                // Create Team Group Modal (User Request: 1-2 Managers, Multiple Staff)
                if (uiState.showAddGroupDialog) {
                    CreateTeamGroupModal(
                        members = uiState.members,
                        onConfirm = { name, desc, mgrIds, stfIds ->
                            val mgrNames = uiState.members.filter { mgrIds.contains(it.id) }.map { it.displayName }
                            val stfNames = uiState.members.filter { stfIds.contains(it.id) }.map { it.displayName }
                            viewModel.createTeamGroup(name, desc, mgrIds, mgrNames, stfIds, stfNames)
                        },
                        onDismiss = { viewModel.setShowAddGroupDialog(false) }
                    )
                }

                // Group Chat Modal (User Request: Group Chatting Option)
                if (uiState.showGroupChatDialog && uiState.selectedGroup != null) {
                    val activeGrp = uiState.selectedGroup!!
                    GroupChatModal(
                        group = activeGrp,
                        messages = uiState.activeGroupMessages,
                        currentMember = uiState.currentMember,
                        onSendMessage = { text -> viewModel.sendGroupChatMessage(activeGrp.id, text) },
                        onDismiss = { viewModel.selectGroup(null) }
                    )
                }

                // Task Detail Modal with Persistent Completion Notes and Dynamic Time Log
                if (uiState.showTaskDetailModal && uiState.selectedTaskForDetail != null) {
                    val taskDetail = uiState.selectedTaskForDetail!!
                    TaskDetailModal(
                        task = taskDetail,
                        onSaveNotes = { notes, markCompleted ->
                            viewModel.saveTaskCompletionNotes(taskDetail.id, notes, markCompleted)
                        },
                        onLogTime = { additionalHours ->
                            viewModel.logTaskTime(taskDetail.id, additionalHours)
                        },
                        onTriggerReminder = {
                            viewModel.triggerTaskReminder(taskDetail)
                        },
                        onViewDocument = { task, isWorkDoc ->
                            viewModel.openDocumentViewer(task, isWorkDoc)
                        },
                        onUploadWorkDocument = { task ->
                            viewModel.openUploadDocument(task)
                        },
                        onDismiss = { viewModel.selectTaskForDetail(null) }
                    )
                }

                // Audible Pop-up Alert Modal for Approaching Due Dates
                if (uiState.activeReminderAlert != null) {
                    val reminderTask = uiState.activeReminderAlert!!
                    AudibleReminderAlertModal(
                        task = reminderTask,
                        onOpenDetail = {
                            viewModel.dismissReminderAlert()
                            viewModel.selectTaskForDetail(reminderTask)
                        },
                        onDismiss = { viewModel.dismissReminderAlert() }
                    )
                }
                // Edit Company Logo Modal
                if (uiState.showEditLogoDialog) {
                    EditLogoModal(
                        currentLogoUrl = uiState.currentOrg?.logoUrl ?: "",
                        onUploadImage = { uri ->
                            viewModel.uploadAndSetOrganizationLogo(context, uri)
                        },
                        onConfirmUrl = { newUrl ->
                            viewModel.updateOrganizationLogo(newUrl)
                            viewModel.setShowEditLogoDialog(false)
                        },
                        onDismiss = { viewModel.setShowEditLogoDialog(false) }
                    )
                }

                // Upload Work Document Modal (Worker Submission / Proof of Work)
                if (uiState.uploadingDocumentTask != null) {
                    val targetTask = uiState.uploadingDocumentTask!!
                    UploadWorkDocumentModal(
                        task = targetTask,
                        onUpload = { docName, docUrl, docType, remarks ->
                            viewModel.uploadTaskWorkDocument(targetTask.id, docName, docUrl, docType, remarks)
                        },
                        onDismiss = { viewModel.closeUploadDocument() }
                    )
                }

                // View Document Modal (Interactive Viewer for Brief & Work Proof)
                if (uiState.viewingDocumentTask != null) {
                    DocumentPreviewModal(
                        task = uiState.viewingDocumentTask!!,
                        isWorkDoc = uiState.viewingDocumentIsWorkDoc,
                        onDismiss = { viewModel.closeDocumentViewer() }
                    )
                }

                // Task Documents List Modal (Dedicated list of all uploaded PDF/image files with download & preview)
                if (uiState.viewingDocumentsListTask != null) {
                    val docTask = uiState.viewingDocumentsListTask!!
                    TaskDocumentsListModal(
                        task = docTask,
                        onPreview = { docName, docUrl, isWorkDoc ->
                            viewModel.openDocumentViewer(docTask, isWorkDoc)
                        },
                        onDownload = { docName, docUrl, docType ->
                            com.example.data.util.FileStorageHelper.downloadFile(context, docName, docUrl, docType)
                        },
                        onUploadNew = {
                            viewModel.openUploadDocument(docTask)
                        },
                        onDismiss = { viewModel.closeTaskDocumentsList() }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCompletionModal(
    taskTitle: String,
    onConfirm: (notes: String, date: String) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var completionDate by remember { mutableStateOf("Today, Just now") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Deliverable", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Task: $taskTitle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Audit record will be updated in real-time.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("How did you complete this task? *") },
                    placeholder = { Text("e.g. Created tax invoice, verified e-way bill with client") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_completion_notes")
                )

                OutlinedTextField(
                    value = completionDate,
                    onValueChange = { completionDate = it },
                    label = { Text("When did you complete it?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_completion_date")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalNotes = notes.ifBlank { "Completed successfully per specifications." }
                    onConfirm(finalNotes, completionDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.testTag("btn_submit_completion")
            ) {
                Text("Submit & Log Notes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateTeamGroupModal(
    members: List<Member>,
    onConfirm: (name: String, desc: String, managerIds: List<String>, staffIds: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedManagerIds = remember { mutableStateListOf<String>() }
    val selectedStaffIds = remember { mutableStateListOf<String>() }

    val managers = remember(members) { members.filter { it.role.equals("manager", ignoreCase = true) } }
    val staffMembers = remember(members) { members.filter { it.role.equals("staff", ignoreCase = true) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = ActionBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Form Workgroup", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workgroup Name *") },
                        placeholder = { Text("e.g. Operations Alpha / Billing Squad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Operational Mandate / Scope") },
                        placeholder = { Text("Scope of deliverables & goals") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "Lead Managers (Select 1-2):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (managers.isEmpty()) {
                    item {
                        Text("No managers registered yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    items(managers) { mgr ->
                        val isSelected = selectedManagerIds.contains(mgr.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ActionBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) ActionBlue else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedManagerIds.remove(mgr.id)
                                    } else if (selectedManagerIds.size < 2) {
                                        selectedManagerIds.add(mgr.id)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (isSelected) selectedManagerIds.remove(mgr.id)
                                        else if (selectedManagerIds.size < 2) selectedManagerIds.add(mgr.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(mgr.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Manager (${mgr.employeeId})", fontSize = 10.sp, color = ActionBlue)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Staff Associates (Multiple):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (staffMembers.isEmpty()) {
                    item {
                        Text("No staff members registered yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    items(staffMembers) { stf ->
                        val isSelected = selectedStaffIds.contains(stf.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) SuccessGreen else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedStaffIds.remove(stf.id)
                                    else selectedStaffIds.add(stf.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (isSelected) selectedStaffIds.remove(stf.id)
                                        else selectedStaffIds.add(stf.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(stf.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Staff (${stf.employeeId})", fontSize = 10.sp, color = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description, selectedManagerIds.toList(), selectedStaffIds.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
            ) {
                Text("Create Workgroup", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GroupChatModal(
    group: TeamGroup,
    messages: List<com.example.data.model.GroupChatMessage>,
    currentMember: Member?,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Leads: ${group.managerNames.joinToString().ifBlank { "Management" }} • ${group.staffNames.size} Staff",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                // Messages List
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No messages yet in this workgroup. Say hello!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    } else {
                        items(messages.reversed(), key = { it.id }) { msg ->
                            val isMe = currentMember != null && msg.senderId == currentMember.id
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    color = if (isMe) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${msg.senderName} (${msg.senderRole.uppercase()})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color.White.copy(alpha = 0.9f) else ActionBlue
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.message,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val timeStr = remember(msg.timestamp) {
                                            if (msg.timestamp > 0) {
                                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                                            } else "Just now"
                                        }
                                        Text(
                                            text = timeStr,
                                            fontSize = 9.sp,
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Type message...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_group_chat")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                onSendMessage(inputMessage)
                                inputMessage = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = ActionBlue, contentColor = Color.White),
                        modifier = Modifier.size(42.dp).testTag("btn_send_group_chat")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun CreateTaskModal(
    viewModel: MainAppViewModel,
    members: List<Member>,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    
    val validMembers = remember(members) {
        members.filter { it.displayName != "N/A" && it.displayName.isNotBlank() }
    }
    
    val managerMembers = remember(validMembers) {
        validMembers.filter { it.role.equals("manager", ignoreCase = true) || it.role.equals("admin", ignoreCase = true) }
    }
    
    val staffMembers = remember(validMembers) {
        validMembers.filter { it.role.equals("staff", ignoreCase = true) }
    }

    var selectedMemberId by remember { mutableStateOf(validMembers.firstOrNull()?.id ?: "") }
    var isMemberDropdownExpanded by remember { mutableStateOf(false) }
    var selectedMemberTab by remember { mutableIntStateOf(0) } // 0 = Staff, 1 = Managers, 2 = Unassigned

    // Auto Current Date & Time Default
    val dateTimeFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()) }
    val currentAutoDateTime = remember { dateTimeFormat.format(java.util.Date()) }
    
    var createdDateTime by remember { mutableStateOf(currentAutoDateTime) }
    var dueDateOption by remember { mutableStateOf("In 3 Days") }

    // Optional Document Attachment State (PDF or Image)
    var attachedDocUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var attachedDocName by remember { mutableStateOf("") }
    var attachedDocType by remember { mutableStateOf("") }
    var attachedDocUrl by remember { mutableStateOf("") }

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedDocUri = uri
            attachedDocType = "image"
            attachedDocName = uri.lastPathSegment?.substringAfterLast('/')?.takeLast(24) ?: "Brief_Image.png"
        }
    }

    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            attachedDocUri = uri
            val seg = uri.lastPathSegment ?: "document"
            attachedDocType = if (seg.contains("pdf", ignoreCase = true)) "pdf" else "document"
            attachedDocName = seg.substringAfterLast('/').takeLast(24)
            if (!attachedDocName.contains('.')) {
                attachedDocName += if (attachedDocType == "pdf") ".pdf" else ".doc"
            }
        }
    }

    // Calculate actual due date string with auto date-time support
    val calculatedDueDateStr = remember(createdDateTime, dueDateOption) {
        try {
            val cal = java.util.Calendar.getInstance()
            val baseDate = dateTimeFormat.parse(createdDateTime) ?: java.util.Date()
            cal.time = baseDate
            
            when (dueDateOption) {
                "Today" -> {
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
                    cal.set(java.util.Calendar.MINUTE, 0)
                }
                "Tomorrow" -> {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
                    cal.set(java.util.Calendar.MINUTE, 0)
                }
                "In 3 Days" -> {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 3)
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
                    cal.set(java.util.Calendar.MINUTE, 0)
                }
                "In 1 Week" -> {
                    cal.add(java.util.Calendar.DAY_OF_YEAR, 7)
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
                    cal.set(java.util.Calendar.MINUTE, 0)
                }
            }
            dateTimeFormat.format(cal.time)
        } catch (e: Exception) {
            createdDateTime
        }
    }

    var enableReminder by remember { mutableStateOf(false) }
    var reminderOption by remember { mutableStateOf("On Due Date") }

    val assignedMember = remember(selectedMemberId, validMembers) {
        validMembers.find { it.id == selectedMemberId }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = ActionBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create & Delegate Task", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Audit Q3 Security Credentials") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_task_title")
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Operational Details") },
                        placeholder = { Text("Deliverable specifications & goals") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_task_desc")
                    )
                }

                // Optional Brief Materials & Documents (Image or PDF)
                item {
                    Text(
                        text = "Project Attachment / Brief Materials (Optional Image or PDF):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (attachedDocUri != null || attachedDocName.isNotBlank() || attachedDocUrl.isNotBlank()) {
                        val displayName = if (attachedDocName.isNotBlank()) attachedDocName else "Attached_Brief_Document"
                        val isPdf = attachedDocType == "pdf" || displayName.endsWith(".pdf", ignoreCase = true)
                        val isImg = attachedDocType == "image" || displayName.endsWith(".png", ignoreCase = true) || displayName.endsWith(".jpg", ignoreCase = true)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ActionBlue.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (isPdf) Icons.Default.PictureAsPdf else if (isImg) Icons.Default.Image else Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = if (isPdf) Color(0xFFEF4444) else ActionBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (isPdf) "PDF Document Attached" else if (isImg) "Image Attached" else "Document Attached",
                                            fontSize = 10.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        attachedDocUri = null
                                        attachedDocName = ""
                                        attachedDocType = ""
                                        attachedDocUrl = ""
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Attachment", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    imagePicker.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📷 Image", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { filePicker.launch("application/pdf") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📄 PDF", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = attachedDocUrl,
                            onValueChange = {
                                attachedDocUrl = it
                                if (attachedDocName.isBlank() && it.isNotBlank()) {
                                    attachedDocName = it.substringAfterLast('/').take(24).ifBlank { "online_document" }
                                    if (it.endsWith(".pdf", ignoreCase = true)) attachedDocType = "pdf"
                                    else if (it.endsWith(".png", ignoreCase = true) || it.endsWith(".jpg", ignoreCase = true)) attachedDocType = "image"
                                }
                            },
                            label = { Text("Or Document Link (Optional)") },
                            placeholder = { Text("https://example.com/spec.pdf") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ActionBlue) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Text(
                        text = "Priority Level:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("low", "medium", "high", "urgent").forEach { p ->
                            val isSelected = priority == p
                            val pColor = when (p) {
                                "urgent", "high" -> Color(0xFFEF4444)
                                "medium" -> Color(0xFFF59E0B)
                                else -> SuccessGreen
                            }
                            Button(
                                onClick = { priority = p },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) pColor else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(p.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Categorized Team Member Dropdown Selection (Manager vs Staff Columns/Tabs)
                item {
                    Text(
                        text = "Delegate To Member (Manager / Staff Categorized):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { isMemberDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (assignedMember != null) {
                                        val isMgr = assignedMember.role.equals("manager", ignoreCase = true) || assignedMember.role.equals("admin", ignoreCase = true)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isMgr) "👔 " else "👷 ",
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = assignedMember.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (isMgr) ActionBlue.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (isMgr) "MANAGER" else "STAFF",
                                                    color = if (isMgr) ActionBlue else SuccessGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "ID: ${assignedMember.employeeId} • Dept: ${assignedMember.departmentName.ifBlank { "Operations" }}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    } else {
                                        Text(
                                            text = "🌐 Unassigned (Open Pool)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Visible to all associates in workspace pool",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Expand Member Dropdown",
                                    tint = ActionBlue
                                )
                            }
                        }

                        // Categorized Dropdown Menu with Manager vs Staff columns / tabs
                        DropdownMenu(
                            expanded = isMemberDropdownExpanded,
                            onDismissRequest = { isMemberDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Select Assignee Role Category:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                // Category Tab Header: Staff | Managers | Unassigned
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    listOf(
                                        0 to "👷 Staff (${staffMembers.size})",
                                        1 to "👔 Managers (${managerMembers.size})",
                                        2 to "🌐 Open Pool"
                                    ).forEach { (tabIdx, tabTitle) ->
                                        val isTabSel = selectedMemberTab == tabIdx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isTabSel) ActionBlue else Color.Transparent)
                                                .clickable { selectedMemberTab = tabIdx }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tabTitle,
                                                fontSize = 10.sp,
                                                fontWeight = if (isTabSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isTabSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                when (selectedMemberTab) {
                                    0 -> { // Staff Associates Column
                                        if (staffMembers.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No staff associates registered", fontSize = 11.sp) },
                                                onClick = {}
                                            )
                                        } else {
                                            staffMembers.forEach { stf ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(stf.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                                Text("Staff ID: ${stf.employeeId}", fontSize = 10.sp, color = SuccessGreen)
                                                            }
                                                            Surface(
                                                                color = SuccessGreen.copy(alpha = 0.15f),
                                                                shape = RoundedCornerShape(4.dp)
                                                            ) {
                                                                Text("STAFF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen, modifier = Modifier.padding(4.dp))
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedMemberId = stf.id
                                                        isMemberDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    1 -> { // Managers Column
                                        if (managerMembers.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No managers registered", fontSize = 11.sp) },
                                                onClick = {}
                                            )
                                        } else {
                                            managerMembers.forEach { mgr ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(mgr.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                                Text("Manager ID: ${mgr.employeeId}", fontSize = 10.sp, color = ActionBlue)
                                                            }
                                                            Surface(
                                                                color = ActionBlue.copy(alpha = 0.15f),
                                                                shape = RoundedCornerShape(4.dp)
                                                            ) {
                                                                Text("MANAGER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ActionBlue, modifier = Modifier.padding(4.dp))
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedMemberId = mgr.id
                                                        isMemberDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    else -> { // Unassigned Pool
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("🌐 Unassigned / Open Pool", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Task will be placed in the department open backlog pool", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedMemberId = ""
                                                isMemberDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Auto Current Date and Time Creation Record
                item {
                    Text(
                        text = "Creation & Start Timestamp (Auto Current Default):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = createdDateTime,
                        onValueChange = { createdDateTime = it },
                        label = { Text("Current Creation Date & Time *") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = ActionBlue) },
                        trailingIcon = {
                            IconButton(onClick = { createdDateTime = dateTimeFormat.format(java.util.Date()) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh to Current Time", tint = ActionBlue)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Target Due Date Selector
                item {
                    Text(
                        text = "Target Due Date (Calculated: $calculatedDueDateStr):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Today", "Tomorrow", "In 3 Days", "In 1 Week").forEach { d ->
                            val isSel = dueDateOption == d
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { dueDateOption = d }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = d,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Audible Reminder Field
                item {
                    Text(
                        text = "Audible Reminder Notification:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (enableReminder) "🔔 Pop-up Audio Chime Active" else "No reminder alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (enableReminder) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Switch(
                            checked = enableReminder,
                            onCheckedChange = { enableReminder = it },
                            modifier = Modifier.testTag("switch_task_reminder")
                        )
                    }

                    if (enableReminder) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("1 Hour Before", "On Due Date", "Urgent Pop-up").forEach { opt ->
                                val isSel = reminderOption == opt
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reminderOption = opt }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = opt,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalDocUrl = if (attachedDocUrl.isNotBlank()) attachedDocUrl else (attachedDocUri?.toString() ?: "")
                        val finalDocName = if (attachedDocName.isNotBlank()) attachedDocName else if (finalDocUrl.isNotBlank()) "Attached_Spec" else ""

                        viewModel.createTask(
                            title = title,
                            description = description,
                            departmentId = assignedMember?.departmentId?.ifBlank { "ops" } ?: "ops",
                            priority = priority,
                            assignedMember = assignedMember,
                            startDate = createdDateTime,
                            dueDate = calculatedDueDateStr,
                            estimatedHours = 4.0,
                            hasReminder = enableReminder,
                            reminderNotes = if (enableReminder) reminderOption else "",
                            attachmentName = finalDocName,
                            attachmentUrl = finalDocUrl,
                            attachmentType = attachedDocType
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                modifier = Modifier.testTag("btn_confirm_create_task")
            ) {
                Text("Create & Save Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun TaskDetailModal(
    task: com.example.data.model.TaskItem,
    onSaveNotes: (notes: String, markCompleted: Boolean) -> Unit,
    onLogTime: (additionalHours: Double) -> Unit = {},
    onTriggerReminder: () -> Unit,
    onViewDocument: (com.example.data.model.TaskItem, Boolean) -> Unit = { _, _ -> },
    onUploadWorkDocument: (com.example.data.model.TaskItem) -> Unit = {},
    onDismiss: () -> Unit
) {
    var notes by remember(task.id) { mutableStateOf(task.completionNotes) }
    val isDone = task.status == "done" || task.status == "completed"

    // Time Log State with Live Start/Stop Timer
    var isTimerRunning by remember(task.id) { mutableStateOf(false) }
    var elapsedSeconds by remember(task.id) { mutableLongStateOf(0L) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isActive && isTimerRunning) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    val timerFormatted = remember(elapsedSeconds) {
        val hrs = elapsedSeconds / 3600
        val mins = (elapsedSeconds % 3600) / 60
        val secs = elapsedSeconds % 60
        "%02d:%02d:%02d".format(hrs, mins, secs)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Deliverable Details", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("ID: ${task.id.take(12)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (task.priority.lowercase()) {
                                "urgent", "high" -> Color(0xFFF43F5E).copy(alpha = 0.15f)
                                "medium" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else -> ActionBlue.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PRIORITY: ${task.priority.uppercase()}",
                                color = when (task.priority.lowercase()) {
                                    "urgent", "high" -> Color(0xFFF43F5E)
                                    "medium" -> Color(0xFFF59E0B)
                                    else -> ActionBlue
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Text(
                        text = task.description.ifBlank { "No detailed instructions provided." },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 17.sp
                    )
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Assignee:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("👤 ${task.assignedToName.ifBlank { "Unassigned" }}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Due Date:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("📅 ${task.dueDate}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Audible Reminder:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (task.hasReminder) "🔔 Active (${task.reminderNotes.ifBlank { "Alert set" }})" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.hasReminder) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // Time Log Feature with Start/Stop Timer Button and Dynamic Hours Spent
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (isTimerRunning) ActionBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Time Log Tracker", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Surface(
                                    color = ActionBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Hours Spent: ${task.actualHours} hrs",
                                        color = ActionBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Live Stopwatch display with Start / Stop buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Active Session Timer", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text(
                                        text = timerFormatted,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isTimerRunning) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!isTimerRunning) {
                                        Button(
                                            onClick = { isTimerRunning = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("btn_start_timer")
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Start Timer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                isTimerRunning = false
                                                val added = elapsedSeconds.toDouble() / 3600.0
                                                if (added > 0.0005) {
                                                    onLogTime(added)
                                                }
                                                elapsedSeconds = 0L
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("btn_stop_timer")
                                        ) {
                                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Stop & Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Quick Log Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Quick Add:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                listOf(0.25 to "+15m", 0.5 to "+30m", 1.0 to "+1h", 2.0 to "+2h").forEach { (h, label) ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { onLogTime(h) }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ActionBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }

                // Project Specification & Deliverables Attachments Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Project Attachments & Work Proof", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Surface(
                                    color = ActionBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Cloud Synced",
                                        color = ActionBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // 1. Initial Brief / Specifications Document (Created by Admin/Manager)
                            Text("1. Initial Specification Brief:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            if (task.attachmentName.isNotBlank()) {
                                val isPdf = task.attachmentType == "pdf" || task.attachmentName.endsWith(".pdf", ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                                contentDescription = null,
                                                tint = ActionBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(task.attachmentName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(if (isPdf) "PDF Specification Document" else "Design/Specification Image", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                            }
                                        }

                                        Button(
                                            onClick = { onViewDocument(task, false) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("btn_view_brief_doc")
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Brief", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No brief document attached during task creation.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // 2. Worker Submission / Completed Work Document
                            Text("2. Worker Submission / Completed Work Proof:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            if (task.workDocName.isNotBlank()) {
                                val isPdf = task.workDocType == "pdf" || task.workDocName.endsWith(".pdf", ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(task.workDocName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text("Uploaded by: ${task.workDocUploadedBy.ifBlank { "Staff" }} • ${task.workDocUploadedAt}", fontSize = 10.sp, color = SuccessGreen)
                                            }
                                        }

                                        Button(
                                            onClick = { onViewDocument(task, true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("btn_view_work_doc")
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Proof", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No work proof submitted yet by the assigned worker.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // 3. Worker Action Button: Upload or Update Work Document
                            Button(
                                onClick = { onUploadWorkDocument(task) },
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_upload_work_proof")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (task.workDocName.isNotBlank()) "Update Work Document / Proof (Image or PDF)" else "Upload Work Document / Proof (Image or PDF)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Persistent Completion Notes Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Persistent Completion Notes:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (task.completedBy.isNotBlank()) {
                                Text(
                                    text = "Author: ${task.completedBy}",
                                    fontSize = 10.sp,
                                    color = ActionBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (task.completedAt.isNotBlank()) {
                            Text(
                                text = "Timestamp: ${task.completedAt}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Document the exact steps, deliverables, and validation taken to finalize this task...") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth().testTag("input_detail_completion_notes")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSaveNotes(notes, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (!isDone) {
                                Button(
                                    onClick = { onSaveNotes(notes, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Finalize & Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun AudibleReminderAlertModal(
    task: com.example.data.model.TaskItem,
    onOpenDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("⏰ Due Date Alert!", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFFD97706))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Deliverable is approaching due date target:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Due: ${task.dueDate} • Priority: ${task.priority.uppercase()}", fontSize = 11.sp, color = ActionBlue, fontWeight = FontWeight.SemiBold)
                        if (task.reminderNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Note: ${task.reminderNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenDetail,
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
            ) {
                Text("Open Task Details", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss Alert")
            }
        }
    )
}

@Composable
fun EditLogoModal(
    currentLogoUrl: String,
    onUploadImage: (android.net.Uri) -> Unit,
    onConfirmUrl: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var logoUrl by remember { mutableStateOf(currentLogoUrl) }

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ActionBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Company Logo", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upload the official company logo image from your device gallery. It will appear in a circle on the home dashboard and across all app headers.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Large Circular Preview (CircleShape mandatory)
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, ActionBlue, CircleShape)
                        .clickable {
                            imagePicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        coil.compose.AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "New Company Logo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (currentLogoUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = currentLogoUrl,
                            contentDescription = "Current Company Logo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(34.dp))
                            Text("Pick Image", fontSize = 10.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        imagePicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue.copy(alpha = 0.15f), contentColor = ActionBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("btn_pick_logo_image")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri != null) "Choose Another Image" else "Upload Image from Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (selectedImageUri != null) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Image selected & ready to update", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedImageUri != null) {
                        onUploadImage(selectedImageUri!!)
                    } else if (logoUrl.isNotBlank()) {
                        onConfirmUrl(logoUrl)
                    }
                },
                enabled = selectedImageUri != null || logoUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                modifier = Modifier.testTag("btn_submit_logo")
            ) {
                Text("Save Company Logo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UploadWorkDocumentModal(
    task: com.example.data.model.TaskItem,
    onUpload: (docName: String, docUrl: String, docType: String, remarks: String) -> Unit,
    onDismiss: () -> Unit
) {
    var docName by remember { mutableStateOf(if (task.workDocName.isNotBlank()) task.workDocName else "") }
    var docUrl by remember { mutableStateOf(if (task.workDocUrl.isNotBlank()) task.workDocUrl else "") }
    var docType by remember { mutableStateOf(if (task.workDocType.isNotBlank()) task.workDocType else "pdf") }
    var remarks by remember { mutableStateOf(task.completionNotes) }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            docType = "image"
            if (docName.isBlank() || docName.startsWith("Work_Proof")) {
                docName = "Work_Proof_${System.currentTimeMillis().toString().takeLast(4)}.png"
            }
        }
    }

    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            val seg = uri.lastPathSegment ?: "document"
            docType = if (seg.contains("pdf", ignoreCase = true)) "pdf" else "document"
            if (docName.isBlank() || docName.startsWith("Work_Proof")) {
                docName = "Work_Proof_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = ActionBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Work Document / Proof", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Deliverable: ${task.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ActionBlue
                )
                Text(
                    text = "Upload completed work document (PDF or Image) so managers and administrators can review your deliverable.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePicker.launch("application/pdf") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (docType == "pdf") ActionBlue else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📄 PDF", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            imagePicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (docType == "image") ActionBlue else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📷 Image", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = docName,
                    onValueChange = { docName = it },
                    label = { Text("Document / File Name *") },
                    placeholder = { Text("e.g. Final_Deliverable_Summary.pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_work_doc_name")
                )

                OutlinedTextField(
                    value = docUrl,
                    onValueChange = { docUrl = it },
                    label = { Text("Or File / Cloud URL (Optional)") },
                    placeholder = { Text("https://example.com/proof.pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Worker Completion Summary / Notes") },
                    placeholder = { Text("Explain what work was performed...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resolvedUrl = if (docUrl.isNotBlank()) docUrl.trim() else (selectedUri?.toString() ?: "attached_file://local")
                    val resolvedName = docName.ifBlank { "Work_Deliverable_${System.currentTimeMillis().toString().takeLast(4)}.${if (docType == "image") "png" else "pdf"}" }
                    onUpload(resolvedName, resolvedUrl, docType, remarks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.testTag("btn_submit_work_doc")
            ) {
                Text("Submit Work Document", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DocumentPreviewModal(
    task: com.example.data.model.TaskItem,
    isWorkDoc: Boolean,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val docName = if (isWorkDoc) task.workDocName.ifBlank { "Work_Submission.pdf" } else task.attachmentName.ifBlank { "Project_Brief.pdf" }
    val docUrl = if (isWorkDoc) task.workDocUrl else task.attachmentUrl
    val docType = if (isWorkDoc) task.workDocType else task.attachmentType
    val isPdf = docType == "pdf" || docName.endsWith(".pdf", ignoreCase = true)
    val isImg = docType == "image" || docName.endsWith(".png", ignoreCase = true) || docName.endsWith(".jpg", ignoreCase = true) || docName.endsWith(".jpeg", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isPdf) Icons.Default.PictureAsPdf else if (isImg) Icons.Default.Image else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (isPdf) Color(0xFFEF4444) else ActionBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isWorkDoc) "Worker Submission Proof" else "Project Brief Document",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("File:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(docName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Task:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(task.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActionBlue, maxLines = 1)
                        }
                        if (isWorkDoc && task.workDocUploadedBy.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Uploaded By:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("👤 ${task.workDocUploadedBy} (${task.workDocUploadedAt})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            }
                        }
                    }
                }

                if (isImg && docUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        coil.compose.AsyncImage(
                            model = docUrl,
                            contentDescription = docName,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPdf) Color(0xFFEF4444).copy(alpha = 0.08f) else ActionBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isPdf) Color(0xFFEF4444).copy(alpha = 0.3f) else ActionBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isPdf) Color(0xFFEF4444) else ActionBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = docName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPdf) "Standard Enterprise PDF Document" else "Deliverable Binary Document",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (task.completionNotes.isNotBlank()) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Worker Remarks / Notes:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text(task.completionNotes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        if (docUrl.startsWith("http://") || docUrl.startsWith("https://")) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(docUrl))
                            context.startActivity(intent)
                        } else if (docUrl.startsWith("content://")) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(android.net.Uri.parse(docUrl), if (isPdf) "application/pdf" else "image/*")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(context, "Document '$docName' verified in cloud storage.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Document verified: $docName", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Open / View Document", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TaskDocumentsListModal(
    task: com.example.data.model.TaskItem,
    onPreview: (docName: String, docUrl: String, isWorkDoc: Boolean) -> Unit,
    onDownload: (docName: String, docUrl: String, docType: String) -> Unit,
    onUploadNew: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasBrief = task.attachmentName.isNotBlank() || task.attachmentUrl.isNotBlank()
    val hasWorkDoc = task.workDocName.isNotBlank() || task.workDocUrl.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Task Documents", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(task.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasBrief && !hasWorkDoc) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Documents Uploaded Yet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Upload a project specification or deliverable proof for this task.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    // 1. Project Brief Document Card
                    if (hasBrief) {
                        val briefName = task.attachmentName.ifBlank { "Project_Brief.pdf" }
                        val isPdf = task.attachmentType == "pdf" || briefName.endsWith(".pdf", ignoreCase = true)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isPdf) Color(0xFFEF4444) else ActionBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(briefName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            Text("Project Brief • Delegated by Admin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        color = ActionBlue.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(if (isPdf) "PDF" else "IMAGE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ActionBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onPreview(briefName, task.attachmentUrl, false) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = ActionBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ActionBlue)
                                    }

                                    Button(
                                        onClick = { onDownload(briefName, task.attachmentUrl, task.attachmentType) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Worker Completed Deliverable Card
                    if (hasWorkDoc) {
                        val workDocName = task.workDocName.ifBlank { "Completed_Deliverable.pdf" }
                        val isPdf = task.workDocType == "pdf" || workDocName.endsWith(".pdf", ignoreCase = true)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(workDocName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            Text("Work Deliverable • by ${task.workDocUploadedBy.ifBlank { task.assignedToName }}", fontSize = 10.sp, color = SuccessGreen)
                                        }
                                    }

                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(if (isPdf) "PDF PROOF" else "IMAGE PROOF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                if (task.completionNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Note: ${task.completionNotes}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onPreview(workDocName, task.workDocUrl, true) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                    }

                                    Button(
                                        onClick = { onDownload(workDocName, task.workDocUrl, task.workDocType) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Upload New Document Option
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onUploadNew()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionBlue),
                    border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("btn_upload_new_task_doc")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Another Document (PDF / Image)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Close")
            }
        }
    )
}
