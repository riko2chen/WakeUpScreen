package com.symeonchen.wakeupscreen.services.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.symeonchen.wakeupscreen.receiver.ReminderAlarmReceiver
import com.symeonchen.wakeupscreen.utils.DataInjection
import java.util.concurrent.TimeUnit

/**
 * Owns the alarm that drives the repeat reminder.
 *
 * Deliberately a chain of one-shot alarms rather than a repeating one: a
 * repeating alarm is not guaranteed to fire under Doze, and re-arming after
 * every round gives a natural place to ask "are there still unread
 * notifications?" and simply stop when the answer is no.
 *
 * [AlarmManager.setAndAllowWhileIdle] avoids optional exact-alarm special access.
 * Android may delay delivery, which the reminder settings explain.
 */
object ReminderScheduler {

    private const val REQUEST_CODE = 20260806

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)

    internal fun deadline(context: Context): Long = preferences(context).getLong("deadline", 0L)

    /** Arms the next reminder, replacing any alarm already pending. */
    @Synchronized
    fun scheduleNext(context: Context) {
        val intervalMillis = TimeUnit.MINUTES.toMillis(DataInjection.repeatReminderIntervalMinutes.toLong())
        arm(context, System.currentTimeMillis() + intervalMillis)
    }

    /** Retry discovery without resetting the batch count; persist its replacement deadline. */
    @Synchronized
    fun scheduleRetry(context: Context, delayMillis: Long) {
        arm(context, System.currentTimeMillis() + delayMillis.coerceIn(1L, 1500L))
    }

    private fun arm(context: Context, triggerAt: Long) {
        val alarmManager = context.alarmManager() ?: return
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent(context, create = true) ?: return,
            )
            preferences(context).edit().putLong("deadline", triggerAt).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Arms the next reminder only if one is not already pending, so that a
     * burst of notifications does not keep pushing the next reminder further
     * away.
     */
    @Synchronized
    fun ensureScheduled(context: Context, restore: Boolean = false) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (!ReminderPolicy.hasRoundsRemaining(
                DataInjection.repeatReminderRoundCount, DataInjection.repeatReminderMaxRounds
            )) return
        if (!restore && isScheduled(context)) return
        // A PendingIntent can survive a delivered alarm, and an alarm can be lost
        // at reboot. Re-arm the stored deadline instead of treating its token as proof.
        arm(context, ReminderPolicy.nextDeadline(
            deadline(context), System.currentTimeMillis(),
            TimeUnit.MINUTES.toMillis(DataInjection.repeatReminderIntervalMinutes.toLong()),
        ))
    }

    /** Reject stale/duplicate deliveries after a cycle was cancelled or re-armed. */
    @Synchronized
    fun claimDue(context: Context): Boolean {
        val due = deadline(context)
        if (due == 0L || due > System.currentTimeMillis()) return false
        cancelAlarm(context)
        return true
    }

    /** Cancels any pending reminder and resets the streak counter. */
    @Synchronized
    fun cancel(context: Context) {
        DataInjection.repeatReminderRoundCount = 0
        cancelAlarm(context)
    }

    /** Keep the count when the batch reaches its limit, until its notifications are gone. */
    @Synchronized
    fun cancelAlarm(context: Context) {
        preferences(context).edit().remove("deadline").commit()
        val alarmManager = context.alarmManager() ?: return
        val pendingIntent = pendingIntent(context, create = false) ?: return
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isScheduled(context: Context): Boolean = deadline(context) > 0L

    private fun pendingIntent(context: Context, create: Boolean): PendingIntent? {
        val intent = Intent(context.applicationContext, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_REMINDER_ALARM
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        if (!create) {
            flags = flags or PendingIntent.FLAG_NO_CREATE
        }
        return PendingIntent.getBroadcast(context.applicationContext, REQUEST_CODE, intent, flags)
    }

    private fun Context.alarmManager(): AlarmManager? =
        applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
}
