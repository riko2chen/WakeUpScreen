package com.symeonchen.wakeupscreen.utils

import android.text.TextUtils

class WhiteListUtils {

    companion object {

        fun getMapFromString(appStr: String): HashMap<String, Int> {
            return getMapFromString(appStr, ",")
        }

        fun saveMapToString(map: HashMap<String, Int>): String {
            return saveMapToString(map, ",")
        }

        private fun getMapFromString(appStr: String, separator: String): HashMap<String, Int> {
            val map = HashMap<String, Int>()
            val appList = appStr.split(separator)
            for (item in appList) {
                map[item] = 1
            }
            return map
        }

        private fun saveMapToString(map: HashMap<String, Int>, separator: String): String {
            var appStr = ""
            for (item in map) {
                if (!TextUtils.isEmpty(appStr)) {
                    appStr += separator
                }
                appStr += item.key
            }
            return appStr
        }
    }
}