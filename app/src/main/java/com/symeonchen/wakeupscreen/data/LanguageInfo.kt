package com.symeonchen.wakeupscreen.data

import androidx.annotation.StringRes
import com.blankj.utilcode.util.LanguageUtils
import com.symeonchen.wakeupscreen.R
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 *
 * Every real language names itself in its own script, so those labels are the
 * same in any locale and stay hard-coded. Only "follow system" describes a
 * behaviour rather than a language, so it carries a [labelRes] and is shown in
 * whatever language the app is currently running in.
 */
@Suppress("UNUSED_PARAMETER", "SpellCheckingInspection")
enum class LanguageInfo(
    val referenceNum: Int,
    val desc: String,
    private val locale: Locale?,
    @StringRes val labelRes: Int? = null,
) {
    FOLLOW_SYSTEM(0, "Follow System", null, R.string.follow_system),
    ENGLISH(1, "English", Locale.US),
    CHINESE_SIMPLE(2, "简体中文", Locale.CHINA),
    ITALIAN(3, "Italiano", Locale.ITALY),
    JAPANESE(4, "日本語", Locale.JAPAN),
    KOREAN(5, "한국어", Locale.KOREA),
    THAI(6, "ไทย", Locale("th")),
    CHINESE_TRADITIONAL(7, "繁體中文", Locale.TAIWAN);

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