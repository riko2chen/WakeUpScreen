package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = SettingViewModel() as T

}