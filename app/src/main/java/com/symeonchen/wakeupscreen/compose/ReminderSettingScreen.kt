package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.theme.Indigo
import com.symeonchen.wakeupscreen.compose.theme.IndigoDark

@Composable
fun ReminderSettingScreen(
    onBack: () -> Unit,
    intervalMinutes: Int,
    intervalOptions: List<Int>,
    onIntervalChange: (Int) -> Unit,
    maxRounds: Int,
    maxRoundsOptions: List<Int>,
    unlimitedRoundsValue: Int,
    onMaxRoundsChange: (Int) -> Unit,
) {
    var showDozeHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.repeat_reminder),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            IntervalCard(
                intervalMinutes = intervalMinutes,
                intervalOptions = intervalOptions,
                onIntervalChange = onIntervalChange,
                onHelpClick = { showDozeHelp = true },
            )

            Spacer(Modifier.height(20.dp))

            MaxRoundsCard(
                maxRounds = maxRounds,
                maxRoundsOptions = maxRoundsOptions,
                unlimitedRoundsValue = unlimitedRoundsValue,
                onMaxRoundsChange = onMaxRoundsChange,
            )

            Spacer(Modifier.height(20.dp))

            PriorityCard()
        }
    }

    if (showDozeHelp) {
        AlertDialog(
            onDismissRequest = { showDozeHelp = false },
            title = { Text(text = stringResource(R.string.reminder_delay_help_title)) },
            text = {
                Text(
                    text = stringResource(R.string.reminder_delay_help_body),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDozeHelp = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun IntervalCard(
    intervalMinutes: Int,
    intervalOptions: List<Int>,
    onIntervalChange: (Int) -> Unit,
    onHelpClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.reminder_interval_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = intervalText(intervalMinutes),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            // A plain 5..60 slider would put most of its travel in values no
            // one picks, so it steps through the preset list instead.
            val selectedIndex = intervalOptions.indexOf(intervalMinutes)
                .takeIf { it >= 0 } ?: 0
            Slider(
                value = selectedIndex.toFloat(),
                onValueChange = { raw ->
                    val index = raw.toInt().coerceIn(0, intervalOptions.lastIndex)
                    onIntervalChange(intervalOptions[index])
                },
                valueRange = 0f..intervalOptions.lastIndex.toFloat(),
                steps = (intervalOptions.size - 2).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = intervalText(intervalOptions.first()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = intervalText(intervalOptions.last()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.reminder_delay_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                HelpButton(
                    onClick = onHelpClick,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun HelpButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.reminder_delay_help_title)
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MaxRoundsCard(
    maxRounds: Int,
    maxRoundsOptions: List<Int>,
    unlimitedRoundsValue: Int,
    onMaxRoundsChange: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.reminder_max_rounds_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.reminder_max_rounds_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (option in maxRoundsOptions) {
                    val isSelected = option == maxRounds
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) Modifier.background(
                                    Brush.linearGradient(
                                        listOf(IndigoDark, Indigo),
                                        start = Offset(0f, 0f),
                                        end = Offset(56f, 56f),
                                    )
                                )
                                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            .clickable { onMaxRoundsChange(option) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (option == unlimitedRoundsValue) {
                                stringResource(R.string.reminder_rounds_unlimited)
                            } else {
                                option.toString()
                            },
                            fontSize = if (option == unlimitedRoundsValue) 13.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.reminder_priority_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.reminder_priority_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )
            Spacer(Modifier.height(10.dp))
            for (line in listOf(
                R.string.reminder_priority_sleep,
                R.string.reminder_priority_dnd,
                R.string.reminder_priority_pocket,
                R.string.reminder_priority_charging,
                R.string.reminder_priority_screen_on,
                R.string.reminder_priority_filter,
            )) {
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(line),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun intervalText(minutes: Int): String =
    if (minutes >= 60 && minutes % 60 == 0) {
        stringResource(R.string.reminder_interval_hours_value, minutes / 60)
    } else {
        stringResource(R.string.reminder_interval_minutes_value, minutes)
    }
