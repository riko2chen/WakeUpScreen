package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
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
import com.symeonchen.wakeupscreen.data.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.NotificationState
import com.symeonchen.wakeupscreen.utils.NotificationState.Companion.closeNotificationService
import com.symeonchen.wakeupscreen.utils.NotificationState.Companion.openNotificationService
import com.symeonchen.wakeupscreen.utils.PermissionState
import com.symeonchen.wakeupscreen.utils.ProximitySensorSingleton
import kotlinx.android.synthetic.main.fragment_layout_main.*

class ScMainFragment : ScBaseFragment() {

    private lateinit var statusModel: StatusViewModel
    private lateinit var settingModel: SettingViewModel
    private var alertDialog: AlertDialog? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusFactory = ViewModelInjection.provideStatusViewModelFactory()
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        statusModel = ViewModelProviders.of(this, statusFactory).get(StatusViewModel::class.java)
        settingModel = ViewModelProviders.of(this, settingFactory).get(SettingViewModel::class.java)

        initView()
        setListener()
        getData()
    }

    private fun initView() {

        main_item_permission_notification.bindData(
            resources.getString(R.string.permission_of_read_notification),
            statusModel.permissionOfReadNotification.value ?: false,
            resources.getString(R.string.to_setting)
        )

        main_item_service.bindData(
            resources.getString(R.string.service_of_background),
            statusModel.statusOfService.value ?: false,
            resources.getString(R.string.click_to_open)
        )

        main_item_battery_saver.bindData(
            resources.getString(R.string.optimize_of_battery_saver),
            settingModel.fakeSwitchOfBatterySaver.value ?: false,
            resources.getString(R.string.how_to_set)
        )

    }

    private fun setListener() {
        statusModel.statusOfService.observe(this, Observer {
            main_item_service.setState(it)
            main_item_service.setBtnText(resources.getString(if (it) R.string.click_to_close else R.string.click_to_open))
            refresh()
        })

        statusModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
            refresh()
        })

        settingModel.switchOfApp.observe(this, Observer {
            btn_control.text = resources.getString(if (it) R.string.wanna_close else R.string.wanna_open)
            refresh()
        })

        settingModel.fakeSwitchOfBatterySaver.observe(this, Observer {
            main_item_battery_saver.setState(it)
        })

        btn_control.setOnClickListener {
            val status = settingModel.switchOfApp.value ?: false
            settingModel.switchOfApp.postValue(!status)
        }


        main_item_permission_notification.listener = object : StatusItem.OnItemClickListener {
            override fun onBtnClick() {
                openNotificationService(context)
                PermissionState.openReadNotificationSetting(context)
            }

            override fun onItemClick() {
                openNotificationService(context)
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

        main_item_battery_saver.listener = object : StatusItem.OnItemClickListener {
            override fun onItemClick() {
                onBatterySaverClick()
            }

            override fun onBtnClick() {
                onBatterySaverClick()
            }
        }
    }

    private fun onBatterySaverClick() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        alertDialog = builder.setMessage(
            resources.getString(R.string.battery_saver_tips)
        ).setPositiveButton(resources.getString(R.string.i_already_close)) { _, _ ->
            settingModel.fakeSwitchOfBatterySaver.postValue(true)
        }.create().apply { show() }
    }


    private fun getData() {

        statusModel.permissionOfReadNotification.postValue(
            PermissionState.hasNotificationListenerServiceEnabled(context!!)
        )
        statusModel.statusOfService.postValue(
            NotificationState.isNotificationServiceOpen(context)
        )
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkStatus()
        registerProximitySensor()
    }

    private fun checkPermission(): Boolean {
        val isPermissionOpen = PermissionState.hasNotificationListenerServiceEnabled(context!!)
        statusModel.permissionOfReadNotification.postValue(isPermissionOpen)
        LogUtils.d("isPermissionOpen is $isPermissionOpen")
        return isPermissionOpen
    }

    private fun checkStatus(): Boolean {
        val isServiceOpen = NotificationState.isNotificationServiceOpen(context)
        statusModel.statusOfService.postValue(isServiceOpen)
        LogUtils.d("isServiceOpen is $isServiceOpen")
        return isServiceOpen
    }

    private fun registerProximitySensor() {
        if (settingModel.switchOfProximity.value == true && !ProximitySensorSingleton.isRegistered()) {
            ProximitySensorSingleton.registerListener(context)
        }
    }

    private fun refreshState(permissionStatus: Boolean?, serviceStatus: Boolean?, customStatus: Boolean?) {
        btn_control.visibility = View.INVISIBLE
        if (permissionStatus != true) {
            val text =
                resources.getString(R.string.permission_of_read_notification) + resources.getString(R.string.not_open)
            tv_status?.text = text
            return
        }
        if (serviceStatus != true) {
            val text =
                resources.getString(R.string.service_of_background) + resources.getString(R.string.not_open)
            tv_status?.text = text
            return
        }
        btn_control.visibility = View.VISIBLE
        if (customStatus != true) {
            tv_status?.text = resources.getString(R.string.already_close)
            return
        }
        tv_status?.text = resources.getString(R.string.already_open)
    }

    private fun refresh() {
        refreshState(
            statusModel.permissionOfReadNotification.value,
            statusModel.statusOfService.value,
            settingModel.switchOfApp.value
        )
    }

}