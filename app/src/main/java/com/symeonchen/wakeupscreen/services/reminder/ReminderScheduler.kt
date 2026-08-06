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
 * [AlarmManager.setAndAllowWhileIdle] is used instead of the exact variant on
 * purpose. The exact one needs `SCHEDULE_EXACT_ALARM` on Android 12+, which
 * Play only grants to alarm-clock style apps, and this app's whole pitch is
 * that it asks for nothing but notification access. The cost is that Doze may
 * hold a reminder back by a few minutes, which the settings screen explains.
 */
object ReminderScheduler {

    private const val REQUEST_CODE = 20260806

    /** Arms the next reminder, replacing any alarm already pending. */
    fun scheduleNext(context: Context) {
        val alarmManager = context.alarmManager() ?: return
        val intervalMillis = TimeUnit.MINUTES.toMillis(
            DataInjection.repeatReminderIntervalMinutes.toLong()
        )
        val triggerAt = System.currentTimeMillis() + intervalMillis
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent(context, create = true) ?: return,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Arms the next reminder only if one is not already pending, so that a
     * burst of notifications does not keep pushing the next reminder further
     * away.
     */
    fun ensureScheduled(context: Context) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        if (isScheduled(context)) {
            return
        }
        scheduleNext(context)
    }

    /**
     * Starts a fresh streak: a new notification means the user is being told
     * about something new, so the reminder count starts over.
     */
    fun restartStreak(context: Context) {
        if (!DataInjection.repeatReminderSwitch || !DataInjection.switchOfApp) {
            return
        }
        DataInjection.repeatReminderRoundCount = 0
        scheduleNext(context)
    }

    /** Cancels any pending reminder and resets the streak counter. */
    fun cancel(context: Context) {
        DataInjection.repeatReminderRoundCount = 0
        val alarmManager = context.alarmManager() ?: return
        val pendingIntent = pendingIntent(context, create = false) ?: return
        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isScheduled(context: Context): Boolean = pendingIntent(context, create = false) != null

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
