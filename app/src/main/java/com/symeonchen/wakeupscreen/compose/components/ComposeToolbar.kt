package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.compose.theme.WakeUpTheme

@Composable
fun ComposeToolbar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    WakeUpTheme.colors.heroGradient,
                    start = Offset(0f, 0f),
                    end = Offset(600f, 0f),
                )
            )
            // Let the gradient bleed under the status bar, but keep the back
            // button and title below it so they aren't hidden by the camera
            // cutout / unreachable behind the status bar (edge-to-edge).
            .statusBarsPadding()
            .height(56.dp),
    ) {
        // Back button
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(12.dp),
        )

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
        )

        // Optional action
        if (action != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
            ) {
                action()
            }
        }
    }
}
