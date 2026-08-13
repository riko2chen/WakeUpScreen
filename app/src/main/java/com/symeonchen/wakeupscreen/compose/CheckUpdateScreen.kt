package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.BuildConfig
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.components.SettingRow

@Composable
fun CheckUpdateScreen(
    onBack: () -> Unit,
    onPlayStoreClick: () -> Unit,
    onFDroidClick: () -> Unit,
    onGitHubClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.check_update),
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.current_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp),
            )
            Text(
                text = stringResource(R.string.check_update_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    SettingRow(
                        title = stringResource(R.string.update_via_play),
                        subtitle = stringResource(R.string.update_via_play_subtitle),
                        onClick = onPlayStoreClick,
                    )
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.update_via_fdroid),
                        subtitle = stringResource(R.string.update_via_fdroid_subtitle),
                        onClick = onFDroidClick,
                    )
                    FlatDivider()
                    SettingRow(
                        title = stringResource(R.string.update_via_github),
                        subtitle = stringResource(R.string.update_via_github_subtitle),
                        onClick = onGitHubClick,
                    )
                }
            }
        }
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
