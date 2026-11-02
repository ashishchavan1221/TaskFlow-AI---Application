package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.*

@Composable
fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 6) {
            OutlinedTextField(
                value = otp.getOrNull(i)?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                        val newOtp = otp.toCharArray().toMutableList().apply {
                            if (i < size) this[i] = newValue.firstOrNull() ?: ' '
                            else add(newValue.firstOrNull() ?: ' ')
                        }.joinToString("").trim()
                        onOtpChange(newOtp)
                        if (newValue.isNotEmpty() && i < 5) focusRequesters[i + 1].requestFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .focusRequester(focusRequesters[i])
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyDown && it.key == Key.Backspace && otp.getOrNull(i) == null && i > 0) {
                            focusRequesters[i - 1].requestFocus()
                            true
                        } else false
                    }
                    .testTag("otp_box_$i"),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 18.sp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                enabled = !isLoading
            )
        }
    }
}
