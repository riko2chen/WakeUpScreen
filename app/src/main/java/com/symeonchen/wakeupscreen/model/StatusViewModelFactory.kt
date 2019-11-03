package com.symeonchen.wakeupscreen.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by SymeonChen on 2019-10-27.
 */
class StatusViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = StatusViewModel() as T

}