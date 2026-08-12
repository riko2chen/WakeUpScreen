package com.symeonchen.wakeupscreen.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.BlockChainView
import com.symeonchen.wakeupscreen.compose.components.ChainLegend
import com.symeonchen.wakeupscreen.compose.components.ChainRow
import com.symeonchen.wakeupscreen.compose.components.ChainSurface
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.services.notification.ChainNodeState
import com.symeonchen.wakeupscreen.services.notification.ChainStep

/**
 * The chain as it stands right now: which gates are armed, and which one of
 * them would stop a notification arriving at this moment.
 *
 * Answers "is my configuration going to let anything through", where the copy
 * in the notification log answers "why did that one not wake the screen".
 */
@Composable
fun BlockChainScreen(
    steps: List<ChainStep>,
    hasNotificationAccess: Boolean,
    onBack: () -> Unit,
    onNodeClick: (String) -> Unit,
    isNodeNavigable: (String) -> Boolean,
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ComposeToolbar(
            title = stringResource(R.string.chain_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.chain_live_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ChainLegend(
                items = listOf(
                    ChainNodeState.PASSED to stringResource(R.string.chain_legend_pass),
                    ChainNodeState.BLOCKED to stringResource(R.string.chain_legend_block),
                    ChainNodeState.DEPENDS to stringResource(R.string.chain_legend_depends),
                    ChainNodeState.SKIPPED to stringResource(R.string.chain_legend_off),
                ),
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            )

            ChainSurface {
                BlockChainView(
                    rows = steps.map { step ->
                        val config = chainConfigSummary(step.key, hasNotificationAccess)
                        val stateLabel = chainStateLabel(step.key, step.state)
                        ChainRow(
                            key = step.key,
                            title = chainNodeTitle(step.key),
                            subtitle = liveSubtitle(config, stateLabel, step.state),
                            state = step.state,
                            onClick = if (isNodeNavigable(step.key)) {
                                { onNodeClick(step.key) }
                            } else {
                                null
                            },
                        )
                    }
                )
            }

            Text(
                text = stringResource(R.string.chain_live_footer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * The configuration already says "off" for a skipped gate and "on" for one
 * that passed, so the state phrase is only worth the line when it adds
 * something the reader cannot infer from the colour.
 */
private fun liveSubtitle(
    config: String?,
    stateLabel: String,
    state: ChainNodeState,
): String = when (state) {
    ChainNodeState.SKIPPED, ChainNodeState.PASSED -> config ?: stateLabel
    else -> listOfNotNull(config, stateLabel).joinToString(" · ")
}
