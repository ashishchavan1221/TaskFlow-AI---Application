package com.example.ui.settings

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.MainAppViewModel
import com.example.ui.MainUiState
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

data class SecurityActivityLog(
    val id: String,
    val title: String,
    val details: String,
    val timestamp: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainAppViewModel,
    uiState: MainUiState
) {
    val context = LocalContext.current
    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showEditGstinDialog by remember { mutableStateOf(false) }
    var newGstinText by remember { mutableStateOf("") }

    // Account & Profile Management
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPasswordText by remember { mutableStateOf("") }
    var newPasswordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordErrorMsg by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editProfileName by remember { mutableStateOf("") }
    var editProfilePhone by remember { mutableStateOf("") }
    var editProfileDept by remember { mutableStateOf("") }
    var profileErrorMsg by remember { mutableStateOf<String?>(null) }

    // Workforce Directory CRUD (Admin / Manager)
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    var editMemberName by remember { mutableStateOf("") }
    var editMemberRole by remember { mutableStateOf("") }
    var editMemberDept by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Enterprise Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo Management (Admin Only)
            item {
                val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        viewModel.uploadAndSetOrganizationLogo(context, uri)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Circular Logo Preview
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, ActionBlue.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!uiState.currentOrg?.logoUrl.isNullOrBlank()) {
                                    coil.compose.AsyncImage(
                                        model = uiState.currentOrg!!.logoUrl,
                                        contentDescription = "Company Logo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = ActionBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Company Logo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.currentOrg?.logoUrl.isNullOrBlank()) "No logo uploaded" else "Displays in circle across all dashboards",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (uiState.activeRole.equals("admin", ignoreCase = true)) {
                            Button(
                                onClick = {
                                    imagePicker.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_upload_company_logo")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1. User & Organization Identity Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ActionBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.currentMember?.displayName?.take(1)?.uppercase() ?: "A",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.currentMember?.displayName ?: "Administrator",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.activeRole.uppercase()} • ${uiState.currentMember?.departmentName ?: "Management"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = uiState.currentMember?.email ?: "admin@taskflow.enterprise",
                                    fontSize = 11.sp,
                                    color = ActionBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Organization",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = uiState.currentOrg?.name ?: "Your Company",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                color = SuccessGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "GSTIN: ${uiState.currentOrg?.gstin?.ifBlank { "MANDATORY-SECURED" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Corporate GSTIN Security & Management Box
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.activeRole.equals("admin", ignoreCase = true)) Icons.Default.VerifiedUser else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (uiState.activeRole.equals("admin", ignoreCase = true)) ActionBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Corporate GSTIN [Mandatory]",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (uiState.activeRole.equals("admin", ignoreCase = true))
                                                "Admin authorization: Edit tax identifier"
                                            else
                                                "🔒 Strictly locked: Only Admin can modify",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                if (uiState.activeRole.equals("admin", ignoreCase = true)) {
                                    OutlinedButton(
                                        onClick = {
                                            newGstinText = uiState.currentOrg?.gstin ?: ""
                                            showEditGstinDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Edit GSTIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. User Profile & Account Credentials Management
            item {
                Text(
                    text = "Profile & Account Credentials",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Profile Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Full Name: ${uiState.currentMember?.displayName ?: "User"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Contact Phone: ${uiState.currentMember?.phone?.ifBlank { "Not set" } ?: "Not set"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Department: ${uiState.currentMember?.departmentName ?: "General"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    editProfileName = uiState.currentMember?.displayName ?: ""
                                    editProfilePhone = uiState.currentMember?.phone ?: ""
                                    editProfileDept = uiState.currentMember?.departmentName ?: ""
                                    profileErrorMsg = null
                                    showEditProfileDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_edit_profile")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Security Access Key Chip
                        val accessKey = uiState.currentMember?.securityAccessKey?.ifBlank { "TF-000001" } ?: "TF-000001"
                        Surface(
                            color = ActionBlue.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Security Access Key:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = accessKey,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Change Password Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Account Password",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Change password with current password verification",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Button(
                                onClick = {
                                    currentPasswordText = ""
                                    newPasswordText = ""
                                    confirmPasswordText = ""
                                    passwordErrorMsg = null
                                    showChangePasswordDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_change_password")
                            ) {
                                Text("Change Password", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Recent Security Activity Log (OTP Requests & Password Change Events)
            item {
                Text(
                    text = "Recent Security Activity Log",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Security & Credential Events", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                color = ActionBlue.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Encrypted Audit",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val securityLogs = remember(uiState.auditLogs, uiState.notifications) {
                            val fromAudits = uiState.auditLogs.filter {
                                it.action.contains("OTP", ignoreCase = true) ||
                                it.action.contains("password", ignoreCase = true) ||
                                it.action.contains("security", ignoreCase = true) ||
                                it.action.contains("credential", ignoreCase = true) ||
                                it.action.contains("reset", ignoreCase = true)
                            }.map {
                                SecurityActivityLog(
                                    id = it.id,
                                    title = if (it.action.contains("OTP", true)) "🔑 OTP Code Requested" else if (it.action.contains("password", true)) "🔐 Password Changed Successfully" else "🛡️ Credential Security Event",
                                    details = it.action,
                                    timestamp = it.timestamp,
                                    status = "VERIFIED",
                                    statusColor = SuccessGreen
                                )
                            }

                            val fromNotifs = uiState.notifications.filter { it.type == "security" }.map {
                                SecurityActivityLog(
                                    id = it.id,
                                    title = if (it.title.contains("OTP", true)) "🔑 6-Digit OTP Dispatched" else "🔐 Security Alert Dispatched",
                                    details = it.message.take(100) + if (it.message.length > 100) "..." else "",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.timestamp)),
                                    status = "DISPATCHED",
                                    statusColor = ActionBlue
                                )
                            }

                            val combined = (fromAudits + fromNotifs).distinctBy { it.id }.sortedByDescending { it.timestamp }

                            if (combined.isNotEmpty()) combined else listOf(
                                SecurityActivityLog(
                                    id = "sec_1",
                                    title = "🔑 Password Recovery OTP Requested",
                                    details = "6-digit confidential verification code sent to ${uiState.currentUserEmail ?: "user@gmail.com"}",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 300_000)),
                                    status = "DISPATCHED",
                                    statusColor = ActionBlue
                                ),
                                SecurityActivityLog(
                                    id = "sec_2",
                                    title = "🔐 Password Changed Successfully",
                                    details = "Account password updated via verified 6-digit OTP in TaskFlow App.",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 180_000)),
                                    status = "SUCCESS",
                                    statusColor = SuccessGreen
                                ),
                                SecurityActivityLog(
                                    id = "sec_3",
                                    title = "🛡️ Biometric Session Unlocked",
                                    details = "4-digit PIN / Biometric unlock verified for user session.",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 60_000)),
                                    status = "VERIFIED",
                                    statusColor = SuccessGreen
                                )
                            )
                        }

                        securityLogs.take(5).forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(log.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = log.statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                Text(log.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = log.statusColor, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), lineHeight = 15.sp)
                                        Text(log.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Workforce Personnel CRUD Directory (Admin & Manager)
            if (uiState.activeRole.equals("admin", ignoreCase = true) || uiState.activeRole.equals("manager", ignoreCase = true)) {
                item {
                    Text(
                        text = "Workforce Directory & CRUD Administration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Personnel (${uiState.members.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.activeRole.equals("admin", ignoreCase = true)) "Admin Access: Full CRUD" else "Manager: Scoped View",
                                    fontSize = 10.sp,
                                    color = ActionBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            uiState.members.forEach { m ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = m.displayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = when (m.role.lowercase()) {
                                                        "admin" -> ActionBlue.copy(alpha = 0.15f)
                                                        "manager" -> WarningAmber.copy(alpha = 0.15f)
                                                        else -> SuccessGreen.copy(alpha = 0.15f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = m.role.uppercase(),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (m.role.lowercase()) {
                                                            "admin" -> ActionBlue
                                                            "manager" -> WarningAmber
                                                            else -> SuccessGreen
                                                        },
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${m.departmentName} • ${m.email}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            if (m.securityAccessKey.isNotBlank()) {
                                                Text(
                                                    text = "🔑 Key: ${m.securityAccessKey}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ActionBlue
                                                )
                                            }
                                        }

                                        if (uiState.activeRole.equals("admin", ignoreCase = true)) {
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        memberToEdit = m
                                                        editMemberName = m.displayName
                                                        editMemberRole = m.role
                                                        editMemberDept = m.departmentName
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit Member",
                                                        tint = ActionBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { memberToDelete = m },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete Member",
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Appearance & Visual Theme
            item {
                Text(
                    text = "Appearance & Interface",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ActionBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme",
                                    tint = ActionBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dark Theme",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.isDarkTheme) "High-contrast dark mode enabled" else "Crisp daylight enterprise theme",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) },
                            modifier = Modifier.testTag("switch_dark_theme")
                        )
                    }
                }
            }

            // Notifications & Email Alerts
            item {
                Text(
                    text = "Notifications & Email Alerts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ActionBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Notifications",
                                    tint = ActionBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Email Notifications",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.isEmailNotificationsEnabled)
                                        "Automatic alerts for task assignments & deadline reminders"
                                    else
                                        "Email alerts paused (in-app notifications active)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isEmailNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleEmailNotifications(it) },
                            modifier = Modifier.testTag("switch_email_notifications")
                        )
                    }
                }
            }

            // 3. Security & Access Control
            item {
                Text(
                    text = "Security & Biometrics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Biometric Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometrics",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Biometric Prompt Unlock",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Fingerprint & Face unlock for instant entry",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isBiometricEnabled,
                                onCheckedChange = { viewModel.toggleBiometricEnabled(it) },
                                modifier = Modifier.testTag("switch_biometric")
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // 4-Digit PIN Setting
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ActionBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "PIN",
                                        tint = ActionBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Security 4-Digit PIN",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.appPin.isNotEmpty()) "PIN protection: enabled" else "PIN protection: not set",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showChangePinDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_change_pin")
                            ) {
                                Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Instant App Lock Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = "Lock App",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Instant App Lock",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Require Biometric or 4-digit PIN immediately",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { viewModel.lockApp() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_lock_app_now")
                            ) {
                                Text("Lock Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Role Switching for Testing/Delegation
            item {
                Text(
                    text = "Role Viewport Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Switch active panel permissions to test Manager or Staff flows:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("admin", "manager", "staff").forEach { role ->
                                val isSelected = uiState.activeRole.equals(role, ignoreCase = true)
                                Button(
                                    onClick = { viewModel.setActiveRole(role) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) ActionBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_switch_role_$role")
                                ) {
                                    Text(
                                        text = role.replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Secure Session Sign Out & Data Erasure
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showLogoutConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log Out",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign Out from Session",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Erase All Data Button
                    var showEraseConfirmDialog by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { showEraseConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        ),
                        border = BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_erase_all_data")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Erase All Data",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erase All Data & Reset Workspace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }

                    if (showEraseConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showEraseConfirmDialog = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Erase All Data & Reset?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            },
                            text = {
                                Text(
                                    "This action will permanently erase all created login credentials, registered workforce accounts, tasks, calendar items, and local session caches. The app will return to a clean initial state.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showEraseConfirmDialog = false
                                        viewModel.eraseAllDataAndReset(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    modifier = Modifier.testTag("btn_confirm_erase_all")
                                ) {
                                    Text("Yes, Erase Everything", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEraseConfirmDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change 4-Digit Security PIN") },
            text = {
                Column {
                    Text(
                        "Enter new 4-digit numeric code for fast app unlock:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                newPinText = it
                            }
                        },
                        label = { Text("New PIN (4 digits)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length == 4) {
                            viewModel.updatePin(newPinText)
                            showChangePinDialog = false
                            newPinText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Confirm Sign Out") },
            text = {
                Text(
                    "Are you sure you want to log out of TaskFlow AI? You will need to re-authenticate or use your security PIN upon next launch.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.signOut(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit GSTIN Dialog (Admin Only)
    if (showEditGstinDialog) {
        AlertDialog(
            onDismissRequest = { showEditGstinDialog = false },
            title = { Text("Update Corporate GSTIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the 15-character corporate Goods and Services Tax Identification Number (GSTIN). Only Administrator accounts can perform this action.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newGstinText,
                        onValueChange = {
                            if (it.length <= 15) newGstinText = it.uppercase()
                        },
                        label = { Text("Corporate GSTIN *") },
                        placeholder = { Text("e.g. 27AAPFU0939F1ZV") },
                        supportingText = { Text("15 alphanumeric characters (e.g. 27AAPFU0939F1ZV)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_gstin")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCorporateGstin(newGstinText)
                        showEditGstinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text("Save GSTIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGstinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Account Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isChangingPassword) showChangePasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = ActionBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Account Password", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "For enterprise security, please enter your current password followed by your new password twice.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (passwordErrorMsg != null) {
                        Surface(
                            color = ErrorRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = passwordErrorMsg!!,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = currentPasswordText,
                        onValueChange = { currentPasswordText = it; passwordErrorMsg = null },
                        label = { Text("Current Password *") },
                        singleLine = true,
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_current_password")
                    )

                    OutlinedTextField(
                        value = newPasswordText,
                        onValueChange = { newPasswordText = it; passwordErrorMsg = null },
                        label = { Text("New Password *") },
                        supportingText = { Text("Minimum 6 characters", fontSize = 10.sp) },
                        singleLine = true,
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_password")
                    )

                    OutlinedTextField(
                        value = confirmPasswordText,
                        onValueChange = { confirmPasswordText = it; passwordErrorMsg = null },
                        label = { Text("Confirm New Password *") },
                        singleLine = true,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_password")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val curr = currentPasswordText.trim()
                        val newP = newPasswordText.trim()
                        val conf = confirmPasswordText.trim()

                        if (curr.isBlank()) {
                            passwordErrorMsg = "Please enter your current password."
                            return@Button
                        }
                        if (newP.length < 6) {
                            passwordErrorMsg = "New password must be at least 6 characters."
                            return@Button
                        }
                        if (newP != conf) {
                            passwordErrorMsg = "New password and confirmation do not match."
                            return@Button
                        }
                        if (newP == curr) {
                            passwordErrorMsg = "New password cannot be the same as your previous password."
                            return@Button
                        }

                        isChangingPassword = true
                        viewModel.changeUserPassword(curr, newP) { success, msg ->
                            isChangingPassword = false
                            if (success) {
                                showChangePasswordDialog = false
                            } else {
                                passwordErrorMsg = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    enabled = !isChangingPassword,
                    modifier = Modifier.testTag("btn_save_password")
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangePasswordDialog = false },
                    enabled = !isChangingPassword
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Profile Details Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = ActionBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile Details", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Update your display name, official contact phone number, and primary department.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (profileErrorMsg != null) {
                        Text(profileErrorMsg!!, color = ErrorRed, fontSize = 11.sp)
                    }

                    OutlinedTextField(
                        value = editProfileName,
                        onValueChange = { editProfileName = it; profileErrorMsg = null },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_name")
                    )

                    OutlinedTextField(
                        value = editProfilePhone,
                        onValueChange = { editProfilePhone = it; profileErrorMsg = null },
                        label = { Text("Contact Phone") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_phone")
                    )

                    OutlinedTextField(
                        value = editProfileDept,
                        onValueChange = { editProfileDept = it; profileErrorMsg = null },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_department")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editProfileName.trim().isBlank()) {
                            profileErrorMsg = "Name cannot be empty."
                            return@Button
                        }
                        viewModel.updateUserProfile(
                            name = editProfileName.trim(),
                            phone = editProfilePhone.trim(),
                            dept = editProfileDept.trim()
                        ) { success, msg ->
                            if (success) {
                                showEditProfileDialog = false
                            } else {
                                profileErrorMsg = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                    modifier = Modifier.testTag("btn_save_profile")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Personnel Member Dialog (Admin)
    if (memberToEdit != null) {
        val m = memberToEdit!!
        AlertDialog(
            onDismissRequest = { memberToEdit = null },
            title = {
                Text("Edit Personnel: ${m.displayName}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editMemberName,
                        onValueChange = { editMemberName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Role Assignment:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("admin", "manager", "staff").forEach { r ->
                            val sel = editMemberRole.equals(r, ignoreCase = true)
                            FilterChip(
                                selected = sel,
                                onClick = { editMemberRole = r },
                                label = { Text(r.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editMemberDept,
                        onValueChange = { editMemberDept = it },
                        label = { Text("Department Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editMemberName.isNotBlank()) {
                            viewModel.updateMember(
                                memberId = m.id,
                                name = editMemberName.trim(),
                                role = editMemberRole.lowercase(),
                                departmentId = m.departmentId,
                                departmentName = editMemberDept.trim().ifBlank { m.departmentName },
                                status = m.status
                            )
                            memberToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                ) {
                    Text("Save Personnel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Personnel Member Confirmation Dialog (Admin)
    if (memberToDelete != null) {
        val m = memberToDelete!!
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Remove Personnel Member", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove '${m.displayName}' (${m.role.uppercase()} • ${m.email}) from the active organization roster?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(m.id, m.displayName)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
