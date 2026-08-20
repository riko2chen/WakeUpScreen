package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.DarkModeInfo
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.SleepSchedule
import com.symeonchen.wakeupscreen.data.SleepSegment
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_BLACK_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.BATTERY_LEVEL_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.BATTERY_LEVEL_THRESHOLD
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_BATTERY_LEVEL_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_BATTERY_LEVEL_THRESHOLD
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_LIST_FLAG
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_WHITE_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.APP_NOTIFY_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.BATTERY_SAVER_FAKE_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.CHARGING_ONLY_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.CUSTOM_STATUS
import com.symeonchen.wakeupscreen.data.ScConstant.DARK_MODE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.DEBUG_MODE_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_BLACK_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_NOTIFY_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_WHITE_LIST_FLAG
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_WHITE_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_BATTERY_SAVER
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_CHARGING_ONLY_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_DND_DETECT_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_IGNORE_SILENT_NOTIFICATION_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_DARK_MODE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_LANGUAGE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_ONGOING_STATUS_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_PERMISSION_OF_SEND_NOTIFICATION
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_PRECISE_SCREEN_ON_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_RADICAL_ONGOING_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_REPEAT_REMINDER_MAX_ROUNDS
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_REPEAT_REMINDER_ROUND_COUNT
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_REPEAT_REMINDER_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SLEEP_MODE_BOOLEAN
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SLEEP_MODE_TIME_END_HOUR
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_APP
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_DEBUG_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_VALUE_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_STATUS_OF_FACE_DOWN
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_FACE_DOWN
import com.symeonchen.wakeupscreen.data.ScConstant.DND_DETECT_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.FACE_DOWN_STATUS
import com.symeonchen.wakeupscreen.data.ScConstant.FACE_DOWN_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_NIGHT_GLOW_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.NIGHT_GLOW_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.IGNORE_SILENT_NOTIFICATION_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.LANGUAGE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.LAST_IN_APP_REVIEW_TIMESTAMP
import com.symeonchen.wakeupscreen.data.ScConstant.ONGOING_STATUS_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.PRECISE_SCREEN_ON_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.PROXIMITY_STATUS
import com.symeonchen.wakeupscreen.data.ScConstant.PROXIMITY_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.RADICAL_ONGOING_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.REPEAT_REMINDER_INTERVAL_MINUTES
import com.symeonchen.wakeupscreen.data.ScConstant.REPEAT_REMINDER_MAX_ROUNDS
import com.symeonchen.wakeupscreen.data.ScConstant.REPEAT_REMINDER_ROUND_COUNT
import com.symeonchen.wakeupscreen.data.ScConstant.REPEAT_REMINDER_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.SEND_NOTIFICATION_PERMISSION
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SLEEP_MODE_SEGMENTS
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_BOOLEAN
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_SEGMENTS
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_TIME_BEGIN
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_TIME_END
import com.symeonchen.wakeupscreen.data.ScConstant.WAKE_SCREEN_SECOND
import com.symeonchen.wakeupscreen.data.ScConstant.LAST_SEEN_VERSION_CODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_LAST_SEEN_VERSION_CODE
import com.symeonchen.wakeupscreen.data.ScConstant.UPDATED_FROM_VERSION_CODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_UPDATED_FROM_VERSION_CODE
import com.symeonchen.wakeupscreen.data.ScConstant.CLICKED_FEATURE_BADGES
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_CLICKED_FEATURE_BADGES
import com.symeonchen.wakeupscreen.data.ScStore

/**
 * Created by SymeonChen on 2019-10-27.
 */
object DataInjection {

    var switchOfApp: Boolean
        get() {
            return ScStore.getBoolean(CUSTOM_STATUS, DEFAULT_SWITCH_OF_APP)
        }
        set(value) {
            ScStore.putBoolean(CUSTOM_STATUS, value)
        }

    /**
     * Reading it the first time after an upgrade brings a value left by an
     * older build — the free-text setting allowed anything, and 4.0.0 raised
     * the floor from 3 to 5 seconds — into the supported range and writes the
     * result, so the raise happens once instead
     * of being re-applied by every reader. Without the write-back the settings
     * page compares a legacy 3s against the presets and highlights none of
     * them, while everything else already reports the clamped 5s.
     */
    var milliSecondOfWakeUpScreen: Long
        get() {
            val stored = ScStore.getLong(
                WAKE_SCREEN_SECOND,
                DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
            )
            val clamped = ScreenOnWindowCalculator.clampSeconds(stored / 1000L) * 1000L
            if (clamped != stored) {
                milliSecondOfWakeUpScreen = clamped
            }
            return clamped
        }
        set(millisSec) {
            if (millisSec < 0) {
                return
            }
            ScStore.putLong(WAKE_SCREEN_SECOND, millisSec)
        }

    var statueOfProximity: Int
        get() {
            return ScStore.getInt(PROXIMITY_STATUS, DEFAULT_VALUE_OF_PROXIMITY)
        }
        set(state) {
            ScStore.putInt(PROXIMITY_STATUS, state)
        }


    var switchOfProximity: Boolean
        get() {
            return ScStore.getBoolean(PROXIMITY_SWITCH, DEFAULT_SWITCH_OF_PROXIMITY)
        }
        set(switch) {
            ScStore.putBoolean(PROXIMITY_SWITCH, switch)
        }

    var switchOfFaceDown: Boolean
        get() {
            return ScStore.getBoolean(FACE_DOWN_SWITCH, DEFAULT_SWITCH_OF_FACE_DOWN)
        }
        set(value) {
            ScStore.putBoolean(FACE_DOWN_SWITCH, value)
        }

    /** Last posture the accelerometer reported; written by the sensor listener alone. */
    var statusOfFaceDown: Boolean
        get() {
            return ScStore.getBoolean(FACE_DOWN_STATUS, DEFAULT_STATUS_OF_FACE_DOWN)
        }
        set(value) {
            ScStore.putBoolean(FACE_DOWN_STATUS, value)
        }

    var fakeSwitchOfBatterySaver: Boolean
        get() {
            return ScStore.getBoolean(BATTERY_SAVER_FAKE_SWITCH, DEFAULT_BATTERY_SAVER)
        }
        set(value) {
            ScStore.putBoolean(BATTERY_SAVER_FAKE_SWITCH, value)
        }

    var permissionOfSendNotification: Boolean
        get() {
            return ScStore.getBoolean(SEND_NOTIFICATION_PERMISSION, DEFAULT_PERMISSION_OF_SEND_NOTIFICATION)
        }
        set(value) {
            ScStore.putBoolean(SEND_NOTIFICATION_PERMISSION, value)
        }

    var switchOfDebugMode: Boolean
        get() {
            return ScStore.getBoolean(DEBUG_MODE_SWITCH, DEFAULT_SWITCH_OF_DEBUG_MODE)
        }
        set(value) {
            ScStore.putBoolean(DEBUG_MODE_SWITCH, value)
        }

    var modeOfCurrent: CurrentMode
        get() {
            return CurrentMode.getModeFromValue(
                ScStore.getInt(
                    APP_NOTIFY_MODE,
                    DEFAULT_APP_NOTIFY_MODE
                )
            )

        }
        set(value) {
            ScStore.putInt(APP_NOTIFY_MODE, value.ordinal)
        }

    var appWhiteListStringOfNotify: String
        get() {
            return ScStore.getString(
                APP_FILTER_WHITE_LIST_STRING,
                DEFAULT_APP_WHITE_LIST_STRING
            ) ?: ""
        }
        set(value) {
            ScStore.putString(APP_FILTER_WHITE_LIST_STRING, value)
        }

    var appBlackListStringOfNotify: String
        get() {
            return ScStore.getString(
                APP_FILTER_BLACK_LIST_STRING,
                DEFAULT_APP_BLACK_LIST_STRING
            ) ?: ""
        }
        set(value) {
            ScStore.putString(APP_FILTER_BLACK_LIST_STRING, value)
        }

    var appListUpdateFlag: Long
        get() {
            return ScStore.getLong(APP_FILTER_LIST_FLAG, DEFAULT_APP_WHITE_LIST_FLAG)
        }
        set(value) {
            ScStore.putLong(APP_FILTER_LIST_FLAG, value)
        }

    var ongoingOptimize: Boolean
        get() {
            return ScStore.getBoolean(ONGOING_STATUS_DETECT, DEFAULT_ONGOING_STATUS_DETECT)
        }
        set(value) {
            ScStore.putBoolean(ONGOING_STATUS_DETECT, value)
        }

    var radicalOngoingOptimize: Boolean
        get() {
            return ScStore.getBoolean(RADICAL_ONGOING_DETECT, DEFAULT_RADICAL_ONGOING_DETECT)
        }
        set(value) {
            ScStore.putBoolean(RADICAL_ONGOING_DETECT, value)
        }

    var languageSelected: LanguageInfo
        get() {
            return LanguageInfo.getModeFromValue(
                ScStore.getInt(
                    LANGUAGE_SELECTED,
                    DEFAULT_LANGUAGE_SELECTED
                )
            )
        }
        set(value) {
            ScStore.putInt(LANGUAGE_SELECTED, value.ordinal)
        }

    var darkModeSelected: DarkModeInfo
        get() {
            return DarkModeInfo.getModeFromValue(
                ScStore.getInt(
                    DARK_MODE_SELECTED,
                    DEFAULT_DARK_MODE_SELECTED
                )
            )
        }
        set(value) {
            ScStore.putInt(DARK_MODE_SELECTED, value.referenceNum)
        }

    var sleepModeBoolean: Boolean
        get() {
            return ScStore.getBoolean(SLEEP_MODE_BOOLEAN, DEFAULT_SLEEP_MODE_BOOLEAN)
        }
        set(value) {
            ScStore.putBoolean(SLEEP_MODE_BOOLEAN, value)
        }

    /**
     * Every sleep window, as `[start, end)` minute pairs.
     *
     * Reading it the first time after an upgrade migrates the old hour-only
     * pair into a single window and writes the result, so the migration runs
     * once and the legacy keys are never consulted again. Clearing every window
     * writes an empty string, which is a value like any other: the getter must
     * not mistake it for "not migrated yet" and resurrect the old range.
     */
    var sleepModeSegments: List<SleepSegment>
        get() {
            val stored = ScStore.getString(SLEEP_MODE_SEGMENTS, DEFAULT_SLEEP_MODE_SEGMENTS)
            if (stored == null) {
                val migrated = listOf(
                    SleepSchedule.fromLegacyHours(sleepModeTimeBeginHour, sleepModeTimeEndHour)
                )
                sleepModeSegments = migrated
                return migrated
            }
            return SleepSchedule.parse(stored)
        }
        set(value) {
            ScStore.putString(SLEEP_MODE_SEGMENTS, SleepSchedule.serialize(value))
        }

    /**
     * Pre-3.2.0 sleep window, kept only so [sleepModeSegments] can migrate an
     * existing install once. Nothing reads it to decide anything any more.
     */
    var sleepModeTimeBeginHour: Int
        get() {
            return ScStore.getInt(
                SLEEP_MODE_TIME_BEGIN,
                DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
            )
        }
        set(value) {
            ScStore.putInt(SLEEP_MODE_TIME_BEGIN, value)
        }

    var sleepModeTimeEndHour: Int
        get() {
            return ScStore.getInt(
                SLEEP_MODE_TIME_END,
                DEFAULT_SLEEP_MODE_TIME_END_HOUR
            )
        }
        set(value) {
            ScStore.putInt(SLEEP_MODE_TIME_END, value)
        }

    /**
     * While on, a notification stopped by the sleep gate still shows the dim
     * red night glow instead of leaving the display dark.
     */
    var nightGlowSwitch: Boolean
        get() {
            return ScStore.getBoolean(NIGHT_GLOW_SWITCH, DEFAULT_NIGHT_GLOW_SWITCH)
        }
        set(value) {
            ScStore.putBoolean(NIGHT_GLOW_SWITCH, value)
        }

    var dndDetectSwitch: Boolean
        get() {
            return ScStore.getBoolean(DND_DETECT_SWITCH, DEFAULT_DND_DETECT_SWITCH)
        }
        set(value) {
            ScStore.putBoolean(DND_DETECT_SWITCH, value)
        }

    var lastInAppReviewTime: String
        get() {
            return ScStore.getString(
                LAST_IN_APP_REVIEW_TIMESTAMP,
                DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP
            ) ?: DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP
        }
        set(value) {
            ScStore.putString(LAST_IN_APP_REVIEW_TIMESTAMP, value)
        }

    var chargingOnlySwitch: Boolean
        get() {
            return ScStore.getBoolean(
                CHARGING_ONLY_SWITCH,
                DEFAULT_CHARGING_ONLY_SWITCH
            )
        }
        set(value) {
            ScStore.putBoolean(CHARGING_ONLY_SWITCH, value)
        }

    var repeatReminderSwitch: Boolean
        get() {
            return ScStore.getBoolean(
                REPEAT_REMINDER_SWITCH,
                DEFAULT_REPEAT_REMINDER_SWITCH
            )
        }
        set(value) {
            ScStore.putBoolean(REPEAT_REMINDER_SWITCH, value)
        }

    var repeatReminderIntervalMinutes: Int
        get() {
            return ScStore.getInt(
                REPEAT_REMINDER_INTERVAL_MINUTES,
                DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
            )
        }
        set(value) {
            if (value <= 0) {
                return
            }
            ScStore.putInt(REPEAT_REMINDER_INTERVAL_MINUTES, value)
        }

    /** Number of reminders allowed per unread streak. 0 means unlimited. */
    var repeatReminderMaxRounds: Int
        get() {
            return ScStore.getInt(
                REPEAT_REMINDER_MAX_ROUNDS,
                DEFAULT_REPEAT_REMINDER_MAX_ROUNDS
            )
        }
        set(value) {
            if (value < 0) {
                return
            }
            ScStore.putInt(REPEAT_REMINDER_MAX_ROUNDS, value)
        }

    /**
     * How many reminders the current unread streak has already fired. Runtime
     * state rather than a user setting; reset whenever the streak ends or a new
     * notification arrives.
     */
    var repeatReminderRoundCount: Int
        get() {
            return ScStore.getInt(
                REPEAT_REMINDER_ROUND_COUNT,
                DEFAULT_REPEAT_REMINDER_ROUND_COUNT
            )
        }
        set(value) {
            ScStore.putInt(REPEAT_REMINDER_ROUND_COUNT, value)
        }

    /**
     * Master switch for the precise screen-on window. While it is off,
     * [milliSecondOfWakeUpScreen] is not consulted at all and the display is
     * woken exactly the way it always was.
     */
    var preciseScreenOnSwitch: Boolean
        get() {
            return ScStore.getBoolean(
                PRECISE_SCREEN_ON_SWITCH,
                DEFAULT_PRECISE_SCREEN_ON_SWITCH
            )
        }
        set(value) {
            ScStore.putBoolean(PRECISE_SCREEN_ON_SWITCH, value)
        }

    /**
     * The configured window in seconds, clamped to the supported range.
     *
     * The value on disk is a millisecond count inherited from the old, inert
     * setting, where nothing ever validated it. Clamping on read means a
     * leftover value cannot ask the new engine for a twenty-minute wake lock.
     */
    val preciseScreenOnSecond: Long
        get() = ScreenOnWindowCalculator.clampSeconds(milliSecondOfWakeUpScreen / 1000L)

    /**
     * Whether notifications the system itself treats as silent should be
     * ignored. See [com.symeonchen.wakeupscreen.services.notification.ImportancePolicy]
     * for what counts as silent.
     */
    var batteryLevelSwitch: Boolean
        get() {
            return ScStore.getBoolean(
                BATTERY_LEVEL_SWITCH,
                DEFAULT_BATTERY_LEVEL_SWITCH
            )
        }
        set(value) {
            ScStore.putBoolean(BATTERY_LEVEL_SWITCH, value)
        }

    /** Battery percentage below which the screen stays dark, while not charging. */
    var batteryLevelThreshold: Int
        get() {
            return ScStore.getInt(
                BATTERY_LEVEL_THRESHOLD,
                DEFAULT_BATTERY_LEVEL_THRESHOLD
            )
        }
        set(value) {
            if (value !in 1..99) {
                return
            }
            ScStore.putInt(BATTERY_LEVEL_THRESHOLD, value)
        }

    /**
     * The versionCode whose What's New content the user has already been shown.
     * Runtime state like [lastInAppReviewTime], so none of these three keys
     * belong in the settings backup: importing them onto another device would
     * suppress (or replay) the What's New sheet there for no reason.
     */
    var lastSeenVersionCode: Int
        get() {
            return ScStore.getInt(
                LAST_SEEN_VERSION_CODE,
                DEFAULT_LAST_SEEN_VERSION_CODE
            )
        }
        set(value) {
            ScStore.putInt(LAST_SEEN_VERSION_CODE, value)
        }

    /**
     * The versionCode the user updated from, kept so the NEW badges can outlive
     * the one-shot What's New sheet: a badge stays until its row is visited,
     * not until the sheet is dismissed. On a fresh install this is the current
     * version, which makes every badge test false.
     */
    var updatedFromVersionCode: Int
        get() {
            return ScStore.getInt(
                UPDATED_FROM_VERSION_CODE,
                DEFAULT_UPDATED_FROM_VERSION_CODE
            )
        }
        set(value) {
            ScStore.putInt(UPDATED_FROM_VERSION_CODE, value)
        }

    /** Comma-joined [com.symeonchen.wakeupscreen.data.FeatureBadge] keys already visited. */
    var clickedFeatureBadges: String
        get() {
            return ScStore.getString(
                CLICKED_FEATURE_BADGES,
                DEFAULT_CLICKED_FEATURE_BADGES
            ) ?: DEFAULT_CLICKED_FEATURE_BADGES
        }
        set(value) {
            ScStore.putString(CLICKED_FEATURE_BADGES, value)
        }

    var ignoreSilentNotificationSwitch: Boolean
        get() {
            return ScStore.getBoolean(
                IGNORE_SILENT_NOTIFICATION_SWITCH,
                DEFAULT_IGNORE_SILENT_NOTIFICATION_SWITCH
            )
        }
        set(value) {
            ScStore.putBoolean(IGNORE_SILENT_NOTIFICATION_SWITCH, value)
        }

}