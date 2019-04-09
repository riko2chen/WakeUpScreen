package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.SettingViewModel
import com.symeonchen.wakeupscreen.utils.ViewModelInjection
import kotlinx.android.synthetic.main.fragment_layout_setting.*


class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory(context!!)
        settingModel = ViewModelProviders.of(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {
        item_setting_wake_screen_time.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initWakeScreenTimeDialog()
            }
        }

        item_setting_proximity_detect.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initProximitySwitchDialog()
            }
        }

        settingModel.timeOfWakeUpScreen.observe(this, Observer {
            item_setting_wake_screen_time.bindData(
                null,
                "${it / 1000}s"
            )
        })

        settingModel.switchOfProximity.observe(this, Observer {
            item_setting_proximity_detect.bindData(
                null,
                if (it) "已开启" else "已关闭"

            )
        })
    }

    private fun initWakeScreenTimeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        val secList = arrayOf("1s", "2s", "3s", "4s", "5s")
        val checkedItem: Int = (settingModel.timeOfWakeUpScreen.value!! / 1000).toInt() - 1
        var index = checkedItem
        alertDialog = builder.setSingleChoiceItems(
            secList, checkedItem
        ) { _, which -> index = which }
            .setPositiveButton("确定") { _, _ ->
                settingModel.timeOfWakeUpScreen.postValue((index + 1) * 1000L)
            }
            .create().apply { show() }
    }

    private fun initProximitySwitchDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        val secList = arrayOf("开启", "关闭")
        var switch = settingModel.switchOfProximity.value!!
        val checkedItem: Int = if (switch) 0 else 1
        alertDialog = builder.setSingleChoiceItems(
            secList, checkedItem
        ) { _, which -> switch = which == 0 }
            .setPositiveButton("确定") { _, _ ->
                settingModel.switchOfProximity.postValue(switch)
            }
            .create().apply { show() }
    }


}