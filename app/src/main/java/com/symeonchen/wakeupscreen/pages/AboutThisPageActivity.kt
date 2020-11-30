package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.databinding.ActivityAboutThisBinding
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.NotificationUtils
import com.symeonchen.wakeupscreen.utils.quickStartActivity


/**
 * Created by SymeonChen on 2019-10-27.
 */
class AboutThisPageActivity : ScBaseActivity() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel
    private lateinit var binding: ActivityAboutThisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutThisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {

        binding.ivBack.setOnClickListener { finish() }

        binding.itemSettingDebugModeToast.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initDebugModeDialog()
            }
        }

        binding.itemSettingDebugModeEntry.setOnClickListener {
            this.quickStartActivity<DebugPageActivity>()
        }

        binding.itemSettingAppIntroduce.setOnClickListener {
            this.quickStartActivity<AppInfoPageActivity>()
        }

        binding.itemSettingDebugDelayToWake.setOnClickListener {
            it.postDelayed({
                NotificationUtils(this.applicationContext).sendNotification(
                    1,
                    "This is a test",
                    "Just for testing wakeup screen"
                )

            }, 5000)
        }

        settingModel.switchOfDebugMode.observe(this, Observer {
            binding.itemSettingDebugModeToast.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close)
            )
            if (it) {
                binding.itemSettingDebugModeEntry.visibility = View.VISIBLE
            } else {
                binding.itemSettingDebugModeEntry.visibility = View.GONE
            }

        })
    }

    private fun initDebugModeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(this)
        val secList =
            arrayOf(resources.getString(R.string.open), resources.getString(R.string.close))
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
