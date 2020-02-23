package com.symeonchen.wakeupscreen.model

/**
 * Created by SymeonChen on 2019-10-27.
 */
object ViewModelInjection {

    fun provideStatusViewModelFactory(): StatusViewModelFactory {
        return StatusViewModelFactory()
    }

    fun provideSettingViewModelFactory(): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

    fun provideWakeUpTimeViewModelFactory(): WakeUpTimeViewModelFactory {
        return WakeUpTimeViewModelFactory()
    }

}