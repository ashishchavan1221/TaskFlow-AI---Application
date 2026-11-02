package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Premium cyber-neon TaskFlow emblem badge rendering the official TF hexagonal mark.
 */
@Composable
fun TaskFlowBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showGlow: Boolean = true
) {
    val cornerRadius = size * 0.24f
    val shape = RoundedCornerShape(cornerRadius)

    val gradientBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00D2FF), // Cyan neon
            Color(0xFF7C3AED), // Purple
            Color(0xFFD500F9)  // Magenta neon
        )
    )

    Surface(
        modifier = modifier
            .size(size)
            .then(
                if (showGlow) Modifier.shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = Color(0xFF00D2FF),
                    spotColor = Color(0xFFD500F9)
                ) else Modifier
            ),
        shape = shape,
        color = Color(0xFF101726),
        border = BorderStroke(1.5.dp, gradientBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.04f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_taskflow_logo),
                contentDescription = "TaskFlow Emblem",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
