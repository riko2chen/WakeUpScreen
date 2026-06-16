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
    const val ONGOING = "ongoing"
    const val SLEEP_MODE = "sleep_mode"
    const val DND = "dnd"
    const val CHARGING = "charging"
}
