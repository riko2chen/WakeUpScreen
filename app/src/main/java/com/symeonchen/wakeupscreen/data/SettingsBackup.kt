package com.symeonchen.wakeupscreen.data

import org.json.JSONObject

/**
 * The file format: a versioned envelope around a flat `settings` object.
 *
 * The envelope logic is separated from the store reads so the version handling
 * — the part that decides whether a file from another build can be trusted —
 * is plain string-in/string-out and unit-testable.
 */
object BackupEnvelope {

    /**
     * Bump this when the meaning of a stored setting changes, and teach
     * [migrate] how to bring the old shape forward. Merely *adding* settings
     * does not need a bump: unknown keys are ignored on import and missing
     * keys keep their current value, so files travel both directions across
     * feature releases.
     */
    const val FORMAT_VERSION = 1

    private const val KEY_APP = "app"
    private const val KEY_FORMAT_VERSION = "format_version"
    private const val KEY_EXPORTED_AT = "exported_at"
    private const val KEY_SETTINGS = "settings"

    private const val APP_MARKER = "WakeUpScreen"

    sealed class UnwrapResult {
        data class Success(val settings: JSONObject) : UnwrapResult()

        /** Not our file at all — wrong marker, or not even JSON. */
        object Corrupt : UnwrapResult()

        /**
         * Written by a build whose format this one does not know yet. Refused
         * rather than half-applied: the newer build changed the format for a
         * reason this build cannot know.
         */
        data class NewerVersion(val version: Int) : UnwrapResult()
    }

    fun wrap(settings: JSONObject, exportedAtMs: Long): JSONObject = JSONObject().apply {
        put(KEY_APP, APP_MARKER)
        put(KEY_FORMAT_VERSION, FORMAT_VERSION)
        put(KEY_EXPORTED_AT, exportedAtMs)
        put(KEY_SETTINGS, settings)
    }

    fun unwrap(raw: String): UnwrapResult {
        val root = try {
            JSONObject(raw)
        } catch (_: Exception) {
            return UnwrapResult.Corrupt
        }
        if (root.optString(KEY_APP) != APP_MARKER) {
            return UnwrapResult.Corrupt
        }
        val version = root.optInt(KEY_FORMAT_VERSION, -1)
        if (version < 1) {
            return UnwrapResult.Corrupt
        }
        if (version > FORMAT_VERSION) {
            return UnwrapResult.NewerVersion(version)
        }
        val settings = root.optJSONObject(KEY_SETTINGS) ?: return UnwrapResult.Corrupt
        return UnwrapResult.Success(migrate(settings, version))
    }

    /**
     * Brings settings written by an older format up to the current one. With
     * only one format in existence this is the identity; the chain grows here
     * when a version bump happens, one step per version.
     */
    private fun migrate(settings: JSONObject, fromVersion: Int): JSONObject {
        var current = settings
        var version = fromVersion
        while (version < FORMAT_VERSION) {
            // when (version) { 1 -> current = migrateV1toV2(current) }
            version++
        }
        return current
    }
}

/**
 * Export and import of every user-configured setting.
 *
 * The catalog below is the single list of what a backup carries. Runtime state
 * (sensor postures, reminder round counters, review timestamps) is deliberately
 * absent: restoring those onto another device would import stale device facts,
 * not preferences.
 */
object SettingsBackup {

    private enum class SettingType { BOOL, INT, LONG, STRING }

    private data class CatalogEntry(val key: String, val type: SettingType)

    private val catalog = listOf(
        CatalogEntry(ScConstant.CUSTOM_STATUS, SettingType.BOOL),
        CatalogEntry(ScConstant.PROXIMITY_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.WAKE_SCREEN_SECOND, SettingType.LONG),
        CatalogEntry(ScConstant.APP_NOTIFY_MODE, SettingType.INT),
        CatalogEntry(ScConstant.APP_FILTER_WHITE_LIST_STRING, SettingType.STRING),
        CatalogEntry(ScConstant.APP_FILTER_BLACK_LIST_STRING, SettingType.STRING),
        CatalogEntry(ScConstant.ONGOING_STATUS_DETECT, SettingType.BOOL),
        CatalogEntry(ScConstant.RADICAL_ONGOING_DETECT, SettingType.BOOL),
        CatalogEntry(ScConstant.LANGUAGE_SELECTED, SettingType.INT),
        CatalogEntry(ScConstant.DARK_MODE_SELECTED, SettingType.INT),
        CatalogEntry(ScConstant.SLEEP_MODE_BOOLEAN, SettingType.BOOL),
        CatalogEntry(ScConstant.SLEEP_MODE_SEGMENTS, SettingType.STRING),
        CatalogEntry(ScConstant.DND_DETECT_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.CHARGING_ONLY_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.REPEAT_REMINDER_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.REPEAT_REMINDER_INTERVAL_MINUTES, SettingType.INT),
        CatalogEntry(ScConstant.REPEAT_REMINDER_MAX_ROUNDS, SettingType.INT),
        CatalogEntry(ScConstant.PRECISE_SCREEN_ON_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.IGNORE_SILENT_NOTIFICATION_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.BATTERY_LEVEL_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.BATTERY_LEVEL_THRESHOLD, SettingType.INT),
        CatalogEntry(ScConstant.FACE_DOWN_SWITCH, SettingType.BOOL),
        CatalogEntry(ScConstant.NIGHT_GLOW_SWITCH, SettingType.BOOL),
    )

    sealed class ImportResult {
        data class Success(val applied: Int) : ImportResult()
        object Corrupt : ImportResult()
        data class NewerVersion(val version: Int) : ImportResult()
    }

    /** The complete backup file content. */
    fun export(nowMs: Long = System.currentTimeMillis()): String {
        val settings = JSONObject()
        for (entry in catalog) {
            // Only keys that were ever written are exported; a fresh install's
            // implicit defaults stay implicit, so a future build with different
            // defaults is not pinned to today's.
            if (!ScStore.containsKey(entry.key)) continue
            when (entry.type) {
                SettingType.BOOL -> settings.put(entry.key, ScStore.getBoolean(entry.key, false))
                SettingType.INT -> settings.put(entry.key, ScStore.getInt(entry.key, 0))
                SettingType.LONG -> settings.put(entry.key, ScStore.getLong(entry.key, 0L))
                SettingType.STRING -> settings.put(entry.key, ScStore.getString(entry.key, "") ?: "")
            }
        }
        return BackupEnvelope.wrap(settings, nowMs).toString(2)
    }

    /**
     * Applies a backup file. Unknown keys in the file are skipped (they come
     * from a build with more settings), and catalog keys absent from the file
     * keep their current value.
     */
    fun import(raw: String): ImportResult {
        val settings = when (val unwrapped = BackupEnvelope.unwrap(raw)) {
            is BackupEnvelope.UnwrapResult.Corrupt -> return ImportResult.Corrupt
            is BackupEnvelope.UnwrapResult.NewerVersion ->
                return ImportResult.NewerVersion(unwrapped.version)
            is BackupEnvelope.UnwrapResult.Success -> unwrapped.settings
        }
        var applied = 0
        for (entry in catalog) {
            if (!settings.has(entry.key)) continue
            try {
                when (entry.type) {
                    SettingType.BOOL -> ScStore.putBoolean(entry.key, settings.getBoolean(entry.key))
                    SettingType.INT -> ScStore.putInt(entry.key, settings.getInt(entry.key))
                    SettingType.LONG -> ScStore.putLong(entry.key, settings.getLong(entry.key))
                    SettingType.STRING -> ScStore.putString(entry.key, settings.getString(entry.key))
                }
                applied++
            } catch (_: Exception) {
                // A value of the wrong type is skipped, not fatal: the rest of
                // the file is still worth applying.
            }
        }
        return ImportResult.Success(applied)
    }
}
