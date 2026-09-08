package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.utils.ReminderDurationPolicy
import kotlin.math.roundToLong

@Composable
internal fun ReminderDurationCard(
    seconds: Long,
    onDurationChange: (Long) -> Unit,
    accessibilitySupported: Boolean,
    accessibilityGranted: Boolean,
    onGrantAccessibilityClick: () -> Unit,
) {
    val inherited = seconds == ReminderDurationPolicy.INHERIT
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(stringResource(R.string.reminder_duration_title), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.reminder_duration_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(Modifier.selectableGroup().padding(top = 12.dp)) {
                    DurationChoice(
                        stringResource(R.string.reminder_duration_inherit), inherited,
                        onClick = { onDurationChange(ReminderDurationPolicy.INHERIT) },
                    )
                    DurationChoice(
                        stringResource(R.string.reminder_duration_custom), !inherited,
                        onClick = { if (inherited) onDurationChange(ReminderDurationPolicy.MIN_SECONDS) },
                    )
                }
                if (!inherited) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.precise_wake_preset_seconds, seconds),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Slider(
                        value = seconds.toFloat(),
                        onValueChange = { onDurationChange(it.roundToLong()) },
                        valueRange = ReminderDurationPolicy.MIN_SECONDS.toFloat()..ReminderDurationPolicy.MAX_SECONDS.toFloat(),
                        steps = (ReminderDurationPolicy.MAX_SECONDS - ReminderDurationPolicy.MIN_SECONDS - 1).toInt(),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.precise_wake_preset_seconds, ReminderDurationPolicy.MIN_SECONDS))
                        Text(stringResource(R.string.precise_wake_preset_seconds, ReminderDurationPolicy.MAX_SECONDS))
                    }
                    if (ReminderDurationPolicy.needsPermissionWarning(seconds, accessibilitySupported, accessibilityGranted)) {
                        Text(
                            stringResource(R.string.reminder_duration_permission_notice),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                }
            }
        }
        if (!inherited) {
            ScreenOffMethodCard(accessibilitySupported, accessibilityGranted, onGrantAccessibilityClick)
        }
    }
}

@Composable
private fun DurationChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
    }
}
