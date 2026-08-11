package com.symeonchen.wakeupscreen.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.symeonchen.wakeupscreen.compose.components.GlyphBadge
import com.symeonchen.wakeupscreen.compose.components.SettingSwitchRow
import com.symeonchen.wakeupscreen.compose.theme.*
import com.symeonchen.wakeupscreen.data.ScConstant

/**
 * The whole precise screen-on feature hangs off the switch at the top. With it
 * off nothing below is shown, because none of it applies: the app only wakes the
 * display and the system decides when it dims again.
 *
 * The duration is a fixed set of presets rather than a free-text field. Every
 * value a user would actually want is in the list, and the field only ever
 * invited numbers the engine would clamp away.
 */
@Composable
fun WakeUpTimeScreen(
    onBack: () -> Unit,
    preciseEnabled: Boolean,
    onPreciseToggle: () -> Unit,
    selectedSecond: Long,
    onPresetClick: (Long) -> Unit,
    hasUnsavedChanges: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    accessibilitySupported: Boolean,
    accessibilityGranted: Boolean,
    onGrantAccessibilityClick: () -> Unit,
) {
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Leaving with a preset tapped but not saved would silently throw the choice
    // away, so both ways out of the screen run through the same check.
    val leave = {
        if (hasUnsavedChanges) showUnsavedDialog = true else onBack()
    }
    BackHandler(enabled = true, onBack = leave)

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.time_of_wake_up_screen),
            onBack = leave,
            action = {
                // The switch and the turn-off method take effect the moment they
                // are touched; only the duration waits for this button.
                if (preciseEnabled) {
                    Text(
                        text = stringResource(R.string.save),
                        color = PinkAccent,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .alpha(if (hasUnsavedChanges) 1f else 0.4f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = hasUnsavedChanges, onClick = onSave)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                SettingSwitchRow(
                    title = stringResource(R.string.precise_wake_switch_title),
                    subtitle = stringResource(R.string.precise_wake_switch_desc),
                    checked = preciseEnabled,
                    onCheckedChange = onPreciseToggle,
                )
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = !preciseEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                NoticeCard(text = stringResource(R.string.precise_wake_disabled_notice))
            }

            AnimatedVisibility(
                visible = preciseEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    DurationCard(
                        selectedSecond = selectedSecond,
                        onPresetClick = onPresetClick,
                    )

                    Spacer(Modifier.height(16.dp))

                    ScreenOffMethodCard(
                        accessibilitySupported = accessibilitySupported,
                        accessibilityGranted = accessibilityGranted,
                        onGrantAccessibilityClick = onGrantAccessibilityClick,
                    )

                    Spacer(Modifier.height(16.dp))

                    NoticeCard(text = stringResource(R.string.precise_wake_battery_notice))
                }
            }
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(text = stringResource(R.string.unsaved_changes_title)) },
            text = {
                Text(
                    text = stringResource(R.string.unsaved_changes_message),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onSave()
                }) {
                    Text(stringResource(R.string.save_and_back))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onDiscard()
                }) {
                    Text(stringResource(R.string.discard_changes))
                }
            },
        )
    }
}

@Composable
private fun DurationCard(
    selectedSecond: Long,
    onPresetClick: (Long) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.precise_wake_duration_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (preset in ScConstant.PRECISE_SCREEN_ON_PRESET_SECONDS) {
                    val isSelected = selectedSecond == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
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
                            .clickable { onPresetClick(preset) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.precise_wake_preset_seconds, preset),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/**
 * There is no method picker any more. Below Android 9 the accessibility action
 * does not exist and the permission-free fallback is all there is; from Android
 * 9 up the fallback is ignored by the platform, so offering it as a choice only
 * gave users a way to pick the broken one.
 */
@Composable
private fun ScreenOffMethodCard(
    accessibilitySupported: Boolean,
    accessibilityGranted: Boolean,
    onGrantAccessibilityClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.screen_off_method_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.screen_off_method_accessibility_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )

            Spacer(Modifier.height(14.dp))

            when {
                !accessibilitySupported -> NoticeCard(
                    text = stringResource(R.string.accessibility_unsupported_notice)
                )

                accessibilityGranted -> GrantedRow()

                else -> GrantGuide(onGrantAccessibilityClick = onGrantAccessibilityClick)
            }
        }
    }
}

@Composable
private fun GrantedRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        GlyphBadge(
            text = "✓",
            size = 20.dp,
            fontSize = 12.sp,
            background = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
        )
        Text(
            text = stringResource(R.string.accessibility_status_granted),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Spelled out step by step. The toggle lives several screens deep in system
 * settings, under a heading that is named differently on almost every vendor
 * build, and the intent can only get the user as far as the top of that tree.
 */
@Composable
private fun GrantGuide(onGrantAccessibilityClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmberSoft,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.accessibility_required_warning),
                style = MaterialTheme.typography.bodySmall,
                color = Amber,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(12.dp))

            val steps = listOf(
                R.string.accessibility_step_open_settings,
                R.string.accessibility_step_installed_apps,
                R.string.accessibility_step_enable_service,
            )
            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(bottom = 6.dp)) {
                    GlyphBadge(
                        text = "${index + 1}",
                        size = 18.dp,
                        fontSize = 11.sp,
                        background = Amber,
                        contentColor = Color.White,
                    )
                    Text(
                        text = stringResource(step),
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = onGrantAccessibilityClick,
                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.accessibility_grant_action))
            }
        }
    }
}

@Composable
private fun NoticeCard(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmberSoft,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            color = Amber,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
            modifier = Modifier.padding(14.dp),
        )
    }
}
