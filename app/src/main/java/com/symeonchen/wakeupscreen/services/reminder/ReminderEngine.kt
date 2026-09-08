package com.symeonchen.wakeupscreen.services.reminder

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.LogTrigger
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.utils.ChannelLogInfo
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.ScreenWakeUtils
import com.symeonchen.wakeupscreen.utils.UnreadNotificationUtils

/**
 * Decides what happens each time the repeat-reminder alarm fires, and keeps the
 * alarm in sync with what is actually sitting in the notification shade.
 */
object ReminderEngine {

    /** Claim and evaluate under the same lock as notification and settings callbacks. */
    @Synchronized
    fun onScheduledAlarm(context: Context) {
        if (ReminderScheduler.claimDue(context)) onAlarm(context)
    }

    /**
     * One reminder round. Runs the same condition chain as a freshly posted
     * notification, so pocket mode, sleep mode, Do Not Disturb, charging-only
     * and "screen already on" all outrank the reminder.
     */
    @Synchronized
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

        val unread = active.filter { UnreadNotificationUtils.isUnread(it) }.sortedByDescending { it.postTime }
        if (unread.isEmpty()) {
            endStreak(BlockReason.REMINDER_ALL_READ, packageName = "", unreadCount = 0, context = appContext)
            return
        }
        if (!ReminderPolicy.hasRoundsRemaining(DataInjection.repeatReminderRoundCount, DataInjection.repeatReminderMaxRounds)) {
            ReminderScheduler.cancelAlarm(appContext)
            return
        }

        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val decision = ReminderPolicy.evaluate(unread, check = { notification ->
            val channel = service.channelInfoOf(notification)
            channel to ListenerManager.provideState(
                ConditionParam(notification, powerManager, appContext as? Application, channel)
            )
        }, reason = { (_, result) -> result.blockingCondition }) ?: return
        val representative = decision.first
        val (channelInfo, result) = decision.second

        if (result.state == ConditionState.BLOCK) {
            val reason = result.blockingCondition ?: ""
            // Keep the batch alive without consuming a round. Global gates may
            // clear later, and notification/channel eligibility can change too.
            // Retry once after the interval, never once per blocked candidate.
            log(
                status = if (reason == BlockReason.INTERACTIVE) {
                    LogStatus.SCREEN_ALREADY_ON
                } else {
                    LogStatus.BLOCKED
                },
                reason = reason,
                packageName = representative.packageName,
                round = DataInjection.repeatReminderRoundCount + 1,
                unreadCount = unread.size,
                channelInfo = channelInfo,
            )
            ReminderScheduler.scheduleNext(appContext)
            return
        }

        ScreenWakeUtils.wakeUpScreenForReminder(appContext, powerManager)
        ReminderVibration.vibrateIfAllowed(appContext)

        val round = DataInjection.repeatReminderRoundCount + 1
        DataInjection.repeatReminderRoundCount = round
        log(
            status = LogStatus.WAKED_UP,
            reason = "",
            packageName = representative.packageName,
            round = round,
            unreadCount = unread.size,
            channelInfo = channelInfo,
        )

        val maxRounds = DataInjection.repeatReminderMaxRounds
        if (!ReminderPolicy.hasRoundsRemaining(round, maxRounds)) {
            endStreak(
                BlockReason.REMINDER_MAX_ROUNDS,
                packageName = representative.packageName,
                unreadCount = unread.size,
                context = appContext,
            )
            return
        }
        ReminderScheduler.scheduleNext(appContext)
    }

    /** New posts and updates join the existing batch without changing its count or deadline. */
    @Synchronized
    fun onNotificationPosted(context: Context, sbn: StatusBarNotification?) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            ReminderScheduler.cancel(context)
            return
        }
        if (sbn != null && UnreadNotificationUtils.isUnread(sbn)) {
            ReminderScheduler.ensureScheduled(context.applicationContext)
        } else {
            // An update can turn the last relevant notification into an ongoing one.
            onSettingsChanged(context)
        }
    }

    /** Null means the listener could not provide a snapshot, not that the shade is empty. */
    @Synchronized
    fun onNotificationRemoved(context: Context, active: Array<StatusBarNotification>?) {
        reconcile(context.applicationContext, active)
    }

    /** Restore the same batch and deadline after reconnect, including a reboot. */
    @Synchronized
    fun onListenerConnected(context: Context, active: Array<StatusBarNotification>?) {
        reconcile(context.applicationContext, active, restore = true)
    }

    /** Call after persisting settings that change which apps participate or the master switch. */
    @Synchronized
    fun onSettingsChanged(context: Context) {
        val active = try {
            ScNotificationListenerService.instance?.activeNotifications
        } catch (_: Exception) { null }
        reconcile(context.applicationContext, active)
    }

    private fun reconcile(context: Context, active: Array<StatusBarNotification>?, restore: Boolean = false) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            ReminderScheduler.cancel(context)
        } else if (active != null) {
            if (UnreadNotificationUtils.hasUnread(active)) {
                ReminderScheduler.ensureScheduled(context, restore)
            } else {
                endStreak(BlockReason.REMINDER_ALL_READ, "", 0, context)
            }
        }
    }

    /** Called when the user flips the reminder switch in settings. */
    @Synchronized
    fun onSwitchChanged(context: Context, enabled: Boolean) {
        // Make persistence-before-scheduling explicit, independent of the UI binding.
        DataInjection.repeatReminderSwitch = enabled
        onSettingsChanged(context)
    }

    /**
     * Re-arms an in-flight reminder against the newly chosen interval. Does
     * nothing when no reminder is pending, so changing the setting never starts
     * a streak on its own.
     */
    @Synchronized
    fun onIntervalChanged(context: Context, minutes: Int = DataInjection.repeatReminderIntervalMinutes) {
        DataInjection.repeatReminderIntervalMinutes = minutes
        val appContext = context.applicationContext
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (!ReminderScheduler.isScheduled(appContext)) {
            return
        }
        ReminderScheduler.scheduleNext(appContext)
    }

    @Synchronized
    fun onMaxRoundsChanged(context: Context, rounds: Int) {
        DataInjection.repeatReminderMaxRounds = rounds
        if (!ReminderPolicy.hasRoundsRemaining(DataInjection.repeatReminderRoundCount, DataInjection.repeatReminderMaxRounds)) {
            ReminderScheduler.cancelAlarm(context)
        } else {
            onSettingsChanged(context)
        }
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
        if (reason == BlockReason.REMINDER_MAX_ROUNDS || reason == BlockReason.REMINDER_SERVICE_UNAVAILABLE) {
            ReminderScheduler.cancelAlarm(context)
        } else {
            ReminderScheduler.cancel(context)
        }
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
