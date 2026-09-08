package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.components.ComposeToolbar
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ReminderAppSelection
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.conditions.FilterListCondition
import com.symeonchen.wakeupscreen.services.reminder.ReminderAppSelectionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReminderAppsActivity : ScBaseActivity() {
    private data class App(val packageName: String, val label: String, val available: Boolean, val allowed: Boolean)
    private var apps by mutableStateOf<List<App>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeUpScreenTheme {
                var custom by remember { mutableStateOf(ReminderAppSelection.load().custom) }
                var selected by remember { mutableStateOf(ReminderAppSelection.load().packages) }
                var query by rememberSaveable { mutableStateOf("") }
                fun save(nextCustom: Boolean, nextSelected: Set<String>) {
                    custom = nextCustom
                    selected = nextSelected
                    ReminderAppSelection(custom, selected).save()
                    ReminderAppSelectionController.onChanged(applicationContext)
                }
                Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                    ComposeToolbar(title = stringResource(R.string.reminder_apps_title), onBack = { finish() })
                    Row(
                        Modifier.fillMaxWidth().toggleable(!custom, role = Role.Switch) { save(!it, selected) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.reminder_apps_inherit), Modifier.weight(1f))
                        Switch(checked = !custom, onCheckedChange = null)
                    }
                    Text(stringResource(R.string.reminder_apps_description), Modifier.padding(horizontal = 16.dp))
                    if (custom) {
                        Text(
                            stringResource(if (selected.isEmpty()) R.string.reminder_apps_empty else R.string.reminder_apps_saved),
                            Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedTextField(query, { query = it }, singleLine = true,
                            label = { Text(stringResource(R.string.reminder_apps_search)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                        val rows = apps
                        if (rows == null) {
                            CircularProgressIndicator(Modifier.padding(16.dp))
                        } else {
                            val visible = rows.filter {
                                (it.allowed || it.packageName in selected) &&
                                    (it.label.contains(query, true) || it.packageName.contains(query, true))
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                if (visible.isEmpty()) item {
                                    Text(stringResource(R.string.reminder_apps_no_results), Modifier.padding(16.dp))
                                }
                                items(visible, key = { it.packageName }) { app ->
                                    val checked = app.packageName in selected
                                    Row(
                                        Modifier.fillMaxWidth().toggleable(
                                            checked, enabled = app.allowed || checked, role = Role.Checkbox,
                                        ) { value -> save(custom, if (value) selected + app.packageName else selected - app.packageName) }
                                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(app.label)
                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                                            if (!app.available || !app.allowed) Text(
                                                stringResource(if (!app.available) R.string.reminder_apps_unavailable else R.string.reminder_apps_blocked),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                        Checkbox(checked, onCheckedChange = null, enabled = app.allowed || checked)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            apps = withContext(Dispatchers.IO) {
                val installed = runCatching { packageManager.getInstalledApplications(0) }
                    .getOrDefault(emptyList()).associateBy { it.packageName }
                val active = try { ScNotificationListenerService.instance?.activeNotifications?.map { it.packageName }.orEmpty() }
                    catch (_: Exception) { emptyList() }
                // Keep saved packages visible even after uninstall or restoring on another device.
                // They remain selected for a reinstall, and can always be explicitly removed.
                (installed.keys + active + ReminderAppSelection.load().packages).map { name ->
                    val info = installed[name]
                    App(name, info?.let { runCatching { packageManager.getApplicationLabel(it).toString() }.getOrDefault(name) } ?: name,
                        info != null || name in active,
                        FilterListCondition().resultForPackage(name) == ConditionState.SUCCESS)
                }.sortedBy { it.label.lowercase() }
            }
        }
    }
}
