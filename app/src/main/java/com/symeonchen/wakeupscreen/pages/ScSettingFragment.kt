package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.compose.SettingScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.AppInfoUtils
import com.symeonchen.wakeupscreen.utils.PlayStoreTools
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

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
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)

        (view as ComposeView).setContent {
            WakeUpScreenTheme {
                val currentMode by settingModel.modeOfCurrent.observeAsState(CurrentMode.MODE_ALL_NOTIFY)
                val language by settingModel.languageSelected.observeAsState(LanguageInfo.FOLLOW_SYSTEM)

                val currentModeText = getString(
                    when (currentMode) {
                        CurrentMode.MODE_BLACK_LIST -> R.string.black_list
                        CurrentMode.MODE_WHITE_LIST -> R.string.white_list
                        else -> R.string.all_pass
                    }
                )

                SettingScreen(
                    currentModeText = currentModeText,
                    languageText = language.desc,
                    showWhiteListEntry = currentMode == CurrentMode.MODE_WHITE_LIST,
                    showBlackListEntry = currentMode == CurrentMode.MODE_BLACK_LIST,
                    onLanguageClick = ::initLanguageSettingDialog,
                    onWakeTimeClick = { context?.quickStartActivity<WakeUptimeSettingActivity>() },
                    onCurrentModeClick = ::initCurrentModeDialog,
                    onWhiteListClick = { FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_WHITE_LIST) },
                    onBlackListClick = { FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_BLACK_LIST) },
                    onAdvancedSettingClick = { context?.quickStartActivity<AdvanceSettingPageActivity>() },
                    onAboutClick = { context?.quickStartActivity<AboutThisPageActivity>() },
                    onAddressClick = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SymeonChen/WakeUpScreen")))
                    },
                    onFeedbackClick = ::openFeedbackEmail,
                    onGiveStarClick = { PlayStoreTools.openPlayStoreWithUrl(context) },
                )
            }
        }
    }

    private fun openFeedbackEmail() {
        var mailBody = ScConstant.DEFAULT_MAIL_BODY
        var mailTitle = ScConstant.DEFAULT_MAIL_HEAD
        try {
            mailBody = AppInfoUtils.getDeviceInfo(context)
            mailTitle = getString(R.string.mail_title)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "plain/text"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ScConstant.AUTHOR_MAIL))
            putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            putExtra(Intent.EXTRA_TEXT, mailBody)
        }
        startActivity(Intent.createChooser(intent, "Choose your mail app"))
    }

    private fun initLanguageSettingDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        val languageArray = LanguageInfo.values()
        val languageNameArray = languageArray.map { it.desc }.toTypedArray()
        val refNum = settingModel.languageSelected.value!!.referenceNum
        val checkedItem = languageArray.map { it.referenceNum }.indexOf(refNum)
        var selectedItem: LanguageInfo? = null

        alertDialog = builder.setSingleChoiceItems(
            languageNameArray, checkedItem
        ) { _, which -> selectedItem = languageArray[which] }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
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
            getString(R.string.all_pass),
            getString(R.string.white_list),
            getString(R.string.black_list)
        )
        var switch = settingModel.modeOfCurrent.value!!
        val checkedItem = when (switch) {
            CurrentMode.MODE_ALL_NOTIFY -> 0
            CurrentMode.MODE_WHITE_LIST -> 1
            else -> 2
        }

        alertDialog = builder
            .setSingleChoiceItems(secList, checkedItem) { _, which ->
                switch = when (which) {
                    0 -> CurrentMode.MODE_ALL_NOTIFY
                    1 -> CurrentMode.MODE_WHITE_LIST
                    else -> CurrentMode.MODE_BLACK_LIST
                }
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                settingModel.modeOfCurrent.postValue(
                    CurrentMode.getModeFromValue(
                        when (switch) {
                            CurrentMode.MODE_ALL_NOTIFY -> 0
                            CurrentMode.MODE_WHITE_LIST -> 1
                            else -> 2
                        }
                    )
                )
            }
            .create().apply { show() }
    }
}
