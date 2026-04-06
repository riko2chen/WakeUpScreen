package com.symeonchen.wakeupscreen.data

import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject

enum class LogStatus(val key: String) {
    SCREEN_ALREADY_ON("already_on"),
    WAKED_UP("waked_up"),
    BLOCKED("blocked");

    companion object {
        fun fromKey(key: String): LogStatus = entries.firstOrNull { it.key == key } ?: BLOCKED
    }
}

data class NotificationLogEntry(
    val timestamp: Long,
    val packageName: String,
    val status: LogStatus,
    val blockReason: String = "",
    val importance: Int = -1,
    val hasSound: Boolean? = null,
    val hasVibration: Boolean? = null,
)

object NotificationLogStore {

    private const val KEY_NOTIFICATION_LOGS = "notification_logs_v2"
    private const val MAX_LOGS = 500

    fun addLog(entry: NotificationLogEntry) {
        val mmkv = MMKV.defaultMMKV() ?: return
        val arr = loadRawArray(mmkv)
        val obj = JSONObject().apply {
            put("ts", entry.timestamp)
            put("pkg", entry.packageName)
            put("status", entry.status.key)
            put("reason", entry.blockReason)
            put("importance", entry.importance)
            if (entry.hasSound != null) put("hasSound", entry.hasSound)
            if (entry.hasVibration != null) put("hasVibration", entry.hasVibration)
        }
        arr.put(obj)
        while (arr.length() > MAX_LOGS) {
            arr.remove(0)
        }
        mmkv.putString(KEY_NOTIFICATION_LOGS, arr.toString())
    }

    fun loadLogs(): List<NotificationLogEntry> {
        val mmkv = MMKV.defaultMMKV() ?: return emptyList()
        val arr = loadRawArray(mmkv)
        val list = mutableListOf<NotificationLogEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                NotificationLogEntry(
                    timestamp = obj.getLong("ts"),
                    packageName = obj.getString("pkg"),
                    status = LogStatus.fromKey(obj.optString("status", "blocked")),
                    blockReason = obj.optString("reason", ""),
                    importance = obj.optInt("importance", -1),
                    hasSound = if (obj.has("hasSound")) obj.getBoolean("hasSound") else null,
                    hasVibration = if (obj.has("hasVibration")) obj.getBoolean("hasVibration") else null,
                )
            )
        }
        return list.asReversed()
    }

    fun clearLogs() {
        MMKV.defaultMMKV()?.putString(KEY_NOTIFICATION_LOGS, "[]")
    }

    private fun loadRawArray(mmkv: MMKV): JSONArray {
        val raw = mmkv.getString(KEY_NOTIFICATION_LOGS, "[]") ?: "[]"
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }
}
