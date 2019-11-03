package com.symeonchen.wakeupscreen.utils

import android.text.TextUtils
import com.symeonchen.wakeupscreen.data.AppInfo

/**
 * Created by SymeonChen on 2019-10-27.
 */
class FilterListUtils {

    companion object {

        fun getMapFromString(appStr: String): HashMap<String, Int> {
            return getMapFromString(appStr, ",")
        }

        fun saveMapToString(map: HashMap<String, Int>): String {
            return saveMapToString(map, ",")
        }

        fun splitWithSelected(
            appList: List<AppInfo>,
            map: HashMap<String, Int>?
        ): MutableList<AppInfo> {
            val dataList: MutableList<AppInfo> = mutableListOf()
            val resultWithSelected: MutableList<AppInfo> = arrayListOf()
            val resultWithUnselected: MutableList<AppInfo> = arrayListOf()
            for (item in appList) {
                if (map?.containsKey(item.packageName) == true) {
                    item.selected = true
                    resultWithSelected.add(item)
                } else {
                    resultWithUnselected.add(item)
                }
            }
            dataList.clear()
            dataList.addAll(resultWithSelected.sortedBy {
                it.simpleName
            })
            dataList.addAll(resultWithUnselected.sortedBy {
                it.simpleName
            })
            return dataList
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