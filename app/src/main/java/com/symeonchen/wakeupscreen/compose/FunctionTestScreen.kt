package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.components.SettingRow

@Composable
fun FunctionTestScreen(
    onBack: () -> Unit,
    onWakeTestClick: () -> Unit,
    onViewLogsClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ComposeToolbar(
            title = stringResource(R.string.function_test),
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    SettingRow(
                        title = stringResource(R.string.delay_to_wakeup),
                        subtitle = stringResource(R.string.click_here_and_press_power_button_within_10_sec),
                        onClick = onWakeTestClick,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    SettingRow(
                        title = stringResource(R.string.view_logs),
                        subtitle = stringResource(R.string.view_logs_subtitle),
                        onClick = onViewLogsClick,
                    )
                }
            }
        }
    }
}
