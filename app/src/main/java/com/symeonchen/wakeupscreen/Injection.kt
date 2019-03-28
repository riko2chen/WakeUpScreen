package com.symeonchen.wakeupscreen

import android.content.Context
import com.symeonchen.wakeupscreen.data.MainViewModelFactory

object Injection {

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        return MainViewModelFactory()
    }

}