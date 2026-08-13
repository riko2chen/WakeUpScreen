package com.symeonchen.wakeupscreen.data

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.symeonchen.wakeupscreen.R

enum class DarkModeInfo(
    val referenceNum: Int,
    @StringRes val labelRes: Int,
    private val nightMode: Int,
) {
    FOLLOW_SYSTEM(0, R.string.follow_system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(1, R.string.dark_mode_light, AppCompatDelegate.MODE_NIGHT_NO),
    DARK(2, R.string.dark_mode_dark, AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        fun getModeFromValue(referenceNum: Int): DarkModeInfo {
            var mode = FOLLOW_SYSTEM
            for (item in values()) {
                if (referenceNum == item.referenceNum) {
                    mode = item
                    break
                }
            }
            return mode
        }
    }

    fun applyDarkMode() {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
