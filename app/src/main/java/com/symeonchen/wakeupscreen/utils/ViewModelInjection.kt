package com.symeonchen.wakeupscreen.utils

import android.content.Context
import com.symeonchen.wakeupscreen.data.SettingViewModelFactory
import com.symeonchen.wakeupscreen.data.StatusViewModelFactory

object ViewModelInjection {

    fun provideMainViewModelFactory(context: Context): StatusViewModelFactory {
        return StatusViewModelFactory()
    }

    fun provideSettingViewModelFactory(context: Context): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

}