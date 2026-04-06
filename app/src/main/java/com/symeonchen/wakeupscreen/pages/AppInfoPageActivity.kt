package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.AppInfoScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme

class AppInfoPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                AppInfoScreen(onBack = { finish() })
            }
        }
    }
}
