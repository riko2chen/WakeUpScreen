package com.symeonchen.wakeupscreen.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
import com.symeonchen.wakeupscreen.utils.NotificationUtils
import com.symeonchen.wakeupscreen.utils.ScLog

/**
 * Runs a function test after the countdown the tester needs to switch the screen
 * off.
 *
 * The countdown is armed twice, and whichever arrives first runs the test:
 *
 * - a delayed message, which lands on time but is lost if the process is
 *   frozen — One UI freezes a backgrounded app a few seconds after the screen
 *   goes off, and disables its wake locks too, so holding one does not help;
 * - an alarm, which always arrives because the broadcast thaws the process, but
 *   is inexact. It has been observed nearly ten seconds late, and a test that
 *   fires long after the tester gave up looks exactly like a test that failed.
 *
 * None of this applies to the features themselves. They are entered from a
 * system callback — a posted notification, the reminder alarm — which does the
 * thawing on its own; only a test kicked off from a paused activity has no such
 * trigger behind it.
 */
class FunctionTestAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FUNCTION_TEST_ALARM =
            "com.symeonchen.wakeupscreen.action.FUNCTION_TEST_ALARM"

        private const val EXTRA_TEST = "com.symeonchen.wakeupscreen.extra.TEST"
        private const val REQUEST_CODE = 20260807
        private const val MODULE = "FunctionTest"

        const val TEST_WAKE = "wake"
        const val TEST_REMINDER = "reminder"

        /** Time the tester gets to switch the screen off before the test fires. */
        const val DELAY_MS = 10000L

        private val handler = Handler(Looper.getMainLooper())

        /** The test still waiting to run, or null once one of the two paths has claimed it. */
        private var pendingTest: String? = null

        fun schedule(context: Context, test: String) {
            val appContext = context.applicationContext
            synchronized(this) { pendingTest = test }

            try {
                val alarmManager =
                    appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                // The idle-tolerant but inexact alarm the reminder also uses; the
                // exact variant needs SCHEDULE_EXACT_ALARM, which this app does
                // not ask for.
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + DELAY_MS,
                    pendingIntent(appContext),
                )
            } catch (e: Exception) {
                ScLog.w(MODULE, "could not arm the backup alarm", e)
            }

            handler.postDelayed({ run(appContext, test, source = "timer") }, DELAY_MS)
            ScLog.i(MODULE, "'$test' test scheduled in ${DELAY_MS}ms")
        }

        private fun run(context: Context, test: String?, source: String) {
            synchronized(this) {
                if (test == null || pendingTest != test) {
                    return
                }
                pendingTest = null
            }
            // Whichever path lost the race must not fire a second time later.
            cancelAlarm(context)

            ScLog.i(MODULE, "running '$test' test (via $source)")
            when (test) {
                TEST_WAKE -> NotificationUtils(context).sendNotification(
                    1, "This is a test", "Just for testing wakeup screen"
                )

                TEST_REMINDER -> ReminderEngine.onAlarm(context)

                else -> ScLog.w(MODULE, "unknown test '$test'")
            }
        }

        private fun cancelAlarm(context: Context) {
            try {
                val alarmManager =
                    context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.cancel(pendingIntent(context))
            } catch (e: Exception) {
                ScLog.w(MODULE, "could not cancel the backup alarm", e)
            }
        }

        private fun pendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, FunctionTestAlarmReceiver::class.java).apply {
                action = ACTION_FUNCTION_TEST_ALARM
            }
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action != ACTION_FUNCTION_TEST_ALARM) {
            return
        }
        run(context.applicationContext, pendingTest, source = "alarm")
    }
}
