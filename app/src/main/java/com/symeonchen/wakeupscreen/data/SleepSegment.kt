package com.symeonchen.wakeupscreen.data

/**
 * One sleep window, stored as minutes since midnight and read as `[start, end)`:
 * the start minute is inside the window, the end minute is already outside it.
 * That is what makes two windows able to touch — `[2:00, 4:00)` and
 * `[4:00, 6:00)` are adjacent, not overlapping.
 *
 * [startMinute] > [endMinute] means the window runs over midnight, which is the
 * normal case for sleep. `[22:00, 0:00)` is written as start 1320, end 0 and
 * covers the rest of the day; it is not an empty window.
 *
 * start == end has no useful reading (empty window, or the whole day?) and is
 * rejected by [isValid] rather than guessed at.
 */
data class SleepSegment(val startMinute: Int, val endMinute: Int) {

    val crossesMidnight: Boolean get() = startMinute > endMinute

    val isValid: Boolean
        get() = startMinute in 0 until MINUTES_PER_DAY &&
            endMinute in 0 until MINUTES_PER_DAY &&
            startMinute != endMinute

    /** How long the window lasts, midnight wrap included. */
    val durationMinutes: Int
        get() = if (crossesMidnight) {
            MINUTES_PER_DAY - startMinute + endMinute
        } else {
            endMinute - startMinute
        }

    fun contains(minuteOfDay: Int): Boolean = if (crossesMidnight) {
        minuteOfDay >= startMinute || minuteOfDay < endMinute
    } else {
        minuteOfDay >= startMinute && minuteOfDay < endMinute
    }

    fun overlaps(other: SleepSegment): Boolean = absoluteRanges().any { a ->
        other.absoluteRanges().any { b -> a.first < b.second && b.first < a.second }
    }

    /**
     * The window as one or two non-wrapping `[from, until)` pairs, so overlap is
     * a plain comparison. A window ending exactly at midnight yields only its
     * first half, because the second would be the empty `[0, 0)`.
     */
    private fun absoluteRanges(): List<Pair<Int, Int>> = if (crossesMidnight) {
        listOf(startMinute to MINUTES_PER_DAY, 0 to endMinute).filter { it.second > it.first }
    } else {
        listOf(startMinute to endMinute)
    }

    companion object {
        const val MINUTES_PER_DAY = 24 * 60

        fun of(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): SleepSegment =
            SleepSegment(startHour * 60 + startMinute, endHour * 60 + endMinute)
    }
}
