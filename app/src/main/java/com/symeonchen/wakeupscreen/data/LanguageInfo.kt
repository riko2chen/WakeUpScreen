package com.symeonchen.wakeupscreen.data

@Suppress("UNUSED_PARAMETER")
enum class LanguageInfo(referenceNum: Int) {
    FOLLOW_SYSTEM(0),
    ENGLISH(1),
    CHINESE_SIMPLE(2);

    companion object {

        fun getModeFromValue(referenceNum: Int): LanguageInfo {
            var mode = FOLLOW_SYSTEM
            for (item in values()) {
                if (referenceNum == item.ordinal) {
                    mode = item
                    break
                }
            }
            return mode
        }

    }
}