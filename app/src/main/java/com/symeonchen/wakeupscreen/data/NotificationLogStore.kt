package com.symeonchen.wakeupscreen.data

import org.json.JSONArray
import org.json.JSONObject

enum class LogStatus(val key: String) {
    SCREEN_ALREADY_ON("already_on"),
    WAKED_UP("waked_up"),
    BLOCKED("blocked"),

    /**
     * Stopped by the sleep gate, but the night glow showed the dim red pulse
     * instead of leaving the display dark. [NotificationLogEntry.blockReason]
     * still names the gate.
     */
    NIGHT_GLOW("night_glow"),

    /** A repeat-reminder streak ended; [NotificationLogEntry.blockReason] says why. */
    REMINDER_STOPPED("reminder_stopped");

    companion object {
        fun fromKey(key: String): LogStatus = entries.firstOrNull { it.key == key } ?: BLOCKED
    }
}

/**
 * What caused this log entry: a notification the system just delivered, or the
 * repeat reminder firing on its own because unread notifications were still
 * sitting in the shade.
 */
enum class LogTrigger(val key: String) {
    NOTIFICATION("notification"),
    REMINDER("reminder");

    companion object {
        fun fromKey(key: String): LogTrigger = entries.firstOrNull { it.key == key } ?: NOTIFICATION
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
    val trigger: LogTrigger = LogTrigger.NOTIFICATION,
    /** Which reminder of the current streak this was. 0 for non-reminder entries. */
    val reminderRound: Int = 0,
    /** Unread notifications counted when the reminder fired. 0 for non-reminder entries. */
    val unreadCount: Int = 0,
)

object NotificationLogStore {

    private const val KEY_NOTIFICATION_LOGS = "notification_logs_v2"
    private const val MAX_LOGS = 500

    fun addLog(entry: NotificationLogEntry) {
        val arr = loadRawArray()
        val obj = JSONObject().apply {
            put("ts", entry.timestamp)
            put("pkg", entry.packageName)
            put("status", entry.status.key)
            put("reason", entry.blockReason)
            put("importance", entry.importance)
            if (entry.hasSound != null) put("hasSound", entry.hasSound)
            if (entry.hasVibration != null) put("hasVibration", entry.hasVibration)
            // Only written for reminder entries so existing logs stay compact
            // and older entries keep reading back as plain notification events.
            if (entry.trigger != LogTrigger.NOTIFICATION) {
                put("trigger", entry.trigger.key)
                put("round", entry.reminderRound)
                put("unread", entry.unreadCount)
            }
        }
        arr.put(obj)
        while (arr.length() > MAX_LOGS) {
            arr.remove(0)
        }
        ScStore.putHistoryString(KEY_NOTIFICATION_LOGS, arr.toString())
    }

    fun loadLogs(): List<NotificationLogEntry> {
        val arr = loadRawArray()
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
                    trigger = LogTrigger.fromKey(obj.optString("trigger", LogTrigger.NOTIFICATION.key)),
                    reminderRound = obj.optInt("round", 0),
                    unreadCount = obj.optInt("unread", 0),
                )
            )
        }
        return list.asReversed()
    }

    /**
     * When the screen last lit up for a notification or reminder, or null if no
     * wake has been recorded (or the log has since been cleared).
     */
    fun lastWakeTimestamp(): Long? =
        loadLogs().firstOrNull { it.status == LogStatus.WAKED_UP }?.timestamp

    fun clearLogs() {
        ScStore.putHistoryString(KEY_NOTIFICATION_LOGS, "[]")
    }

    private fun loadRawArray(): JSONArray {
        val raw = ScStore.getHistoryString(KEY_NOTIFICATION_LOGS, "[]")
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }
}
