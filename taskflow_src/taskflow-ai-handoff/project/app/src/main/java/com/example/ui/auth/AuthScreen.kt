package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainAppViewModel
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen

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
    var regOtp by remember { mutableStateOf("") }
    val regOtpStage = viewModel.uiState.collectAsState().value.otpStage
    var regIndustry by remember { mutableStateOf("Technology & Enterprise") }

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
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavyPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "TaskFlow Logo",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "TaskFlow Enterprise",
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Role-Based Delegation & Workforce Orchestration",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
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
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In to TaskFlow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                } else {
                    // ---------------- REGISTER TAB ----------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Register Organization & Admin",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Your Full Name *") },
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
                        label = { Text("Official Work Email *") },
                        placeholder = { Text("e.g. vikram@nexusenterprise.com") },
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

                    // Role selector
                    Text(
                        text = "Designated Role:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "admin" to "Administrator",
                            "manager" to "Manager",
                            "staff" to "Staff"
                        ).forEach { (roleKey, label) ->
                            val isSelected = regRole == roleKey
                            Button(
                                onClick = { regRole = roleKey },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) ActionBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

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
                        label = { Text("GSTIN * (15 characters)") },
                        placeholder = { Text("15-character GSTIN") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ActionBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (regOtpStage == "awaiting_code") {
                        OutlinedTextField(
                            value = regOtp,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) regOtp = it },
                            label = { Text("6-digit code from your e-mail") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth().testTag("field_otp")
                        )
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.verifyOtpAndRegister(regFullName, regCompanyName, regEmail, regPhone, regPassword, regConfirm, regGstin, regOtp)
                            },
                            enabled = !isLoading && regOtp.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_auth_register")
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Verify & Create Company", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.requestRegistrationOtp(regFullName, regCompanyName, regEmail, regPhone, regPassword, regConfirm, regGstin) },
                            enabled = !isLoading
                        ) { Text("Resend code (once per minute)") }
                        androidx.compose.material3.TextButton(onClick = { regOtp = ""; viewModel.cancelRegistrationOtp() }) { Text("Edit details") }
                    } else {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.requestRegistrationOtp(regFullName, regCompanyName, regEmail, regPhone, regPassword, regConfirm, regGstin)
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
                                Text("Send verification code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🔒 Enterprise Session Guard: Sessions require a valid Firebase sign-in. PIN or biometric unlock is requested when you reopen the app.",
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
