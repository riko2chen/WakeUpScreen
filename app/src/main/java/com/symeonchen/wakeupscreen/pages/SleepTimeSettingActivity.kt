package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.SleepTimeScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection

class SleepTimeSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val range by settingModel.sleepModeTimeRange.observeAsState(
                    Pair(ScConstant.DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR, ScConstant.DEFAULT_SLEEP_MODE_TIME_END_HOUR)
                )

                SleepTimeScreen(
                    onBack = { finish() },
                    beginHour = range.first,
                    endHour = range.second,
                    onBeginHourChange = { hour ->
                        settingModel.sleepModeTimeRange.postValue(Pair(hour, range.second))
                    },
                    onEndHourChange = { hour ->
                        settingModel.sleepModeTimeRange.postValue(Pair(range.first, hour))
                    },
                )
            }
        }
    }
}
