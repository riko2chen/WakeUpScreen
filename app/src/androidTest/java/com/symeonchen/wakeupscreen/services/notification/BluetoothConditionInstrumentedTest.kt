package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.*
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor
import com.symeonchen.wakeupscreen.services.notification.conditions.BluetoothCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class BluetoothConditionInstrumentedTest {
    private var savedEnabled = false
    private lateinit var savedDevices: Set<String>
    private val application get() = InstrumentationRegistry.getTargetContext().applicationContext as Application
    @Before fun saveSettings() {
        savedEnabled = DataInjection.bluetoothWakeSwitch
        savedDevices = DataInjection.bluetoothWakeDevices
    }
    @After fun restoreSettings() {
        DataInjection.bluetoothWakeSwitch = savedEnabled
        DataInjection.bluetoothWakeDevices = savedDevices
        BluetoothConnectionMonitor.settingsChanged(application)
    }
    @Test fun disabledConditionPassesWithoutContextOrPermission() {
        DataInjection.bluetoothWakeSwitch = false
        assertEquals(ConditionState.SUCCESS, BluetoothCondition().provideResult(null))
        assertFalse(BluetoothCondition().isArmed())
    }
    @Test fun enabledConditionWithoutContextFailsClosed() {
        DataInjection.bluetoothWakeSwitch = true
        assertEquals(ConditionState.BLOCK, BluetoothCondition().provideResult(null))
    }
    @Test fun emptySelectionBlocksOnRealDeviceRegardlessOfRadioOrPermission() {
        DataInjection.bluetoothWakeSwitch = true
        DataInjection.bluetoothWakeDevices = emptySet()
        BluetoothConnectionMonitor.acquire(application, this)
        try {
            assertEquals(ConditionState.BLOCK, BluetoothCondition().provideResult(application))
            assertTrue(BluetoothCondition().wouldBlockNow(application))
        } finally { BluetoothConnectionMonitor.release(this) }
    }
    @Test fun bluetoothBlocksBeforeSleepAndCannotBecomeNightGlowInLogChain() {
        val keys = ListenerManager.orderedKeys()
        assertTrue(keys.indexOf(BlockReason.BLUETOOTH) < keys.indexOf(BlockReason.SLEEP_MODE))
        val chain = BlockChain.forLogEntry(NotificationLogEntry(
            timestamp = 1L, packageName = "test", status = LogStatus.BLOCKED,
            blockReason = BlockReason.BLUETOOTH))
        assertEquals(ChainNodeState.BLOCKED, chain.first { it.key == BlockReason.BLUETOOTH }.state)
        assertEquals(ChainNodeState.NOT_EVALUATED, chain.first { it.key == BlockReason.SLEEP_MODE }.state)
        assertEquals(ChainNodeState.NOT_EVALUATED, chain.last().state)
    }
    @Test fun backupRoundTripPreservesSelectionAndEnabledRule() {
        val addresses = setOf("AA:BB:CC:DD:EE:01", "AA:BB:CC:DD:EE:02")
        DataInjection.bluetoothWakeSwitch = true
        DataInjection.bluetoothWakeDevices = addresses
        val raw = SettingsBackup.export()
        DataInjection.bluetoothWakeSwitch = false
        DataInjection.bluetoothWakeDevices = emptySet()
        assertTrue(SettingsBackup.import(raw) is SettingsBackup.ImportResult.Success)
        assertTrue(DataInjection.bluetoothWakeSwitch)
        assertEquals(addresses, DataInjection.bluetoothWakeDevices)
    }
    @Test fun malformedBackupSelectionCannotAuthorizeAWake() {
        val settings = org.json.JSONObject().put(ScConstant.BLUETOOTH_WAKE_DEVICES, "invalid,address")
        SettingsBackup.import(BackupEnvelope.wrap(settings, 1L).toString())
        assertTrue(DataInjection.bluetoothWakeDevices.isEmpty())
    }
}
