package com.symeonchen.wakeupscreen.pages

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.WakeUpTimeScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.model.WakeUpTimeViewModel
import com.symeonchen.wakeupscreen.services.ScLockScreenAccessibilityService
import com.symeonchen.wakeupscreen.utils.ScLog

class WakeUptimeSettingActivity : ScBaseActivity() {

    private companion object {
        const val MODULE = "WakeTimeSetting"
    }

    private var viewModel: WakeUpTimeViewModel? = null

    /**
     * Recomputed in [onResume] because the grant happens in system settings,
     * which gives no callback — the user simply comes back to this screen.
     */
    private val accessibilityGranted = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelInjection.provideWakeUpTimeViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(WakeUpTimeViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val savedTime by viewModel!!.timeOfWakeUpScreen.observeAsState(
                    ScConstant.DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
                )
                val pendingTime by viewModel!!.temporaryTimeOfWakeUpScreen.observeAsState(savedTime)
                val preciseEnabled by viewModel!!.preciseScreenOnSwitch.observeAsState(
                    ScConstant.DEFAULT_PRECISE_SCREEN_ON_SWITCH
                )

                WakeUpTimeScreen(
                    onBack = { finish() },
                    preciseEnabled = preciseEnabled,
                    onPreciseToggle = {
                        val next = !preciseEnabled
                        ScLog.i(MODULE, "precise screen-on switch -> $next")
                        viewModel?.preciseScreenOnSwitch?.postValue(next)
                    },
                    selectedSecond = pendingTime / 1000,
                    onPresetClick = { seconds ->
                        // Only staged here; the save button is what writes it.
                        viewModel?.temporaryTimeOfWakeUpScreen?.postValue(seconds * 1000)
                    },
                    hasUnsavedChanges = pendingTime != savedTime,
                    onSave = { save(pendingTime) },
                    onDiscard = {
                        viewModel?.temporaryTimeOfWakeUpScreen?.postValue(savedTime)
                    },
                    accessibilitySupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
                    accessibilityGranted = accessibilityGranted.value,
                    onGrantAccessibilityClick = { openAccessibilitySettings() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessibilityGranted.value =
            ScLockScreenAccessibilityService.isEnabledInSettings(applicationContext)
    }

    /**
     * Saving no longer finishes the activity: whether the screen may actually
     * be left is the screen's decision, because it holds the exit guard that
     * warns when the feature is on without its accessibility grant.
     */
    private fun save(pendingTime: Long) {
        viewModel?.timeOfWakeUpScreen?.postValue(pendingTime)
        ScLog.i(MODULE, "precise screen-on duration saved: ${pendingTime / 1000}s")
        ToastUtils.showLong(getString(R.string.saved_successfully))
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Exception) {
            ScLog.w(MODULE, "cannot open accessibility settings", e)
            ToastUtils.showLong(getString(R.string.accessibility_open_settings_failed))
        }
    }
}
