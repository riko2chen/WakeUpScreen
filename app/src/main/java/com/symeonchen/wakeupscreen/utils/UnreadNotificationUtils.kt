package com.symeonchen.wakeupscreen.utils

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.conditions.FilterListCondition

/**
 * Decides which of the currently posted notifications still count as "unread".
 *
 * Android has no notion of a read/unread message, so the closest proxy is a
 * notification the user has not dismissed yet: still sitting in the shade means
 * still not dealt with.
 */
object UnreadNotificationUtils {

    /**
     * @param count how many notifications still count as unread.
     * @param representative the newest unread notification, used as the source
     * app shown in the log. Null when [count] is zero.
     */
    data class UnreadSnapshot(
        val count: Int,
        val representative: StatusBarNotification?,
    )

    fun snapshot(active: Array<StatusBarNotification>?): UnreadSnapshot {
        val unread = active?.filter { isUnread(it) }.orEmpty()
        return UnreadSnapshot(
            count = unread.size,
            representative = unread.maxByOrNull { it.postTime },
        )
    }

    fun hasUnread(active: Array<StatusBarNotification>?): Boolean =
        active?.any { isUnread(it) } == true

    /**
     * Ongoing and non-clearable notifications are excluded unconditionally,
     * regardless of the ongoing-detection switches in advanced settings. Those
     * switches only decide whether a *newly posted* notification wakes the
     * screen once; here the stakes are different — a media player or navigation
     * notification never goes away, so honouring the switch would let the
     * reminder wake the screen forever with no way for the user to stop it
     * short of clearing a notification they cannot clear.
     */
    fun isUnread(sbn: StatusBarNotification): Boolean {
        // isClearable already covers both FLAG_ONGOING_EVENT and FLAG_NO_CLEAR.
        if (!sbn.isClearable) {
            return false
        }
        // Group summaries duplicate the children they stand for.
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            return false
        }
        return FilterListCondition().provideResult(sbn) == ConditionState.SUCCESS
    }
}
