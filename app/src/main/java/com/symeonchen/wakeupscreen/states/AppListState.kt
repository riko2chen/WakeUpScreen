package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
                var applicationInfoList =
                    appContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                for (_app in applicationInfoList) {
                    if ((_app.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                        continue
                    }
//                    LogUtils.d(
//                        TAG,
//                        "package name is ${_app.packageName}\n appName is ${_app.loadLabel(appContext.packageManager)} \n"
//                    )
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