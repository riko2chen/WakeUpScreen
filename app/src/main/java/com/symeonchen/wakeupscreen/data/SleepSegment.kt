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
 *
 * [days] are the ISO weekdays (1 = Monday … 7 = Sunday) the window *starts* on.
 * A window over midnight belongs to the evening it began: 23:00-07:00 on Friday
 * still covers Saturday 06:00. The default is every day, which is both what the
 * pre-weekday format meant and what most windows will stay set to.
 */
data class SleepSegment(
    val startMinute: Int,
    val endMinute: Int,
    val days: Set<Int> = Weekdays.EVERY_DAY,
) {

    val crossesMidnight: Boolean get() = startMinute > endMinute

    val isValid: Boolean
        get() = startMinute in 0 until MINUTES_PER_DAY &&
            endMinute in 0 until MINUTES_PER_DAY &&
            startMinute != endMinute &&
            days.isNotEmpty() && days.all { it in Weekdays.MONDAY..Weekdays.SUNDAY }

    /** How long one occurrence of the window lasts, midnight wrap included. */
    val durationMinutes: Int
        get() = if (crossesMidnight) {
            MINUTES_PER_DAY - startMinute + endMinute
        } else {
            endMinute - startMinute
        }

    /**
     * Whether the given moment of the week falls inside the window. The day the
     * window began on is the one checked against [days], so the morning side of
     * an over-midnight window asks about yesterday.
     */
    fun contains(isoDay: Int, minuteOfDay: Int): Boolean = if (crossesMidnight) {
        (minuteOfDay >= startMinute && isoDay in days) ||
            (minuteOfDay < endMinute && Weekdays.previous(isoDay) in days)
    } else {
        minuteOfDay >= startMinute && minuteOfDay < endMinute && isoDay in days
    }

    /** Time-of-day check alone, for drawing the 24-hour ring where the weekday is not part of the picture. */
    fun containsMinute(minuteOfDay: Int): Boolean = if (crossesMidnight) {
        minuteOfDay >= startMinute || minuteOfDay < endMinute
    } else {
        minuteOfDay >= startMinute && minuteOfDay < endMinute
    }

    /**
     * Two windows overlap only when some actual moment of the week is inside
     * both — same times on disjoint days is not a conflict.
     */
    fun overlaps(other: SleepSegment): Boolean = weeklyRanges().any { a ->
        other.weeklyRanges().any { b -> a.first < b.second && b.first < a.second }
    }

    /**
     * The window's occurrences as non-wrapping `[from, until)` minute-of-week
     * pairs, Monday 00:00 being minute zero. An occurrence that runs past the
     * end of the week wraps to the start; empty halves are dropped the same way
     * a midnight-exact end drops its second half.
     */
    private fun weeklyRanges(): List<Pair<Int, Int>> = days.flatMap { day ->
        val start = (day - 1) * MINUTES_PER_DAY + startMinute
        val end = start + durationMinutes
        if (end <= MINUTES_PER_WEEK) {
            listOf(start to end)
        } else {
            listOf(start to MINUTES_PER_WEEK, 0 to end - MINUTES_PER_WEEK)
        }
    }.filter { it.second > it.first }

    companion object {
        const val MINUTES_PER_DAY = 24 * 60
        const val MINUTES_PER_WEEK = 7 * MINUTES_PER_DAY

        fun of(
            startHour: Int,
            startMinute: Int,
            endHour: Int,
            endMinute: Int,
            days: Set<Int> = Weekdays.EVERY_DAY,
        ): SleepSegment =
            SleepSegment(startHour * 60 + startMinute, endHour * 60 + endMinute, days)
    }
}
