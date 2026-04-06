package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.WakeUpTimeScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.model.WakeUpTimeViewModel

class WakeUptimeSettingActivity : ScBaseActivity() {

    private var viewModel: WakeUpTimeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelInjection.provideWakeUpTimeViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(WakeUpTimeViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val tempTime by viewModel!!.temporaryTimeOfWakeUpScreen.observeAsState(3000L)
                val currentSecond = tempTime / 1000
                var inputText by remember(currentSecond) { mutableStateOf("$currentSecond") }

                WakeUpTimeScreen(
                    onBack = { finish() },
                    onSave = { tryToSaveWakeUpTime(inputText) },
                    selectedSecond = currentSecond,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onPresetClick = { sec ->
                        viewModel?.temporaryTimeOfWakeUpScreen?.postValue(sec * 1000)
                    },
                )
            }
        }
    }

    private fun tryToSaveWakeUpTime(inputText: String) {
        val etNum = try {
            inputText.toLong()
        } catch (e: NumberFormatException) {
            -1L
        }
        if (etNum <= 0) {
            ToastUtils.showLong(getString(R.string.invalid_number))
            viewModel?.temporaryTimeOfWakeUpScreen?.postValue(3000L)
            return
        }
        viewModel?.timeOfWakeUpScreen?.postValue(etNum * 1000)
        ToastUtils.showLong(getString(R.string.saved_successfully))
        finish()
    }
}
