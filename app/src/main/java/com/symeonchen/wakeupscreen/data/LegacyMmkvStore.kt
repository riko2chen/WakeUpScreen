package com.symeonchen.wakeupscreen.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * A read-only reader for the pair of files MMKV left behind.
 *
 * `mmkv.default` holds records of `varint keyLength, key bytes, varint
 * valueLength, value bytes`, one after another. MMKV appends rather than
 * rewrites, so a key that appears twice keeps its later value, and a
 * zero-length value marks a key that was removed.
 *
 * Where those records begin and end is the part that needs care. The first
 * four bytes of the file are a length that MMKV 2 stopped maintaining — it
 * reads 0 on every file this app wrote — with the live one kept in the
 * `mmkv.default.crc` companion instead. That length covers everything from
 * offset 4 on, and MMKV 2 parks a four-byte placeholder there before the first
 * record. Both are checked here, newest layout first, because the bound is
 * what stops the scan from walking into stale bytes left past the payload by
 * an earlier compaction.
 *
 * Anything inconsistent ends the scan and keeps what was read so far: a
 * truncated tail costs the newest writes, not every setting.
 */
object LegacyMmkvFile {

    /** The length field at the head of the data file, live only before MMKV 2. */
    private const val HEADER_SIZE = 4

    /** MMKV 2's placeholder, sitting between the header and the first record. */
    private const val HOLDER_SIZE = 4

    /** Offset of the live payload length inside the `.crc` companion. */
    private const val META_ACTUAL_SIZE_OFFSET = 0x1C

    private const val MAX_VARINT_BYTES = 10

    /**
     * @param meta the `.crc` companion, when it is there. Without it the scan
     * falls back to the data file's own header and then to its full length.
     */
    fun parse(data: ByteArray, meta: ByteArray? = null): Map<String, ByteArray> {
        if (data.size <= HEADER_SIZE) {
            return emptyMap()
        }
        val fromMeta = payloadEnd(meta, META_ACTUAL_SIZE_OFFSET, data.size)
        val fromHeader = payloadEnd(data, 0, data.size)
        val layouts = listOfNotNull(
            fromMeta?.let { HEADER_SIZE + HOLDER_SIZE to it },
            fromHeader?.let { HEADER_SIZE to it },
            HEADER_SIZE + HOLDER_SIZE to data.size,
            HEADER_SIZE to data.size,
        )
        for ((start, end) in layouts) {
            val entries = scan(data, start, end)
            if (entries.isNotEmpty()) {
                return entries
            }
        }
        return emptyMap()
    }

    /** The recorded length covers everything after the header, or it is unusable. */
    private fun payloadEnd(bytes: ByteArray?, offset: Int, limit: Int): Int? {
        if (bytes == null || bytes.size < offset + 4) {
            return null
        }
        val size = (bytes[offset].toLong() and 0xFF) or
            ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
            ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
            ((bytes[offset + 3].toLong() and 0xFF) shl 24)
        val end = HEADER_SIZE + size
        return if (size <= 0L || end > limit) null else end.toInt()
    }

    private fun scan(bytes: ByteArray, start: Int, end: Int): Map<String, ByteArray> {
        if (start >= end) {
            return emptyMap()
        }
        val entries = LinkedHashMap<String, ByteArray>()
        val cursor = Cursor(bytes, end)
        cursor.pos = start
        while (cursor.pos < end) {
            val keyLength = cursor.readVarint()?.toInt() ?: break
            if (keyLength <= 0 || !cursor.has(keyLength)) break
            val key = String(bytes, cursor.pos, keyLength, Charsets.UTF_8)
            cursor.pos += keyLength

            val valueLength = cursor.readVarint()?.toInt() ?: break
            if (valueLength < 0 || !cursor.has(valueLength)) break
            if (valueLength == 0) {
                entries.remove(key)
            } else {
                entries[key] = bytes.copyOfRange(cursor.pos, cursor.pos + valueLength)
            }
            cursor.pos += valueLength
        }
        return entries
    }

    /** MMKV stores a bool as a single-byte varint. */
    fun asBoolean(blob: ByteArray): Boolean? = asLong(blob)?.let { it != 0L }

    /**
     * A negative int is written as a sign-extended 64-bit varint, so reading
     * the wider form and narrowing covers both signs.
     */
    fun asInt(blob: ByteArray): Int? = asLong(blob)?.toInt()

    fun asLong(blob: ByteArray): Long? = Cursor(blob, blob.size).readVarint()

    /** A string blob is itself length-prefixed, inside the record's own length. */
    fun asString(blob: ByteArray): String? {
        val cursor = Cursor(blob, blob.size)
        val length = cursor.readVarint()?.toInt() ?: return null
        if (length < 0 || !cursor.has(length)) {
            return null
        }
        return String(blob, cursor.pos, length, Charsets.UTF_8)
    }

    private class Cursor(private val bytes: ByteArray, private val end: Int) {
        var pos: Int = 0

        fun has(count: Int): Boolean = count >= 0 && pos + count <= end

        /** LEB128, as protobuf writes it. Null when the bytes run out or never terminate. */
        fun readVarint(): Long? {
            var result = 0L
            var shift = 0
            while (shift < MAX_VARINT_BYTES * 7) {
                if (pos >= end) return null
                val byte = bytes[pos++].toInt()
                result = result or ((byte.toLong() and 0x7F) shl shift)
                if (byte and 0x80 == 0) return result
                shift += 7
            }
            return null
        }
    }
}

/**
 * Carries what MMKV persisted into [ScStore], once, on the first launch after
 * the upgrade. Nothing writes the legacy file any more, so the read is safe at
 * any point, and the file is deleted as soon as its contents are committed.
 */
object LegacyMmkvStore {

    /** Lives in the settings file so a single flag vouches for the whole migration. */
    private const val MIGRATED_FLAG = "legacy_mmkv_migrated"

    private const val LEGACY_DIR = "mmkv"
    private const val LEGACY_FILE = "mmkv.default"
    private const val LEGACY_META_FILE = "mmkv.default.crc"

    private enum class Type { BOOL, INT, LONG, STRING }

    private data class LegacyKey(val key: String, val type: Type)

    /**
     * Every key the last MMKV build wrote, with the type it wrote it as.
     *
     * Frozen on purpose: it describes 4.0.0's disk contents, not today's
     * settings. A setting added after the switch never existed in MMKV, so it
     * does not belong here — it joins the [SettingsBackup] catalog alone.
     */
    private val catalog = listOf(
        LegacyKey(ScConstant.CUSTOM_STATUS, Type.BOOL),
        LegacyKey(ScConstant.PROXIMITY_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.FACE_DOWN_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.FACE_DOWN_STATUS, Type.BOOL),
        LegacyKey(ScConstant.BATTERY_SAVER_FAKE_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.SEND_NOTIFICATION_PERMISSION, Type.BOOL),
        LegacyKey(ScConstant.DEBUG_MODE_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.ONGOING_STATUS_DETECT, Type.BOOL),
        LegacyKey(ScConstant.RADICAL_ONGOING_DETECT, Type.BOOL),
        LegacyKey(ScConstant.SLEEP_MODE_BOOLEAN, Type.BOOL),
        LegacyKey(ScConstant.NIGHT_GLOW_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.DND_DETECT_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.CHARGING_ONLY_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.REPEAT_REMINDER_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.PRECISE_SCREEN_ON_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.IGNORE_SILENT_NOTIFICATION_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.BATTERY_LEVEL_SWITCH, Type.BOOL),
        LegacyKey(ScConstant.PROXIMITY_STATUS, Type.INT),
        LegacyKey(ScConstant.APP_NOTIFY_MODE, Type.INT),
        LegacyKey(ScConstant.LANGUAGE_SELECTED, Type.INT),
        LegacyKey(ScConstant.DARK_MODE_SELECTED, Type.INT),
        LegacyKey(ScConstant.SLEEP_MODE_TIME_BEGIN, Type.INT),
        LegacyKey(ScConstant.SLEEP_MODE_TIME_END, Type.INT),
        LegacyKey(ScConstant.REPEAT_REMINDER_INTERVAL_MINUTES, Type.INT),
        LegacyKey(ScConstant.REPEAT_REMINDER_MAX_ROUNDS, Type.INT),
        LegacyKey(ScConstant.REPEAT_REMINDER_ROUND_COUNT, Type.INT),
        LegacyKey(ScConstant.BATTERY_LEVEL_THRESHOLD, Type.INT),
        LegacyKey(ScConstant.LAST_SEEN_VERSION_CODE, Type.INT),
        LegacyKey(ScConstant.UPDATED_FROM_VERSION_CODE, Type.INT),
        LegacyKey(ScConstant.WAKE_SCREEN_SECOND, Type.LONG),
        LegacyKey(ScConstant.APP_FILTER_LIST_FLAG, Type.LONG),
        LegacyKey(ScConstant.APP_FILTER_WHITE_LIST_STRING, Type.STRING),
        LegacyKey(ScConstant.APP_FILTER_BLACK_LIST_STRING, Type.STRING),
        LegacyKey(ScConstant.SLEEP_MODE_SEGMENTS, Type.STRING),
        LegacyKey(ScConstant.LAST_IN_APP_REVIEW_TIMESTAMP, Type.STRING),
        LegacyKey(ScConstant.CLICKED_FEATURE_BADGES, Type.STRING),
    )

    /**
     * Spelled out rather than referenced: these are the names 4.0.0 wrote, and
     * they must keep being read even after the stores bump their own key.
     */
    private val historyKeys = listOf("attention_stats_v1", "notification_logs_v2")

    fun migrateOnce(
        context: Context,
        settings: SharedPreferences,
        history: SharedPreferences,
    ) {
        if (settings.getBoolean(MIGRATED_FLAG, false)) {
            return
        }
        try {
            val legacyDir = File(context.filesDir, LEGACY_DIR)
            val entries = read(
                File(legacyDir, LEGACY_FILE),
                File(legacyDir, LEGACY_META_FILE),
            )

            // History first, the flag last: a crash in between replays the
            // whole migration, and every write of it is idempotent.
            writeHistory(entries, history)
            if (!writeSettings(entries, settings)) {
                return
            }
            legacyDir.deleteRecursively()
        } catch (_: Throwable) {
            // Left unflagged, so the next launch tries again. A user who lands
            // here keeps the defaults, which is where a plain removal would
            // have put everyone.
        }
    }

    private fun read(file: File, meta: File): Map<String, ByteArray> =
        if (file.isFile) {
            LegacyMmkvFile.parse(file.readBytes(), meta.takeIf { it.isFile }?.readBytes())
        } else {
            emptyMap()
        }

    private fun writeSettings(
        entries: Map<String, ByteArray>,
        settings: SharedPreferences,
    ): Boolean {
        val editor = settings.edit()
        for (entry in catalog) {
            val blob = entries[entry.key] ?: continue
            when (entry.type) {
                Type.BOOL -> LegacyMmkvFile.asBoolean(blob)?.let { editor.putBoolean(entry.key, it) }
                Type.INT -> LegacyMmkvFile.asInt(blob)?.let { editor.putInt(entry.key, it) }
                Type.LONG -> LegacyMmkvFile.asLong(blob)?.let { editor.putLong(entry.key, it) }
                Type.STRING -> LegacyMmkvFile.asString(blob)?.let { editor.putString(entry.key, it) }
            }
        }
        editor.putBoolean(MIGRATED_FLAG, true)
        // Committed rather than applied: the flag and the values it vouches for
        // have to be on disk before the legacy file is deleted.
        return editor.commit()
    }

    private fun writeHistory(entries: Map<String, ByteArray>, history: SharedPreferences) {
        val editor = history.edit()
        for (key in historyKeys) {
            val blob = entries[key] ?: continue
            LegacyMmkvFile.asString(blob)?.let { editor.putString(key, it) }
        }
        editor.commit()
    }
}
