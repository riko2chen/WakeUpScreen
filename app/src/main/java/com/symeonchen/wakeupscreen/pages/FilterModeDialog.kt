package com.symeonchen.wakeupscreen.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.compose.components.SelectionDialog
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * The app filter node's destination.
 *
 * Unlike every other gate, this one is not configured by a switch on a page
 * that can be opened — the mode lives in a dialog on the settings screen, and
 * the list pages only mean anything once a mode is chosen. Sending the reader
 * straight to a list would skip the decision that actually matters, so the
 * chain opens the mode picker itself and follows through to the list when the
 * chosen mode has one.
 */
@Composable
fun FilterModeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val current = DataInjection.modeOfCurrent

    SelectionDialog(
        title = stringResource(R.string.current_mode),
        options = listOf(
            stringResource(R.string.all_pass),
            stringResource(R.string.white_list),
            stringResource(R.string.black_list),
        ),
        selectedIndex = when (current) {
            CurrentMode.MODE_ALL_NOTIFY -> 0
            CurrentMode.MODE_WHITE_LIST -> 1
            else -> 2
        },
        confirmText = stringResource(R.string.ok),
        onSelect = { index ->
            val selected = CurrentMode.getModeFromValue(index)
            // The settings view model's listener does exactly this and nothing
            // more, so there is no behaviour to route through it.
            DataInjection.modeOfCurrent = selected
            onDismiss()
            if (selected != CurrentMode.MODE_ALL_NOTIFY) {
                FilterListActivity.actionStartWithMode(context, selected)
            }
        },
        onDismiss = onDismiss,
    )
}
