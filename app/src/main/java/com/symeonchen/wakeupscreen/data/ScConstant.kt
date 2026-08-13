package com.symeonchen.wakeupscreen.data

/**
 * Created by SymeonChen on 2019-10-27.
 */
object ScConstant {
    const val CUSTOM_STATUS = "custom_status"
    const val WAKE_SCREEN_SECOND = "wake_screen_second"
    const val PROXIMITY_STATUS = "proximity_status"
    const val PROXIMITY_SWITCH = "proximity_switch"
    const val BATTERY_SAVER_FAKE_SWITCH = "battery_saver_fake_switch"
    const val SEND_NOTIFICATION_PERMISSION = "send_notification_permission"
    const val DEBUG_MODE_SWITCH = "debug_mode_switch"
    const val APP_NOTIFY_MODE = "app_white_list_switch"
    const val APP_FILTER_WHITE_LIST_STRING = "white_list_app"
    const val APP_FILTER_BLACK_LIST_STRING = "black_list_app"
    const val APP_FILTER_LIST_FLAG = "white_list_flag"
    const val ONGOING_STATUS_DETECT = "ongoing_status_detect"
    const val RADICAL_ONGOING_DETECT = "radical_ongoing_detect"
    const val LANGUAGE_SELECTED = "language_selected"
    const val DARK_MODE_SELECTED = "dark_mode_selected"
    const val SLEEP_MODE_BOOLEAN = "sleep_mode_boolean"
    const val SLEEP_MODE_TIME_BEGIN = "sleep_mode_time_begin"
    const val SLEEP_MODE_TIME_END = "sleep_mode_time_end"
    const val SLEEP_MODE_SEGMENTS = "sleep_mode_segments"
    const val DND_DETECT_SWITCH = "dnd_detect_switch"
    const val LAST_IN_APP_REVIEW_TIMESTAMP = "last_in_app_review_timestamp"
    const val CHARGING_ONLY_SWITCH = "charging_only_switch"
    const val REPEAT_REMINDER_SWITCH = "repeat_reminder_switch"
    const val REPEAT_REMINDER_INTERVAL_MINUTES = "repeat_reminder_interval_minutes"
    const val REPEAT_REMINDER_MAX_ROUNDS = "repeat_reminder_max_rounds"
    const val REPEAT_REMINDER_ROUND_COUNT = "repeat_reminder_round_count"
    const val PRECISE_SCREEN_ON_SWITCH = "precise_screen_on_switch"
    const val IGNORE_SILENT_NOTIFICATION_SWITCH = "ignore_silent_notification_switch"

    const val DEFAULT_SWITCH_OF_APP: Boolean = true
    const val DEFAULT_SWITCH_OF_PROXIMITY: Boolean = true
    const val DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS: Long = 2000
    const val DEFAULT_VALUE_OF_PROXIMITY: Int = 1
    const val DEFAULT_BATTERY_SAVER: Boolean = false
    const val DEFAULT_PERMISSION_OF_SEND_NOTIFICATION: Boolean = false
    const val DEFAULT_SWITCH_OF_DEBUG_MODE: Boolean = false
    const val DEFAULT_APP_NOTIFY_MODE: Int = 0
    const val DEFAULT_APP_WHITE_LIST_STRING: String = ""
    const val DEFAULT_APP_BLACK_LIST_STRING: String = ""
    const val DEFAULT_APP_WHITE_LIST_FLAG = 0L
    const val DEFAULT_ONGOING_STATUS_DETECT = true
    const val DEFAULT_RADICAL_ONGOING_DETECT = true
    const val DEFAULT_LANGUAGE_SELECTED: Int = 0
    const val DEFAULT_DARK_MODE_SELECTED: Int = 0
    const val DEFAULT_SLEEP_MODE_BOOLEAN: Boolean = false
    const val DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR = 2
    const val DEFAULT_SLEEP_MODE_TIME_END_HOUR = 4

    /**
     * Absent means "never written since the upgrade", which is what triggers the
     * one-time migration from the hour-only keys above. An empty string is a
     * real value: every window was deleted on purpose.
     */
    val DEFAULT_SLEEP_MODE_SEGMENTS: String? = null

    /** Windows a user may configure. A guard against a runaway list, not a UX limit. */
    const val MAX_SLEEP_SEGMENTS = 12
    const val DEFAULT_DND_DETECT_SWITCH = true
    const val DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP = "0"
    const val DEFAULT_CHARGING_ONLY_SWITCH = false
    const val DEFAULT_REPEAT_REMINDER_SWITCH = false
    const val DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES = 15
    const val DEFAULT_REPEAT_REMINDER_MAX_ROUNDS = 5
    const val DEFAULT_REPEAT_REMINDER_ROUND_COUNT = 0

    /**
     * Off by default on purpose. With the switch off the app keeps doing what
     * it has always done — wake the display and let the system time it out —
     * so an update can never change the behaviour of an existing install.
     */
    const val DEFAULT_PRECISE_SCREEN_ON_SWITCH: Boolean = false

    /**
     * Off by default, for the same reason as above: an update must not start
     * suppressing notifications that used to wake the screen.
     */
    const val DEFAULT_IGNORE_SILENT_NOTIFICATION_SWITCH: Boolean = false

    /**
     * Bounds the engine will honour, in seconds.
     *
     * Nothing in the UI can produce a value outside [PRECISE_SCREEN_ON_PRESET_SECONDS]
     * any more; these exist because the duration is stored under the key the old
     * free-text setting used, where nothing ever validated it, and a leftover
     * value must not be able to ask for a twenty-minute wake lock.
     */
    const val MIN_PRECISE_SCREEN_ON_SECOND: Long = 1
    const val MAX_PRECISE_SCREEN_ON_SECOND: Long = 300

    /**
     * The durations offered on the settings screen, in seconds. A fixed list
     * rather than free text: every value a user would realistically pick is
     * here, and the field only ever invited numbers the engine clamps away.
     */
    val PRECISE_SCREEN_ON_PRESET_SECONDS = listOf(3L, 5L, 10L, 15L, 30L)

    /**
     * Longest gap between two wake-lock refreshes while a precise window is
     * running. The deadline itself is absolute, so this only bounds how stale
     * the wake lock may get — not the accuracy of the window.
     */
    const val PRECISE_SCREEN_ON_TICK_INTERVAL_MS: Long = 2000

    /**
     * Ignore screen-off requests that arrive within this window of the previous
     * one, so a burst of notifications cannot fight over the display.
     */
    const val SCREEN_OFF_REQUEST_COOLDOWN_MS: Long = 2000

    /**
     * How long [com.symeonchen.wakeupscreen.pages.ScreenOffActivity] may stay up
     * before it closes itself. It is a bail-out, not the configured duration:
     * if the system has not blanked the display by then, something stopped it
     * and sitting on a black full-screen activity would only trap the user.
     */
    const val SCREEN_OFF_ACTIVITY_SAFETY_TIMEOUT_MS: Long = 10000

    /**
     * Selectable intervals for the repeat reminder, in minutes.
     *
     * Capped at one hour on purpose: a reminder that only fires once every few
     * hours is indistinguishable from no reminder at all. The lower bound is
     * kept at 5 minutes even though Doze usually stretches anything under ~15
     * minutes, because the delay is explained in the UI rather than hidden by
     * removing the option.
     */
    val REPEAT_REMINDER_INTERVAL_OPTIONS = listOf(5, 10, 15, 20, 30, 45, 60)

    /** Selectable reminder-count caps. [REPEAT_REMINDER_ROUNDS_UNLIMITED] means "never stop". */
    val REPEAT_REMINDER_MAX_ROUNDS_OPTIONS = listOf(1, 3, 5, 10, 0)
    const val REPEAT_REMINDER_ROUNDS_UNLIMITED = 0


    const val AUTHOR_MAIL = "symeonchen@gmail.com"
    const val DEFAULT_MAIL_HEAD = "[Question] [Wake Up Screen] write title here"
    const val DEFAULT_MAIL_BODY = ""

}
