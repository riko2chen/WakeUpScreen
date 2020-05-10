package com.symeonchen.wakeupscreen.utils

import android.view.View


/**
 * Created by SymeonChen on 2020/5/7.
 */

@Suppress("unused")
fun View?.setOnAntiShakeClickListener(intervalMillis: Long = 1000, listener: (View) -> Unit) {
    var lastClickTime: Long = 0
    this?.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= intervalMillis) {
            listener.invoke(it)
            lastClickTime = currentTime
        }
    }
}