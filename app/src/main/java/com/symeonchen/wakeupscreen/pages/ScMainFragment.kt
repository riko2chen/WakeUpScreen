package com.symeonchen.wakeupscreen.pages

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.compose.MainScreen
import com.symeonchen.wakeupscreen.compose.StatusDisplayState
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
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
import kotlinx.coroutines.launch

class ScMainFragment : ScBaseFragment() {

    private lateinit var statusModel: StatusViewModel
    private lateinit var settingModel: SettingViewModel
    private var alertDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val statusFactory = ViewModelInjection.provideStatusViewModelFactory()
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        statusModel = ViewModelProvider(this, statusFactory).get(StatusViewModel::class.java)
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        getData()

        (view as ComposeView).setContent {
            WakeUpScreenTheme {
                val permissionOk by statusModel.permissionOfReadNotification.observeAsState(false)
                val serviceOk by statusModel.statusOfService.observeAsState(false)
                val appSwitch by settingModel.switchOfApp.observeAsState(false)
                val batteryOk by settingModel.fakeSwitchOfBatterySaver.observeAsState(false)
                val notifPermOk by settingModel.permissionOfSendNotification.observeAsState(false)

                val statusDisplay = deriveStatusDisplay(permissionOk, serviceOk, appSwitch)

                MainScreen(
                    statusDisplay = statusDisplay,
                    tipsText = getString(R.string.tip_desc_time_cost),
                    isToggleChecked = appSwitch,
                    onToggleClick = ::handleToggle,

                    serviceName = getString(R.string.service_of_background),
                    serviceOk = serviceOk,
                    serviceBtn = getString(if (serviceOk) R.string.click_to_close else R.string.click_to_open),
                    onServiceItemClick = {},
                    onServiceBtnClick = ::handleServiceToggle,

                    permissionName = getString(R.string.permission_of_read_notification),
                    permissionOk = permissionOk,
                    permissionBtn = getString(R.string.to_setting),
                    onPermissionItemClick = { openNotificationService(context) },
                    onPermissionBtnClick = {
                        openNotificationService(context)
                        PermissionState.openReadNotificationSetting(context)
                    },

                    batteryName = getString(R.string.optimize_of_battery_saver),
                    batteryOk = batteryOk,
                    batteryBtn = getString(R.string.how_to_set),
                    onBatteryItemClick = ::onBatterySaverClick,
                    onBatteryBtnClick = ::onBatterySaverClick,

                    notifPermName = getString(R.string.send_notification_permission),
                    notifPermOk = notifPermOk,
                    notifPermBtn = getString(R.string.click_to_open),
                    onNotifPermItemClick = {
                        openNotificationService(context)
                        PermissionState.openSendNotificationSetting(context, settingModel)
                    },
                    onNotifPermBtnClick = {
                        openNotificationService(context)
                        PermissionState.openSendNotificationSetting(context, settingModel)
                    },

                    noticeText = getString(R.string.still_have_problem),
                    onNoticeClick = ::jumpToAdvanceSettingPage,
                )
            }
        }
    }

    private fun deriveStatusDisplay(
        permissionOk: Boolean,
        serviceOk: Boolean,
        appSwitch: Boolean,
    ): StatusDisplayState {
        var statusText = getString(R.string.already_open)
        var isError = false
        var isToggleVisible = true
        var isNoticeVisible = true

        if (permissionOk != true) {
            statusText = getString(R.string.permission_of_read_notification) + " " + getString(R.string.not_open)
            isToggleVisible = false
            isError = true
            isNoticeVisible = false
        }
        if (serviceOk != true) {
            statusText = getString(R.string.service_of_background) + " " + getString(R.string.not_open)
            isToggleVisible = false
            isError = true
            isNoticeVisible = false
        }
        if (appSwitch != true) {
            statusText = getString(R.string.already_close)
            isError = true
            isNoticeVisible = false
        }

        return StatusDisplayState(statusText, isError, isToggleVisible, isNoticeVisible)
    }

    private fun handleToggle() {
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

    private fun handleServiceToggle() {
        if (statusModel.statusOfService.value == true) {
            closeNotificationService(context)
            statusModel.statusOfService.postValue(false)
        } else {
            openNotificationService(context)
            statusModel.statusOfService.postValue(true)
        }
    }

    @Synchronized
    private fun jumpToAdvanceSettingPage() {
        context?.run { this.quickStartActivity<AdvanceSettingPageActivity>() }
    }

    @Synchronized
    private fun resetApp() {
        lifecycleScope.launch { clearData() }
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
                Runtime.getRuntime().exec("pm clear $packageName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onBatterySaverClick() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        alertDialog = builder.setMessage(
            getString(R.string.battery_saver_tips)
        ).setPositiveButton(getString(R.string.to_setting)) { _, _ ->
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
        checkNotificationPermission()
    }

    private fun checkPermission(): Boolean {
        val v = PermissionState.hasNotificationListenerServiceEnabled(requireContext())
        statusModel.permissionOfReadNotification.postValue(v)
        LogUtils.d("isPermissionOpen is $v")
        return v
    }

    private fun checkStatus(): Boolean {
        val v = NotificationState.isNotificationServiceOpen(context)
        statusModel.statusOfService.postValue(v)
        LogUtils.d("isServiceOpen is $v")
        return v
    }

    private fun checkBatteryOptimization(): Boolean {
        val v = BatteryOptimizationState.hasIgnoreBatteryOptimization(context)
        settingModel.fakeSwitchOfBatterySaver.postValue(v)
        LogUtils.d("isIgnoreBatteryOptimization is $v")
        return v
    }

    private fun checkNotificationPermission(): Boolean {
        val v = PermissionState.hasSendNotificationPermission(context)
        settingModel.permissionOfSendNotification.postValue(v)
        LogUtils.d("isNotificationPermissionOpen is $v")
        return v
    }

    private fun registerProximitySensor() {
        if (settingModel.switchOfProximity.value == true && !ProximitySensorState.isRegistered()) {
            ProximitySensorState.registerListener(context)
        }
    }
}
