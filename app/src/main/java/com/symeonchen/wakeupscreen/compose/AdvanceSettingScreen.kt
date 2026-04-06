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
    persistentChecked: Boolean,
    persistentSubtitle: String,
    onPersistentToggle: () -> Unit,
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
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ComposeToolbar(
            title = stringResource(R.string.advanced_setting),
            onBack = onBack,
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
            ) {
                Column {
                    SettingSwitchRow(
                        title = stringResource(R.string.pocket_mode),
                        subtitle = proximitySubtitle,
                        checked = proximityChecked,
                        onCheckedChange = onProximityToggle,
                    )
                    FlatDivider()
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
                        title = stringResource(R.string.show_notification_when_service_start),
                        subtitle = persistentSubtitle,
                        checked = persistentChecked,
                        onCheckedChange = onPersistentToggle,
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
            }
        }
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
