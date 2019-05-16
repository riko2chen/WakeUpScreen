package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.data.SettingViewModel
import com.symeonchen.wakeupscreen.data.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.ProximitySensorSingleton
import kotlinx.android.synthetic.main.fragment_layout_setting.*


class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_layout_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProviders.of(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {
        item_setting_language.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                ToastUtils.showShort(resources.getString(R.string.tip_change_language_developing))
            }
        }

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

        item_setting_address.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://github.com/SymeonChen/WakeUpScreen")
                startActivity(i)
            }
        }

        item_setting_question.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ScConstant.AUTHOR_MAIL))
                intent.putExtra(Intent.EXTRA_SUBJECT, ScConstant.DEFAULT_MAIL_HEAD)
                intent.putExtra(Intent.EXTRA_TEXT, ScConstant.DEFAULT_MAIL_BODY)
                startActivity(Intent.createChooser(intent, ""))
            }
        }

        item_setting_debug_mode_toast.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initDebugModeDialog()
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
                resources.getString(if (it) R.string.already_open else R.string.already_close)
            )
            if (it) {
                if (!ProximitySensorSingleton.isRegistered()) {
                    ProximitySensorSingleton.registerListener(context)
                }
            } else {
                if (ProximitySensorSingleton.isRegistered())
                    ProximitySensorSingleton.unRegisterListener(context)
            }
        })

        settingModel.switchOfDebugMode.observe(this, Observer {
            item_setting_debug_mode_toast.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close)
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
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                settingModel.timeOfWakeUpScreen.postValue((index + 1) * 1000L)
            }
            .create().apply { show() }
    }

    private fun initProximitySwitchDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        val secList = arrayOf(resources.getString(R.string.open), resources.getString(R.string.close))
        var switch = settingModel.switchOfProximity.value!!
        val checkedItem: Int = if (switch) 0 else 1
        alertDialog = builder.setSingleChoiceItems(
            secList, checkedItem
        ) { _, which -> switch = which == 0 }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                settingModel.switchOfProximity.postValue(switch)
            }
            .create().apply { show() }
    }

    private fun initDebugModeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        val secList = arrayOf(resources.getString(R.string.open), resources.getString(R.string.close))
        var switch = settingModel.switchOfDebugMode.value!!
        val checkedItem: Int = if (switch) 0 else 1
        alertDialog = builder.setSingleChoiceItems(
            secList, checkedItem
        ) { _, which -> switch = which == 0 }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                settingModel.switchOfDebugMode.postValue(switch)
            }
            .create().apply { show() }
    }


}