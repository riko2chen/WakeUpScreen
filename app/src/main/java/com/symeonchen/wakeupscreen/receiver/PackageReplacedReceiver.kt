package com.symeonchen.wakeupscreen.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symeonchen.wakeupscreen.states.NotificationState

/**
 * Created by SymeonChen on 2019-10-27.
 */
class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        intent.action ?: return
        if (intent.action.equals("android.intent.action.MY_PACKAGE_REPLACED", true)) {
            NotificationState.openNotificationService(context)
        }
    }
}