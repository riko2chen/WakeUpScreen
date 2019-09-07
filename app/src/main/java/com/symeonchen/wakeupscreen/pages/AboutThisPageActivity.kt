package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import kotlinx.android.synthetic.main.activity_about_this.*


class AboutThisPageActivity : ScBaseActivity() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_this)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    companion object {
        fun actionStart(context: Context) {
            context.startActivity(Intent(context, AboutThisPageActivity::class.java))
        }
    }


    private fun setListener() {

        iv_back.setOnClickListener { finish() }

        item_setting_debug_mode_toast.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initDebugModeDialog()
            }
        }

        item_setting_debug_mode_entry.setOnClickListener {
            DebugPageActivity.actionStart(this)
        }

        item_setting_app_introduce.setOnClickListener {
            AppInfoPageActivity.actionStart(this)
        }

        item_setting_give_star.setOnClickListener {
            try {

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen"
                    )
                    setPackage("com.android.vending")
                }
                startActivity(intent)
            } catch (anfe: ActivityNotFoundException) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(
                    "https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen"
                )
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        settingModel.switchOfDebugMode.observe(this, Observer {
            item_setting_debug_mode_toast.bindData(
                null,
                resources.getString(if (it) R.string.already_open else R.string.already_close)
            )
            if (it) {
                item_setting_debug_mode_entry.visibility = View.VISIBLE
            } else {
                item_setting_debug_mode_entry.visibility = View.GONE
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
