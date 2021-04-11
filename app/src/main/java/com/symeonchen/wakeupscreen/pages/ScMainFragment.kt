package com.symeonchen.wakeupscreen.pages

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.uicomponent.views.StatusItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.StatusViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.states.BatteryOptimizationState
import com.symeonchen.wakeupscreen.states.NotificationState
import com.symeonchen.wakeupscreen.states.NotificationState.Companion.closeNotificationService
import com.symeonchen.wakeupscreen.states.NotificationState.Companion.openNotificationService
import com.symeonchen.wakeupscreen.states.PermissionState
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.quickStartActivity
import kotlinx.android.synthetic.main.fragment_layout_main.*
import kotlinx.coroutines.launch

/**
 * Created by SymeonChen on 2019-10-27.
 */
class ScMainFragment : ScBaseFragment() {

    private lateinit var statusModel: StatusViewModel
    private lateinit var settingModel: SettingViewModel
    private var alertDialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusFactory = ViewModelInjection.provideStatusViewModelFactory()
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        statusModel = ViewModelProvider(this, statusFactory).get(StatusViewModel::class.java)
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        initView()
        setDataListener()
        setViewListener()
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

    private fun setDataListener() {
        statusModel.statusOfService.observe(viewLifecycleOwner, {
            main_item_service.setState(it)
            main_item_service.setBtnText(resources.getString(if (it) R.string.click_to_close else R.string.click_to_open))
            refresh()
        })

        statusModel.permissionOfReadNotification.observe(viewLifecycleOwner, {
            main_item_permission_notification.setState(it)
            refresh()
        })

        settingModel.switchOfApp.observe(viewLifecycleOwner, {
            btn_control.isChecked = it
            refresh()
        })

        settingModel.fakeSwitchOfBatterySaver.observe(viewLifecycleOwner, {
            main_item_battery_saver.setState(it)
        })

    }

    private fun setViewListener() {
        btn_control.setOnClickListener {
            val status = settingModel.switchOfApp.value ?: false
            settingModel.switchOfApp.postValue(!status)
            if (status) {
                closeNotificationService(context)
                statusModel.statusOfService.postValue(false)
            } else {
                openNotificationService(context)
                statusModel.statusOfService.postValue(true)
            }
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

        tv_reset_application?.setOnClickListener {
            if (tv_reset_application?.visibility != View.VISIBLE) {
                return@setOnClickListener
            }
            jumpToAdvanceSettingPage()
        }
    }

    @Synchronized
    private fun jumpToAdvanceSettingPage() {
        context?.run { this.quickStartActivity<AdvanceSettingPageActivity>() }
    }

    @Synchronized
    private fun resetApp() {
        lifecycleScope.launch {
            clearData()
        }
    }

    private fun clearData() {
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            try {
                (requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val packageName = requireContext().applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onBatterySaverClick() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        alertDialog = builder.setMessage(
            resources.getString(R.string.battery_saver_tips)
        ).setPositiveButton(resources.getString(R.string.to_setting)) { _, _ ->
            if (!BatteryOptimizationState.hasIgnoreBatteryOptimization(context)) {
                BatteryOptimizationState.openBatteryOptimization(context)
                settingModel.fakeSwitchOfBatterySaver.postValue(true)
            } else {
                ToastUtils.showShort(R.string.set_up_successfully)
            }
        }.create().apply { show() }
    }

    private fun getData() {
        statusModel.permissionOfReadNotification.postValue(
            PermissionState.hasNotificationListenerServiceEnabled(requireContext())
        )
        statusModel.statusOfService.postValue(
            NotificationState.isNotificationServiceOpen(context)
        )
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        checkStatus()
        checkBatteryOptimization()
        registerProximitySensor()
    }

    private fun checkPermission(): Boolean {
        val isPermissionOpen =
            PermissionState.hasNotificationListenerServiceEnabled(requireContext())
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

    private fun checkBatteryOptimization(): Boolean {
        val isIgnoreBatteryOptimization =
            BatteryOptimizationState.hasIgnoreBatteryOptimization(context)
        settingModel.fakeSwitchOfBatterySaver.postValue(isIgnoreBatteryOptimization)
        LogUtils.d("isIgnoreBatteryOptimization is $isIgnoreBatteryOptimization")
        return isIgnoreBatteryOptimization
    }

    private fun registerProximitySensor() {
        if (settingModel.switchOfProximity.value == true && !ProximitySensorState.isRegistered()) {
            ProximitySensorState.registerListener(context)
        }
    }

    private fun refreshState(
        permissionStatus: Boolean?,
        serviceStatus: Boolean?,
        customStatus: Boolean?
    ) {
        btn_control.visibility = View.INVISIBLE

        var btnVisibility = View.VISIBLE
        var tvStatusText = resources.getString(R.string.already_open)
        var headerBackgroundColor = context?.getColor(R.color.colorPrimary)
        var visibilityOfResetNotice = View.VISIBLE

        if (permissionStatus != true) {
            tvStatusText =
                resources.getString(R.string.permission_of_read_notification) + " " + resources.getString(
                    R.string.not_open
                )
            btnVisibility = View.INVISIBLE
            headerBackgroundColor = context?.getColor(R.color.red)
            visibilityOfResetNotice = View.INVISIBLE
        }
        if (serviceStatus != true) {
            tvStatusText =
                resources.getString(R.string.service_of_background) + " " + resources.getString(R.string.not_open)
            btnVisibility = View.INVISIBLE
            headerBackgroundColor = context?.getColor(R.color.red)
            visibilityOfResetNotice = View.INVISIBLE
        }
        if (customStatus != true) {
            tvStatusText = resources.getString(R.string.already_close)
            headerBackgroundColor = context?.getColor(R.color.red)
            visibilityOfResetNotice = View.INVISIBLE
        }

        btn_control.visibility = btnVisibility
        tv_status?.text = tvStatusText
        cl_header?.setBackgroundColor(headerBackgroundColor ?: Color.WHITE)
        tv_reset_application?.visibility = visibilityOfResetNotice
    }

    private fun refresh() {
        refreshState(
            statusModel.permissionOfReadNotification.value,
            statusModel.statusOfService.value,
            settingModel.switchOfApp.value
        )
    }

}