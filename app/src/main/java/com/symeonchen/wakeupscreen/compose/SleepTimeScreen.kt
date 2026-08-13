package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.components.SleepDayRing
import com.symeonchen.wakeupscreen.compose.components.SleepRingColors
import com.symeonchen.wakeupscreen.data.SleepSchedule
import com.symeonchen.wakeupscreen.data.SleepSegment
import com.symeonchen.wakeupscreen.data.Weekdays
import com.symeonchen.wakeupscreen.utils.TimeOfDayFormatter
import java.text.DateFormatSymbols
import java.util.Locale

@Composable
fun SleepTimeScreen(
    onBack: () -> Unit,
    segments: List<SleepSegment>,
    maxSegments: Int,
    onAdd: (SleepSegment) -> Unit,
    onDelete: (SleepSegment) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.sleep_time),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            RingCard(segments = segments)

            Spacer(Modifier.height(20.dp))

            SegmentListCard(
                segments = segments,
                canAddMore = segments.size < maxSegments,
                maxSegments = maxSegments,
                onAddClick = { showAddDialog = true },
                onDelete = onDelete,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.sleep_range_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showAddDialog) {
        AddSegmentDialog(
            existing = segments,
            onDismiss = { showAddDialog = false },
            onConfirm = { segment ->
                onAdd(segment)
                showAddDialog = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RingCard(segments: List<SleepSegment>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 20.dp),
        ) {
            SleepDayRing(
                segments = segments,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalMinutes = segments.sumOf { it.durationMinutes }
                    Text(
                        text = if (segments.isEmpty()) {
                            stringResource(R.string.sleep_segments_empty)
                        } else {
                            durationText(totalMinutes)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    if (segments.size > 1) {
                        Text(
                            text = stringResource(R.string.sleep_segments_count, segments.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Flows onto a second line rather than clipping, since the labels
            // in a long language may not fit one row on a narrow screen.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LegendDot(color = SleepRingColors.asleep, label = stringResource(R.string.sleep_ring_asleep))
                LegendDot(color = SleepRingColors.awake, label = stringResource(R.string.sleep_ring_awake))
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun SegmentListCard(
    segments: List<SleepSegment>,
    canAddMore: Boolean,
    maxSegments: Int,
    onAddClick: () -> Unit,
    onDelete: (SleepSegment) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            if (segments.isEmpty()) {
                Text(
                    text = stringResource(R.string.sleep_segments_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                )
            } else {
                segments.forEachIndexed { index, segment ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    SegmentRow(segment = segment, onDelete = { onDelete(segment) })
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )

            TextButton(
                onClick = onAddClick,
                enabled = canAddMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (canAddMore) {
                        stringResource(R.string.sleep_add_segment)
                    } else {
                        stringResource(R.string.sleep_max_segments_reached, maxSegments)
                    },
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

/**
 * Which days a window applies to, in one phrase: the three common shapes get
 * their own words, anything else lists the short weekday names.
 */
@Composable
fun weekdaySummary(days: Set<Int>): String = when (days) {
    Weekdays.EVERY_DAY -> stringResource(R.string.sleep_days_every_day)
    Weekdays.WEEKDAYS -> stringResource(R.string.sleep_days_weekdays)
    Weekdays.WEEKEND -> stringResource(R.string.sleep_days_weekend)
    else -> {
        val symbols = DateFormatSymbols(Locale.getDefault()).shortWeekdays
        days.sorted().joinToString(" ") { symbols[Weekdays.toCalendar(it)] }
    }
}

@Composable
private fun SegmentRow(segment: SleepSegment, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val duration = durationText(segment.durationMinutes)
        val crossesMidnight = stringResource(R.string.sleep_crosses_midnight)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = TimeOfDayFormatter.formatSegment(segment),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val detail = buildString {
                append(weekdaySummary(segment.days))
                append(" · ")
                append(duration)
                if (segment.crossesMidnight) {
                    append(" · ")
                    append(crossesMidnight)
                }
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.sleep_delete_segment),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddSegmentDialog(
    existing: List<SleepSegment>,
    onDismiss: () -> Unit,
    onConfirm: (SleepSegment) -> Unit,
) {
    val startState = rememberTimePickerState(initialHour = 23, initialMinute = 0, is24Hour = true)
    val endState = rememberTimePickerState(initialHour = 7, initialMinute = 0, is24Hour = true)
    var selectedDays by remember { mutableStateOf(Weekdays.EVERY_DAY) }
    var error by remember { mutableStateOf<String?>(null) }

    // Resolved through the context rather than stringResource: the checks below
    // run inside a click handler, which is not a composable scope.
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.sleep_add_segment)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.sleep_start_time),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                TimeInput(state = startState)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.sleep_end_time),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                TimeInput(state = endState)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.sleep_days_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                // One chip per weekday; an over-midnight window belongs to the
                // evening it starts on, which the note below the list explains.
                val daySymbols = DateFormatSymbols(Locale.getDefault()).shortWeekdays
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    (Weekdays.MONDAY..Weekdays.SUNDAY).forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(daySymbols[Weekdays.toCalendar(day)]) },
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { selectedDays = Weekdays.EVERY_DAY }) {
                        Text(stringResource(R.string.sleep_days_every_day))
                    }
                    TextButton(onClick = { selectedDays = Weekdays.WEEKDAYS }) {
                        Text(stringResource(R.string.sleep_days_weekdays))
                    }
                    TextButton(onClick = { selectedDays = Weekdays.WEEKEND }) {
                        Text(stringResource(R.string.sleep_days_weekend))
                    }
                }

                error?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val candidate = SleepSegment.of(
                    startState.hour,
                    startState.minute,
                    endState.hour,
                    endState.minute,
                    selectedDays,
                )
                val conflicts = SleepSchedule.conflictsWith(existing, candidate)
                error = when {
                    selectedDays.isEmpty() -> context.getString(R.string.sleep_days_empty_error)
                    !candidate.isValid -> context.getString(R.string.sleep_same_time_error)
                    conflicts.isNotEmpty() -> context.getString(
                        R.string.sleep_overlap_error,
                        conflicts.joinToString(", ") { TimeOfDayFormatter.formatSegment(it) },
                    )
                    else -> null
                }
                if (error == null) {
                    onConfirm(candidate)
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun durationText(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.sleep_duration_hm, hours, minutes)
    } else {
        stringResource(R.string.sleep_duration_m, minutes)
    }
}
