package com.symeonchen.wakeupscreen.pages

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.components.*
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor
import com.symeonchen.wakeupscreen.utils.DataInjection
import kotlinx.coroutines.*

class BluetoothSettingActivity : ScBaseActivity() {
    private var enabled by mutableStateOf(DataInjection.bluetoothWakeSwitch)
    private var selected by mutableStateOf(DataInjection.bluetoothWakeDevices)
    private var snapshot by mutableStateOf(BluetoothConnectionMonitor.Snapshot(false, false, false))
    private var refreshJob: Job? = null
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        BluetoothConnectionMonitor.settingsChanged(applicationContext)
        refresh()
    }
    private fun refresh() {
        enabled = DataInjection.bluetoothWakeSwitch
        selected = DataInjection.bluetoothWakeDevices
        snapshot = BluetoothConnectionMonitor.snapshot(applicationContext)
    }
    private fun select(address: String) {
        selected = if (address in selected) selected - address else selected + address
        DataInjection.bluetoothWakeDevices = selected
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeUpScreenTheme {
                Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                    ComposeToolbar(stringResource(R.string.bluetooth_wake_title), onBack = { finish() })
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                        SettingSwitchRow(
                            title = stringResource(R.string.bluetooth_wake_title),
                            subtitle = stringResource(R.string.bluetooth_wake_desc),
                            checked = enabled,
                            onCheckedChange = {
                                enabled = !enabled
                                DataInjection.bluetoothWakeSwitch = enabled
                                BluetoothConnectionMonitor.settingsChanged(applicationContext)
                                if (enabled && Build.VERSION.SDK_INT >= 31 &&
                                    !BluetoothConnectionMonitor.hasPermission(this@BluetoothSettingActivity)) {
                                    permission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                }
                                refresh()
                            },
                        )
                        Text(stringResource(when {
                            !enabled -> R.string.bluetooth_wake_off
                            !snapshot.available -> R.string.bluetooth_wake_unavailable
                            !snapshot.permitted -> R.string.bluetooth_wake_permission
                            !snapshot.powered -> R.string.bluetooth_wake_disabled
                            selected.isEmpty() -> R.string.bluetooth_wake_empty
                            snapshot.allows(true, selected) -> R.string.bluetooth_wake_ready
                            snapshot.needsStartupWait(true, selected) -> R.string.bluetooth_wake_initializing
                            else -> R.string.bluetooth_wake_blocked
                        }), modifier = Modifier.padding(vertical = 12.dp))
                        if (enabled && !snapshot.permitted) {
                            Button(onClick = {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:$packageName")))
                            }) { Text(stringResource(R.string.bluetooth_wake_permission_settings)) }
                        }
                        if (snapshot.available) {
                            TextButton(onClick = { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }) {
                                Text(stringResource(R.string.bluetooth_wake_system_settings))
                            }
                        }
                        Text(stringResource(R.string.bluetooth_wake_scope), style = MaterialTheme.typography.bodySmall)
                        SectionLabel(stringResource(R.string.bluetooth_wake_devices), Modifier.padding(top = 20.dp))
                        if (snapshot.permitted && snapshot.powered && snapshot.devices.isEmpty()) {
                            Text(stringResource(R.string.bluetooth_wake_no_paired))
                        }
                        for (device in snapshot.devices) {
                            SettingSwitchRow(
                                title = device.name,
                                subtitle = device.address + " · " + stringResource(
                                    if (device.address in snapshot.connected) R.string.bluetooth_wake_connected
                                    else R.string.bluetooth_wake_not_detected),
                                checked = device.address in selected,
                                onCheckedChange = { select(device.address) },
                            )
                        }
                        for (address in selected - snapshot.devices.map { it.address }.toSet()) {
                            SettingSwitchRow(
                                title = address,
                                subtitle = stringResource(if (snapshot.permitted && snapshot.powered)
                                    R.string.bluetooth_wake_unpaired else R.string.bluetooth_wake_unknown),
                                checked = true,
                                onCheckedChange = { select(address) },
                            )
                        }
                        if (selected.isNotEmpty()) {
                            TextButton(onClick = {
                                selected = emptySet()
                                DataInjection.bluetoothWakeDevices = emptySet()
                            }) { Text(stringResource(R.string.bluetooth_wake_clear)) }
                        }
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        BluetoothConnectionMonitor.acquire(applicationContext, this)
        refreshJob = lifecycleScope.launch {
            while (isActive) { refresh(); delay(1000) }
        }
    }
    override fun onPause() {
        refreshJob?.cancel()
        BluetoothConnectionMonitor.release(this)
        super.onPause()
    }
}
