package com.symeonchen.wakeupscreen.data

/** Shared bounds for input, persisted settings and settings restored from backup. */
object ReminderIntervalPolicy {
    const val MIN_MINUTES = 5
    const val MAX_MINUTES = 1440

    fun isValid(minutes: Int): Boolean = minutes in MIN_MINUTES..MAX_MINUTES

    fun normalize(minutes: Int): Int =
        minutes.takeIf(::isValid) ?: ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES

    fun parse(input: String): Int? {
        val digits = input.trim()
        if (digits.isEmpty() || digits.any { it !in '0'..'9' }) return null
        return digits.toIntOrNull()?.takeIf(::isValid)
    }
}
