package com.symeonchen.wakeupscreen.data

import android.content.Context
import com.symeonchen.wakeupscreen.data.SettingViewModelFactory
import com.symeonchen.wakeupscreen.data.StatusViewModelFactory

object ViewModelInjection {

    fun provideStatusViewModelFactory(context: Context): StatusViewModelFactory {
        return StatusViewModelFactory()
    }

    fun provideSettingViewModelFactory(context: Context): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

}