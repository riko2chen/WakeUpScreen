package com.symeonchen.wakeupscreen.data

import android.graphics.drawable.Drawable

/**
 * Created by SymeonChen on 2019-10-27.
 */
data class AppInfo(
    var simpleName: String = "",
    var packageName: String = "",
    var iconDrawable: Drawable? = null,
    var selected: Boolean = false
)