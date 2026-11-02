package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.delay
import java.util.Locale
import com.example.ui.MainAppViewModel
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.components.TaskFlowBrandLogo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainAppViewModel,
    isLoading: Boolean,
    errorMessage: String?
) {
    // 0: Sign In, 1: Register Account
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    // Sign In State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginRoleHint by remember { mutableStateOf("admin") } // admin, manager, staff

    // Register State
    var regFullName by remember { mutableStateOf("") }
    var regCompanyName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("admin") }
    var regGstin by remember { mutableStateOf("") }
    var regConfirm by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }

    var regIndustry by remember { mutableStateOf("Technology & Enterprise") }
    
    // Logo State
    var logoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var regLogoUrl by remember { mutableStateOf("") }
    val logoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri -> logoUri = uri }

    // WP2 Recovery State
    var showOwnerResetDialog by remember { mutableStateOf(false) }
    var ownerResetStep by remember { mutableIntStateOf(1) }
    var ownerResetEmail by remember { mutableStateOf("") }
    var ownerResetCode by remember { mutableStateOf("") }
    var ownerResetToken by remember { mutableStateOf("") }
    var ownerResetNewPass by remember { mutableStateOf("") }
    var ownerResetConfirmPass by remember { mutableStateOf("") }
    var ownerResetStatusMsg by remember { mutableStateOf<String?>(null) }
    var ownerResetErrorMsg by remember { mutableStateOf<String?>(null) }

    var showStaffPassDialog by remember { mutableStateOf(false) }
    var staffRecoveryEmpId by remember { mutableStateOf("") }
    var staffPassStatusMsg by remember { mutableStateOf<String?>(null) }

    var showStaffIdDialog by remember { mutableStateOf(false) }
    var staffRecoveryName by remember { mutableStateOf("") }
    var staffRecoveryEmail by remember { mutableStateOf("") }
    var staffIdStatusMsg by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Enterprise Brand Badge
        TaskFlowBrandLogo(
            size = 80.dp,
            showGlow = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "TaskFlow .AI",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Task Management Application",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Welcome! Let's get things done.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ActionBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Unified Segmented Navigation Control
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Sign In Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedAuthTab == 0) ActionBlue else Color.Transparent)
                        .clickable {
                            selectedAuthTab = 0
                            viewModel.clearMessages()
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_auth_signin"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAuthTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Register Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedAuthTab == 1) ActionBlue else Color.Transparent)
                        .clickable {
                            selectedAuthTab = 1
                            viewModel.clearMessages()
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_auth_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAuthTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Error Feedback Alert
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main Auth Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedAuthTab == 0) {
                    // ---------------- SIGN IN TAB ----------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Secure Session",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }

                    // Role fast selector for sign-in
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "admin" to "Admin",
                            "manager" to "Manager",
                            "staff" to "Staff"
                        ).forEach { (roleKey, label) ->
                            val isSelected = loginRoleHint == roleKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ActionBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable {
                                        loginRoleHint = roleKey
                                        loginIdentifier = ""
                                        loginPassword = ""
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ActionBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("Work Email or Employee ID") },
                        placeholder = { Text("e.g. admin@nexusenterprise.com or MGR-101") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_id")
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ActionBlue) },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (loginIdentifier.contains("@")) {
                                viewModel.signInWithEmail(loginIdentifier, loginPassword)
                            } else {
                                viewModel.signInWithStaffCredentials(loginIdentifier, loginPassword)
                            }
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_password")
                    )

                    // Portal-specific credential recovery links
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (loginRoleHint == "admin") {
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = {
                                    ownerResetStep = 1
                                    ownerResetEmail = if (loginIdentifier.contains("@")) loginIdentifier else ""
                                    ownerResetErrorMsg = null
                                    ownerResetStatusMsg = null
                                    showOwnerResetDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Forgot Password?", fontSize = 12.sp, color = ActionBlue)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    staffRecoveryEmpId = loginIdentifier
                                    staffPassStatusMsg = null
                                    showStaffPassDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Forgot Password?", fontSize = 12.sp, color = ActionBlue)
                            }
                            TextButton(
                                onClick = {
                                    staffRecoveryName = ""
                                    staffRecoveryEmail = ""
                                    staffIdStatusMsg = null
                                    showStaffIdDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Forgot Employee ID?", fontSize = 12.sp, color = ActionBlue)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (loginIdentifier.contains("@")) {
                                viewModel.signInWithEmail(loginIdentifier, loginPassword)
                            } else {
                                viewModel.signInWithStaffCredentials(loginIdentifier, loginPassword)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_auth_submit")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In to TaskFlow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                } else {
                    // ---------------- REGISTER TAB (ADMIN & COMPANY ONLY) ----------------
                    // Logo Picker (Centered, Optional)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { logoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUri != null) {
                                    coil.compose.AsyncImage(
                                        model = logoUri,
                                        contentDescription = "Company Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(32.dp))
                                }
                            }
                            Text("Tap to Choose Photo (Optional)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                        }
                    }

                    OutlinedTextField(
                        value = regLogoUrl,
                        onValueChange = { regLogoUrl = it },
                        label = { Text("Or Paste Company Logo URL (Optional)") },
                        placeholder = { Text("https://example.com/logo.png") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ActionBlue) },
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_logo_url")
                    )
                    
                    // Decorative Element
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                         Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = ActionBlue.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Register Company & Admin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = ActionBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ADMIN ACCESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActionBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Notice explaining that Manager/Staff cannot register here
                    Surface(
                        color = ActionBlue.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ActionBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ActionBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Registration is exclusively for Company Administrators. Managers and Staff accounts are created by the Administrator inside the Admin Portal.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Admin Full Name *") },
                        placeholder = { Text("e.g. Vikram Sharma") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regCompanyName,
                        onValueChange = { regCompanyName = it },
                        label = { Text("Company Legal Name *") },
                        placeholder = { Text("Your company name") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Official Admin Work Email *") },
                        placeholder = { Text("e.g. admin@nexusenterprise.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    


                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Create Strong Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ActionBlue) },
                        visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                Icon(
                                    imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regConfirm,
                        onValueChange = { regConfirm = it },
                        label = { Text("Confirm password *") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        label = { Text("Company phone *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = regGstin,
                        onValueChange = {
                            if (it.length <= 15) regGstin = it.uppercase()
                        },
                        label = { Text("Corporate GSTIN * [Mandatory]") },
                        placeholder = { Text("e.g. 27AAPFU0939F1ZV") },
                        supportingText = {
                            Text("15-digit alphanumeric corporate tax identifier (e.g. 27AAPFU0939F1ZV)", fontSize = 10.sp)
                        },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_gstin")
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val resolvedLogo = if (regLogoUrl.isNotBlank()) regLogoUrl.trim() else (logoUri?.toString() ?: "")
                            viewModel.registerDirectly(regFullName, regCompanyName, regEmail, regPhone, regPassword, regConfirm, regGstin, resolvedLogo)
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_auth_register")
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.DomainAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Register & Create Company", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(16.dp))



        // 1. Password Recovery OTP Wizard (Step 1: Backend Email Check -> Step 2: 60s OTP Timer -> Step 3: New Password)
        if (showOwnerResetDialog) {
            PasswordRecoveryOtpModal(
                initialEmail = ownerResetEmail.ifBlank { if (loginIdentifier.contains("@")) loginIdentifier else "admin@taskflow.com" },
                onDismiss = { showOwnerResetDialog = false },
                onSendOtp = { emailToVerify, callback ->
                    viewModel.sendAdminForgotOtp(emailToVerify) { ok, msg, otpCode ->
                        callback(ok, msg, otpCode)
                    }
                },
                onVerifyAndReset = { targetEmail, otpInput, expectedOtp, otpTimestampMs, newPass, confirmPass, callback ->
                    viewModel.verifyAdminOtpAndResetPassword(
                        email = targetEmail,
                        otpInput = otpInput,
                        expectedOtp = expectedOtp,
                        otpTimestampMs = otpTimestampMs,
                        newPassword = newPass,
                        confirmPassword = confirmPass
                    ) { ok, msg ->
                        callback(ok, msg)
                    }
                },
                onSuccessReset = { resetEmail ->
                    showOwnerResetDialog = false
                    loginIdentifier = resetEmail
                    loginPassword = ""
                    viewModel.clearMessages()
                }
            )
        }

        // 2. Staff & Manager Forgot Password Dialog -> Submits Request to Admin Panel
        if (showStaffPassDialog) {
            AlertDialog(
                onDismissRequest = { showStaffPassDialog = false },
                title = { Text("Request Password Reset from Admin", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter your Employee ID or Email. A credential reset request will be sent directly to the Admin Panel for ID/Password regeneration.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = staffRecoveryEmpId,
                            onValueChange = { staffRecoveryEmpId = it.uppercase() },
                            label = { Text("Employee ID / Email (e.g. MGR-001 or STF-001)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        staffPassStatusMsg?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.submitCredentialResetRequest(
                                requesterName = if (loginRoleHint == "manager") "Manager ($staffRecoveryEmpId)" else "Staff ($staffRecoveryEmpId)",
                                requesterEmpId = staffRecoveryEmpId,
                                email = if (staffRecoveryEmpId.contains("@")) staffRecoveryEmpId else "${staffRecoveryEmpId.lowercase()}@taskflow.enterprise",
                                role = loginRoleHint,
                                reason = "Forgot password / ID reset requested for $staffRecoveryEmpId"
                            ) { ok, msg ->
                                staffPassStatusMsg = msg
                                if (ok) {
                                    staffRecoveryEmpId = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                    ) {
                        Text("Submit Request to Admin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStaffPassDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // 3. Staff & Manager Forgot Employee ID Dialog -> Submits Request to Admin Panel
        if (showStaffIdDialog) {
            AlertDialog(
                onDismissRequest = { showStaffIdDialog = false },
                title = { Text("Request Employee ID Recovery from Admin", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter your registered Full Name and Email to request Employee ID and Password regeneration from Admin.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = staffRecoveryName,
                            onValueChange = { staffRecoveryName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = staffRecoveryEmail,
                            onValueChange = { staffRecoveryEmail = it },
                            label = { Text("Registered Company Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        staffIdStatusMsg?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.submitCredentialResetRequest(
                                requesterName = staffRecoveryName,
                                requesterEmpId = "RECOVER-ID",
                                email = staffRecoveryEmail,
                                role = loginRoleHint,
                                reason = "Forgot Employee ID & Password recovery request for $staffRecoveryName ($staffRecoveryEmail)"
                            ) { ok, msg ->
                                staffIdStatusMsg = msg
                                if (ok) {
                                    staffRecoveryName = ""
                                    staffRecoveryEmail = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
                    ) {
                        Text("Submit Request to Admin")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStaffIdDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun PasswordRecoveryOtpModal(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSendOtp: (email: String, callback: (Boolean, String, String) -> Unit) -> Unit,
    onVerifyAndReset: (email: String, otpInput: String, expectedOtp: String, otpTimestampMs: Long, newPass: String, confirmPass: String, callback: (Boolean, String) -> Unit) -> Unit,
    onSuccessReset: (email: String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP, 3: Password, 4: Congrats
    var email by remember { mutableStateOf(initialEmail) }
    var generatedOtp by remember { mutableStateOf("") }
    var otpTimestampMs by remember { mutableLongStateOf(0L) }
    var timerSeconds by remember { mutableIntStateOf(300) } // 5 minutes = 300s
    var isTimerActive by remember { mutableStateOf(false) }

    val focusRequesters = remember { List(6) { FocusRequester() } }

    var otpDigit1 by remember { mutableStateOf("") }
    var otpDigit2 by remember { mutableStateOf("") }
    var otpDigit3 by remember { mutableStateOf("") }
    var otpDigit4 by remember { mutableStateOf("") }
    var otpDigit5 by remember { mutableStateOf("") }
    var otpDigit6 by remember { mutableStateOf("") }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isTimerActive, timerSeconds) {
        if (isTimerActive && timerSeconds > 0) {
            delay(1000L)
            timerSeconds -= 1
        } else if (timerSeconds == 0) {
            isTimerActive = false
        }
    }

    // Auto-advance focus to first digit box when Step 2 opens
    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            try { focusRequesters[0].requestFocus() } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentStep == 4) SuccessGreen.copy(alpha = 0.15f) else ActionBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (currentStep == 4) Icons.Default.CheckCircle else Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = if (currentStep == 4) SuccessGreen else ActionBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Password Recovery", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = when (currentStep) {
                                    1 -> "Step 1 of 3: Email Verification"
                                    2 -> "Step 2 of 3: 5-Min Security OTP"
                                    3 -> "Step 3 of 3: New Password"
                                    else -> "Complete: Account Secured 🎉"
                                },
                                fontSize = 11.sp,
                                color = if (currentStep == 4) SuccessGreen else ActionBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) { index ->
                        val stepNum = index + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (stepNum <= currentStep) ActionBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                errorMessage?.let { err ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                successMessage?.let { msg ->
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                when (currentStep) {
                    1 -> {
                        Text(
                            text = "Enter your registered email address. The backend system will verify registration before sending a 5-minute security OTP.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("Registered Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ActionBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                onSendOtp(email) { ok, msg, otp ->
                                    isLoading = false
                                    if (ok) {
                                        generatedOtp = otp
                                        otpTimestampMs = System.currentTimeMillis()
                                        timerSeconds = 300 // 5 Minutes
                                        isTimerActive = true
                                        currentStep = 2
                                        successMessage = "OTP dispatched to $email via TaskFlow Security."
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying Account...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify Email & Send 5-Min OTP", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("TaskFlow Enterprise Verification Email", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                                        Text("From: TaskFlow Enterprise Support <support@taskflow.com>", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = SuccessGreen.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Subject: TaskFlow Enterprise Verification Code",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dear Valued Member,\n\nYou have requested a 6-digit verification code to reset your account password.\n\nYour 6-digit OTP code has been dispatched to $email.\n\nThis code is valid for 5 minutes. Please keep this code secure and confidential.\n\nThank you for using TaskFlow Enterprise.\n\nBest regards,\nTaskFlow Security & Operations Team",
                                    fontSize = 10.5.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "✓ Anti-Spam Verified • Delivered to Primary Inbox",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 5-Minute Timer Display
                        Surface(
                            color = if (timerSeconds > 0) ActionBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (timerSeconds > 0) ActionBlue.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (timerSeconds > 0) ActionBlue else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (timerSeconds > 0) "OTP Expiry Countdown (5 Min)" else "OTP Expired!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timerSeconds > 0) ActionBlue else MaterialTheme.colorScheme.error
                                    )
                                }

                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", timerSeconds / 60, timerSeconds % 60),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (timerSeconds > 0) ActionBlue else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 6-Digit Auto-Focus Advancing Input Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val digits = listOf(otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6)
                            val setters = listOf(
                                { v: String -> otpDigit1 = v },
                                { v: String -> otpDigit2 = v },
                                { v: String -> otpDigit3 = v },
                                { v: String -> otpDigit4 = v },
                                { v: String -> otpDigit5 = v },
                                { v: String -> otpDigit6 = v }
                            )

                            for (i in 0 until 6) {
                                OutlinedTextField(
                                    value = digits[i],
                                    onValueChange = { input ->
                                        if (input.length == 1 && input.all { it.isDigit() }) {
                                            setters[i](input)
                                            if (i < 5) {
                                                focusRequesters[i + 1].requestFocus()
                                            } else {
                                                // All 6 digits filled -> Auto verify
                                                val autoEntered = otpDigit1 + otpDigit2 + otpDigit3 + otpDigit4 + otpDigit5 + input
                                                if (timerSeconds > 0 && autoEntered == generatedOtp) {
                                                    errorMessage = null
                                                    successMessage = "✅ OTP verified! Enter new password."
                                                    currentStep = 3
                                                }
                                            }
                                        } else if (input.isEmpty()) {
                                            setters[i]("")
                                            if (i > 0) {
                                                focusRequesters[i - 1].requestFocus()
                                            }
                                        } else if (input.length == 6 && input.all { it.isDigit() }) {
                                            otpDigit1 = input.getOrNull(0)?.toString() ?: ""
                                            otpDigit2 = input.getOrNull(1)?.toString() ?: ""
                                            otpDigit3 = input.getOrNull(2)?.toString() ?: ""
                                            otpDigit4 = input.getOrNull(3)?.toString() ?: ""
                                            otpDigit5 = input.getOrNull(4)?.toString() ?: ""
                                            otpDigit6 = input.getOrNull(5)?.toString() ?: ""
                                            focusRequesters[5].requestFocus()
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .focusRequester(focusRequesters[i])
                                        .onKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyUp && event.key == Key.Backspace) {
                                                if (digits[i].isEmpty() && i > 0) {
                                                    focusRequesters[i - 1].requestFocus()
                                                    true
                                                } else false
                                            } else false
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (timerSeconds > 0) "Didn't receive code?" else "Code expired. Request a new OTP.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    onSendOtp(email) { ok, msg, otp ->
                                        isLoading = false
                                        if (ok) {
                                            generatedOtp = otp
                                            otpTimestampMs = System.currentTimeMillis()
                                            timerSeconds = 300 // 5 Minutes
                                            isTimerActive = true
                                            otpDigit1 = ""; otpDigit2 = ""; otpDigit3 = ""; otpDigit4 = ""; otpDigit5 = ""; otpDigit6 = ""
                                            successMessage = "New 5-min OTP code dispatched to $email."
                                            try { focusRequesters[0].requestFocus() } catch (_: Exception) {}
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                },
                                enabled = timerSeconds == 0 && !isLoading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (timerSeconds == 0) ActionBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val enteredOtp = otpDigit1 + otpDigit2 + otpDigit3 + otpDigit4 + otpDigit5 + otpDigit6
                                if (timerSeconds == 0) {
                                    errorMessage = "❌ OTP has expired (5 minute limit). Please click Resend OTP."
                                    return@Button
                                }
                                if (enteredOtp.length < 6) {
                                    errorMessage = "Please enter the complete 6-digit OTP code."
                                    return@Button
                                }
                                if (enteredOtp != generatedOtp) {
                                    errorMessage = "❌ Incorrect 6-digit OTP code. Please check and try again."
                                    return@Button
                                }

                                errorMessage = null
                                successMessage = "✅ OTP verified successfully! Enter your new password."
                                currentStep = 3
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify 6-Digit OTP Code", fontWeight = FontWeight.Bold)
                        }
                    }

                    3 -> {
                        Text(
                            text = "Create a new secure password for $email. Password must be at least 8 characters long with letters and numbers.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password (8+ chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ActionBlue) },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm New Password") },
                            leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = ActionBlue) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = {
                                if (newPassword.isBlank() || confirmPassword.isBlank()) {
                                    errorMessage = "Please enter and confirm your new password."
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    errorMessage = "❌ Passwords do not match."
                                    return@Button
                                }
                                if (newPassword.length < 8 || newPassword.none { it.isLetter() } || newPassword.none { it.isDigit() }) {
                                    errorMessage = "❌ Password must be at least 8 characters with letters and numbers."
                                    return@Button
                                }

                                val enteredOtp = otpDigit1 + otpDigit2 + otpDigit3 + otpDigit4 + otpDigit5 + otpDigit6
                                isLoading = true
                                errorMessage = null

                                onVerifyAndReset(email, enteredOtp, generatedOtp, otpTimestampMs, newPassword, confirmPassword) { ok, msg ->
                                    isLoading = false
                                    if (ok) {
                                        currentStep = 4 // Move to Congratulations Step!
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Updating Password...")
                            } else {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save New Password & Finish", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    4 -> {
                        // STEP 4: Congratulations 🎉
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Congratulations! 🎉",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Password Changed Successfully",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Your TaskFlow Enterprise account ($email) password has been updated securely.",
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "A confirmation security alert email was dispatched to your Gmail inbox. Passwords and credentials remain strictly encrypted and confidential.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Button(
                                onClick = {
                                    onSuccessReset(email)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Return to Login & Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
