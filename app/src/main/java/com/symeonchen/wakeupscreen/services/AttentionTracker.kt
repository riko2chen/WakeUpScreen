package com.symeonchen.wakeupscreen.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import com.symeonchen.wakeupscreen.data.AttentionStats
import com.symeonchen.wakeupscreen.data.AttentionStatsStore

/**
 * Connects the two ends of the attention statistic: a wake for a package now,
 * an unlock (or not) within [AttentionStats.FOLLOW_WINDOW_MS].
 *
 * Pending wakes live in memory only. They matter for half a minute, and the
 * process that records the wake is the same one that receives the unlock; if
 * the process dies in between, one data point is lost, which is nothing
 * against a 30-day aggregate.
 */
object AttentionTracker {

    private data class PendingWake(val packageName: String, val elapsedMs: Long)

    private val pending = mutableListOf<PendingWake>()

    private var receiverRegistered = false

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                onUnlocked()
            }
        }
    }

    fun register(context: Context) {
        if (receiverRegistered) return
        try {
            // ACTION_USER_PRESENT is a protected system broadcast; receiving it
            // needs no permission and reveals nothing but "the user unlocked".
            context.applicationContext.registerReceiver(
                unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT)
            )
            receiverRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister(context: Context) {
        if (!receiverRegistered) return
        try {
            context.applicationContext.unregisterReceiver(unlockReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        receiverRegistered = false
    }

    /** Called right after the screen was woken for a notification. */
    @Synchronized
    fun onScreenWoken(packageName: String) {
        AttentionStatsStore.recordWake(packageName)
        val now = SystemClock.elapsedRealtime()
        pending.removeAll { now - it.elapsedMs > AttentionStats.FOLLOW_WINDOW_MS }
        pending.add(PendingWake(packageName, now))
    }

    @Synchronized
    private fun onUnlocked() {
        val now = SystemClock.elapsedRealtime()
        // Each package counts once per unlock, however many wakes it stacked
        // up inside the window — the question is "was it looked at", not "how
        // many notifications were pending".
        pending
            .filter { now - it.elapsedMs <= AttentionStats.FOLLOW_WINDOW_MS }
            .map { it.packageName }
            .distinct()
            .forEach { AttentionStatsStore.recordFollow(it) }
        pending.clear()
    }
}
