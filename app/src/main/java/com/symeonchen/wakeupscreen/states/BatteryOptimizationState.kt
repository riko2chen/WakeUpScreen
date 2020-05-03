package com.symeonchen.wakeupscreen.states

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * Created by SymeonChen on 2019-10-27.
 */
class BatteryOptimizationState {
    companion object {
        @SuppressLint("BatteryLife")
        fun openBatteryOptimization(context: Context?) {
            if (context == null) {
                return
            }
            if (!hasIgnoreBatteryOptimization(
                    context
                )
            ) {
                val appContext = context.applicationContext
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:" + appContext.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(intent)
            }


        }

        fun hasIgnoreBatteryOptimization(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val appContext = context.applicationContext
            return try {
                val powerManager = appContext.getSystemService(POWER_SERVICE) as PowerManager
                val hasIgnored = powerManager.isIgnoringBatteryOptimizations(appContext.packageName)
                hasIgnored
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}