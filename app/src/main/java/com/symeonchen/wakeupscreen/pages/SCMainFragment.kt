package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.uicomponent.views.StatusItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.SCBaseFragment
import com.symeonchen.wakeupscreen.data.MainViewModel
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton.closeNotificationService
import com.symeonchen.wakeupscreen.utils.NotificationStateSingleton.openNotificationService
import com.symeonchen.wakeupscreen.utils.PermissionSingleton
import com.symeonchen.wakeupscreen.utils.ViewModelInjection
import kotlinx.android.synthetic.main.fragment_layout_main.*

class SCMainFragment : SCBaseFragment() {


    lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = ViewModelInjection.provideMainViewModelFactory(context!!)
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)

        initView()
        setListener()
        getData()
    }

    private fun initView() {

        main_item_permission_notification.bindData(
            "权限：读取通知",
            viewModel.permissionOfReadNotification.value ?: false,
            "去设置"
        )

        main_item_service.bindData(
            "服务：亮屏通知",
            viewModel.statusOfService.value ?: false,
            "点击开启"
        )

    }

    private fun setListener() {
        viewModel.statusOfService.observe(this, Observer {
            main_item_service.setState(it)
            main_item_service.setBtnText(if (it) "点击关闭" else "点击开启")
            refresh()
        })

        viewModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
            refresh()
        })

        viewModel.customStatus.observe(this, Observer {
            DataInjection.setSwitchOfCustom(it)
            btn_control.text = if (it) "我要关闭" else "我要开启"
            refresh()
        })

        btn_control.setOnClickListener {
            val status = viewModel.customStatus.value ?: false
            viewModel.customStatus.postValue(!status)
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
                if (viewModel.statusOfService.value == true) {
                    closeNotificationService(context)
                    viewModel.statusOfService.postValue(false)
                } else {
                    openNotificationService(context)
                    viewModel.statusOfService.postValue(true)
                }
                refresh()
            }
        }

        btn_bottom.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_SCSettingFragment)
        }


    }

    private fun getData() {
        viewModel.customStatus.postValue(
            DataInjection.getSwitchOfCustom()
        )

        viewModel.permissionOfReadNotification.postValue(
            PermissionSingleton.hasNotificationListenerServiceEnabled(context!!)
        )
        viewModel.statusOfService.postValue(
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
        viewModel.permissionOfReadNotification.postValue(isPermissionOpen)
        LogUtils.d("isPermissionOpen is $isPermissionOpen")
        return isPermissionOpen
    }

    private fun checkStatus(): Boolean {
        val isServiceOpen = NotificationStateSingleton.isNotificationServiceOpen(context)
        viewModel.statusOfService.postValue(isServiceOpen)
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
            viewModel.permissionOfReadNotification.value,
            viewModel.statusOfService.value,
            viewModel.customStatus.value
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}