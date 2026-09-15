package com.symeonchen.wakeupscreen.services.bluetooth

import kotlinx.coroutines.*

/** Main-thread queue for the initial Bluetooth snapshot only, never for future reconnects.
 * Updates replace their payload without extending the wait. One batch lets the caller
 * recheck active notifications and all rules, then coalesce the batch into at most one wake.
 */
class BluetoothStartupQueue<T>(
    private val scope: CoroutineScope,
    private val isPending: () -> Boolean,
    private val onReady: (List<T>) -> Unit,
    private val waitMillis: Long = BluetoothStartupDiscovery.TIMEOUT_MILLIS,
) {
    private val pending = linkedMapOf<String, T>()
    private var job: Job? = null

    val hasPending: Boolean get() = pending.isNotEmpty()

    fun post(key: String, value: T) {
        pending.remove(key)
        pending[key] = value
        if (job != null) return
        job = scope.launch(start = CoroutineStart.LAZY) {
            withTimeoutOrNull(waitMillis) {
                while (isPending()) delay(50L)
            }
            val batch = pending.values.toList().asReversed()
            pending.clear()
            job = null
            onReady(batch)
        }
        job?.start()
    }
    fun remove(key: String): T? {
        val removed = pending.remove(key)
        if (pending.isEmpty()) { job?.cancel(); job = null }
        return removed
    }
    fun clear() { job?.cancel(); job = null; pending.clear() }
}

/** Recheck current payloads and stop after the first actual wake, not the first candidate. */
object BluetoothStartupBatch {
    fun <T> recheck(
        pending: List<T>,
        current: (T) -> T?,
        dismissed: (T) -> Unit,
        tryWake: (T) -> Boolean,
    ) {
        for (original in pending) {
            val candidate = current(original)
            if (candidate == null) dismissed(original)
            else if (tryWake(candidate)) return
        }
    }
}
