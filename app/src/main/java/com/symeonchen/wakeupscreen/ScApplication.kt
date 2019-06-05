package com.symeonchen.wakeupscreen

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.tencent.mmkv.MMKV
import io.realm.Realm


@Suppress("unused")
class ScApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Realm.init(this)
        filterLog()
    }

    /**
     * Determine whether to print logs according to the environment
     */
    private fun filterLog() {
        if (BuildConfig.DEBUG) {
            LogUtils.getConfig().setConsoleSwitch(false)
        } else {
            LogUtils.getConfig().setConsoleSwitch(true)
        }
    }

}
