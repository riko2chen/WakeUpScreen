package com.symeonchen.wakeupscreen.utils

import android.util.Log

/**
 * Single logcat tag for the screen-control path, so a whole precise screen-on
 * window can be followed with `adb logcat -s WakeUpScreen`.
 *
 * Ticks are logged at DEBUG because a long window emits one every two seconds;
 * everything that marks a decision or a state change is INFO.
 */
object ScLog {

    const val TAG = "WakeUpScreen"

    fun d(module: String, message: String) {
        Log.d(TAG, "[$module] $message")
    }

    fun i(module: String, message: String) {
        Log.i(TAG, "[$module] $message")
    }

    fun w(module: String, message: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.w(TAG, "[$module] $message")
        } else {
            Log.w(TAG, "[$module] $message", throwable)
        }
    }
}
