package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import android.os.PowerManager
import android.service.notification.StatusBarNotification


/**
 * Created by SymeonChen on 2020/6/25.
 */
data class ConditionParam(
    var sbn: StatusBarNotification? = null,
    var pm: PowerManager? = null,
    var appContext: Application? = null
)