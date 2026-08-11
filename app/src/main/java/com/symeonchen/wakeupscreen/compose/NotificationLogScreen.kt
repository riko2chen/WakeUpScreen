package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.app.NotificationManager
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.LogTrigger
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationLogScreen(
    logs: List<NotificationLogEntry>,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    var selectedEntry by remember { mutableStateOf<NotificationLogEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.view_logs),
            onBack = onBack,
            action = {
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
            },
        )

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.log_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs) { entry ->
                    LogEntryCard(entry, onClick = { selectedEntry = entry })
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        LogDetailDialog(entry = entry, onDismiss = { selectedEntry = null })
    }
}

@Composable
private fun LogEntryCard(entry: NotificationLogEntry, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(entry.timestamp))

    val statusText = logStatusText(entry.status)
    val statusColor = logStatusColor(entry.status)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.packageName.ifEmpty { stringResource(R.string.log_no_source_app) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    if (entry.trigger == LogTrigger.REMINDER) {
                        ReminderBadge(modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun LogDetailDialog(entry: NotificationLogEntry, onDismiss: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(entry.timestamp))

    val statusText = logStatusText(entry.status)

    val isReminder = entry.trigger == LogTrigger.REMINDER
    val description = when (entry.status) {
        LogStatus.SCREEN_ALREADY_ON ->
            if (isReminder) stringResource(R.string.log_status_reminder_already_on_desc)
            else stringResource(R.string.log_status_already_on_desc)
        LogStatus.WAKED_UP ->
            if (isReminder) stringResource(R.string.log_status_reminder_waked_up_desc)
            else stringResource(R.string.log_status_waked_up_desc)
        LogStatus.BLOCKED -> blockReasonToString(entry.blockReason)
        LogStatus.REMINDER_STOPPED -> blockReasonToString(entry.blockReason)
    }

    val importanceText = importanceToString(entry.importance)

    val soundText = when (entry.hasSound) {
        true -> stringResource(R.string.log_yes)
        false -> stringResource(R.string.log_no)
        null -> stringResource(R.string.log_channel_unknown)
    }

    val vibrationText = when (entry.hasVibration) {
        true -> stringResource(R.string.log_yes)
        false -> stringResource(R.string.log_no)
        null -> stringResource(R.string.log_channel_unknown)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.log_detail_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(
                    label = stringResource(R.string.log_detail_source_app),
                    value = entry.packageName.ifEmpty { stringResource(R.string.log_no_source_app) },
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_time),
                    value = timeStr,
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_trigger),
                    value = if (isReminder) stringResource(R.string.log_trigger_reminder)
                    else stringResource(R.string.log_trigger_notification),
                )
                if (isReminder && entry.reminderRound > 0) {
                    DetailRow(
                        label = stringResource(R.string.log_detail_reminder_round),
                        value = stringResource(R.string.log_reminder_round_value, entry.reminderRound),
                    )
                }
                if (isReminder) {
                    DetailRow(
                        label = stringResource(R.string.log_detail_unread_count),
                        value = entry.unreadCount.toString(),
                    )
                }
                DetailRow(
                    label = stringResource(R.string.log_detail_status),
                    value = statusText,
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_reason),
                    value = description,
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_importance),
                    value = importanceText,
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_sound),
                    value = soundText,
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_vibration),
                    value = vibrationText,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun logStatusText(status: LogStatus): String = when (status) {
    LogStatus.SCREEN_ALREADY_ON -> stringResource(R.string.log_screen_already_on)
    LogStatus.WAKED_UP -> stringResource(R.string.log_screen_waked_up)
    LogStatus.BLOCKED -> stringResource(R.string.log_screen_blocked)
    LogStatus.REMINDER_STOPPED -> stringResource(R.string.log_reminder_stopped)
}

@Composable
private fun logStatusColor(status: LogStatus) = when (status) {
    LogStatus.WAKED_UP -> MaterialTheme.colorScheme.primary
    LogStatus.SCREEN_ALREADY_ON -> MaterialTheme.colorScheme.tertiary
    LogStatus.BLOCKED -> MaterialTheme.colorScheme.error
    // Not a failure — the streak simply ran its course, so keep it neutral.
    LogStatus.REMINDER_STOPPED -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun ReminderBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.log_trigger_reminder),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun importanceToString(importance: Int): String {
    return when (importance) {
        NotificationManager.IMPORTANCE_NONE -> stringResource(R.string.log_importance_none)
        NotificationManager.IMPORTANCE_MIN -> stringResource(R.string.log_importance_min)
        NotificationManager.IMPORTANCE_LOW -> stringResource(R.string.log_importance_low)
        NotificationManager.IMPORTANCE_DEFAULT -> stringResource(R.string.log_importance_default)
        NotificationManager.IMPORTANCE_HIGH -> stringResource(R.string.log_importance_high)
        else -> stringResource(R.string.log_importance_unknown)
    }
}

@Composable
private fun blockReasonToString(reason: String): String {
    return when (reason) {
        BlockReason.APP_SWITCH_OFF -> stringResource(R.string.log_reason_app_switch_off)
        BlockReason.POCKET_MODE -> stringResource(R.string.log_reason_pocket_mode)
        BlockReason.FILTER_LIST -> stringResource(R.string.log_reason_filter_list)
        BlockReason.ONGOING -> stringResource(R.string.log_reason_ongoing)
        BlockReason.SLEEP_MODE -> stringResource(R.string.log_reason_sleep_mode)
        BlockReason.DND -> stringResource(R.string.log_reason_dnd)
        BlockReason.CHARGING -> stringResource(R.string.log_reason_charging)
        BlockReason.INTERACTIVE -> stringResource(R.string.log_status_already_on_desc)
        BlockReason.REMINDER_ALL_READ -> stringResource(R.string.log_reason_reminder_all_read)
        BlockReason.REMINDER_MAX_ROUNDS -> stringResource(R.string.log_reason_reminder_max_rounds)
        BlockReason.REMINDER_SWITCH_OFF -> stringResource(R.string.log_reason_reminder_switch_off)
        BlockReason.REMINDER_SERVICE_UNAVAILABLE ->
            stringResource(R.string.log_reason_reminder_service_unavailable)
        // For any unmapped reason, show the raw value as-is so no information
        // is lost when something unexpected blocks a notification.
        else -> reason
    }
}
