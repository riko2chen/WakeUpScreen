package com.symeonchen.wakeupscreen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.Injection
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.data.MainViewModel
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper.closeNotificationService
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper.openNotificationService
import com.symeonchen.wakeupscreen.utils.PermissionHelper
import com.symeonchen.wakeupscreen.view.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_layout_main.*

class MainFragment : Fragment() {

    companion object {
        var TAG: String = this::class.java.simpleName
    }

    private var isFirstInit = true
    private var mStatus: TextView? = null
    lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_layout_main, container, false)
        mStatus = v.findViewById(R.id.tv_status)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = Injection.provideMainViewModelFactory(context!!)
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
        main_item_permission_notification.listener = object : OnItemClickListener {
            override fun onBtnClick() {
                if (isFirstInit) {
                    isFirstInit = false
                    NotificationStateHelper.openNotificationService(context)
                }
                PermissionHelper.openReadNotificationSetting(context)
            }

            override fun onItemClick() {

            }
        }

        viewModel.statusOfService.observe(this, Observer {
            main_item_service.setState(it == true)
            main_item_service.setBtnText(if (it == true) "点击关闭" else "点击开启")
        })

        viewModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
        })


        main_item_service.listener = object : OnItemClickListener {
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
    }

    private fun getData() {
        viewModel.permissionOfReadNotification.postValue(
            PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        )
        viewModel.statusOfService.postValue(
            NotificationStateHelper.isNotificationServiceOpen(context)
        )
    }

    override fun onResume() {
        super.onResume()
        var permissionStatus = checkPermission()
        var serviceStatus = checkStatus()
        refreshState(permissionStatus, serviceStatus)
    }

    private fun checkPermission(): Boolean {
        val isPermissionOpen = PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        viewModel.permissionOfReadNotification.postValue(isPermissionOpen)
        LogUtils.d(TAG, "isPermissionOpen is $isPermissionOpen")
        return isPermissionOpen
    }

    private fun checkStatus(): Boolean {
        val isServiceOpen = NotificationStateHelper.isNotificationServiceOpen(context)
        viewModel.statusOfService.postValue(isServiceOpen)
        LogUtils.d(TAG, "isServiceOpen is $isServiceOpen")
        return isServiceOpen
    }

    private fun refreshState(permissionStatus: Boolean, serviceStatus: Boolean) {
        var text = "当前状态："
        if (permissionStatus && serviceStatus) {
            text += "已开启"
        } else {
            text += "未开启"
        }
        tv_status?.text = text
    }

    private fun refresh() {
        refreshState(checkPermission(), checkStatus())
    }
}