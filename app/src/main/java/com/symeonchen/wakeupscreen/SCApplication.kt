package com.symeonchen.wakeupscreen

import android.app.Application
import com.tencent.mmkv.MMKV

class SCApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }

}
