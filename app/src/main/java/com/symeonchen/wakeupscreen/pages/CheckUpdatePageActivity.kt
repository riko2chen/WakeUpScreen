package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.CheckUpdateScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.utils.UpdateChannelTools

class CheckUpdatePageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                CheckUpdateScreen(
                    onBack = { finish() },
                    onPlayStoreClick = { UpdateChannelTools.openPlayStore(this) },
                    onFDroidClick = { UpdateChannelTools.openFDroid(this) },
                    onGitHubClick = { UpdateChannelTools.openGitHubReleases(this) },
                )
            }
        }
    }
}
