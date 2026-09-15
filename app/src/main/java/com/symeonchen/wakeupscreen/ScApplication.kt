package com.symeonchen.wakeupscreen

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.ScStore
import com.symeonchen.wakeupscreen.states.PostureSensorCoordinator
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
        // Pocket mode has to outlive the UI: the listener service is what
        // actually decides wakes, and it often comes back in a process that
        // never opened the home fragment.
        PostureSensorCoordinator.sync(this)
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
