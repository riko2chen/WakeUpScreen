package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StatusViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = StatusViewModel() as T

}