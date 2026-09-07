package com.symeonchen.wakeupscreen.services.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coalesces updates only while a key is waiting. Callers must use the same
 * single-thread dispatcher as [scope] for every operation.
 */
internal class NotificationGraceQueue<T>(
    private val scope: CoroutineScope,
    private val onReady: (T) -> Unit,
) {
    private class Pending<T>(var latest: T, val job: Job)

    private val pending = mutableMapOf<String, Pending<T>>()

    fun post(key: String, value: T, gracePeriodMs: Long) {
        require(gracePeriodMs > 0L)
        pending[key]?.let {
            it.latest = value
            return
        }

        // Install the entry before starting its coroutine, even with an
        // immediate dispatcher. Cancellation of an old job cannot erase a new one.
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                delay(gracePeriodMs)
                val entry = pending.remove(key) ?: return@launch
                onReady(entry.latest)
            } finally {
                if (pending[key]?.job === coroutineContext[Job]) {
                    pending.remove(key)
                }
            }
        }
        pending[key] = Pending(value, job)
        job.start()
    }

    /** Returns the latest payload exactly once so removal can log it immediately. */
    fun remove(key: String): T? {
        val entry = pending.remove(key) ?: return null
        entry.job.cancel()
        return entry.latest
    }

    fun clear() {
        val entries = pending.values.toList()
        pending.clear()
        entries.forEach { it.job.cancel() }
    }
}
