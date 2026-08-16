package com.symeonchen.wakeupscreen.data

/**
 * The set of sleep windows as a whole: parsing what is on disk, deciding
 * whether a new window may be added, and answering whether a given moment is
 * inside any window.
 */
object SleepSchedule {

    private const val SEGMENT_SEPARATOR = ","
    private const val BOUND_SEPARATOR = "-"
    private const val DAYS_SEPARATOR = "@"

    /**
     * Reads the stored form, dropping anything unparseable rather than throwing.
     * The value has to survive downgrades and hand edits, and a schedule that
     * silently loses one broken window still beats a listener service that
     * crashes on every notification.
     *
     * A window is `start-end`, optionally followed by `@` and the ISO weekday
     * digits it starts on (`1320-360@67`). No `@` part means every day, which
     * is what the pre-weekday format meant for every window it stored.
     */
    fun parse(stored: String?): List<SleepSegment> {
        if (stored.isNullOrBlank()) {
            return emptyList()
        }
        return stored.split(SEGMENT_SEPARATOR)
            .mapNotNull { part ->
                val timeAndDays = part.trim().split(DAYS_SEPARATOR)
                if (timeAndDays.size > 2) return@mapNotNull null
                val bounds = timeAndDays[0].trim().split(BOUND_SEPARATOR)
                if (bounds.size != 2) return@mapNotNull null
                val start = bounds[0].trim().toIntOrNull() ?: return@mapNotNull null
                val end = bounds[1].trim().toIntOrNull() ?: return@mapNotNull null
                val days = if (timeAndDays.size == 2) {
                    parseDays(timeAndDays[1].trim()) ?: return@mapNotNull null
                } else {
                    Weekdays.EVERY_DAY
                }
                SleepSegment(start, end, days).takeIf { it.isValid }
            }
            .sortedBy { it.startMinute }
    }

    private fun parseDays(digits: String): Set<Int>? {
        if (digits.isEmpty()) return null
        val days = digits.map { char ->
            char.digitToIntOrNull()?.takeIf { it in Weekdays.MONDAY..Weekdays.SUNDAY }
                ?: return null
        }
        return days.toSet()
    }

    /**
     * An every-day window is written in the old, suffix-free form, so a
     * schedule that never uses weekdays stays readable to a 3.2.0 install.
     */
    fun serialize(segments: List<SleepSegment>): String =
        segments.sortedBy { it.startMinute }
            .joinToString(SEGMENT_SEPARATOR) { segment ->
                val time = "${segment.startMinute}$BOUND_SEPARATOR${segment.endMinute}"
                if (segment.days == Weekdays.EVERY_DAY) {
                    time
                } else {
                    time + DAYS_SEPARATOR + segment.days.sorted().joinToString("")
                }
            }

    fun contains(segments: List<SleepSegment>, isoDay: Int, minuteOfDay: Int): Boolean =
        segments.any { it.contains(isoDay, minuteOfDay) }

    /** The windows [candidate] would collide with, empty when it is free to add. */
    fun conflictsWith(segments: List<SleepSegment>, candidate: SleepSegment): List<SleepSegment> =
        segments.filter { it.overlaps(candidate) }

    fun canAdd(segments: List<SleepSegment>, candidate: SleepSegment): Boolean =
        candidate.isValid && conflictsWith(segments, candidate).isEmpty()

    fun add(segments: List<SleepSegment>, candidate: SleepSegment): List<SleepSegment> =
        (segments + candidate).sortedBy { it.startMinute }

    fun remove(segments: List<SleepSegment>, target: SleepSegment): List<SleepSegment> =
        segments.filterNot { it == target }

    /**
     * The single window the pre-3.2.0 hour-only setting described, used once to
     * carry an existing install over. The old range was read as inclusive on
     * both ends, but an end hour meant "wake at the top of that hour", so
     * `[2, 4]` becomes `[2:00, 4:00)` and keeps behaving the same.
     *
     * The old setting also treated begin == end as the whole day. There is no
     * way to say that in one half-open window, so it becomes a window covering
     * all but the final minute.
     */
    fun fromLegacyHours(beginHour: Int, endHour: Int): SleepSegment {
        if (beginHour == endHour) {
            return SleepSegment(beginHour * 60, (beginHour * 60 + SleepSegment.MINUTES_PER_DAY - 1) % SleepSegment.MINUTES_PER_DAY)
        }
        return SleepSegment(beginHour * 60, endHour * 60)
    }
}
