package com.symeonchen.wakeupscreen.services.bluetooth

/** One bounded initialization window; repeated reads never extend its deadline. */
class BluetoothStartupDiscovery(
    private val nowMillis: () -> Long,
    private val timeoutMillis: Long = TIMEOUT_MILLIS,
) {
    companion object { const val TIMEOUT_MILLIS = 1_500L }
    private var deadline = 0L
    private val pending = mutableSetOf<Int>()

    fun begin(profiles: Collection<Int>) {
        pending.clear()
        pending.addAll(profiles)
        deadline = nowMillis() + timeoutMillis
    }
    fun complete(profile: Int) { pending.remove(profile) }
    fun clear() { pending.clear(); deadline = 0L }
    fun remainingMillis(): Long = if (pending.isEmpty()) 0L else (deadline - nowMillis()).coerceAtLeast(0L)
}
