package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.theme.PinkAccent

@Composable
fun SleepTimeScreen(
    onBack: () -> Unit,
    beginHour: Int,
    endHour: Int,
    onBeginHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ComposeToolbar(
            title = stringResource(R.string.sleep_time),
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Slider card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "${beginHour}:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = beginHour.toFloat(),
                        onValueChange = { onBeginHourChange(it.toInt()) },
                        valueRange = 0f..23f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                    )

                    Text(
                        text = "${endHour}:00",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = { onEndHourChange(it.toInt()) },
                        valueRange = 0f..23f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Display card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp),
                ) {
                    Text(
                        text = stringResource(R.string.sleep_mode_open_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${beginHour}:00",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "  →  ",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${endHour}:00",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinkAccent,
                        )
                    }
                }
            }
        }
    }
}
