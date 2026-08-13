package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.symeonchen.wakeupscreen.services.notification.ChainNodeState

/**
 * One node of the chain, already resolved to display text by the caller.
 *
 * [onClick] is what turns the diagram from an explanation into a shortcut: it
 * jumps to wherever that gate is actually configured.
 */
data class ChainRow(
    val key: String,
    val title: String,
    val subtitle: String? = null,
    val state: ChainNodeState,
    val onClick: (() -> Unit)? = null,
)

/**
 * Draws the chain as a single vertical track.
 *
 * The chain is linear by construction — [com.symeonchen.wakeupscreen.services.notification.ListenerManager]
 * evaluates conditions in order and returns on the first block — so this needs
 * no graph layout, just a rail and a row per node.
 */
@Composable
fun BlockChainView(
    rows: List<ChainRow>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            ChainRowItem(row = row, isLast = index == rows.lastIndex)
        }
    }
}

@Composable
private fun ChainRowItem(row: ChainRow, isLast: Boolean) {
    val accent = chainStateColor(row.state)
    val dimmed = row.state == ChainNodeState.NOT_EVALUATED
    val contentAlpha = if (dimmed) 0.45f else 1f

    val rowBackground = if (row.state == ChainNodeState.BLOCKED) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .let { if (row.onClick != null) it.clickable(onClick = row.onClick) else it }
            .background(rowBackground)
            .padding(horizontal = 16.dp),
    ) {
        // Rail: the marker for this node, plus the connector down to the next.
        Column(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(15.dp))
            ChainDot(state = row.state, color = accent)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            if (dimmed) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)
                            }
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (row.state == ChainNodeState.BLOCKED) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                },
            )
            if (!row.subtitle.isNullOrEmpty()) {
                Text(
                    text = row.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (row.state == ChainNodeState.BLOCKED) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    },
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        if (row.onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f * contentAlpha),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(20.dp),
            )
        }
    }
}

@Composable
private fun ChainDot(state: ChainNodeState, color: Color) {
    // A gate that is armed but cannot decide without a notification reads as an
    // outline, so it is not mistaken for one that definitely lets things past.
    if (state == ChainNodeState.DEPENDS) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .border(2.dp, color, CircleShape)
        )
        return
    }

    val size = if (state == ChainNodeState.BLOCKED) 12.dp else 10.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun chainStateColor(state: ChainNodeState): Color = when (state) {
    ChainNodeState.PASSED, ChainNodeState.REACHED -> MaterialTheme.colorScheme.tertiary
    ChainNodeState.BLOCKED -> MaterialTheme.colorScheme.error
    ChainNodeState.DEPENDS -> MaterialTheme.colorScheme.primary
    ChainNodeState.SKIPPED, ChainNodeState.NOT_EVALUATED ->
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
}

/**
 * Legend for the marker colours. Both chain screens show one — the states are
 * not self-evident, and a wrong reading here is worse than no diagram.
 */
@Composable
fun ChainLegend(
    items: List<Pair<ChainNodeState, String>>,
    modifier: Modifier = Modifier,
) {
    FlowRowCompat(modifier = modifier) {
        items.forEach { (state, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 14.dp, bottom = 6.dp),
            ) {
                ChainDot(state = state, color = chainStateColor(state))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

/**
 * Minimal wrapping row. Avoids depending on the experimental FlowRow so the
 * legend does not tie the module to an opt-in API.
 */
@Composable
private fun FlowRowCompat(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val maxWidth = constraints.maxWidth
        var x = 0
        var y = 0
        var rowHeight = 0
        val positions = mutableListOf<Pair<Int, Int>>()
        placeables.forEach { placeable ->
            if (x + placeable.width > maxWidth && x > 0) {
                x = 0
                y += rowHeight
                rowHeight = 0
            }
            positions.add(x to y)
            x += placeable.width
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        layout(maxWidth, y + rowHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (px, py) = positions[index]
                placeable.placeRelative(px, py)
            }
        }
    }
}

/**
 * Rounded container the chain sits in on both screens.
 */
@Composable
fun ChainSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}
