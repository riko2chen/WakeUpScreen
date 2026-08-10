package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.FunctionTestScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.receiver.FunctionTestAlarmReceiver
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class FunctionTestPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                FunctionTestScreen(
                    onBack = { finish() },
                    onWakeTestClick = {
                        toast(R.string.wake_test_toast)
                        FunctionTestAlarmReceiver.schedule(
                            applicationContext, FunctionTestAlarmReceiver.TEST_WAKE
                        )
                    },
                    onReminderTestClick = {
                        toast(R.string.reminder_test_toast)
                        // Runs one reminder round, exactly as the alarm would, so
                        // the feature can be verified without waiting out the
                        // interval.
                        FunctionTestAlarmReceiver.schedule(
                            applicationContext, FunctionTestAlarmReceiver.TEST_REMINDER
                        )
                    },
                    onViewLogsClick = {
                        quickStartActivity<NotificationLogPageActivity>()
                    },
                )
            }
        }
    }

    private fun toast(messageId: Int) {
        Toast.makeText(applicationContext, messageId, Toast.LENGTH_LONG).show()
    }
}
