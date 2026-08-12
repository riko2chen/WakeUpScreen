package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.BlockChainScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.services.notification.BlockChain
import com.symeonchen.wakeupscreen.states.PermissionState

class BlockChainPageActivity : ScBaseActivity() {

    private var snapshotVersion by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                // None of what the walk reads is observable, so the version
                // bumped in onResume is what re-runs it after the user comes
                // back from changing one of these settings.
                val version = snapshotVersion
                val hasAccess = remember(version) {
                    PermissionState.hasNotificationListenerServiceEnabled(this)
                }
                val steps = remember(version) {
                    BlockChain.liveSnapshot(application, hasAccess)
                }

                var showModeDialog by remember { mutableStateOf(false) }

                BlockChainScreen(
                    steps = steps,
                    hasNotificationAccess = hasAccess,
                    onBack = { finish() },
                    onNodeClick = { key ->
                        if (ChainNavigation.opensModeDialog(key)) {
                            showModeDialog = true
                        } else {
                            ChainNavigation.navigate(this, key)
                        }
                    },
                    isNodeNavigable = ChainNavigation::isNavigable,
                )

                if (showModeDialog) {
                    FilterModeDialog(
                        onDismiss = {
                            showModeDialog = false
                            // The mode may have just changed under the walk.
                            snapshotVersion++
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sleep windows lapse, chargers get unplugged, and the user may have
        // just toggled the very switch they tapped through to. Nothing here is
        // observable, so the snapshot is retaken on every return to the screen.
        snapshotVersion++
    }
}
