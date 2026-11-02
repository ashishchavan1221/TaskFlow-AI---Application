package com.example.ui.team

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.Member
import com.example.data.model.TeamGroup
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    members: List<Member>,
    departments: List<Department>,
    groups: List<TeamGroup> = emptyList(),
    activeRole: String,
    onAddMember: (String, String, String, String, String, String, String, String, String) -> Unit,
    onUpdateMember: (String, String, String, String, String, String) -> Unit,
    onDeleteMember: (String, String) -> Unit,
    onOpenCreateGroup: () -> Unit = {},
    onOpenGroupChat: (TeamGroup) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Roster, 1 = Workgroups & Chat
    var showAddDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberEmail by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf("staff") }
    var newMemberEmpId by remember { mutableStateOf("") }
    var newMemberPassword by remember { mutableStateOf("") }
    var newMemberDept by remember { mutableStateOf("Operations & Management") }
    var newMemberAccessKey by remember { mutableStateOf("TF-%06d".format((100000..999999).random())) }
    var editingMember by remember { mutableStateOf<Member?>(null) }

    val isAdmin = activeRole.equals("admin", ignoreCase = true)
    val isManager = activeRole.equals("manager", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enterprise Team Directory", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "${members.size} Personnel • ${groups.size} Active Workgroups",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onOpenCreateGroup, modifier = Modifier.testTag("btn_top_create_group")) {
                            Icon(Icons.Default.GroupAdd, contentDescription = "Create Workgroup", tint = ActionBlue)
                        }
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
            // Segmented Tab Selector (Roster vs Workgroups)
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ActionBlue
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Personnel Roster (${members.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workgroups & Chat (${groups.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedSubTab == 0) {
                // Roster List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isAdmin) {
                        item {
                            Surface(
                                color = ActionBlue.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth().clickable { showAddDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(ActionBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Provision New Account", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text("Create Manager or Staff login credentials", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ActionBlue)
                                }
                            }
                        }
                    }

                    items(members) { member ->
                        MemberCard(
                            member = member,
                            onUpdate = onUpdateMember,
                            onDelete = onDeleteMember,
                            onEdit = { editingMember = it },
                            canDelete = isAdmin
                        )
                    }
                }
            } else {
                // Workgroups & Chat List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Workgroup Collaborations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text("1-2 Leading Managers + Staff Teams with Group Chat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    if (isAdmin) {
                                        Button(
                                            onClick = onOpenCreateGroup,
                                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("New Group", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (groups.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Groups, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("No Workgroups Created Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isAdmin) "Tap '+ New Group' to create a workgroup with 1-2 managers and multiple staff!" else "Admin has not formed any cross-functional workgroups yet.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(groups, key = { it.id }) { group ->
                            WorkgroupCard(
                                group = group,
                                onOpenChat = { onOpenGroupChat(group) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isManager) "Submit Staff Request to Admin" else "Provision Manager / Staff Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Aarav Sharma") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role selector
                    Text(
                        text = "Role:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("manager" to "Manager", "staff" to "Staff Associate").forEach { (role, label) ->
                            val isSelected = newMemberRole == role
                            Button(
                                onClick = {
                                    newMemberRole = role
                                    if (newMemberEmpId.isBlank() || newMemberEmpId.startsWith("MGR-") || newMemberEmpId.startsWith("STF-")) {
                                        val count = members.count { it.role.equals(role, ignoreCase = true) } + 1
                                        newMemberEmpId = if (role == "manager") "MGR-%03d".format(count) else "STF-%03d".format(count)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newMemberEmail,
                        onValueChange = { newMemberEmail = it },
                        label = { Text("Work Login Email *") },
                        placeholder = { Text("e.g. aarav@company.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMemberPassword,
                        onValueChange = { newMemberPassword = it },
                        label = { Text("Initial Login Password *") },
                        placeholder = { Text("e.g. Password@123") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMemberEmpId,
                        onValueChange = { newMemberEmpId = it },
                        label = { Text("Employee ID") },
                        placeholder = { Text(if (newMemberRole == "manager") "MGR-001" else "STF-001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newMemberDept,
                        onValueChange = { newMemberDept = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMemberAccessKey,
                            onValueChange = { newMemberAccessKey = it },
                            label = { Text("Unique Security Access Key *") },
                            placeholder = { Text("e.g. TF-784920") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = {
                            newMemberAccessKey = "TF-%06d".format((100000..999999).random())
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate Key", tint = ActionBlue)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemberName.isNotBlank() && newMemberEmail.isNotBlank()) {
                            val finalPass = newMemberPassword.ifBlank { "Password@123" }
                            val finalEmpId = newMemberEmpId.ifBlank {
                                val count = members.count { it.role.equals(newMemberRole, ignoreCase = true) } + 1
                                if (newMemberRole == "manager") "MGR-%03d".format(count) else "STF-%03d".format(count)
                            }
                            onAddMember(
                                newMemberName,
                                finalEmpId,
                                newMemberEmail,
                                finalPass,
                                newMemberRole,
                                if (newMemberRole == "manager") "ops" else "eng",
                                newMemberDept,
                                "",
                                newMemberAccessKey
                            )
                            showAddDialog = false
                            newMemberName = ""
                            newMemberEmpId = ""
                            newMemberEmail = ""
                            newMemberPassword = ""
                            newMemberAccessKey = "TF-%06d".format((100000..999999).random())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text(if (isManager) "Submit Request" else "Create Login Credentials", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Member Profile Dialog (CRUD for Admin)
    editingMember?.let { mem ->
        var editName by remember(mem) { mutableStateOf(mem.displayName) }
        var editRole by remember(mem) { mutableStateOf(mem.role) }
        var editDept by remember(mem) { mutableStateOf(mem.departmentName) }
        var editStatus by remember(mem) { mutableStateOf(mem.status) }

        AlertDialog(
            onDismissRequest = { editingMember = null },
            title = {
                Text("Edit Personnel Profile", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Employee ID: ${mem.employeeId}", fontSize = 12.sp, color = ActionBlue, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDept,
                        onValueChange = { editDept = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("manager" to "Manager", "staff" to "Staff").forEach { (r, label) ->
                            Button(
                                onClick = { editRole = r },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editRole == r) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (editRole == r) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onUpdateMember(
                                mem.id,
                                editName.trim(),
                                editRole,
                                if (editRole == "manager") "ops" else "eng",
                                editDept.trim(),
                                editStatus
                            )
                            editingMember = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMember = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WorkgroupCard(
    group: TeamGroup,
    onOpenChat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ActionBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${group.managerNames.size} Managers • ${group.staffNames.size} Staff Members",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Button(
                    onClick = onOpenChat,
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (group.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = group.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Leading Managers
            Text(
                text = "Leading Managers (${group.managerNames.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (group.managerNames.isEmpty()) {
                    Text("None Assigned", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                } else {
                    group.managerNames.forEach { mgrName ->
                        Surface(
                            color = ActionBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "👑 $mgrName",
                                color = ActionBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Staff Associates
            Text(
                text = "Staff Associates (${group.staffNames.size}):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (group.staffNames.isEmpty()) {
                    Text("None Assigned", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                } else {
                    group.staffNames.take(4).forEach { stfName ->
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "👤 $stfName",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (group.staffNames.size > 4) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+${group.staffNames.size - 4} more",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: Member,
    onUpdate: (String, String, String, String, String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    onEdit: (Member) -> Unit = {},
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
                val key = if (member.securityAccessKey.isNotBlank()) member.securityAccessKey else "TF-" + member.id.takeLast(6).uppercase()
                Text(
                    text = "🔑 Security Key: $key",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            if (canDelete) {
                IconButton(onClick = { onEdit(member) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = ActionBlue)
                }
            }
            if (canDelete && member.role != "admin") {
                IconButton(onClick = { onDelete(member.id, member.displayName) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                }
            }
        }
    }
}
