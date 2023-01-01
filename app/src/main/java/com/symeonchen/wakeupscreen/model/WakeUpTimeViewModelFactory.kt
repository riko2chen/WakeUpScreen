package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Created by SymeonChen on 2020-02-22.
 */
class WakeUpTimeViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = WakeUpTimeViewModel() as T

}