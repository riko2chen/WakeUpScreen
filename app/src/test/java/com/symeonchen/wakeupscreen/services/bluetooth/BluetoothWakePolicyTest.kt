package com.symeonchen.wakeupscreen.services.bluetooth

import org.junit.Assert.*
import org.junit.Test

class BluetoothWakePolicyTest {
    private val a = "AA:BB:CC:DD:EE:01"
    private val b = "AA:BB:CC:DD:EE:02"
    @Test fun disabledRuleNeverRestrictsWakes() {
        for (available in listOf(false, true)) for (permitted in listOf(false, true)) {
            assertTrue(BluetoothWakePolicy.allows(false, available, permitted, false,
                emptySet(), emptySet(), emptySet()))
        }
    }
    @Test fun anySelectedPairedConnectionIsEnough() {
        assertTrue(BluetoothWakePolicy.allows(true, true, true, true, setOf(a, b), setOf(a, b), setOf(b)))
    }
    @Test fun pairingAloneAndUnselectedConnectionsNeverAllowWakes() {
        assertFalse(BluetoothWakePolicy.allows(true, true, true, true, setOf(a), setOf(a, b), emptySet()))
        assertFalse(BluetoothWakePolicy.allows(true, true, true, true, setOf(a), setOf(a, b), setOf(b)))
    }
    @Test fun missingSelectionAndRestoredUnpairedSelectionFailClosed() {
        assertFalse(BluetoothWakePolicy.allows(true, true, true, true, emptySet(), setOf(a), setOf(a)))
        assertFalse(BluetoothWakePolicy.allows(true, true, true, true, setOf(a), setOf(b), setOf(a)))
    }
    @Test fun revokedPermissionOrUnavailableRadioOverridesCachedConnection() {
        for (available in listOf(false, true)) for (permitted in listOf(false, true)) {
            for (powered in listOf(false, true)) {
                assertEquals(available && permitted && powered, BluetoothWakePolicy.allows(
                    true, available, permitted, powered, setOf(a), setOf(a), setOf(a)))
            }
        }
    }
    @Test fun disconnectImmediatelyBlocksAndReconnectAllowsAgain() {
        fun allows(connected: Set<String>) = BluetoothWakePolicy.allows(
            true, true, true, true, setOf(a), setOf(a), connected)
        assertTrue(allows(setOf(a)))
        assertFalse(allows(emptySet()))
        assertTrue(allows(setOf(a)))
    }
}
