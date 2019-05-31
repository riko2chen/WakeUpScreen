package com.symeonchen.wakeupscreen.data

import android.graphics.drawable.Drawable

data class AppInfo(
    var simpleName: String = "",
    var packageName: String = "",
    var iconDrawable: Drawable? = null,
    var selected: Boolean = false
)