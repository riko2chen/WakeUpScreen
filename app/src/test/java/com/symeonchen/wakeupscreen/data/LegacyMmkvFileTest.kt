package com.symeonchen.wakeupscreen.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reader is only ever pointed at files this app will never write again, so
 * the fixtures below re-create MMKV's own encoding rather than a recorded file.
 *
 * [mmkvFile] is the layout MMKV 2 wrote — a dead length field, a four-byte
 * placeholder, then the records, with the live length in the `.crc` companion
 * — and [legacyMmkvFile] is the older one, where the length at the head of the
 * data file was the real thing and the records followed it directly.
 */
class LegacyMmkvFileTest {

    @Test
    fun `reads every type MMKV wrote`() {
        val records = listOf(
            record("flag", boolValue(true)),
            record("off", boolValue(false)),
            record("count", intValue(4200)),
            record("stamp", longValue(1_700_000_000_000L)),
            record("list", stringValue("com.tencent.mm,com.foo")),
        )

        val entries = LegacyMmkvFile.parse(mmkvFile(records), metaFile(records))

        assertEquals(true, LegacyMmkvFile.asBoolean(entries.getValue("flag")))
        assertEquals(false, LegacyMmkvFile.asBoolean(entries.getValue("off")))
        assertEquals(4200, LegacyMmkvFile.asInt(entries.getValue("count")))
        assertEquals(1_700_000_000_000L, LegacyMmkvFile.asLong(entries.getValue("stamp")))
        assertEquals("com.tencent.mm,com.foo", LegacyMmkvFile.asString(entries.getValue("list")))
    }

    /** MMKV sign-extends a negative int to a ten-byte varint. */
    @Test
    fun `reads a negative int`() {
        val entries = parseNew(record("delta", intValue(-7)))

        assertEquals(-7, LegacyMmkvFile.asInt(entries.getValue("delta")))
    }

    @Test
    fun `reads a multi-byte utf8 string`() {
        val entries = parseNew(record("note", stringValue("夜间微光")))

        assertEquals("夜间微光", LegacyMmkvFile.asString(entries.getValue("note")))
    }

    @Test
    fun `an empty string is a value, not an absent key`() {
        val entries = parseNew(record("segments", stringValue("")))

        assertEquals("", LegacyMmkvFile.asString(entries.getValue("segments")))
    }

    /** MMKV appends instead of rewriting, so the last write of a key is the value. */
    @Test
    fun `the later write of a key wins`() {
        val entries = parseNew(
            record("threshold", intValue(20)),
            record("threshold", intValue(50)),
        )

        assertEquals(50, LegacyMmkvFile.asInt(entries.getValue("threshold")))
    }

    /** A removed key is appended with a zero-length value. */
    @Test
    fun `a zero-length value removes the key`() {
        val entries = parseNew(
            record("gone", intValue(1)),
            record("kept", intValue(2)),
            record("gone", ByteArray(0)),
        )

        assertTrue("gone" !in entries)
        assertEquals(2, LegacyMmkvFile.asInt(entries.getValue("kept")))
    }

    /** The pre-MMKV-2 layout: a live length at the head, records right after it. */
    @Test
    fun `reads the layout that predates the placeholder`() {
        val entries = LegacyMmkvFile.parse(legacyMmkvFile(listOf(record("mode", intValue(2)))))

        assertEquals(2, LegacyMmkvFile.asInt(entries.getValue("mode")))
    }

    /**
     * The recorded length is what keeps a compaction's leftovers out. Those
     * bytes are older writes of live keys, so reading them would not merely add
     * noise — it would put stale values back.
     */
    @Test
    fun `stale bytes past the recorded length are not read`() {
        val live = listOf(record("threshold", intValue(50)))
        val stale = record("threshold", intValue(20))
        val bytes = mmkvFile(live) + stale

        val entries = LegacyMmkvFile.parse(bytes, metaFile(live))

        assertEquals(50, LegacyMmkvFile.asInt(entries.getValue("threshold")))
    }

    /** Without a usable length, the whole file is the best guess left. */
    @Test
    fun `falls back to the file length when no length is recorded`() {
        val entries = LegacyMmkvFile.parse(mmkvFile(listOf(record("mode", intValue(2)))), meta = null)

        assertEquals(2, LegacyMmkvFile.asInt(entries.getValue("mode")))
    }

    @Test
    fun `a truncated tail keeps the records that were complete`() {
        val records = listOf(record("kept", intValue(9)), record("cut", stringValue("half")))
        val full = mmkvFile(records)
        val truncated = full.copyOfRange(0, full.size - 2)

        val entries = LegacyMmkvFile.parse(truncated, metaFile(records))

        assertEquals(9, LegacyMmkvFile.asInt(entries.getValue("kept")))
        assertTrue("cut" !in entries)
    }

    @Test
    fun `an empty or absurd file reads as nothing`() {
        assertEquals(emptyMap<String, ByteArray>(), LegacyMmkvFile.parse(ByteArray(0)))
        assertEquals(emptyMap<String, ByteArray>(), LegacyMmkvFile.parse(ByteArray(4)))
        assertEquals(emptyMap<String, ByteArray>(), LegacyMmkvFile.parse(ByteArray(64)))
        assertEquals(
            emptyMap<String, ByteArray>(),
            LegacyMmkvFile.parse(ByteArray(64) { -1 }, ByteArray(64) { -1 }),
        )
    }

    /**
     * A length big enough to overflow the bounds check when added to the
     * cursor. Reachable from real bytes: the fallback layouts scan from a guess
     * at the payload start, which can land mid-record.
     */
    @Test
    fun `an absurd record length is refused, not thrown on`() {
        val absurd = byteArrayOf(-1, -1, -1, -1, 7) // varint for Int.MAX_VALUE
        val bytes = uint32(0) + uint32(0) + absurd + "junk".toByteArray(Charsets.UTF_8)

        assertEquals(emptyMap<String, ByteArray>(), LegacyMmkvFile.parse(bytes))
    }

    @Test
    fun `an absurd string length reads as null, not thrown on`() {
        assertNull(LegacyMmkvFile.asString(byteArrayOf(-1, -1, -1, -1, 7)))
    }

    @Test
    fun `a string blob that overruns its own length reads as null`() {
        assertNull(LegacyMmkvFile.asString(byteArrayOf(20, 'a'.code.toByte())))
    }

    // --- MMKV's encoding, for fixtures --------------------------------------

    private fun parseNew(vararg records: ByteArray): Map<String, ByteArray> {
        val list = records.toList()
        return LegacyMmkvFile.parse(mmkvFile(list), metaFile(list))
    }

    private fun varint(value: Long): ByteArray {
        var rest = value
        val out = mutableListOf<Byte>()
        while (true) {
            val chunk = (rest and 0x7F).toInt()
            rest = rest ushr 7
            if (rest == 0L) {
                out.add(chunk.toByte())
                return out.toByteArray()
            }
            out.add((chunk or 0x80).toByte())
        }
    }

    private fun boolValue(value: Boolean): ByteArray = byteArrayOf(if (value) 1 else 0)

    private fun intValue(value: Int): ByteArray = varint(value.toLong())

    private fun longValue(value: Long): ByteArray = varint(value)

    private fun stringValue(value: String): ByteArray {
        val utf8 = value.toByteArray(Charsets.UTF_8)
        return varint(utf8.size.toLong()) + utf8
    }

    private fun record(key: String, value: ByteArray): ByteArray {
        val utf8 = key.toByteArray(Charsets.UTF_8)
        return varint(utf8.size.toLong()) + utf8 + varint(value.size.toLong()) + value
    }

    private fun uint32(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte(),
    )

    private fun payloadOf(records: List<ByteArray>): ByteArray =
        records.fold(ByteArray(0)) { acc, bytes -> acc + bytes }

    /** MMKV 2: a dead length, the four-byte placeholder, then the records. */
    private fun mmkvFile(records: List<ByteArray>): ByteArray =
        uint32(0) + uint32(0x5CF88BCE) + payloadOf(records)

    /** The `.crc` companion, which is where MMKV 2 keeps the live length. */
    private fun metaFile(records: List<ByteArray>): ByteArray {
        val actualSize = 4 + payloadOf(records).size
        return ByteArray(0x1C) + uint32(actualSize)
    }

    /** Before MMKV 2: a live length at the head, records straight after it. */
    private fun legacyMmkvFile(records: List<ByteArray>): ByteArray {
        val payload = payloadOf(records)
        return uint32(payload.size) + payload
    }
}
