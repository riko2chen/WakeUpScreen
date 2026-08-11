package com.symeonchen.wakeupscreen.services.reminder

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.LogTrigger
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.utils.ChannelLogInfo
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.ScreenWakeUtils
import com.symeonchen.wakeupscreen.utils.UnreadNotificationUtils
import com.symeonchen.wakeupscreen.utils.toLogInfo

/**
 * Decides what happens each time the repeat-reminder alarm fires, and keeps the
 * alarm in sync with what is actually sitting in the notification shade.
 */
object ReminderEngine {

    /**
     * One reminder round. Runs the same condition chain as a freshly posted
     * notification, so pocket mode, sleep mode, Do Not Disturb, charging-only
     * and "screen already on" all outrank the reminder.
     */
    fun onAlarm(context: Context) {
        val appContext = context.applicationContext

        if (!DataInjection.switchOfApp) {
            endStreak(BlockReason.APP_SWITCH_OFF, packageName = "", unreadCount = 0, context = appContext)
            return
        }
        if (!DataInjection.repeatReminderSwitch) {
            endStreak(BlockReason.REMINDER_SWITCH_OFF, packageName = "", unreadCount = 0, context = appContext)
            return
        }

        val service = ScNotificationListenerService.instance
        val active = try {
            service?.activeNotifications
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (service == null || active == null) {
            endStreak(
                BlockReason.REMINDER_SERVICE_UNAVAILABLE,
                packageName = "",
                unreadCount = 0,
                context = appContext,
            )
            return
        }

        val snapshot = UnreadNotificationUtils.snapshot(active)
        val representative = snapshot.representative
        if (representative == null) {
            endStreak(BlockReason.REMINDER_ALL_READ, packageName = "", unreadCount = 0, context = appContext)
            return
        }

        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val result = ListenerManager.provideState(
            ConditionParam(representative, powerManager, appContext as? Application)
        )
        val channelInfo = service.channelOf(representative).toLogInfo()

        if (result.state == ConditionState.BLOCK) {
            val reason = result.blockingCondition ?: ""
            // The streak is deliberately kept alive here. Every one of these
            // conditions is temporary — the phone comes out of the pocket, the
            // sleep window ends — and stopping on the first block would mean
            // the user is never reminded again.
            log(
                status = if (reason == BlockReason.INTERACTIVE) {
                    LogStatus.SCREEN_ALREADY_ON
                } else {
                    LogStatus.BLOCKED
                },
                reason = reason,
                packageName = representative.packageName,
                round = DataInjection.repeatReminderRoundCount + 1,
                unreadCount = snapshot.count,
                channelInfo = channelInfo,
            )
            ReminderScheduler.scheduleNext(appContext)
            return
        }

        ScreenWakeUtils.wakeUpScreen(appContext, powerManager)

        val round = DataInjection.repeatReminderRoundCount + 1
        DataInjection.repeatReminderRoundCount = round
        log(
            status = LogStatus.WAKED_UP,
            reason = "",
            packageName = representative.packageName,
            round = round,
            unreadCount = snapshot.count,
            channelInfo = channelInfo,
        )

        val maxRounds = DataInjection.repeatReminderMaxRounds
        if (maxRounds != ScConstant.REPEAT_REMINDER_ROUNDS_UNLIMITED && round >= maxRounds) {
            endStreak(
                BlockReason.REMINDER_MAX_ROUNDS,
                packageName = representative.packageName,
                unreadCount = snapshot.count,
                context = appContext,
            )
            return
        }
        ReminderScheduler.scheduleNext(appContext)
    }

    /**
     * A newly posted notification restarts the streak, so the reminder count
     * starts over and the next reminder is a full interval away.
     *
     * Notifications that do not count as unread — ongoing ones especially — are
     * ignored on purpose: a media player refreshing its notification every few
     * seconds would otherwise push the next reminder back forever.
     */
    fun onNotificationPosted(context: Context, sbn: StatusBarNotification?) {
        sbn ?: return
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (!UnreadNotificationUtils.isUnread(sbn)) {
            return
        }
        ReminderScheduler.restartStreak(context.applicationContext)
    }

    /** Stops reminding as soon as the last unread notification is dismissed. */
    fun onNotificationRemoved(context: Context, active: Array<StatusBarNotification>?) {
        val appContext = context.applicationContext
        if (!DataInjection.repeatReminderSwitch) {
            return
        }
        if (!ReminderScheduler.isScheduled(appContext)) {
            return
        }
        if (UnreadNotificationUtils.hasUnread(active)) {
            return
        }
        endStreak(BlockReason.REMINDER_ALL_READ, packageName = "", unreadCount = 0, context = appContext)
    }

    /**
     * Re-arms the alarm when the listener (re)connects. This is what restores
     * reminders after a reboot, which is why the app needs no boot receiver and
     * no `RECEIVE_BOOT_COMPLETED` permission.
     */
    fun onListenerConnected(context: Context, active: Array<StatusBarNotification>?) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (UnreadNotificationUtils.hasUnread(active)) {
            ReminderScheduler.ensureScheduled(context.applicationContext)
        }
    }

    /** Called when the user flips the reminder switch in settings. */
    fun onSwitchChanged(context: Context, enabled: Boolean) {
        val appContext = context.applicationContext
        if (!enabled) {
            ReminderScheduler.cancel(appContext)
            return
        }
        val active = try {
            ScNotificationListenerService.instance?.activeNotifications
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (UnreadNotificationUtils.hasUnread(active)) {
            ReminderScheduler.restartStreak(appContext)
        }
    }

    /**
     * Re-arms an in-flight reminder against the newly chosen interval. Does
     * nothing when no reminder is pending, so changing the setting never starts
     * a streak on its own.
     */
    fun onIntervalChanged(context: Context) {
        val appContext = context.applicationContext
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (!ReminderScheduler.isScheduled(appContext)) {
            return
        }
        ReminderScheduler.scheduleNext(appContext)
    }

    private fun endStreak(
        reason: String,
        packageName: String,
        unreadCount: Int,
        context: Context,
    ) {
        // Only worth a log line if the streak actually reminded someone;
        // otherwise it is just bookkeeping for a reminder that never fired.
        val round = DataInjection.repeatReminderRoundCount
        if (round > 0) {
            log(
                status = LogStatus.REMINDER_STOPPED,
                reason = reason,
                packageName = packageName,
                round = round,
                unreadCount = unreadCount,
                channelInfo = ChannelLogInfo(),
            )
        }
        ReminderScheduler.cancel(context)
    }

    private fun log(
        status: LogStatus,
        reason: String,
        packageName: String,
        round: Int,
        unreadCount: Int,
        channelInfo: ChannelLogInfo,
    ) {
        try {
            NotificationLogStore.addLog(
                NotificationLogEntry(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    status = status,
                    blockReason = reason,
                    importance = channelInfo.importance,
                    hasSound = channelInfo.hasSound,
                    hasVibration = channelInfo.hasVibration,
                    trigger = LogTrigger.REMINDER,
                    reminderRound = round,
                    unreadCount = unreadCount,
                )
            )
        } catch (_: Exception) {
        }
    }
}
