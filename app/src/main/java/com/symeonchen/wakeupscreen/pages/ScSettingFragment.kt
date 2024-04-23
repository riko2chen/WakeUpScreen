package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.databinding.FragmentLayoutSettingBinding
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.AppInfoUtils
import com.symeonchen.wakeupscreen.utils.PlayStoreTools
import com.symeonchen.wakeupscreen.utils.quickStartActivity

/**
 * Created by SymeonChen on 2019-10-27.
 * modified by balkce on 2024-04-23.
 */
class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

    private var _binding: FragmentLayoutSettingBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLayoutSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
    }

    private fun setListener() {
        binding.itemSettingLanguage.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initLanguageSettingDialog()
            }
        }

        binding.itemSettingWakeScreenTime.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                context?.quickStartActivity<WakeUptimeSettingActivity>()
            }
        }

        binding.itemSettingAddress.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://github.com/SymeonChen/WakeUpScreen")
                startActivity(i)
            }
        }

        binding.itemSettingQuestion.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                var mailBody = ScConstant.DEFAULT_MAIL_BODY
                var mailTitle = ScConstant.DEFAULT_MAIL_HEAD
                try {
                    mailBody = AppInfoUtils.getDeviceInfo(context)
                    mailTitle = resources.getString(R.string.mail_title)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ScConstant.AUTHOR_MAIL))
                intent.putExtra(Intent.EXTRA_SUBJECT, mailTitle)
                intent.putExtra(Intent.EXTRA_TEXT, mailBody)
                startActivity(Intent.createChooser(intent, "Choose your mail app"))
            }
        }

        binding.itemSettingCurrentMode.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initCurrentModeDialog()
            }
        }

        binding.itemSettingWhiteListEntry.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_WHITE_LIST)
            }
        }

        binding.itemSettingBlackListEntry.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_BLACK_LIST)
            }
        }

        binding.itemSettingAdvancedSetting.setOnClickListener {
            context?.run { this.quickStartActivity<AdvanceSettingPageActivity>() }
        }

        binding.itemSettingAboutThis.setOnClickListener {
            context?.run { this.quickStartActivity<AboutThisPageActivity>() }
        }

        binding.itemSettingGiveStar.setOnClickListener {
            PlayStoreTools.openPlayStoreWithUrl(context)
        }

        settingModel.modeOfCurrent.observe(viewLifecycleOwner) {
            binding.itemSettingCurrentMode.bindData(
                null,
                resources.getString(
                    when (it) {
                        CurrentMode.MODE_BLACK_LIST -> R.string.black_list
                        CurrentMode.MODE_WHITE_LIST -> R.string.white_list
                        CurrentMode.MODE_ALL_NOTIFY -> R.string.all_pass
                        CurrentMode.MODE_IGNORE_SILENT -> R.string.ignore_silent
                        else -> R.string.all_pass
                    }
                )
            )
            when (it) {
                CurrentMode.MODE_WHITE_LIST -> {
                    binding.itemSettingWhiteListEntry.visibility = View.VISIBLE
                    binding.itemSettingBlackListEntry.visibility = View.GONE
                }
                CurrentMode.MODE_BLACK_LIST -> {
                    binding.itemSettingWhiteListEntry.visibility = View.GONE
                    binding.itemSettingBlackListEntry.visibility = View.VISIBLE
                }
                else -> {
                    binding.itemSettingWhiteListEntry.visibility = View.GONE
                    binding.itemSettingBlackListEntry.visibility = View.GONE
                }
            }
        }

        settingModel.languageSelected.observe(viewLifecycleOwner) {
            binding.itemSettingLanguage.bindData(
                null,
                it.desc
            )
        }

    }

    private fun initLanguageSettingDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        val languageArray = LanguageInfo.values()
        val languageNameArray = languageArray.map { it.desc }.toTypedArray()
        val refNum = settingModel.languageSelected.value!!.referenceNum
        val checkedItem: Int = languageArray.map { it.referenceNum }.indexOf(refNum)
        var selectedItem: LanguageInfo? = null

        alertDialog = builder.setSingleChoiceItems(
            languageNameArray, checkedItem
        ) { _, which -> selectedItem = languageArray[which] }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                selectedItem?.run {
                    settingModel.languageSelected.postValue(this)
                    this.applyLanguage()
                }
            }
            .create().apply { show() }

    }


    private fun initCurrentModeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        val secList = arrayOf(
            resources.getString(R.string.all_pass),
            resources.getString(R.string.white_list),
            resources.getString(R.string.black_list),
            resources.getString(R.string.ignore_silent)
        )
        var switch = settingModel.modeOfCurrent.value!!
        val checkedItem: Int = when (switch) {
            CurrentMode.MODE_ALL_NOTIFY -> 0
            CurrentMode.MODE_WHITE_LIST -> 1
            CurrentMode.MODE_IGNORE_SILENT -> 3
            else -> 2
        }

        alertDialog = builder
            .setSingleChoiceItems(
                secList, checkedItem
            ) { _, which ->
                switch =
                    when (which) {
                        0 -> CurrentMode.MODE_ALL_NOTIFY
                        1 -> CurrentMode.MODE_WHITE_LIST
                        3 -> CurrentMode.MODE_IGNORE_SILENT
                        else -> CurrentMode.MODE_BLACK_LIST
                    }
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                settingModel.modeOfCurrent.postValue(
                    CurrentMode.getModeFromValue(
                        when (switch) {
                            CurrentMode.MODE_ALL_NOTIFY -> 0
                            CurrentMode.MODE_WHITE_LIST -> 1
                            CurrentMode.MODE_IGNORE_SILENT -> 3
                            else -> 2
                        }
                    )
                )
            }
            .create().apply { show() }
    }


}
