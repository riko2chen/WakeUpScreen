package com.symeonchen.wakeupscreen.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.services.notification.BlockChain
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ChainNodeState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.TimeOfDayFormatter

/**
 * Display text for the chain nodes.
 *
 * The titles are deliberately shorter than the corresponding setting rows —
 * "持续通知优化(如微信视频、QQ电话)" reads fine as a settings label but not as a
 * node on a diagram that has to be scannable top to bottom.
 */
@Composable
fun chainNodeTitle(key: String): String = when (key) {
    BlockChain.KEY_NOTIFICATION_ACCESS -> stringResource(R.string.chain_node_notification_access)
    BlockReason.APP_SWITCH_OFF -> stringResource(R.string.chain_node_master_switch)
    BlockReason.POCKET_MODE -> stringResource(R.string.chain_node_pocket_mode)
    BlockReason.INTERACTIVE -> stringResource(R.string.chain_node_interactive)
    BlockReason.FILTER_LIST -> stringResource(R.string.chain_node_filter_list)
    BlockReason.LOW_IMPORTANCE -> stringResource(R.string.chain_node_low_importance)
    BlockReason.ONGOING -> stringResource(R.string.chain_node_ongoing)
    BlockReason.SLEEP_MODE -> stringResource(R.string.chain_node_sleep_mode)
    BlockReason.DND -> stringResource(R.string.chain_node_dnd)
    BlockReason.CHARGING -> stringResource(R.string.chain_node_charging)
    BlockChain.KEY_WAKE_UP -> stringResource(R.string.chain_node_wake_up)
    // A gate registered by a newer build than these labels know about. Showing
    // the raw key keeps the diagram complete instead of quietly dropping a step.
    else -> key
}

/**
 * What happened at this node, in one phrase.
 */
@Composable
fun chainStateLabel(key: String, state: ChainNodeState): String {
    if (key == BlockChain.KEY_WAKE_UP) {
        return when (state) {
            ChainNodeState.REACHED -> stringResource(R.string.chain_state_reached)
            else -> stringResource(R.string.chain_state_not_reached)
        }
    }
    return when (state) {
        ChainNodeState.PASSED -> stringResource(R.string.chain_state_passed)
        ChainNodeState.BLOCKED -> stringResource(R.string.chain_state_blocked)
        ChainNodeState.SKIPPED -> stringResource(R.string.chain_state_skipped)
        ChainNodeState.DEPENDS -> stringResource(R.string.chain_state_depends)
        ChainNodeState.NOT_EVALUATED -> stringResource(R.string.chain_state_not_evaluated)
        ChainNodeState.REACHED -> stringResource(R.string.chain_state_passed)
    }
}

/**
 * How this gate is configured right now.
 *
 * Read live from [DataInjection] in both views. On a log entry that makes it
 * the *current* setting rather than the one in force when the notification
 * arrived — the log records which gate blocked, never the values behind it —
 * which is why the log screen labels it as such instead of presenting it as
 * history.
 */
@Composable
fun chainConfigSummary(key: String, hasNotificationAccess: Boolean): String? {
    val openText = stringResource(R.string.already_open)
    val closeText = stringResource(R.string.already_close)
    fun onOff(on: Boolean) = if (on) openText else closeText

    return when (key) {
        BlockChain.KEY_NOTIFICATION_ACCESS -> stringResource(
            if (hasNotificationAccess) R.string.chain_config_granted
            else R.string.chain_config_not_granted
        )
        BlockReason.APP_SWITCH_OFF -> onOff(DataInjection.switchOfApp)
        BlockReason.POCKET_MODE -> onOff(DataInjection.switchOfProximity)
        // No setting sits behind the screen check, so there is nothing to
        // report; the log row shows the verdict alone.
        BlockReason.FILTER_LIST -> stringResource(
            when (DataInjection.modeOfCurrent) {
                CurrentMode.MODE_WHITE_LIST -> R.string.white_list
                CurrentMode.MODE_BLACK_LIST -> R.string.black_list
                else -> R.string.all_pass
            }
        )
        BlockReason.LOW_IMPORTANCE -> onOff(DataInjection.ignoreSilentNotificationSwitch)
        BlockReason.ONGOING -> onOff(
            DataInjection.ongoingOptimize || DataInjection.radicalOngoingOptimize
        )
        BlockReason.SLEEP_MODE -> if (DataInjection.sleepModeBoolean) {
            val segments = DataInjection.sleepModeSegments
            when (segments.size) {
                0 -> stringResource(R.string.sleep_segments_empty)
                1 -> stringResource(
                    R.string.chain_config_sleep_window,
                    TimeOfDayFormatter.formatMinuteOfDay(segments[0].startMinute),
                    TimeOfDayFormatter.formatMinuteOfDay(segments[0].endMinute),
                )
                else -> stringResource(R.string.sleep_segments_count, segments.size)
            }
        } else {
            onOff(false)
        }
        BlockReason.DND -> onOff(DataInjection.dndDetectSwitch)
        BlockReason.CHARGING -> onOff(DataInjection.chargingOnlySwitch)
        BlockChain.KEY_WAKE_UP -> if (DataInjection.preciseScreenOnSwitch) {
            stringResource(R.string.precise_wake_summary_on, DataInjection.preciseScreenOnSecond)
        } else {
            stringResource(R.string.precise_wake_summary_off)
        }
        else -> null
    }
}
