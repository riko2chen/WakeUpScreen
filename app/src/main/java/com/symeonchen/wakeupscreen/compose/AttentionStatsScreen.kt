package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.components.SectionLabel
import com.symeonchen.wakeupscreen.data.AppAttention

/**
 * One row of the list, with the label already resolved so the composable never
 * touches the package manager.
 */
data class AttentionRowUi(
    val packageName: String,
    val label: String,
    val wakes: Int,
    val followed: Int,
)

@Composable
fun AttentionStatsScreen(
    onBack: () -> Unit,
    rows: List<AttentionRowUi>,
    suggestions: List<AttentionRowUi>,
    onAddToBlackList: (AttentionRowUi) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.attention_stats_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (suggestions.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.attention_suggestion_title),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
                )
                Card {
                    suggestions.forEachIndexed { index, row ->
                        if (index > 0) FlatDivider()
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Text(
                                text = stringResource(
                                    R.string.attention_suggestion_body,
                                    row.label,
                                    row.wakes,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                            )
                            TextButton(onClick = { onAddToBlackList(row) }) {
                                Text(stringResource(R.string.attention_add_blacklist))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            SectionLabel(
                text = stringResource(R.string.attention_stats_list_title),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 10.dp),
            )
            Card {
                if (rows.isEmpty()) {
                    Text(
                        text = stringResource(R.string.attention_stats_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                } else {
                    rows.forEachIndexed { index, row ->
                        if (index > 0) FlatDivider()
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Text(
                                text = row.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(
                                    R.string.attention_stats_row,
                                    row.wakes,
                                    row.followed,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.attention_stats_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Card(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
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
