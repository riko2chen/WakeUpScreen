package com.symeonchen.wakeupscreen.utils

import android.app.NotificationChannel
import android.os.Build

/**
 * The channel facts shown in the notification log, extracted once so the
 * notification path and the repeat-reminder path report them identically.
 */
data class ChannelLogInfo(
    val importance: Int = -1,
    val hasSound: Boolean? = null,
    val hasVibration: Boolean? = null,
)

fun NotificationChannel?.toLogInfo(): ChannelLogInfo {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || this == null) {
        return ChannelLogInfo()
    }
    return try {
        ChannelLogInfo(
            importance = importance,
            hasSound = sound != null && sound.toString().isNotEmpty(),
            hasVibration = shouldVibrate(),
        )
    } catch (_: Exception) {
        ChannelLogInfo()
    }
}
