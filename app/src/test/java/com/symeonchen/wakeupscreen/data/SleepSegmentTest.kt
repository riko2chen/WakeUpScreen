package com.symeonchen.wakeupscreen.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepSegmentTest {

    private fun at(hour: Int, minute: Int = 0) = hour * 60 + minute

    @Test
    fun `start is inside the window and end is not`() {
        val segment = SleepSegment.of(2, 0, 4, 0)

        assertTrue(segment.containsMinute(at(2)))
        assertTrue(segment.containsMinute(at(3, 59)))
        assertFalse(segment.containsMinute(at(4)))
        assertFalse(segment.containsMinute(at(1, 59)))
    }

    @Test
    fun `minute precision is honoured`() {
        val segment = SleepSegment.of(23, 30, 6, 45)

        assertFalse(segment.containsMinute(at(23, 29)))
        assertTrue(segment.containsMinute(at(23, 30)))
        assertTrue(segment.containsMinute(at(6, 44)))
        assertFalse(segment.containsMinute(at(6, 45)))
    }

    @Test
    fun `window running over midnight covers both sides`() {
        val segment = SleepSegment.of(22, 0, 4, 0)

        assertTrue(segment.containsMinute(at(22)))
        assertTrue(segment.containsMinute(at(23)))
        assertTrue(segment.containsMinute(at(0)))
        assertTrue(segment.containsMinute(at(3, 59)))
        assertFalse(segment.containsMinute(at(4)))
        assertFalse(segment.containsMinute(at(21, 59)))
    }

    @Test
    fun `window ending exactly at midnight is not empty`() {
        val segment = SleepSegment.of(22, 0, 0, 0)

        assertTrue(segment.containsMinute(at(22)))
        assertTrue(segment.containsMinute(at(23, 59)))
        assertFalse(segment.containsMinute(at(0)))
        assertEquals(120, segment.durationMinutes)
    }

    @Test
    fun `duration accounts for the midnight wrap`() {
        assertEquals(120, SleepSegment.of(2, 0, 4, 0).durationMinutes)
        assertEquals(6 * 60, SleepSegment.of(22, 0, 4, 0).durationMinutes)
        assertEquals(75, SleepSegment.of(23, 30, 0, 45).durationMinutes)
    }

    @Test
    fun `adjacent windows do not overlap`() {
        val first = SleepSegment.of(2, 0, 4, 0)
        val second = SleepSegment.of(4, 0, 6, 0)

        assertFalse(first.overlaps(second))
        assertFalse(second.overlaps(first))
    }

    @Test
    fun `windows sharing any minute overlap`() {
        val base = SleepSegment.of(2, 0, 4, 0)

        assertTrue(base.overlaps(SleepSegment.of(3, 0, 5, 0)))
        assertTrue(base.overlaps(SleepSegment.of(1, 0, 3, 0)))
        assertTrue(base.overlaps(SleepSegment.of(1, 0, 5, 0)))
        assertTrue(base.overlaps(SleepSegment.of(2, 30, 3, 30)))
        assertTrue(base.overlaps(base))
    }

    @Test
    fun `overlap is detected across midnight`() {
        val night = SleepSegment.of(22, 0, 6, 0)

        assertTrue(night.overlaps(SleepSegment.of(5, 0, 7, 0)))
        assertTrue(night.overlaps(SleepSegment.of(21, 0, 23, 0)))
        assertTrue(night.overlaps(SleepSegment.of(0, 0, 1, 0)))
        assertFalse(night.overlaps(SleepSegment.of(6, 0, 22, 0)))
        assertFalse(night.overlaps(SleepSegment.of(12, 0, 13, 0)))
    }

    @Test
    fun `two windows over midnight always overlap`() {
        assertTrue(SleepSegment.of(23, 0, 5, 0).overlaps(SleepSegment.of(22, 0, 1, 0)))
    }

    @Test
    fun `equal bounds are rejected`() {
        assertFalse(SleepSegment.of(2, 0, 2, 0).isValid)
        assertTrue(SleepSegment.of(2, 0, 2, 1).isValid)
    }

    @Test
    fun `out of range bounds are rejected`() {
        assertFalse(SleepSegment(-1, 60).isValid)
        assertFalse(SleepSegment(0, SleepSegment.MINUTES_PER_DAY).isValid)
    }

    @Test
    fun `same time on disjoint days does not overlap`() {
        val weekdays = SleepSegment.of(22, 0, 6, 0, Weekdays.WEEKDAYS)
        val weekend = SleepSegment.of(22, 0, 6, 0, Weekdays.WEEKEND)

        assertFalse(weekdays.overlaps(weekend))
        assertTrue(weekdays.overlaps(SleepSegment.of(22, 0, 6, 0)))
    }

    @Test
    fun `overlap sees the morning spill of the previous day`() {
        // Friday 23:00 - Saturday 07:00 collides with a Saturday-morning window.
        val fridayNight = SleepSegment.of(23, 0, 7, 0, setOf(5))
        val saturdayMorning = SleepSegment.of(6, 0, 8, 0, setOf(6))
        val sundayMorning = SleepSegment.of(6, 0, 8, 0, setOf(7))

        assertTrue(fridayNight.overlaps(saturdayMorning))
        assertFalse(fridayNight.overlaps(sundayMorning))
    }

    @Test
    fun `sunday night wraps into monday`() {
        val sundayNight = SleepSegment.of(23, 0, 7, 0, setOf(7))
        val mondayMorning = SleepSegment.of(6, 0, 8, 0, setOf(1))

        assertTrue(sundayNight.overlaps(mondayMorning))
        assertTrue(sundayNight.contains(Weekdays.MONDAY, 6 * 60))
        assertFalse(sundayNight.contains(2, 6 * 60))
    }

    @Test
    fun `empty or out-of-range day sets are rejected`() {
        assertFalse(SleepSegment.of(2, 0, 4, 0, emptySet()).isValid)
        assertFalse(SleepSegment.of(2, 0, 4, 0, setOf(0)).isValid)
        assertFalse(SleepSegment.of(2, 0, 4, 0, setOf(8)).isValid)
        assertTrue(SleepSegment.of(2, 0, 4, 0, setOf(1, 7)).isValid)
    }
}
