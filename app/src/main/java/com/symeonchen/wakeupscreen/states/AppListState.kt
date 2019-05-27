package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.AppInfo

class AppListState {
    companion object {

        private var TAG: String = this::class.java.simpleName ?: ""

        fun getInstalledAppList(context: Context?): ArrayList<AppInfo> {
            var appList = arrayListOf<AppInfo>()
            if (context == null) {
                return appList
            }
            val appContext = context.applicationContext
            try {
                var packageInfo = appContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                for (_packageInfo in packageInfo) {
                    if ((_packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue
                    }
                    LogUtils.d(
                        TAG,
                        "package name is ${_packageInfo.packageName}\n appName is ${_packageInfo.loadLabel(appContext.packageManager)} \n"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                return appList
            }
        }
    }
}