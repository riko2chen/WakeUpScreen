package com.symeonchen.wakeupscreen.services.bluetooth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothStartupQueueTest {
    @Test fun alreadyConnectedDeviceDiscoveredAsynchronouslyReleasesFirstNotification() = runTest {
        var initializing = true
        val batches = mutableListOf<List<String>>()
        val queue = BluetoothStartupQueue(this, { initializing }, { batches.add(it) })
        queue.post("key", "first notification")
        advanceTimeBy(200)
        assertTrue(batches.isEmpty())
        initializing = false
        advanceTimeBy(50)
        runCurrent()
        assertEquals(listOf(listOf("first notification")), batches)
        assertFalse(queue.hasPending)
    }
    @Test fun timeoutIsBoundedAndUpdatesDoNotExtendIt() = runTest {
        val batches = mutableListOf<List<String>>()
        val queue = BluetoothStartupQueue(this, { true }, { batches.add(it) })
        queue.post("key", "old")
        advanceTimeBy(1000)
        queue.post("key", "latest")
        advanceTimeBy(500)
        runCurrent()
        assertEquals(listOf(listOf("latest")), batches)
        advanceTimeBy(5000)
        assertEquals(1, batches.size)
    }
    @Test fun dismissalAndListenerDisconnectCancelDeferredWork() = runTest {
        var calls = 0
        val queue = BluetoothStartupQueue<String>(this, { true }, { calls++ })
        queue.post("key", "dismissed")
        assertEquals("dismissed", queue.remove("key"))
        advanceTimeBy(2000)
        assertEquals(0, calls)
        queue.post("other", "disconnected listener")
        queue.clear()
        advanceTimeBy(2000)
        assertEquals(0, calls)
    }
    @Test fun oneBatchRechecksDismissalAndRulesAndStopsAfterOneWake() = runTest {
        var initializing = true
        var active = setOf("one", "two", "three")
        var allowed = active
        val attempts = mutableListOf<String>()
        val wakes = mutableListOf<String>()
        val queue = BluetoothStartupQueue<String>(this, { initializing }, { candidates ->
            BluetoothStartupBatch.recheck(candidates,
                current = { it.takeIf { it in active } },
                dismissed = {},
                tryWake = { candidate ->
                    attempts.add(candidate)
                    (candidate in allowed).also { if (it) wakes.add(candidate) }
                })
        })
        queue.post("one", "one")
        queue.post("two", "two")
        queue.post("three", "three")
        // The newest was dismissed, the next now fails a wake rule.
        active = setOf("one", "two")
        allowed = setOf("one")
        initializing = false
        runCurrent()
        assertEquals(listOf("two", "one"), attempts)
        assertEquals(listOf("one"), wakes)
    }
    @Test fun eligibleBurstProducesOnlyOneWakeAndRechecksLatestPayload() {
        val attempted = mutableListOf<String>()
        BluetoothStartupBatch.recheck(listOf("new", "old"),
            current = { "$it updated" }, dismissed = {},
            tryWake = { attempted.add(it); true })
        assertEquals(listOf("new updated"), attempted)
    }

    @Test fun completedQueueCanAcceptAnotherIndependentBatch() = runTest {
        val batches = mutableListOf<List<String>>()
        val queue = BluetoothStartupQueue(this, { false }, { batches.add(it) })
        queue.post("one", "one")
        runCurrent()
        queue.post("two", "two")
        runCurrent()
        assertEquals(listOf(listOf("one"), listOf("two")), batches)
    }
}
