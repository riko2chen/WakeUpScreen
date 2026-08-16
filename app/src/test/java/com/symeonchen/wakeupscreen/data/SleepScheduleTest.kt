package com.symeonchen.wakeupscreen.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepScheduleTest {

    private fun at(hour: Int, minute: Int = 0) = hour * 60 + minute

    @Test
    fun `serialize and parse round trip`() {
        val segments = listOf(
            SleepSegment.of(22, 30, 6, 15),
            SleepSegment.of(13, 0, 14, 0),
        )

        val restored = SleepSchedule.parse(SleepSchedule.serialize(segments))

        assertEquals(listOf(SleepSegment.of(13, 0, 14, 0), SleepSegment.of(22, 30, 6, 15)), restored)
    }

    @Test
    fun `parse sorts by start time`() {
        val parsed = SleepSchedule.parse("1320-360,780-840")

        assertEquals(listOf(SleepSegment.of(13, 0, 14, 0), SleepSegment.of(22, 0, 6, 0)), parsed)
    }

    @Test
    fun `parse tolerates junk and drops invalid windows`() {
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse(null))
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse(""))
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse("nonsense"))
        // 120-120 is empty, 5000-10 is out of range; only the valid one survives
        assertEquals(
            listOf(SleepSegment.of(2, 0, 4, 0)),
            SleepSchedule.parse("120-120,5000-10,120-240"),
        )
    }

    @Test
    fun `contains checks every window`() {
        val segments = listOf(SleepSegment.of(13, 0, 14, 0), SleepSegment.of(22, 0, 6, 0))

        assertTrue(SleepSchedule.contains(segments, Weekdays.MONDAY, at(13, 30)))
        assertTrue(SleepSchedule.contains(segments, Weekdays.MONDAY, at(23)))
        assertTrue(SleepSchedule.contains(segments, Weekdays.MONDAY, at(2)))
        assertFalse(SleepSchedule.contains(segments, Weekdays.MONDAY, at(14)))
        assertFalse(SleepSchedule.contains(segments, Weekdays.MONDAY, at(9)))
        assertFalse(SleepSchedule.contains(emptyList(), Weekdays.MONDAY, at(2)))
    }

    @Test
    fun `a window may be added when it touches but does not overlap`() {
        val segments = listOf(SleepSegment.of(2, 0, 4, 0))

        assertTrue(SleepSchedule.canAdd(segments, SleepSegment.of(4, 0, 6, 0)))
        assertTrue(SleepSchedule.canAdd(segments, SleepSegment.of(0, 0, 2, 0)))
        assertFalse(SleepSchedule.canAdd(segments, SleepSegment.of(3, 0, 5, 0)))
        assertFalse(SleepSchedule.canAdd(segments, SleepSegment.of(2, 0, 2, 0)))
    }

    @Test
    fun `conflicts name every window in the way`() {
        val segments = listOf(
            SleepSegment.of(2, 0, 4, 0),
            SleepSegment.of(5, 0, 6, 0),
            SleepSegment.of(13, 0, 14, 0),
        )

        val conflicts = SleepSchedule.conflictsWith(segments, SleepSegment.of(3, 0, 5, 30))

        assertEquals(listOf(SleepSegment.of(2, 0, 4, 0), SleepSegment.of(5, 0, 6, 0)), conflicts)
    }

    @Test
    fun `add keeps the list sorted and remove drops the exact window`() {
        val segments = listOf(SleepSegment.of(22, 0, 6, 0))

        val added = SleepSchedule.add(segments, SleepSegment.of(13, 0, 14, 0))
        assertEquals(listOf(SleepSegment.of(13, 0, 14, 0), SleepSegment.of(22, 0, 6, 0)), added)

        assertEquals(
            listOf(SleepSegment.of(22, 0, 6, 0)),
            SleepSchedule.remove(added, SleepSegment.of(13, 0, 14, 0)),
        )
    }

    @Test
    fun `legacy hours become one window with the same behaviour`() {
        assertEquals(SleepSegment.of(2, 0, 4, 0), SleepSchedule.fromLegacyHours(2, 4))
        assertEquals(SleepSegment.of(22, 0, 4, 0), SleepSchedule.fromLegacyHours(22, 4))
    }

    @Test
    fun `legacy all-day setting stays all but one minute`() {
        val migrated = SleepSchedule.fromLegacyHours(1, 1)

        assertTrue(migrated.isValid)
        assertTrue(migrated.containsMinute(at(1)))
        assertTrue(migrated.containsMinute(at(12)))
        assertTrue(migrated.containsMinute(at(0, 30)))
        assertEquals(SleepSegment.MINUTES_PER_DAY - 1, migrated.durationMinutes)
    }

    @Test
    fun `weekday suffix survives a round trip and legacy form stays suffix-free`() {
        val segments = listOf(
            SleepSegment.of(22, 0, 6, 0, setOf(5, 6)),
            SleepSegment.of(13, 0, 14, 0),
        )

        val stored = SleepSchedule.serialize(segments)

        assertEquals("780-840,1320-360@56", stored)
        assertEquals(
            listOf(SleepSegment.of(13, 0, 14, 0), SleepSegment.of(22, 0, 6, 0, setOf(5, 6))),
            SleepSchedule.parse(stored),
        )
    }

    @Test
    fun `parse without a day suffix means every day`() {
        val parsed = SleepSchedule.parse("1320-360")

        assertEquals(listOf(SleepSegment.of(22, 0, 6, 0, Weekdays.EVERY_DAY)), parsed)
    }

    @Test
    fun `parse drops windows with a broken day suffix`() {
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse("120-240@"))
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse("120-240@08"))
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse("120-240@x"))
        assertEquals(emptyList<SleepSegment>(), SleepSchedule.parse("120-240@1@2"))
    }

    @Test
    fun `a window only applies on its selected days`() {
        val weekendOnly = listOf(SleepSegment.of(9, 0, 11, 0, Weekdays.WEEKEND))

        assertTrue(SleepSchedule.contains(weekendOnly, 6, at(10)))
        assertTrue(SleepSchedule.contains(weekendOnly, 7, at(10)))
        assertFalse(SleepSchedule.contains(weekendOnly, Weekdays.MONDAY, at(10)))
    }

    @Test
    fun `an over-midnight window spills into the next morning`() {
        // Friday night into Saturday morning, Friday selected only.
        val fridayNight = listOf(SleepSegment.of(23, 0, 7, 0, setOf(5)))

        assertTrue(SleepSchedule.contains(fridayNight, 5, at(23, 30)))
        assertTrue(SleepSchedule.contains(fridayNight, 6, at(6)))
        assertFalse(SleepSchedule.contains(fridayNight, 6, at(23, 30)))
        assertFalse(SleepSchedule.contains(fridayNight, 7, at(6)))
        assertFalse(SleepSchedule.contains(fridayNight, 5, at(7)))
    }
}
