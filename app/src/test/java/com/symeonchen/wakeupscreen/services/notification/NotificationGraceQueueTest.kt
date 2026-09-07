package com.symeonchen.wakeupscreen.services.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationGraceQueueTest {
    @Test
    fun frequentUpdatesDeliverLatestContentAtTheFirstDeadline() = runTest {
        val delivered = mutableListOf<Pair<Long, String>>()
        val queue = NotificationGraceQueue<String>(this) {
            delivered.add(testScheduler.currentTime to it)
        }
        queue.post("A", "first", 1000)
        runCurrent()
        repeat(3) { index ->
            advanceTimeBy(300)
            queue.post("A", "update $index", 1000)
            runCurrent()
        }
        advanceTimeBy(99)
        runCurrent()
        assertTrue(delivered.isEmpty())
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(1000L to "update 2"), delivered)
        advanceUntilIdle()
        assertEquals(1, delivered.size)
    }

    @Test
    fun differentKeysKeepIndependentDeadlines() = runTest {
        val delivered = mutableListOf<String>()
        val queue = NotificationGraceQueue<String>(this) { delivered.add(it) }
        queue.post("A", "first", 1000)
        runCurrent()
        advanceTimeBy(300)
        queue.post("B", "second", 1000)
        runCurrent()
        advanceTimeBy(700)
        runCurrent()
        assertEquals(listOf("first"), delivered)
        advanceTimeBy(300)
        runCurrent()
        assertEquals(listOf("first", "second"), delivered)
    }

    @Test
    fun removalReturnsLatestContentOnceAndCancelsDelivery() = runTest {
        val delivered = mutableListOf<String>()
        val queue = NotificationGraceQueue<String>(this) { delivered.add(it) }
        queue.post("A", "first", 1000)
        runCurrent()
        advanceTimeBy(300)
        queue.post("A", "latest", 1000)
        assertEquals("latest", queue.remove("A"))
        assertNull(queue.remove("A"))
        advanceUntilIdle()
        assertTrue(delivered.isEmpty())
    }

    @Test
    fun sameKeyAfterRemovalGetsAFullNewWait() = runTest {
        val delivered = mutableListOf<Pair<Long, String>>()
        val queue = NotificationGraceQueue<String>(this) {
            delivered.add(testScheduler.currentTime to it)
        }
        queue.post("A", "removed", 1000)
        runCurrent()
        advanceTimeBy(300)
        queue.remove("A")
        // Repost before the cancelled coroutine's finally has run.
        queue.post("A", "reposted", 1000)
        runCurrent()
        advanceTimeBy(700)
        runCurrent()
        assertTrue(delivered.isEmpty())
        advanceTimeBy(300)
        runCurrent()
        assertEquals(listOf(1300L to "reposted"), delivered)
    }

    @Test
    fun sameKeyAfterDeliveryCanBeProcessedAgain() = runTest {
        val delivered = mutableListOf<String>()
        val queue = NotificationGraceQueue<String>(this) { delivered.add(it) }
        queue.post("A", "first", 500)
        advanceUntilIdle()
        queue.post("A", "second", 500)
        runCurrent()
        advanceTimeBy(499)
        runCurrent()
        assertEquals(listOf("first"), delivered)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("first", "second"), delivered)
    }

    @Test
    fun clearCancelsAllKeysAndAllowsReuseAfterReconnect() = runTest {
        val delivered = mutableListOf<String>()
        val queue = NotificationGraceQueue<String>(this) { delivered.add(it) }
        queue.post("A", "old A", 500)
        queue.post("B", "old B", 500)
        runCurrent()
        queue.clear()
        queue.post("A", "new A", 500)
        advanceUntilIdle()
        assertEquals(listOf("new A"), delivered)
    }

    @Test
    fun serviceScopeCancellationPreventsDelivery() = runTest {
        val serviceScope = CoroutineScope(coroutineContext + SupervisorJob())
        val delivered = mutableListOf<String>()
        val queue = NotificationGraceQueue<String>(serviceScope) { delivered.add(it) }
        queue.post("A", "first", 500)
        runCurrent()
        serviceScope.cancel()
        queue.clear()
        advanceUntilIdle()
        assertTrue(delivered.isEmpty())
        assertNull(queue.remove("A"))
    }
}
