package com.symeonchen.wakeupscreen.pages

import android.content.Context
import com.symeonchen.wakeupscreen.services.notification.BlockChain
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.states.PermissionState
import com.symeonchen.wakeupscreen.utils.quickStartActivity

/**
 * Where each chain node is actually configured.
 *
 * This is what makes the diagram worth more than an explanation: the settings
 * behind the chain live across four different screens, and a node the reader
 * has just been told is responsible should take one tap to reach.
 */
object ChainNavigation {

    fun isNavigable(key: String): Boolean = when (key) {
        BlockChain.KEY_NOTIFICATION_ACCESS -> true
        BlockReason.POCKET_MODE,
        BlockReason.LOW_IMPORTANCE,
        BlockReason.ONGOING,
        BlockReason.SLEEP_MODE,
        BlockReason.DND,
        BlockReason.CHARGING -> true
        // Reachable in every mode, "all pass" included: that is precisely the
        // state where the reader needs to arm the filter, and the mode picker
        // is buried in a dialog on another screen.
        BlockReason.FILTER_LIST -> true
        BlockChain.KEY_WAKE_UP -> true
        // The master switch lives on the home screen the user came from, and
        // the interactive gate has no setting at all.
        else -> false
    }

    /**
     * Whether this node's destination is a dialog the screen has to host
     * itself rather than an activity [navigate] can start.
     */
    fun opensModeDialog(key: String): Boolean = key == BlockReason.FILTER_LIST

    fun navigate(context: Context, key: String) {
        when (key) {
            BlockChain.KEY_NOTIFICATION_ACCESS ->
                PermissionState.openReadNotificationSetting(context)

            BlockReason.POCKET_MODE,
            BlockReason.LOW_IMPORTANCE,
            BlockReason.ONGOING,
            BlockReason.SLEEP_MODE,
            BlockReason.DND,
            BlockReason.CHARGING ->
                context.quickStartActivity<AdvanceSettingPageActivity>()

            // BlockReason.FILTER_LIST is absent on purpose — see
            // [opensModeDialog] and FilterModeDialog.

            BlockChain.KEY_WAKE_UP ->
                context.quickStartActivity<WakeUptimeSettingActivity>()
        }
    }
}
