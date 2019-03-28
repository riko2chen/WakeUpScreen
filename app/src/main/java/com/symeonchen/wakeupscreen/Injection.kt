package com.symeonchen.wakeupscreen

import android.content.Context
import com.symeonchen.wakeupscreen.viewmodels.MainViewModelFactory

object Injection {

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        return MainViewModelFactory()
    }

}