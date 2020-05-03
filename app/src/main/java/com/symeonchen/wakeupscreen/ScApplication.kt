package com.symeonchen.wakeupscreen

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.tencent.mmkv.MMKV

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("unused")
class ScApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        filterLog()
    }

    /**
     * Determine whether to print logs according to the environment
     */
    private fun filterLog() {
        if (BuildConfig.DEBUG) {
            LogUtils.getConfig().setConsoleSwitch(true)
        } else {
            LogUtils.getConfig().setConsoleSwitch(false)
        }
    }

}
