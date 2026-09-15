package com.symeonchen.wakeupscreen.data

/**
 * Created by SymeonChen on 2019-10-27.
 */
data class AppInfo(
    var simpleName: String = "",
    var packageName: String = "",
    var selected: Boolean = false,
    var systemApp: Boolean = false
)