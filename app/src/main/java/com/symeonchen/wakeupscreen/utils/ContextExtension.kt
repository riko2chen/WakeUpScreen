package com.symeonchen.wakeupscreen.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent


/**
 * Created by SymeonChen on 2020/6/11.
 */

inline fun <reified T : Activity> Context.quickStartActivity() {
    val intent = Intent(this, T::class.java)
    if (this is Application) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}