package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.symeonchen.wakeupscreen.data.AppInfo

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AppListState {
    companion object {

        @Suppress("unused")
        private var TAG: String = this::class.java.simpleName ?: ""

        fun getInstalledAppList(
            context: Context?,
            includeSystemApp: Boolean = false
        ): ArrayList<AppInfo> {
            val appList = arrayListOf<AppInfo>()
            if (context == null) {
                return appList
            }
            val appContext = context.applicationContext
            try {
                val applicationInfoList =
                    appContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                for (_app in applicationInfoList) {
                    if (!includeSystemApp && ((_app.flags and ApplicationInfo.FLAG_SYSTEM) != 0)) {
                        continue
                    }
                    val appInfo = AppInfo()
                    appInfo.simpleName = _app.loadLabel(appContext.packageManager).toString()
                    appInfo.packageName = _app.packageName
                    appInfo.iconDrawable = _app.loadIcon(appContext.packageManager)
                    appList.add(appInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                return appList
            }
        }


    }
}