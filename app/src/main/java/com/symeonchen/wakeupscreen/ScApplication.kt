package com.symeonchen.wakeupscreen

import android.app.Application
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
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
        initToast()
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

    private fun initToast() {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.setBgColor(ContextCompat.getColor(this, R.color.black))
        ToastUtils.setMsgColor(ContextCompat.getColor(this, R.color.white))
        ToastUtils.setMsgTextSize(16)
    }

}
