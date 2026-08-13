package com.symeonchen.wakeupscreen.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.SectionLabel
import com.symeonchen.wakeupscreen.compose.components.SettingRow
import com.symeonchen.wakeupscreen.compose.components.SettingsSearchField
import com.symeonchen.wakeupscreen.compose.theme.GradientEnd
import com.symeonchen.wakeupscreen.compose.theme.GradientMid
import com.symeonchen.wakeupscreen.compose.theme.GradientStart

/**
 * One searchable settings item: its localized title, the localized name of the
 * page it lives on (shown as the result subtitle), and the click action that
 * brings the user to that page. Items on second-level pages navigate to their
 * parent page rather than deep into a widget, so search never bypasses the
 * conditions (e.g. sleep mode enabled) that gate the detail screens.
 */
private data class SettingsSearchEntry(
    val title: String,
    val page: String,
    val onClick: () -> Unit,
)

@Composable
fun SettingScreen(
    languageText: String,
    darkModeText: String,
    showWhiteListEntry: Boolean,
    showBlackListEntry: Boolean,
    onLanguageClick: () -> Unit,
    onDarkModeClick: () -> Unit,
    onAdvancedSettingClick: () -> Unit,
    onBlockChainClick: () -> Unit,
    onFunctionTestClick: () -> Unit,
    onViewLogsClick: () -> Unit,
    onAddressClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onGiveStarClick: () -> Unit,
    onCheckUpdateClick: () -> Unit,
) {
    var appInfoExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val settingsPage = stringResource(R.string.setting)
    val advancedPage = stringResource(R.string.advanced_setting)
    val functionTestPage = stringResource(R.string.function_test)
    val aboutPage = stringResource(R.string.about)

    val searchEntries = listOfNotNull(
        SettingsSearchEntry(stringResource(R.string.language), settingsPage, onLanguageClick),
        SettingsSearchEntry(stringResource(R.string.dark_mode), settingsPage, onDarkModeClick),
        SettingsSearchEntry(stringResource(R.string.chain_title), settingsPage, onBlockChainClick),
        SettingsSearchEntry(stringResource(R.string.advanced_setting), settingsPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.time_of_wake_up_screen), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.current_mode), advancedPage, onAdvancedSettingClick),
        if (showWhiteListEntry) {
            SettingsSearchEntry(stringResource(R.string.white_list), advancedPage, onAdvancedSettingClick)
        } else null,
        if (showBlackListEntry) {
            SettingsSearchEntry(stringResource(R.string.black_list), advancedPage, onAdvancedSettingClick)
        } else null,
        SettingsSearchEntry(stringResource(R.string.pocket_mode), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.ongoing_status), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.radical_ongoing_detact), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.ignore_silent_notification), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.dnd_detect_title), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.charging_only_title), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.sleep_mode), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.sleep_time), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.repeat_reminder), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.repeat_reminder_detail), advancedPage, onAdvancedSettingClick),
        SettingsSearchEntry(stringResource(R.string.function_test), settingsPage, onFunctionTestClick),
        SettingsSearchEntry(stringResource(R.string.view_logs), settingsPage, onViewLogsClick),
        SettingsSearchEntry(stringResource(R.string.delay_to_wakeup), functionTestPage, onFunctionTestClick),
        SettingsSearchEntry(stringResource(R.string.reminder_test), functionTestPage, onFunctionTestClick),
        SettingsSearchEntry(stringResource(R.string.project_address), aboutPage, onAddressClick),
        SettingsSearchEntry(stringResource(R.string.feedback), aboutPage, onFeedbackClick),
        SettingsSearchEntry(stringResource(R.string.check_update), aboutPage, onCheckUpdateClick),
        SettingsSearchEntry(stringResource(R.string.give_star), aboutPage, onGiveStarClick),
    )

    val trimmedQuery = searchQuery.trim()
    val searchResults = if (trimmedQuery.isEmpty()) {
        emptyList()
    } else {
        searchEntries.filter {
            it.title.contains(trimmedQuery, ignoreCase = true) ||
                it.page.contains(trimmedQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(GradientStart, GradientMid, GradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 300f),
                    )
                )
                // Gradient bleeds under the status bar; the title sits in a
                // 64dp band below it (so the total header isn't over-inflated).
                .statusBarsPadding()
                .height(64.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = stringResource(R.string.setting),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        SettingsSearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        )

        if (trimmedQuery.isNotEmpty()) {
            // Search results replace the grouped list until the query is cleared.
            if (searchResults.isEmpty()) {
                Text(
                    text = stringResource(R.string.setting_search_no_result),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    Column {
                        searchResults.forEachIndexed { index, entry ->
                            if (index > 0) FlatDivider()
                            SettingRow(
                                title = entry.title,
                                subtitle = entry.page,
                                onClick = entry.onClick,
                            )
                        }
                    }
                }
            }
        } else {
            // GENERAL section
            SectionLabel(
                text = stringResource(R.string.section_general),
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
            )

            SettingsCard {
                SettingRow(
                    title = stringResource(R.string.language),
                    subtitle = languageText,
                    onClick = onLanguageClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.dark_mode),
                    subtitle = darkModeText,
                    onClick = onDarkModeClick,
                )
            }

            // ADVANCED section
            SectionLabel(
                text = stringResource(R.string.section_advanced),
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
            )

            SettingsCard {
                SettingRow(
                    title = stringResource(R.string.chain_title),
                    subtitle = stringResource(R.string.chain_entry_subtitle),
                    onClick = onBlockChainClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.advanced_setting),
                    onClick = onAdvancedSettingClick,
                )
            }

            // DIAGNOSTICS section
            SectionLabel(
                text = stringResource(R.string.section_diagnostics),
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
            )

            SettingsCard {
                SettingRow(
                    title = stringResource(R.string.function_test),
                    onClick = onFunctionTestClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.view_logs),
                    subtitle = stringResource(R.string.view_logs_subtitle),
                    onClick = onViewLogsClick,
                )
            }

            // ABOUT section
            SectionLabel(
                text = stringResource(R.string.about),
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
            )

            SettingsCard {
                SettingRow(
                    title = stringResource(R.string.app_info),
                    onClick = { appInfoExpanded = !appInfoExpanded },
                    trailingIcon = if (appInfoExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                )
                AnimatedVisibility(
                    visible = appInfoExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val context = LocalContext.current
                        val iconBitmap = remember {
                            val drawable = context.packageManager.getApplicationIcon(context.packageName)
                            val size = (72 * context.resources.displayMetrics.density).toInt()
                            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            drawable.setBounds(0, 0, size, size)
                            drawable.draw(canvas)
                            bitmap.asImageBitmap()
                        }
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(72.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.app_intro_full),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                        )
                    }
                }
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.project_address),
                    subtitle = "github.com/riko2chen/WakeUpScreen",
                    onClick = onAddressClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.feedback),
                    subtitle = stringResource(R.string.feedback_subtitle),
                    onClick = onFeedbackClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.check_update),
                    subtitle = stringResource(R.string.check_update_subtitle),
                    onClick = onCheckUpdateClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.give_star),
                    subtitle = stringResource(R.string.click_here_to_open_google_play_store),
                    onClick = onGiveStarClick,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
private fun FlatDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}
