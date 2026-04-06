package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.AboutScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.utils.NotificationUtils
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class AboutThisPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                AboutScreen(
                    onBack = { finish() },
                    onAppIntroduceClick = { quickStartActivity<AppInfoPageActivity>() },
                    onDebugTestClick = {
                        window.decorView.postDelayed({
                            NotificationUtils(applicationContext).sendNotification(
                                1, "This is a test", "Just for testing wakeup screen"
                            )
                        }, 5000)
                    },
                )
            }
        }
    }
}
