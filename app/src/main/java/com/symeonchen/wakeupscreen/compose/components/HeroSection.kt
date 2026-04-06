package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.theme.*

@Composable
fun HeroSection(
    isError: Boolean,
    statusText: String,
    tipsText: String,
    isToggleChecked: Boolean,
    isToggleVisible: Boolean,
    onToggleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradientColors = if (isError) {
        listOf(ErrorGradientStart, ErrorGradientMid, ErrorGradientEnd)
    } else {
        listOf(GradientStart, GradientMid, GradientEnd)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(Brush.linearGradient(gradientColors, start = Offset(0f, 0f), end = Offset(400f, 800f))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Decorative app label
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
            )

            Spacer(Modifier.height(10.dp))

            // Status text
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            if (isToggleVisible) {
                Spacer(Modifier.height(24.dp))

                // Toggle in frosted pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(40.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(40.dp))
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Switch(
                        checked = isToggleChecked,
                        onCheckedChange = { onToggleClick() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PinkAccent,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.9f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                            uncheckedBorderColor = Color.White.copy(alpha = 0.3f),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tips
            Text(
                text = tipsText,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
            )
        }
    }
}
