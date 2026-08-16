package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.SleepSegment
import java.util.Locale

/**
 * Sleep windows are shown on a 24-hour clock in every locale, matching the rest
 * of the app's time displays and keeping a window that runs over midnight
 * readable without an am/pm suffix on each end.
 */
object TimeOfDayFormatter {

    fun formatMinuteOfDay(minuteOfDay: Int): String = String.format(
        Locale.US,
        "%02d:%02d",
        minuteOfDay / 60,
        minuteOfDay % 60,
    )

    fun formatSegment(segment: SleepSegment): String =
        "${formatMinuteOfDay(segment.startMinute)} - ${formatMinuteOfDay(segment.endMinute)}"
}
