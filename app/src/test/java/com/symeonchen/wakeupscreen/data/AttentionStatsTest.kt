package com.symeonchen.wakeupscreen.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttentionStatsTest {

    private val day = 20_000L

    private fun windowStart(today: Long) = today - AttentionStats.RETENTION_DAYS + 1

    @Test
    fun `wakes and follows accumulate per app`() {
        var store = JSONObject()
        store = AttentionStats.recordWake(store, "com.a", day)
        store = AttentionStats.recordWake(store, "com.a", day)
        store = AttentionStats.recordWake(store, "com.b", day)
        store = AttentionStats.recordFollow(store, "com.a", day)

        val summary = AttentionStats.summarize(store, windowStart(day))

        assertEquals(
            listOf(AppAttention("com.a", 2, 1), AppAttention("com.b", 1, 0)),
            summary,
        )
    }

    @Test
    fun `wakes spread over days are summed`() {
        var store = JSONObject()
        store = AttentionStats.recordWake(store, "com.a", day)
        store = AttentionStats.recordWake(store, "com.a", day + 1)
        store = AttentionStats.recordWake(store, "com.a", day + 2)

        val summary = AttentionStats.summarize(store, windowStart(day + 2))

        assertEquals(listOf(AppAttention("com.a", 3, 0)), summary)
    }

    @Test
    fun `buckets older than the window are pruned on write`() {
        var store = JSONObject()
        store = AttentionStats.recordWake(store, "com.old", day)
        // A wake far in the future pushes the old bucket out of retention.
        store = AttentionStats.recordWake(store, "com.new", day + AttentionStats.RETENTION_DAYS)

        assertTrue(!store.has("com.old"))
        val summary = AttentionStats.summarize(store, windowStart(day + AttentionStats.RETENTION_DAYS))
        assertEquals(listOf(AppAttention("com.new", 1, 0)), summary)
    }

    @Test
    fun `summarize ignores days before the window even if still stored`() {
        var store = JSONObject()
        store = AttentionStats.recordWake(store, "com.a", day)

        val summary = AttentionStats.summarize(store, day + 1)

        assertTrue(summary.isEmpty())
    }

    @Test
    fun `suggestions need enough wakes and zero follow-ups`() {
        val ignoredOften = AppAttention("com.spam", AttentionStats.SUGGEST_MIN_WAKES, 0)
        val ignoredRarely = AppAttention("com.rare", AttentionStats.SUGGEST_MIN_WAKES - 1, 0)
        val followedOnce = AppAttention("com.used", 50, 1)

        val suggestions = AttentionStats.suggestions(
            listOf(ignoredOften, ignoredRarely, followedOnce)
        )

        assertEquals(listOf(ignoredOften), suggestions)
    }

    @Test
    fun `a corrupt store entry is skipped not fatal`() {
        val store = JSONObject().put("com.weird", "not an object")
        val summary = AttentionStats.summarize(store, 0)
        assertTrue(summary.isEmpty())
    }
}
