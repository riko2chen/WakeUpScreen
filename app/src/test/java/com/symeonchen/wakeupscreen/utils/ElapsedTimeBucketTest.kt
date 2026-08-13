package com.symeonchen.wakeupscreen.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ElapsedTimeBucketTest {

    private val now = 1_000_000_000_000L

    private fun ago(ms: Long) = ElapsedTimeBucket.between(now - ms, now)

    @Test
    fun `under a minute is just now`() {
        assertEquals(ElapsedTimeBucket.JustNow, ago(0))
        assertEquals(ElapsedTimeBucket.JustNow, ago(59_999))
    }

    @Test
    fun `minutes until an hour`() {
        assertEquals(ElapsedTimeBucket.Minutes(1), ago(60_000))
        assertEquals(ElapsedTimeBucket.Minutes(59), ago(59 * 60_000L + 30_000))
    }

    @Test
    fun `hours until a day`() {
        assertEquals(ElapsedTimeBucket.Hours(1), ago(60 * 60_000L))
        assertEquals(ElapsedTimeBucket.Hours(23), ago(23 * 60 * 60_000L + 5))
    }

    @Test
    fun `days from there on`() {
        assertEquals(ElapsedTimeBucket.Days(1), ago(24 * 60 * 60_000L))
        assertEquals(ElapsedTimeBucket.Days(30), ago(30 * 24 * 60 * 60_000L))
    }

    @Test
    fun `a future timestamp reads as just now`() {
        assertEquals(ElapsedTimeBucket.JustNow, ElapsedTimeBucket.between(now + 5_000, now))
    }
}
