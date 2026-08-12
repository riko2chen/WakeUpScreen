package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import android.os.PowerManager
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.utils.ChannelLogInfo


/**
 * Created by SymeonChen on 2020/6/25.
 */
data class ConditionParam(
    var sbn: StatusBarNotification? = null,
    var pm: PowerManager? = null,
    var appContext: Application? = null,
    /**
     * The channel this notification was posted to, resolved once by the caller.
     * Only the listener service can look this up (it needs the live ranking),
     * so conditions are handed the result instead of asking for it.
     */
    var channelInfo: ChannelLogInfo? = null,
)
