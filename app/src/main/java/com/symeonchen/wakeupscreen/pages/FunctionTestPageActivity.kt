package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.FunctionTestScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.utils.NotificationUtils
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class FunctionTestPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                FunctionTestScreen(
                    onBack = { finish() },
                    onWakeTestClick = {
                        Toast.makeText(
                            applicationContext,
                            R.string.wake_test_toast,
                            Toast.LENGTH_LONG
                        ).show()
                        window.decorView.postDelayed({
                            NotificationUtils(applicationContext).sendNotification(
                                1, "This is a test", "Just for testing wakeup screen"
                            )
                        }, 10000)
                    },
                    onViewLogsClick = {
                        quickStartActivity<NotificationLogPageActivity>()
                    },
                )
            }
        }
    }
}
