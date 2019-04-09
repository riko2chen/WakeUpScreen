package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.uicomponent.views.StatusItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.SettingViewModel
import com.symeonchen.wakeupscreen.data.StatusViewModel
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton.closeNotificationService
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton.openNotificationService
import com.symeonchen.wakeupscreen.utils.PermissionSingleton
import com.symeonchen.wakeupscreen.utils.ViewModelInjection
import kotlinx.android.synthetic.main.fragment_layout_main.*

class ScMainFragment : ScBaseFragment() {

    private lateinit var statusModel: StatusViewModel
    private lateinit var settingModel: SettingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusFactory = ViewModelInjection.provideStatusViewModelFactory(context!!)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory(context!!)
        statusModel = ViewModelProviders.of(this, statusFactory).get(StatusViewModel::class.java)
        settingModel = ViewModelProviders.of(this, settingFactory).get(SettingViewModel::class.java)

        initView()
        setListener()
        getData()
    }

    private fun initView() {

        main_item_permission_notification.bindData(
            "权限：读取通知",
            statusModel.permissionOfReadNotification.value ?: false,
            "去设置"
        )

        main_item_service.bindData(
            "服务：亮屏通知",
            statusModel.statusOfService.value ?: false,
            "点击开启"
        )

    }

    private fun setListener() {
        statusModel.statusOfService.observe(this, Observer {
            main_item_service.setState(it)
            main_item_service.setBtnText(if (it) "点击关闭" else "点击开启")
            refresh()
        })

        statusModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
            refresh()
        })

        settingModel.switchOfApp.observe(this, Observer {
            btn_control.text = if (it) "我要关闭" else "我要开启"
            refresh()
        })

        btn_control.setOnClickListener {
            val status = settingModel.switchOfApp.value ?: false
            settingModel.switchOfApp.postValue(!status)
        }


        main_item_permission_notification.listener = object : StatusItem.OnItemClickListener {
            override fun onBtnClick() {
                NotificationStateSingleton.openNotificationService(context)
                PermissionSingleton.openReadNotificationSetting(context)
            }

            override fun onItemClick() {
                NotificationStateSingleton.openNotificationService(context)
            }
        }

        main_item_service.listener = object : StatusItem.OnItemClickListener {
            override fun onItemClick() {
            }

            override fun onBtnClick() {
                if (statusModel.statusOfService.value == true) {
                    closeNotificationService(context)
                    statusModel.statusOfService.postValue(false)
                } else {
                    openNotificationService(context)
                    statusModel.statusOfService.postValue(true)
                }
                refresh()
            }
        }


    }

    private fun getData() {

        statusModel.permissionOfReadNotification.postValue(
            PermissionSingleton.hasNotificationListenerServiceEnabled(context!!)
        )
        statusModel.statusOfService.postValue(
            NotificationStateSingleton.isNotificationServiceOpen(context)
        )
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkStatus()
        registerProximitySensor()
    }

    private fun checkPermission(): Boolean {
        val isPermissionOpen = PermissionSingleton.hasNotificationListenerServiceEnabled(context!!)
        statusModel.permissionOfReadNotification.postValue(isPermissionOpen)
        LogUtils.d("isPermissionOpen is $isPermissionOpen")
        return isPermissionOpen
    }

    private fun checkStatus(): Boolean {
        val isServiceOpen = NotificationStateSingleton.isNotificationServiceOpen(context)
        statusModel.statusOfService.postValue(isServiceOpen)
        LogUtils.d("isServiceOpen is $isServiceOpen")
        return isServiceOpen
    }

    private fun registerProximitySensor() {

    }

    private fun refreshState(permissionStatus: Boolean?, serviceStatus: Boolean?, customStatus: Boolean?) {
        btn_control.visibility = View.INVISIBLE
        if (permissionStatus != true) {
            tv_status?.text = "读取通知权限未开启"
            return
        }
        if (serviceStatus != true) {
            tv_status?.text = "后台服务未开启"
            return
        }
        btn_control.visibility = View.VISIBLE
        if (customStatus != true) {
            tv_status?.text = "准备工作已完成，目前关闭中"
            return
        }
        tv_status?.text = "已开启"
    }

    private fun refresh() {
        refreshState(
            statusModel.permissionOfReadNotification.value,
            statusModel.statusOfService.value,
            settingModel.switchOfApp.value
        )
    }

}