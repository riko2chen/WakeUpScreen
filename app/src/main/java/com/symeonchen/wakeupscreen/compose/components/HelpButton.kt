package com.symeonchen.wakeupscreen.compose.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symeonchen.wakeupscreen.R

/**
 * The small "?" next to a setting that needs more explanation than a subtitle
 * can carry. Pair it with [HelpDialog].
 */
@Composable
fun HelpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
) {
    GlyphBadge(
        text = "?",
        size = 20.dp,
        fontSize = 13.sp,
        background = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        onClick = onClick,
        onClickLabel = onClickLabel,
    )
}

/**
 * The explanation a [HelpButton] opens. The body scrolls, because these texts
 * run to several paragraphs and a dialog on a short screen would otherwise cut
 * the last one off.
 */
@Composable
fun HelpDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}
