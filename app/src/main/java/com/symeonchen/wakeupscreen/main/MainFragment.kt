package com.symeonchen.wakeupscreen.main

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.symeonchen.wakeupscreen.NotificationMonitor
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.utils.PermissionHelper
import com.symeonchen.wakeupscreen.view.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_layout_main.*

class MainFragment : Fragment() {

    var status: MutableLiveData<Boolean> = MutableLiveData()

    var permisson: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permisson = PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        main_item_permission_notification.bindData(
            "权限：读取通知",
            permisson
        )
        main_item_permission_notification.listener = object : OnItemClickListener {
            override fun onClick() {
                if (!permisson) {
                    PermissionHelper.openReadNotificationSetting(context!!)
                }
            }
        }

        status.observe(this, Observer {
            iv_status.setBackgroundColor(
                if (it == true) Color.parseColor("#45CE73")
                else Color.parseColor("#A52844")
            )
        })


        iv_status.isClickable = true
        iv_status.setOnClickListener {
            if (status.value == true) {
                closeNotificationService(context!!)
                status.postValue(false)
            } else {
                openNotificationService(context!!)
                status.postValue(true)
            }
        }


    }

    private fun closeNotificationService(context: Context) {
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                context, NotificationMonitor::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
    }

    private fun openNotificationService(context: Context) {
        val pm = context.packageManager
        closeNotificationService(context)

        pm.setComponentEnabledSetting(
            ComponentName(
                context, NotificationMonitor::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

}