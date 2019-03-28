package com.symeonchen.wakeupscreen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
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

    lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
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

        iv_status.bindData(
            "当前状态：未开启",
            viewModel.status.value ?: false,
            "开启"
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

        viewModel.status.observe(this, Observer {
            iv_status.bindData(
                if (it == true) "当前状态：已开启" else "当前状态：未开启",
                it == true,
                if (it == true) "关闭" else "开启"
            )
        })

        viewModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
        })


        iv_status.listener = object : OnItemClickListener {
            override fun onItemClick() {
            }

            override fun onBtnClick() {
                if (viewModel.status.value == true) {
                    closeNotificationService(context)
                } else {
                    if (checkPermission()) {
                        openNotificationService(context)
                        viewModel.status.postValue(true)
                        return
                    } else {
                        ToastUtils.showShort("请先开启通知权限")
                    }
                }
                viewModel.status.postValue(false)
                return
            }
        }
    }

    private fun getData() {
        viewModel.permissionOfReadNotification.postValue(
            PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        )
        viewModel.status.postValue(
            NotificationStateHelper.isNotificationServiceOpen(context)
        )
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkStatus()
    }

    private fun checkPermission(): Boolean {
        val isPermissionOpen = PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        viewModel.permissionOfReadNotification.postValue(isPermissionOpen)
        LogUtils.d(TAG, "isPermissionOpen is $isPermissionOpen")
        return isPermissionOpen
    }

    private fun checkStatus(): Boolean {
        val isServiceOpen = NotificationStateHelper.isNotificationServiceOpen(context)
        viewModel.status.postValue(isServiceOpen)
        LogUtils.d(TAG, "isServiceOpen is $isServiceOpen")
        return isServiceOpen
    }

}