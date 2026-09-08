package com.symeonchen.wakeupscreen.services.bluetooth

import org.junit.Assert.*
import org.junit.Test

class BluetoothStartupDiscoveryTest {
    @Test fun asynchronousProfileRequestsHaveOneFixedDeadline() {
        var now = 100L
        val discovery = BluetoothStartupDiscovery({ now })
        discovery.begin(listOf(1, 2))
        assertEquals(1500L, discovery.remainingMillis())
        discovery.complete(1)
        now += 900L
        assertEquals(600L, discovery.remainingMillis())
        now += 600L
        assertEquals(0L, discovery.remainingMillis())
        // A late callback cannot start a second wait or trigger a catch-up wake.
        discovery.complete(2)
        assertEquals(0L, discovery.remainingMillis())
    }
    @Test fun completedOrRejectedProxiesFinishEarlyAndClearCancelsDiscovery() {
        val discovery = BluetoothStartupDiscovery({ 0L })
        discovery.begin(listOf(1, 2))
        discovery.complete(1)
        discovery.complete(2)
        assertEquals(0L, discovery.remainingMillis())
        discovery.begin(listOf(1))
        discovery.clear()
        assertEquals(0L, discovery.remainingMillis())
    }
    @Test fun onlyInitializingPairedSelectionsNeedAWait() {
        val a = "AA:BB:CC:DD:EE:01"
        val status = BluetoothConnectionMonitor.Snapshot(true, true, true,
            listOf(BluetoothConnectionMonitor.Device(a, "Headphones")), initializing = true)
        assertFalse(status.allows(true, setOf(a)))
        assertTrue(status.needsStartupWait(true, setOf(a)))
        assertFalse(status.needsStartupWait(false, setOf(a)))
        assertFalse(status.needsStartupWait(true, emptySet()))
        assertFalse(status.needsStartupWait(true, setOf("unpaired")))
        assertFalse(status.copy(initializing = false).needsStartupWait(true, setOf(a)))
        assertFalse(status.copy(permitted = false).needsStartupWait(true, setOf(a)))
        assertFalse(status.copy(powered = false).needsStartupWait(true, setOf(a)))
        assertFalse(status.copy(available = false).needsStartupWait(true, setOf(a)))
        // A selected connection is enough even if another proxy has not loaded yet.
        assertFalse(status.copy(connected = setOf(a)).needsStartupWait(true, setOf(a)))
    }
}
