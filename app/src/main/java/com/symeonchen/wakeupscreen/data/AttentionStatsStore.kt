package com.symeonchen.wakeupscreen.data

import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * MMKV persistence for [AttentionStats]. All the arithmetic lives there; this
 * is the disk round-trip plus the wall-clock-to-epoch-day conversion.
 */
object AttentionStatsStore {

    private const val KEY_ATTENTION_STATS = "attention_stats_v1"

    fun epochDay(timestampMs: Long): Long = TimeUnit.MILLISECONDS.toDays(timestampMs)

    fun recordWake(packageName: String, timestampMs: Long = System.currentTimeMillis()) {
        update { store -> AttentionStats.recordWake(store, packageName, epochDay(timestampMs)) }
    }

    fun recordFollow(packageName: String, timestampMs: Long = System.currentTimeMillis()) {
        update { store -> AttentionStats.recordFollow(store, packageName, epochDay(timestampMs)) }
    }

    fun summary(nowMs: Long = System.currentTimeMillis()): List<AppAttention> =
        AttentionStats.summarize(load(), epochDay(nowMs) - AttentionStats.RETENTION_DAYS + 1)

    fun clear() {
        MMKV.defaultMMKV()?.putString(KEY_ATTENTION_STATS, "{}")
    }

    private fun load(): JSONObject {
        val raw = MMKV.defaultMMKV()?.getString(KEY_ATTENTION_STATS, "{}") ?: "{}"
        return try {
            JSONObject(raw)
        } catch (_: Exception) {
            JSONObject()
        }
    }

    private fun update(transform: (JSONObject) -> JSONObject) {
        val mmkv = MMKV.defaultMMKV() ?: return
        try {
            mmkv.putString(KEY_ATTENTION_STATS, transform(load()).toString())
        } catch (_: Exception) {
        }
    }
}
