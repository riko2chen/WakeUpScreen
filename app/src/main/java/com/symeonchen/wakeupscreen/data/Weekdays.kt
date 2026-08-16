package com.symeonchen.wakeupscreen.data

import java.util.Calendar

/**
 * Weekdays as ISO-8601 numbers, 1 = Monday through 7 = Sunday. One vocabulary
 * for storage, the sleep-window arithmetic and the UI, so a day never changes
 * meaning between a file written on one device and read on another.
 */
object Weekdays {

    const val MONDAY = 1
    const val SUNDAY = 7

    val EVERY_DAY: Set<Int> = (MONDAY..SUNDAY).toSet()
    val WEEKDAYS: Set<Int> = (1..5).toSet()
    val WEEKEND: Set<Int> = setOf(6, 7)

    fun previous(isoDay: Int): Int = if (isoDay == MONDAY) SUNDAY else isoDay - 1

    /** [Calendar.DAY_OF_WEEK] counts Sunday = 1; ISO counts Monday = 1. */
    fun fromCalendar(calendarDayOfWeek: Int): Int =
        if (calendarDayOfWeek == Calendar.SUNDAY) SUNDAY else calendarDayOfWeek - 1

    fun toCalendar(isoDay: Int): Int =
        if (isoDay == SUNDAY) Calendar.SUNDAY else isoDay + 1
}
