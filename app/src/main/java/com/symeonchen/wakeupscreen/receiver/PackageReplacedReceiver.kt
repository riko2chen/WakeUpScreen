package com.symeonchen.wakeupscreen.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.symeonchen.wakeupscreen.states.NotificationState

class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        intent.action ?: return
        if (intent.action.equals("android.intent.action.MY_PACKAGE_REPLACED", true)) {
            NotificationState.openNotificationService(context)
        }
    }
}