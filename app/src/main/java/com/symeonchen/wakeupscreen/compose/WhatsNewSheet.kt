package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.data.ChangelogVersion

/**
 * The in-app changelog, as a swipe-away sheet rather than a page: it should
 * interrupt the launch as little as possible, and the same surface doubles as
 * the "Changelog" entry under Settings → About. One scrollable list holds
 * every version passed in, so an update that skipped releases shows each of
 * them in order, and the full-history link sits at the end of the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    versions: List<ChangelogVersion>,
    onDismiss: () -> Unit,
    onViewFullChangelog: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = stringResource(R.string.whats_new_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            ) {
                versions.forEach { version ->
                    item(key = "v${version.versionCode}") {
                        Text(
                            text = version.versionName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 6.dp),
                        )
                    }
                    items(version.bulletRes) { bullet ->
                        Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 3.dp)) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(bullet),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
                item(key = "full_changelog") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(onClick = onViewFullChangelog) {
                            Text(text = stringResource(R.string.changelog_view_full))
                        }
                    }
                }
            }
        }
    }
}
