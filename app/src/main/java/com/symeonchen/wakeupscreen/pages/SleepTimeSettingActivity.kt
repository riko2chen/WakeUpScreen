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
import com.symeonchen.wakeupscreen.data.SleepSchedule
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.DataInjection

class SleepTimeSettingActivity : ScBaseActivity() {

    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        setContent {
            WakeUpScreenTheme {
                val segments by settingModel.sleepModeSegments.observeAsState(emptyList())

                SleepTimeScreen(
                    onBack = { finish() },
                    segments = segments,
                    maxSegments = ScConstant.MAX_SLEEP_SEGMENTS,
                    onAdd = { segment ->
                        settingModel.sleepModeSegments.postValue(
                            SleepSchedule.add(segments, segment)
                        )
                    },
                    onDelete = { segment ->
                        settingModel.sleepModeSegments.postValue(
                            SleepSchedule.remove(segments, segment)
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingModel.sleepModeSegments.postValue(DataInjection.sleepModeSegments)
    }
}
