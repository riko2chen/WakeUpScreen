package com.symeonchen.wakeupscreen.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.symeonchen.wakeupscreen.Injection
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper.Companion.closeNotificationService
import com.symeonchen.wakeupscreen.utils.NotificationStateHelper.Companion.openNotificationService
import com.symeonchen.wakeupscreen.utils.PermissionHelper
import com.symeonchen.wakeupscreen.view.OnItemClickListener
import com.symeonchen.wakeupscreen.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_layout_main.*

class MainFragment : Fragment() {

    companion object {
        var TAG: String = this::class.java.simpleName
    }

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
            viewModel.permissionOfReadNotification.value ?: false
        )
    }

    private fun setListener() {
        main_item_permission_notification.listener = object : OnItemClickListener {
            override fun onBtnClick() {
                NotificationStateHelper.openNotificationService(context!!)
                PermissionHelper.openReadNotificationSetting(context!!)
            }

            override fun onItemClick() {

            }
        }

        viewModel.status.observe(this, Observer {
            iv_status.setBackgroundColor(
                if (it == true) ContextCompat.getColor(context!!, R.color.success)
                else ContextCompat.getColor(context!!, R.color.fail)
            )
        })

        viewModel.permissionOfReadNotification.observe(this, Observer {
            main_item_permission_notification.setState(it)
        })


        iv_status.isClickable = true
        iv_status.setOnClickListener {
            if (viewModel.status.value == true) {
                closeNotificationService(context!!)
                viewModel.status.postValue(false)
            } else {
                openNotificationService(context!!)
                viewModel.status.postValue(true)
            }
        }
    }

    private fun getData() {
        viewModel.permissionOfReadNotification.postValue(
            PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        )
        viewModel.status.postValue(
            NotificationStateHelper.isNotificationServiceOpen(context!!)
        )
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkStatus()
    }

    private fun checkPermission() {
        val isPermissionOpen = PermissionHelper.hasNotificationListenerServiceEnabled(context!!)
        viewModel.permissionOfReadNotification.postValue(isPermissionOpen)
        Log.d(TAG, "isPermissionOpen is $isPermissionOpen")
    }

    private fun checkStatus() {
        val isServiceOpen = NotificationStateHelper.isNotificationServiceOpen(context!!)
        viewModel.status.postValue(isServiceOpen)
        Log.d(TAG, "isServiceOpen is $isServiceOpen")
    }

}