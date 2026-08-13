package com.symeonchen.wakeupscreen.data

/**
 * The set of sleep windows as a whole: parsing what is on disk, deciding
 * whether a new window may be added, and answering whether a given moment is
 * inside any window.
 */
object SleepSchedule {

    private const val SEGMENT_SEPARATOR = ","
    private const val BOUND_SEPARATOR = "-"

    /**
     * Reads the stored form, dropping anything unparseable rather than throwing.
     * The value has to survive downgrades and hand edits, and a schedule that
     * silently loses one broken window still beats a listener service that
     * crashes on every notification.
     */
    fun parse(stored: String?): List<SleepSegment> {
        if (stored.isNullOrBlank()) {
            return emptyList()
        }
        return stored.split(SEGMENT_SEPARATOR)
            .mapNotNull { part ->
                val bounds = part.trim().split(BOUND_SEPARATOR)
                if (bounds.size != 2) return@mapNotNull null
                val start = bounds[0].trim().toIntOrNull() ?: return@mapNotNull null
                val end = bounds[1].trim().toIntOrNull() ?: return@mapNotNull null
                SleepSegment(start, end).takeIf { it.isValid }
            }
            .sortedBy { it.startMinute }
    }

    fun serialize(segments: List<SleepSegment>): String =
        segments.sortedBy { it.startMinute }
            .joinToString(SEGMENT_SEPARATOR) { "${it.startMinute}$BOUND_SEPARATOR${it.endMinute}" }

    fun contains(segments: List<SleepSegment>, minuteOfDay: Int): Boolean =
        segments.any { it.contains(minuteOfDay) }

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
