package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.*

@Composable
fun AdvanceSettingScreen(
    onBack: () -> Unit,
    // Switch states
    proximityChecked: Boolean,
    proximitySubtitle: String,
    onProximityToggle: () -> Unit,
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
    sleepChecked: Boolean,
    sleepSubtitle: String,
    onSleepToggle: () -> Unit,
    // Sleep detail
    showSleepDetail: Boolean,
    sleepDetailSubtitle: String,
    onSleepDetailClick: () -> Unit,
    // Repeat reminder
    repeatReminderChecked: Boolean,
    repeatReminderSubtitle: String,
    onRepeatReminderToggle: () -> Unit,
    showRepeatReminderDetail: Boolean,
    repeatReminderDetailSubtitle: String,
    onRepeatReminderDetailClick: () -> Unit,
) {
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
            // Which notifications count
            SectionLabel(
                text = stringResource(R.string.section_notification_rules),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
            )
            GroupCard {
                SettingSwitchRow(
                    title = stringResource(R.string.ongoing_status),
                    subtitle = ongoingSubtitle,
                    checked = ongoingChecked,
                    onCheckedChange = onOngoingToggle,
                )
                FlatDivider()
                SettingSwitchRow(
                    title = stringResource(R.string.radical_ongoing_detact),
                    subtitle = radicalOngoingSubtitle,
                    checked = radicalOngoingChecked,
                    onCheckedChange = onRadicalOngoingToggle,
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
        color = MaterialTheme.colorScheme.surface,
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
