package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.DarkModeInfo
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_BLACK_LIST_STRING
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
import com.symeonchen.wakeupscreen.data.ScConstant.DND_DETECT_SWITCH
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
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_BOOLEAN
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_TIME_BEGIN
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_TIME_END
import com.symeonchen.wakeupscreen.data.ScConstant.WAKE_SCREEN_SECOND
import com.tencent.mmkv.MMKV

/**
 * Created by SymeonChen on 2019-10-27.
 */
object DataInjection {

    var switchOfApp: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(CUSTOM_STATUS, DEFAULT_SWITCH_OF_APP)
                ?: DEFAULT_SWITCH_OF_APP
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(CUSTOM_STATUS, value)
        }

    var milliSecondOfWakeUpScreen: Long
        get() {
            return MMKV.defaultMMKV()
                ?.getLong(WAKE_SCREEN_SECOND, DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS)
                ?: DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
        }
        set(millisSec) {
            if (millisSec < 0) {
                return
            }
            MMKV.defaultMMKV()?.putLong(WAKE_SCREEN_SECOND, millisSec)
        }

    var statueOfProximity: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(PROXIMITY_STATUS, DEFAULT_VALUE_OF_PROXIMITY)
                ?: DEFAULT_VALUE_OF_PROXIMITY
        }
        set(state) {
            MMKV.defaultMMKV()?.putInt(PROXIMITY_STATUS, state)
        }


    var switchOfProximity: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(PROXIMITY_SWITCH, DEFAULT_SWITCH_OF_PROXIMITY)
                ?: DEFAULT_SWITCH_OF_PROXIMITY
        }
        set(switch) {
            MMKV.defaultMMKV()?.putBoolean(PROXIMITY_SWITCH, switch)
        }

    var fakeSwitchOfBatterySaver: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(BATTERY_SAVER_FAKE_SWITCH, DEFAULT_BATTERY_SAVER)
                ?: DEFAULT_BATTERY_SAVER
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(BATTERY_SAVER_FAKE_SWITCH, value)
        }

    var permissionOfSendNotification: Boolean
        get() {
            return MMKV.defaultMMKV()
                ?.getBoolean(SEND_NOTIFICATION_PERMISSION, DEFAULT_PERMISSION_OF_SEND_NOTIFICATION)
                ?: DEFAULT_PERMISSION_OF_SEND_NOTIFICATION
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(SEND_NOTIFICATION_PERMISSION, value)
        }

    var switchOfDebugMode: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(DEBUG_MODE_SWITCH, DEFAULT_SWITCH_OF_DEBUG_MODE)
                ?: DEFAULT_SWITCH_OF_DEBUG_MODE
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(DEBUG_MODE_SWITCH, value)
        }

    var modeOfCurrent: CurrentMode
        get() {
            return CurrentMode.getModeFromValue(
                MMKV.defaultMMKV()?.getInt(
                    APP_NOTIFY_MODE,
                    DEFAULT_APP_NOTIFY_MODE
                ) ?: DEFAULT_APP_NOTIFY_MODE
            )

        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(APP_NOTIFY_MODE, value.ordinal)
        }

    var appWhiteListStringOfNotify: String
        get() {
            return MMKV.defaultMMKV()?.getString(
                APP_FILTER_WHITE_LIST_STRING,
                DEFAULT_APP_WHITE_LIST_STRING
            ) ?: ""
        }
        set(value) {
            MMKV.defaultMMKV()?.putString(APP_FILTER_WHITE_LIST_STRING, value)
        }

    var appBlackListStringOfNotify: String
        get() {
            return MMKV.defaultMMKV()?.getString(
                APP_FILTER_BLACK_LIST_STRING,
                DEFAULT_APP_BLACK_LIST_STRING
            ) ?: ""
        }
        set(value) {
            MMKV.defaultMMKV()?.putString(APP_FILTER_BLACK_LIST_STRING, value)
        }

    var appListUpdateFlag: Long
        get() {
            return MMKV.defaultMMKV()?.getLong(APP_FILTER_LIST_FLAG, DEFAULT_APP_WHITE_LIST_FLAG)
                ?: DEFAULT_APP_WHITE_LIST_FLAG
        }
        set(value) {
            MMKV.defaultMMKV()?.putLong(APP_FILTER_LIST_FLAG, value)
        }

    var ongoingOptimize: Boolean
        get() {
            return MMKV.defaultMMKV()
                ?.getBoolean(ONGOING_STATUS_DETECT, DEFAULT_ONGOING_STATUS_DETECT)
                ?: DEFAULT_ONGOING_STATUS_DETECT
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(ONGOING_STATUS_DETECT, value)
        }

    var radicalOngoingOptimize: Boolean
        get() {
            return MMKV.defaultMMKV()
                ?.getBoolean(RADICAL_ONGOING_DETECT, DEFAULT_RADICAL_ONGOING_DETECT)
                ?: DEFAULT_RADICAL_ONGOING_DETECT
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(RADICAL_ONGOING_DETECT, value)
        }

    var languageSelected: LanguageInfo
        get() {
            return LanguageInfo.getModeFromValue(
                MMKV.defaultMMKV()?.getInt(
                    LANGUAGE_SELECTED,
                    DEFAULT_LANGUAGE_SELECTED
                ) ?: DEFAULT_LANGUAGE_SELECTED
            )
        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(LANGUAGE_SELECTED, value.ordinal)
        }

    var darkModeSelected: DarkModeInfo
        get() {
            return DarkModeInfo.getModeFromValue(
                MMKV.defaultMMKV()?.getInt(
                    DARK_MODE_SELECTED,
                    DEFAULT_DARK_MODE_SELECTED
                ) ?: DEFAULT_DARK_MODE_SELECTED
            )
        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(DARK_MODE_SELECTED, value.referenceNum)
        }

    var sleepModeBoolean: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(SLEEP_MODE_BOOLEAN, DEFAULT_SLEEP_MODE_BOOLEAN)
                ?: DEFAULT_SLEEP_MODE_BOOLEAN
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(SLEEP_MODE_BOOLEAN, value)
        }

    var sleepModeTimeBeginHour: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(
                SLEEP_MODE_TIME_BEGIN,
                DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
            ) ?: DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR
        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(SLEEP_MODE_TIME_BEGIN, value)
        }

    var sleepModeTimeEndHour: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(
                SLEEP_MODE_TIME_END,
                DEFAULT_SLEEP_MODE_TIME_END_HOUR
            ) ?: DEFAULT_SLEEP_MODE_TIME_END_HOUR
        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(SLEEP_MODE_TIME_END, value)
        }

    var dndDetectSwitch: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(DND_DETECT_SWITCH, DEFAULT_DND_DETECT_SWITCH)
                ?: DEFAULT_DND_DETECT_SWITCH
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(DND_DETECT_SWITCH, value)
        }

    var lastInAppReviewTime: String
        get() {
            return MMKV.defaultMMKV()?.getString(
                LAST_IN_APP_REVIEW_TIMESTAMP,
                DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP
            ) ?: DEFAULT_LAST_IN_APP_REVIEW_TIMESTAMP
        }
        set(value) {
            MMKV.defaultMMKV()?.putString(LAST_IN_APP_REVIEW_TIMESTAMP, value)
        }

    var chargingOnlySwitch: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(
                CHARGING_ONLY_SWITCH,
                DEFAULT_CHARGING_ONLY_SWITCH
            ) ?: DEFAULT_CHARGING_ONLY_SWITCH
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(CHARGING_ONLY_SWITCH, value)
        }

    var repeatReminderSwitch: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(
                REPEAT_REMINDER_SWITCH,
                DEFAULT_REPEAT_REMINDER_SWITCH
            ) ?: DEFAULT_REPEAT_REMINDER_SWITCH
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(REPEAT_REMINDER_SWITCH, value)
        }

    var repeatReminderIntervalMinutes: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(
                REPEAT_REMINDER_INTERVAL_MINUTES,
                DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
            ) ?: DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES
        }
        set(value) {
            if (value <= 0) {
                return
            }
            MMKV.defaultMMKV()?.putInt(REPEAT_REMINDER_INTERVAL_MINUTES, value)
        }

    /** Number of reminders allowed per unread streak. 0 means unlimited. */
    var repeatReminderMaxRounds: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(
                REPEAT_REMINDER_MAX_ROUNDS,
                DEFAULT_REPEAT_REMINDER_MAX_ROUNDS
            ) ?: DEFAULT_REPEAT_REMINDER_MAX_ROUNDS
        }
        set(value) {
            if (value < 0) {
                return
            }
            MMKV.defaultMMKV()?.putInt(REPEAT_REMINDER_MAX_ROUNDS, value)
        }

    /**
     * How many reminders the current unread streak has already fired. Runtime
     * state rather than a user setting; reset whenever the streak ends or a new
     * notification arrives.
     */
    var repeatReminderRoundCount: Int
        get() {
            return MMKV.defaultMMKV()?.getInt(
                REPEAT_REMINDER_ROUND_COUNT,
                DEFAULT_REPEAT_REMINDER_ROUND_COUNT
            ) ?: DEFAULT_REPEAT_REMINDER_ROUND_COUNT
        }
        set(value) {
            MMKV.defaultMMKV()?.putInt(REPEAT_REMINDER_ROUND_COUNT, value)
        }

    /**
     * Master switch for the precise screen-on window. While it is off,
     * [milliSecondOfWakeUpScreen] is not consulted at all and the display is
     * woken exactly the way it always was.
     */
    var preciseScreenOnSwitch: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(
                PRECISE_SCREEN_ON_SWITCH,
                DEFAULT_PRECISE_SCREEN_ON_SWITCH
            ) ?: DEFAULT_PRECISE_SCREEN_ON_SWITCH
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(PRECISE_SCREEN_ON_SWITCH, value)
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
    var ignoreSilentNotificationSwitch: Boolean
        get() {
            return MMKV.defaultMMKV()?.getBoolean(
                IGNORE_SILENT_NOTIFICATION_SWITCH,
                DEFAULT_IGNORE_SILENT_NOTIFICATION_SWITCH
            ) ?: DEFAULT_IGNORE_SILENT_NOTIFICATION_SWITCH
        }
        set(value) {
            MMKV.defaultMMKV()?.putBoolean(IGNORE_SILENT_NOTIFICATION_SWITCH, value)
        }

}