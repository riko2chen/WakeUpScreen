package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.*

/**
 * The switches whose behaviour needs more than a subtitle to explain.
 */
private enum class HelpTopic(val titleRes: Int, val bodyRes: Int) {
    ONGOING(R.string.ongoing_status, R.string.ongoing_status_help),
    RADICAL_ONGOING(R.string.radical_ongoing_detact, R.string.radical_ongoing_help),
}

@Composable
fun AdvanceSettingScreen(
    onBack: () -> Unit,
    // Screen-on duration
    wakeTimeText: String,
    onWakeTimeClick: () -> Unit,
    // Notification filter
    currentModeText: String,
    onCurrentModeClick: () -> Unit,
    showWhiteListEntry: Boolean,
    onWhiteListClick: () -> Unit,
    showBlackListEntry: Boolean,
    onBlackListClick: () -> Unit,
    // Switch states
    proximityChecked: Boolean,
    proximitySubtitle: String,
    onProximityToggle: () -> Unit,
    faceDownChecked: Boolean,
    onFaceDownToggle: () -> Unit,
    ongoingChecked: Boolean,
    ongoingSubtitle: String,
    onOngoingToggle: () -> Unit,
    radicalOngoingChecked: Boolean,
    radicalOngoingSubtitle: String,
    onRadicalOngoingToggle: () -> Unit,
    ignoreSilentChecked: Boolean,
    onIgnoreSilentToggle: () -> Unit,
    dndChecked: Boolean,
    onDndToggle: () -> Unit,
    chargingOnlyChecked: Boolean,
    chargingOnlySubtitle: String,
    onChargingOnlyToggle: () -> Unit,
    batteryLevelChecked: Boolean,
    onBatteryLevelToggle: () -> Unit,
    showBatteryLevelDetail: Boolean,
    batteryLevelDetailSubtitle: String,
    onBatteryLevelDetailClick: () -> Unit,
    sleepChecked: Boolean,
    sleepSubtitle: String,
    onSleepToggle: () -> Unit,
    // Sleep detail
    showSleepDetail: Boolean,
    sleepDetailSubtitle: String,
    onSleepDetailClick: () -> Unit,
    nightGlowChecked: Boolean,
    onNightGlowToggle: () -> Unit,
    // Repeat reminder
    repeatReminderChecked: Boolean,
    repeatReminderSubtitle: String,
    onRepeatReminderToggle: () -> Unit,
    showRepeatReminderDetail: Boolean,
    repeatReminderDetailSubtitle: String,
    onRepeatReminderDetailClick: () -> Unit,
    faceDownBadge: Boolean = false,
    batteryLevelBadge: Boolean = false,
    nightGlowBadge: Boolean = false,
    sleepWeekdayBadge: Boolean = false,
) {
    var helpTopic by remember { mutableStateOf<HelpTopic?>(null) }

    helpTopic?.let { topic ->
        HelpDialog(
            title = stringResource(topic.titleRes),
            body = stringResource(topic.bodyRes),
            onDismiss = { helpTopic = null },
        )
    }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.advanced_setting),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Screen-on duration
            SectionLabel(
                text = stringResource(R.string.section_screen_on_duration),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingRow(
                    title = stringResource(R.string.time_of_wake_up_screen),
                    subtitle = wakeTimeText,
                    onClick = onWakeTimeClick,
                )
            }

            // Which apps are allowed to wake the screen
            SectionLabel(
                text = stringResource(R.string.section_notification_filter),
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingRow(
                    title = stringResource(R.string.current_mode),
                    subtitle = currentModeText,
                    onClick = onCurrentModeClick,
                )
                if (showWhiteListEntry) {
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.white_list),
                        subtitle = stringResource(R.string.click_to_enter_white_list_page),
                        onClick = onWhiteListClick,
                    )
                }
                if (showBlackListEntry) {
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.black_list),
                        subtitle = stringResource(R.string.click_to_enter_black_list_page),
                        onClick = onBlackListClick,
                    )
                }
            }

            // Which notifications count
            SectionLabel(
                text = stringResource(R.string.section_notification_rules),
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingSwitchRow(
                    title = stringResource(R.string.ongoing_status),
                    subtitle = ongoingSubtitle,
                    checked = ongoingChecked,
                    onCheckedChange = onOngoingToggle,
                    onHelpClick = { helpTopic = HelpTopic.ONGOING },
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.radical_ongoing_detact),
                    subtitle = radicalOngoingSubtitle,
                    checked = radicalOngoingChecked,
                    onCheckedChange = onRadicalOngoingToggle,
                    onHelpClick = { helpTopic = HelpTopic.RADICAL_ONGOING },
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.ignore_silent_notification),
                    subtitle = stringResource(R.string.ignore_silent_notification_desc),
                    checked = ignoreSilentChecked,
                    onCheckedChange = onIgnoreSilentToggle,
                )
            }

            // When the device should stay dark
            SectionLabel(
                text = stringResource(R.string.section_device_state),
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingSwitchRow(
                    title = stringResource(R.string.pocket_mode),
                    subtitle = proximitySubtitle,
                    checked = proximityChecked,
                    onCheckedChange = onProximityToggle,
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.face_down_title),
                    subtitle = stringResource(R.string.face_down_desc),
                    checked = faceDownChecked,
                    onCheckedChange = onFaceDownToggle,
                    showBadge = faceDownBadge,
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.dnd_detect_title),
                    subtitle = stringResource(R.string.dnd_detect_desc),
                    checked = dndChecked,
                    onCheckedChange = onDndToggle,
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.charging_only_title),
                    subtitle = chargingOnlySubtitle,
                    checked = chargingOnlyChecked,
                    onCheckedChange = onChargingOnlyToggle,
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.battery_level_title),
                    subtitle = stringResource(R.string.battery_level_desc),
                    checked = batteryLevelChecked,
                    onCheckedChange = onBatteryLevelToggle,
                    showBadge = batteryLevelBadge,
                )
                if (showBatteryLevelDetail) {
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.battery_level_threshold_title),
                        subtitle = batteryLevelDetailSubtitle,
                        onClick = onBatteryLevelDetailClick,
                    )
                }
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.sleep_mode),
                    subtitle = sleepSubtitle,
                    checked = sleepChecked,
                    onCheckedChange = onSleepToggle,
                )
                if (showSleepDetail) {
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.sleep_time),
                        subtitle = sleepDetailSubtitle,
                        onClick = onSleepDetailClick,
                        showBadge = sleepWeekdayBadge,
                    )
                    FlatDivider()
                    SettingSwitchRow(
                        title = stringResource(R.string.night_glow_title),
                        subtitle = stringResource(R.string.night_glow_desc),
                        checked = nightGlowChecked,
                        onCheckedChange = onNightGlowToggle,
                        showBadge = nightGlowBadge,
                    )
                }
            }

            // Repeat reminder
            SectionLabel(
                text = stringResource(R.string.repeat_reminder),
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingSwitchRow(
                    title = stringResource(R.string.repeat_reminder),
                    subtitle = repeatReminderSubtitle,
                    checked = repeatReminderChecked,
                    onCheckedChange = onRepeatReminderToggle,
                )
                if (showRepeatReminderDetail) {
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.repeat_reminder_detail),
                        subtitle = repeatReminderDetailSubtitle,
                        onClick = onRepeatReminderDetailClick,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun FlatDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}
