package com.symeonchen.wakeupscreen.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine

/**
 * Receives the repeat-reminder alarm. Kept trivial on purpose — everything the
 * reminder does lives in [ReminderEngine] so the function-test screen can run
 * exactly the same code path without waiting for an alarm.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER_ALARM = "com.symeonchen.wakeupscreen.action.REMINDER_ALARM"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action != ACTION_REMINDER_ALARM) {
            return
        }
        ReminderEngine.onAlarm(context)
    }
}
