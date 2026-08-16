package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.blankj.utilcode.util.ToastUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.AttentionRowUi
import com.symeonchen.wakeupscreen.compose.AttentionStatsScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.AppAttention
import com.symeonchen.wakeupscreen.data.AttentionStats
import com.symeonchen.wakeupscreen.data.AttentionStatsStore
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.FilterListUtils

class AttentionStatsActivity : ScBaseActivity() {

    private var rowsState by mutableStateOf<List<AttentionRowUi>>(emptyList())
    private var suggestionsState by mutableStateOf<List<AttentionRowUi>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                AttentionStatsScreen(
                    onBack = { finish() },
                    rows = rowsState,
                    suggestions = suggestionsState,
                    onAddToBlackList = ::addToBlackList,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val summary = AttentionStatsStore.summary()
        val blackList =
            FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
        rowsState = summary.map(::toRow)
        // Apps already on the blacklist have been dealt with; suggesting them
        // again would nag about a decision already taken.
        suggestionsState = AttentionStats.suggestions(summary)
            .filterNot { blackList.containsKey(it.packageName) }
            .map(::toRow)
    }

    private fun toRow(item: AppAttention): AttentionRowUi = AttentionRowUi(
        packageName = item.packageName,
        label = appLabel(item.packageName),
        wakes = item.wakes,
        followed = item.followed,
    )

    private fun appLabel(packageName: String): String = try {
        packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, 0)
        ).toString()
    } catch (_: Exception) {
        // Uninstalled since the wake was recorded; the package name is still
        // an honest label.
        packageName
    }

    private fun addToBlackList(row: AttentionRowUi) {
        val map = FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
        map[row.packageName] = 1
        DataInjection.appBlackListStringOfNotify = FilterListUtils.saveMapToString(map)
        DataInjection.appListUpdateFlag = System.currentTimeMillis()
        if (DataInjection.modeOfCurrent == CurrentMode.MODE_BLACK_LIST) {
            ToastUtils.showShort(R.string.attention_added_blacklist)
        } else {
            // The list was updated, but with a different filter mode active it
            // does nothing yet — say so rather than let the wakes continue.
            ToastUtils.showShort(R.string.attention_added_blacklist_mode_hint)
        }
        refresh()
    }
}
