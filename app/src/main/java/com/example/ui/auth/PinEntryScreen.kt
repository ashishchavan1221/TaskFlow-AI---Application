package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActionBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.components.TaskFlowBrandLogo

@Composable
fun PinEntryScreen(
    onPinVerified: () -> Unit,
    onTriggerBiometric: () -> Unit,
    isBiometricAvailable: Boolean = true,
    onVerify: ((String) -> Boolean)? = null,
    securityNotice: String? = null,
    onSignOut: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Trigger startup biometric authentication immediately upon screen entry
    LaunchedEffect(Unit) {
        if (isBiometricAvailable) {
            onTriggerBiometric()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            TaskFlowBrandLogo(
                size = 76.dp,
                showGlow = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "TaskFlow Secure Access",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Authenticate via biometric prompt or enter your 4-digit PIN fallback to access your enterprise dashboard.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (securityNotice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = ActionBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = securityNotice,
                        fontSize = 12.sp,
                        color = ActionBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4-PIN Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) ActionBlue else Color.Transparent)
                            .border(
                                2.dp,
                                if (isFilled) ActionBlue else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = ActionBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Enter your 4-digit PIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ActionBlue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            )

            keyRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    when (key) {
                                        "bio" -> ActionBlue.copy(alpha = 0.15f)
                                        "del" -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                    CircleShape
                                )
                                .clickable {
                                    when (key) {
                                        "bio" -> onTriggerBiometric()
                                        "del" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                if (enteredPin.length == 4) {
                                                    if (onVerify?.invoke(enteredPin) == true) {
                                                        onPinVerified()
                                                    } else {
                                                        errorMessage = securityNotice ?: "Incorrect PIN."
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("btn_pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            when (key) {
                                "bio" -> {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Prompt Unlock",
                                        tint = ActionBlue,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                "del" -> {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (onSignOut != null) {
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("btn_switch_account_lock")
                ) {
                    Text(
                        text = "Sign in with different credentials",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
