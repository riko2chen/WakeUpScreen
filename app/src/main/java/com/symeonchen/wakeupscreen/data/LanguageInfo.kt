package com.symeonchen.wakeupscreen.data

import com.blankj.utilcode.util.LanguageUtils
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("UNUSED_PARAMETER", "SpellCheckingInspection")
enum class LanguageInfo(val referenceNum: Int, val desc: String, private val locale: Locale?) {
    FOLLOW_SYSTEM(0, "Follow System", null),
    ENGLISH(1, "English", Locale.US),
    CHINESE_SIMPLE(2, "简体中文", Locale.CHINA),
    ITALIAN(3, "Italiano", Locale.ITALY);

    companion object {
        fun getModeFromValue(referenceNum: Int): LanguageInfo {
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

    fun applyLanguage() {
        if (this.locale == null) {
            LanguageUtils.applySystemLanguage(false)
        } else {
            LanguageUtils.applyLanguage(this.locale, false)
        }
    }
}