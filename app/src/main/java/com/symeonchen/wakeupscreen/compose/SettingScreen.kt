package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.SectionLabel
import com.symeonchen.wakeupscreen.compose.components.SettingRow
import com.symeonchen.wakeupscreen.compose.theme.GradientEnd
import com.symeonchen.wakeupscreen.compose.theme.GradientMid
import com.symeonchen.wakeupscreen.compose.theme.GradientStart

@Composable
fun SettingScreen(
    currentModeText: String,
    languageText: String,
    showWhiteListEntry: Boolean,
    showBlackListEntry: Boolean,
    onLanguageClick: () -> Unit,
    onWakeTimeClick: () -> Unit,
    onCurrentModeClick: () -> Unit,
    onWhiteListClick: () -> Unit,
    onBlackListClick: () -> Unit,
    onAdvancedSettingClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAddressClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onGiveStarClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.linearGradient(
                        listOf(GradientStart, GradientMid, GradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 300f),
                    )
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(
                text = stringResource(R.string.setting),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        // CUSTOMIZE section
        SectionLabel(
            text = stringResource(R.string.custom),
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column {
                SettingRow(
                    title = stringResource(R.string.language),
                    subtitle = languageText,
                    onClick = onLanguageClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.time_of_wake_up_screen),
                    onClick = onWakeTimeClick,
                )
                FlatDivider()
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
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.advanced_setting),
                    onClick = onAdvancedSettingClick,
                )
            }
        }

        // ABOUT section
        SectionLabel(
            text = stringResource(R.string.about),
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            Column {
                SettingRow(
                    title = stringResource(R.string.app_info),
                    onClick = onAboutClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.project_address),
                    subtitle = "github.com/SymeonChen",
                    onClick = onAddressClick,
                )
                FlatDivider()
                SettingRow(
                    title = stringResource(R.string.feedback),
                    subtitle = "symeonchen@gmail.com",
                    onClick = onFeedbackClick,
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
private fun FlatDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline,
    )
}
