package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.HelpButton
import com.symeonchen.wakeupscreen.data.ReminderIntervalPolicy

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IntervalCard(
    intervalMinutes: Int,
    intervalOptions: List<Int>,
    onIntervalChange: (Int) -> Unit,
    onHelpClick: () -> Unit,
) {
    var showCustom by rememberSaveable { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.reminder_interval_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                intervalText(intervalMinutes),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                intervalOptions.forEach { minutes ->
                    FilterChip(
                        selected = intervalMinutes == minutes,
                        onClick = { onIntervalChange(minutes) },
                        label = { Text(intervalText(minutes)) },
                    )
                }
                FilterChip(
                    selected = intervalMinutes !in intervalOptions,
                    onClick = { showCustom = true },
                    label = { Text(stringResource(R.string.reminder_interval_custom)) },
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reminder_delay_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                HelpButton(
                    onClick = onHelpClick,
                    onClickLabel = stringResource(R.string.reminder_delay_help_title),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
    if (showCustom) {
        CustomIntervalDialog(
            intervalMinutes = intervalMinutes,
            onDismiss = { showCustom = false },
            onSave = { minutes ->
                onIntervalChange(minutes)
                showCustom = false
            },
        )
    }
}

@Composable
private fun CustomIntervalDialog(intervalMinutes: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var input by rememberSaveable { mutableStateOf(intervalMinutes.toString()) }
    val parsed = ReminderIntervalPolicy.parse(input)
    val focusRequester = remember { FocusRequester() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_interval_custom_title)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.reminder_interval_minutes_label)) },
                supportingText = { Text(stringResource(R.string.reminder_interval_custom_range)) },
                isError = parsed == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { parsed?.let(onSave) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(enabled = parsed != null, onClick = { parsed?.let(onSave) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
