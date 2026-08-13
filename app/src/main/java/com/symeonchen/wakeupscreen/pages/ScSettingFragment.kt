package com.symeonchen.wakeupscreen.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.DarkModeInfo
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.SettingsBackup
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.PlayStoreTools
import com.symeonchen.wakeupscreen.utils.quickStartActivity

class ScSettingFragment : ScBaseFragment() {

    private lateinit var settingModel: SettingViewModel

    /**
     * SAF pickers for the settings backup. The user chooses the location both
     * ways, so no storage permission is involved.
     */
    private val exportBackupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri ?: return@registerForActivityResult
            writeBackup(uri)
        }

    private val importBackupLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            readBackup(uri)
        }

    private fun writeBackup(uri: Uri) {
        try {
            val content = SettingsBackup.export()
            requireContext().contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("no stream")
            ToastUtils.showShort(R.string.backup_export_success)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showShort(R.string.backup_export_failed)
        }
    }

    private fun readBackup(uri: Uri) {
        val raw = try {
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (raw == null) {
            ToastUtils.showShort(R.string.backup_import_failed_corrupt)
            return
        }
        when (val result = SettingsBackup.import(raw)) {
            is SettingsBackup.ImportResult.Success -> {
                // Language and dark mode are the two settings whose effect is
                // not read lazily; everything else applies on next read.
                DataInjection.languageSelected.applyLanguage()
                DataInjection.darkModeSelected.applyDarkMode()
                ToastUtils.showShort(getString(R.string.backup_import_success, result.applied))
            }
            is SettingsBackup.ImportResult.NewerVersion ->
                ToastUtils.showShort(R.string.backup_import_failed_newer)
            is SettingsBackup.ImportResult.Corrupt ->
                ToastUtils.showShort(R.string.backup_import_failed_corrupt)
        }
    }

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
                val darkMode by settingModel.darkModeSelected.observeAsState(DarkModeInfo.FOLLOW_SYSTEM)

                var showLanguageDialog by remember { mutableStateOf(false) }
                var showDarkModeDialog by remember { mutableStateOf(false) }

                SettingScreen(
                    languageText = language.labelRes?.let { stringResource(it) } ?: language.desc,
                    darkModeText = stringResource(darkMode.labelRes),
                    showWhiteListEntry = currentMode == CurrentMode.MODE_WHITE_LIST,
                    showBlackListEntry = currentMode == CurrentMode.MODE_BLACK_LIST,
                    onLanguageClick = { showLanguageDialog = true },
                    onDarkModeClick = { showDarkModeDialog = true },
                    onAdvancedSettingClick = { context?.quickStartActivity<AdvanceSettingPageActivity>() },
                    onBlockChainClick = { context?.quickStartActivity<BlockChainPageActivity>() },
                    onFunctionTestClick = { context?.quickStartActivity<FunctionTestPageActivity>() },
                    onViewLogsClick = { context?.quickStartActivity<NotificationLogPageActivity>() },
                    onBackupExportClick = {
                        exportBackupLauncher.launch("wakeupscreen-backup.json")
                    },
                    onBackupImportClick = {
                        importBackupLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    },
                    onAttentionStatsClick = { context?.quickStartActivity<AttentionStatsActivity>() },
                    onAddressClick = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/riko2chen/WakeUpScreen")))
                    },
                    onFeedbackClick = { context?.quickStartActivity<FeedbackPageActivity>() },
                    onGiveStarClick = { PlayStoreTools.openPlayStoreWithUrl(context) },
                    onCheckUpdateClick = { context?.quickStartActivity<CheckUpdatePageActivity>() },
                )

                // Language dialog
                if (showLanguageDialog) {
                    val languageArray = LanguageInfo.values()
                    val currentIdx = languageArray.indexOfFirst { it.referenceNum == language.referenceNum }
                    SelectionDialog(
                        title = stringResource(R.string.language),
                        options = languageArray.map { item ->
                            item.labelRes?.let { stringResource(it) } ?: item.desc
                        },
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

                // Dark mode dialog
                if (showDarkModeDialog) {
                    val darkModeArray = DarkModeInfo.values()
                    val currentIdx = darkModeArray.indexOfFirst { it.referenceNum == darkMode.referenceNum }
                    SelectionDialog(
                        title = stringResource(R.string.dark_mode),
                        options = darkModeArray.map { stringResource(it.labelRes) },
                        selectedIndex = if (currentIdx >= 0) currentIdx else 0,
                        confirmText = stringResource(R.string.ok),
                        onSelect = { idx ->
                            showDarkModeDialog = false
                            val selected = darkModeArray[idx]
                            settingModel.darkModeSelected.postValue(selected)
                            selected.applyDarkMode()
                        },
                        onDismiss = { showDarkModeDialog = false },
                    )
                }
            }
        }
    }

}
