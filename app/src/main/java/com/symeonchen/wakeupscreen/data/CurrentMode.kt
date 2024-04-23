package com.symeonchen.wakeupscreen.data

/**
 * Created by SymeonChen on 2019-10-27.
 * modified by balkce on 2024-04-23.
 */
@Suppress("UNUSED_PARAMETER")
enum class CurrentMode(name: String, referenceNum: Int) {
    MODE_ALL_NOTIFY("All Notify", 0),
    MODE_WHITE_LIST("Whitelist mode", 1),
    MODE_BLACK_LIST("Blacklist mode", 2);
    MODE_IGNORE_SILENT("Ignore silent", 3);

    companion object {

        fun getModeFromValue(referenceNum: Int): CurrentMode {
            var mode = MODE_ALL_NOTIFY
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
