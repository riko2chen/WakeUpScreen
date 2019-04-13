package com.symeonchen.wakeupscreen.data

object ViewModelInjection {

    fun provideStatusViewModelFactory(): StatusViewModelFactory {
        return StatusViewModelFactory()
    }

    fun provideSettingViewModelFactory(): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

}