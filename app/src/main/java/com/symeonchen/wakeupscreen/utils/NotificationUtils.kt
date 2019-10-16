package com.symeonchen.wakeupscreen.utils

import android.annotation.TargetApi
import android.app.Notification.PRIORITY_DEFAULT
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import com.symeonchen.wakeupscreen.R


class NotificationUtils(appContext: Context) : ContextWrapper(appContext) {

    companion object {

        val CHANNEL_ID = "default"
        private val CHANNEL_NAME = "Default Channel"
        private val CHANNEL_DESCRIPTION = "this is default channel!"
    }

    private var mManager: NotificationManager? = null

    private val manager: NotificationManager?
        get() {
            if (mManager == null) {
                mManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        channel.canBypassDnd()
        channel.enableLights(false)
        channel.lockscreenVisibility = VISIBILITY_SECRET
        channel.lightColor = Color.RED
        channel.canShowBadge()
        channel.enableVibration(true)
        channel.audioAttributes
        channel.group
        channel.setBypassDnd(true)
        channel.vibrationPattern = longArrayOf(100, 100, 200)
        channel.shouldShowLights()
        manager!!.createNotificationChannel(channel)
    }

    /**
     * 发送通知
     */
    fun sendNotification(title: String, content: String) {
        val builder = getNotification(title, content)
        manager!!.notify(1, builder.build())
    }

    private fun getNotification(title: String, content: String): NotificationCompat.Builder {
        var builder: NotificationCompat.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        } else {
            builder = NotificationCompat.Builder(applicationContext)
            builder.priority = PRIORITY_DEFAULT
        }
        //标题
        builder.setContentTitle(title)
        //文本内容
        builder.setContentText(content)
        //小图标
        builder.setSmallIcon(R.mipmap.ic_launcher)
        //设置点击信息后自动清除通知
        builder.setAutoCancel(true)
        return builder
    }

    /**
     * 发送通知
     */
    fun sendNotification(notifyId: Int, title: String, content: String) {
        val builder = getNotification(title, content)
        manager!!.notify(notifyId, builder.build())
    }

}