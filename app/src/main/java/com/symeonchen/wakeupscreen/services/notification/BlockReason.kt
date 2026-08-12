package com.symeonchen.wakeupscreen.services.notification


/**
 * Stable identifiers for the reason a notification was blocked.
 *
 * These MUST be hard-coded string constants rather than derived from class
 * names (e.g. `::class.java.simpleName`), because R8 obfuscates class names in
 * release builds and the obfuscated name would never match the literals used
 * when rendering the notification log.
 */
object BlockReason {
    const val APP_SWITCH_OFF = "app_switch_off"
    const val POCKET_MODE = "pocket_mode"
    const val INTERACTIVE = "interactive"
    const val FILTER_LIST = "filter_list"
    const val LOW_IMPORTANCE = "low_importance"
    const val ONGOING = "ongoing"
    const val SLEEP_MODE = "sleep_mode"
    const val DND = "dnd"
    const val CHARGING = "charging"

    /**
     * Reasons a repeat-reminder streak ended. Same vocabulary and the same
     * obfuscation constraint as above, reported on
     * [com.symeonchen.wakeupscreen.data.LogStatus.REMINDER_STOPPED] entries.
     */
    const val REMINDER_ALL_READ = "reminder_all_read"
    const val REMINDER_MAX_ROUNDS = "reminder_max_rounds"
    const val REMINDER_SWITCH_OFF = "reminder_switch_off"
    const val REMINDER_SERVICE_UNAVAILABLE = "reminder_service_unavailable"
}
