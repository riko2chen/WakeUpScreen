package com.symeonchen.wakeupscreen.utils

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AppInfoUtils {
    companion object {
        fun getDeviceInfo(context: Context?): String {
            context ?: return ""
            return "\n\n\n\n{\n" +
                    "OS Version : ${Build.VERSION.RELEASE},\n" +
                    "Brand : ${Build.BRAND},\n" +
                    "Model : ${Build.MODEL},\n" +
                    "Version Name : ${getVersionName(context)},\n" +
                    "Version Code : ${getVersionCode(context)},\n" +
                    "}"
        }

        private fun getVersionName(context: Context): String {
            val appContext = context.applicationContext
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            return packageInfo.versionName
        }

        private fun getVersionCode(context: Context): String {
            val appContext = context.applicationContext
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            return PackageInfoCompat.getLongVersionCode(packageInfo).toString()
        }
    }
}