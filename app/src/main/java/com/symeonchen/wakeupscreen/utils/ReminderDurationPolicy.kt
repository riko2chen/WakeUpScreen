package com.symeonchen.wakeupscreen.utils

/** Reminder-only policy. Inherited wakes deliberately bypass all new capability checks. */
object ReminderDurationPolicy {
    const val INHERIT = 0L
    const val MIN_SECONDS = 5L
    const val MAX_SECONDS = 30L

    sealed interface Wake {
        data object Inherit : Wake
        data object SystemTimeout : Wake
        data class Precise(val seconds: Long) : Wake
    }

    /** Invalid negative backups return to the default; positive durations stay in the safe range. */
    fun normalize(seconds: Long): Long =
        if (seconds <= INHERIT) INHERIT else seconds.coerceIn(MIN_SECONDS, MAX_SECONDS)

    fun resolve(seconds: Long, accessibilityRequired: Boolean, accessibilityAvailable: Boolean): Wake {
        val duration = normalize(seconds)
        return when {
            duration == INHERIT -> Wake.Inherit
            accessibilityRequired && !accessibilityAvailable -> Wake.SystemTimeout
            else -> Wake.Precise(duration)
        }
    }

    fun needsPermissionWarning(seconds: Long, accessibilityRequired: Boolean, granted: Boolean): Boolean =
        normalize(seconds) != INHERIT && accessibilityRequired && !granted
}
