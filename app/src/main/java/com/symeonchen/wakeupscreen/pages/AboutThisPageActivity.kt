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

    private val binding by lazy { ActivityAboutThisBinding.inflate(layoutInflater) }
    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {

        binding.ivBack.setOnClickListener { finish() }

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

    }

}
