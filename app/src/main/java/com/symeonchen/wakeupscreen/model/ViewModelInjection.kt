package com.symeonchen.wakeupscreen.model

object ViewModelInjection {

    fun provideStatusViewModelFactory(): StatusViewModelFactory {
        return StatusViewModelFactory()
    }

    fun provideSettingViewModelFactory(): SettingViewModelFactory {
        return SettingViewModelFactory()
    }

}