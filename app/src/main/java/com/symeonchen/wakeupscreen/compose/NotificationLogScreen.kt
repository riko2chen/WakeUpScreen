package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.app.NotificationManager
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.BlockChainView
import com.symeonchen.wakeupscreen.compose.components.ChainRow
import com.symeonchen.wakeupscreen.compose.components.ChainSurface
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.theme.WakeUpTheme
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.LogTrigger
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.services.notification.BlockChain
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ChainNodeState
import com.symeonchen.wakeupscreen.services.notification.ChainStep
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationLogScreen(
    logs: List<NotificationLogEntry>,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onChainNodeClick: (String) -> Unit = {},
    isChainNodeNavigable: (String) -> Boolean = { false },
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
        LogDetailDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
            onChainNodeClick = onChainNodeClick,
            isChainNodeNavigable = isChainNodeNavigable,
        )
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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
private fun LogDetailDialog(
    entry: NotificationLogEntry,
    onDismiss: () -> Unit,
    onChainNodeClick: (String) -> Unit,
    isChainNodeNavigable: (String) -> Boolean,
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(entry.timestamp))

    val statusText = logStatusText(entry.status)

    // Empty whenever the entry is not a chain outcome (a reminder streak
    // ending, or a reason this build does not know); the plain rows below take
    // over in that case.
    val chainSteps = remember(entry) { BlockChain.forLogEntry(entry) }
    var showRawFields by remember { mutableStateOf(chainSteps.isEmpty()) }

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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailRow(
                    label = stringResource(R.string.log_detail_source_app),
                    value = entry.packageName.ifEmpty { stringResource(R.string.log_no_source_app) },
                )
                DetailRow(
                    label = stringResource(R.string.log_detail_time),
                    value = timeStr,
                )
                if (isReminder) {
                    DetailRow(
                        label = stringResource(R.string.log_detail_trigger),
                        value = if (entry.reminderRound > 0) {
                            stringResource(R.string.log_trigger_reminder) + " · " +
                                stringResource(R.string.log_reminder_round_value, entry.reminderRound)
                        } else {
                            stringResource(R.string.log_trigger_reminder)
                        },
                    )
                    DetailRow(
                        label = stringResource(R.string.log_detail_unread_count),
                        value = entry.unreadCount.toString(),
                    )
                }

                if (chainSteps.isNotEmpty()) {
                    ChainSection(
                        steps = chainSteps,
                        onChainNodeClick = onChainNodeClick,
                        isChainNodeNavigable = isChainNodeNavigable,
                    )
                } else {
                    DetailRow(
                        label = stringResource(R.string.log_detail_status),
                        value = statusText,
                    )
                    DetailRow(
                        label = stringResource(R.string.log_detail_reason),
                        value = description,
                    )
                }

                if (showRawFields) {
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
                } else {
                    TextButton(
                        onClick = { showRawFields = true },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.chain_show_notification_fields),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

/**
 * The recorded outcome drawn as the chain the notification actually walked.
 *
 * Everything shown here is replayed from what was written at the time, so the
 * verdicts are exact. The one thing that is not history is the configuration
 * under the blocking node — the log never recorded the values behind a gate,
 * only which gate it was — so that line is labelled as the current setting.
 */
@Composable
private fun ChainSection(
    steps: List<ChainStep>,
    onChainNodeClick: (String) -> Unit,
    isChainNodeNavigable: (String) -> Boolean,
) {
    Column {
        Text(
            text = stringResource(R.string.chain_log_section_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ChainSurface(modifier = Modifier.padding(top = 6.dp)) {
            BlockChainView(
                rows = steps.map { step ->
                    val navigable = step.state == ChainNodeState.BLOCKED &&
                        isChainNodeNavigable(step.key)
                    ChainRow(
                        key = step.key,
                        title = chainNodeTitle(step.key),
                        subtitle = logSubtitle(step),
                        state = step.state,
                        onClick = if (navigable) {
                            { onChainNodeClick(step.key) }
                        } else {
                            null
                        },
                    )
                }
            )
        }
    }
}

@Composable
private fun logSubtitle(step: ChainStep): String {
    val stateLabel = chainStateLabel(step.key, step.state)
    if (step.state != ChainNodeState.BLOCKED) {
        return stateLabel
    }
    val config = chainConfigSummary(step.key, hasNotificationAccess = true)
        ?: return stateLabel
    return stateLabel + " · " + stringResource(R.string.chain_current_setting, config)
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
    // Success role, not tertiary: tertiary is pink and reads as error-red.
    LogStatus.SCREEN_ALREADY_ON -> WakeUpTheme.colors.success
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
        BlockReason.LOW_IMPORTANCE -> stringResource(R.string.log_reason_low_importance)
        BlockReason.ONGOING -> stringResource(R.string.log_reason_ongoing)
        BlockReason.SLEEP_MODE -> stringResource(R.string.log_reason_sleep_mode)
        BlockReason.DND -> stringResource(R.string.log_reason_dnd)
        BlockReason.CHARGING -> stringResource(R.string.log_reason_charging)
        BlockReason.BATTERY_LEVEL -> stringResource(R.string.log_reason_battery_level)
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
