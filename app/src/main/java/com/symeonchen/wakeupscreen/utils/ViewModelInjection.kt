package com.symeonchen.wakeupscreen.utils

import android.content.Context
import com.symeonchen.wakeupscreen.data.MainViewModelFactory
import com.symeonchen.wakeupscreen.data.SettingViewModelFactory

object ViewModelInjection {

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        return MainViewModelFactory()
    }

    fun provideSettingViewModelFactory(context: Context): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

}