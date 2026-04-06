package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.NotificationLogScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.NotificationLogStore

class NotificationLogPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                var logs by remember { mutableStateOf(NotificationLogStore.loadLogs()) }

                NotificationLogScreen(
                    logs = logs,
                    onBack = { finish() },
                    onClear = {
                        NotificationLogStore.clearLogs()
                        logs = emptyList()
                    },
                )
            }
        }
    }
}
