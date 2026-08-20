package com.symeonchen.wakeupscreen

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.ScStore
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("unused")
class ScApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ScStore.init(this)
        DataInjection.darkModeSelected.applyDarkMode()
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
