package com.symeonchen.wakeupscreen.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.compose.SettingScreen
import com.symeonchen.wakeupscreen.compose.components.SelectionDialog
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.PlayStoreTools
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class ScSettingFragment : ScBaseFragment() {

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

                var showLanguageDialog by remember { mutableStateOf(false) }
                var showModeDialog by remember { mutableStateOf(false) }

                val currentModeText = stringResource(
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
                    onLanguageClick = { showLanguageDialog = true },
                    onWakeTimeClick = { context?.quickStartActivity<WakeUptimeSettingActivity>() },
                    onCurrentModeClick = { showModeDialog = true },
                    onWhiteListClick = { FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_WHITE_LIST) },
                    onBlackListClick = { FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_BLACK_LIST) },
                    onAdvancedSettingClick = { context?.quickStartActivity<AdvanceSettingPageActivity>() },
                    onFunctionTestClick = { context?.quickStartActivity<FunctionTestPageActivity>() },
                    onAddressClick = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SymeonChen/WakeUpScreen")))
                    },
                    onFeedbackClick = { context?.quickStartActivity<FeedbackPageActivity>() },
                    onGiveStarClick = { PlayStoreTools.openPlayStoreWithUrl(context) },
                )

                // Language dialog
                if (showLanguageDialog) {
                    val languageArray = LanguageInfo.values()
                    val currentIdx = languageArray.indexOfFirst { it.referenceNum == language.referenceNum }
                    SelectionDialog(
                        title = stringResource(R.string.language),
                        options = languageArray.map { it.desc },
                        selectedIndex = if (currentIdx >= 0) currentIdx else 0,
                        confirmText = stringResource(R.string.ok),
                        onSelect = { idx ->
                            showLanguageDialog = false
                            val selected = languageArray[idx]
                            settingModel.languageSelected.postValue(selected)
                            selected.applyLanguage()
                        },
                        onDismiss = { showLanguageDialog = false },
                    )
                }

                // Mode dialog
                if (showModeDialog) {
                    val modeLabels = listOf(
                        stringResource(R.string.all_pass),
                        stringResource(R.string.white_list),
                        stringResource(R.string.black_list),
                    )
                    val currentIdx = when (currentMode) {
                        CurrentMode.MODE_ALL_NOTIFY -> 0
                        CurrentMode.MODE_WHITE_LIST -> 1
                        else -> 2
                    }
                    SelectionDialog(
                        title = stringResource(R.string.current_mode),
                        options = modeLabels,
                        selectedIndex = currentIdx,
                        confirmText = stringResource(R.string.ok),
                        onSelect = { idx ->
                            showModeDialog = false
                            settingModel.modeOfCurrent.postValue(
                                CurrentMode.getModeFromValue(idx)
                            )
                        },
                        onDismiss = { showModeDialog = false },
                    )
                }
            }
        }
    }

}
