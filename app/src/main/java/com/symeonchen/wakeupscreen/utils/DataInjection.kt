package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_BLACK_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_LIST_FLAG
import com.symeonchen.wakeupscreen.data.ScConstant.APP_FILTER_WHITE_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.APP_NOTIFY_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.BATTERY_SAVER_FAKE_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.CUSTOM_STATUS
import com.symeonchen.wakeupscreen.data.ScConstant.DEBUG_MODE_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_BLACK_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_NOTIFY_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_WHITE_LIST_FLAG
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_APP_WHITE_LIST_STRING
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_BATTERY_SAVER
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_LANGUAGE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_ONGOING_STATUS_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SLEEP_MODE_BOOLEAN
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_APP
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_DEBUG_MODE
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_SWITCH_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
import com.symeonchen.wakeupscreen.data.ScConstant.DEFAULT_VALUE_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.ScConstant.LANGUAGE_SELECTED
import com.symeonchen.wakeupscreen.data.ScConstant.ONGOING_STATUS_DETECT
import com.symeonchen.wakeupscreen.data.ScConstant.PROXIMITY_STATUS
import com.symeonchen.wakeupscreen.data.ScConstant.PROXIMITY_SWITCH
import com.symeonchen.wakeupscreen.data.ScConstant.SLEEP_MODE_BOOLEAN
import com.symeonchen.wakeupscreen.data.ScConstant.WAKE_SCREEN_SECOND
import com.tencent.mmkv.MMKV

object DataInjection {

    var switchOfApp: Boolean
        get() {
            return MMKV.defaultMMKV().getBoolean(CUSTOM_STATUS, DEFAULT_SWITCH_OF_APP)
        }
        set(value) {
            MMKV.defaultMMKV().putBoolean(CUSTOM_STATUS, value)
        }

    var milliSecondOfWakeUpScreen: Long
        get() {
            return MMKV.defaultMMKV()
                .getLong(WAKE_SCREEN_SECOND, DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS)
        }
        set(millisSec) {
            if (millisSec < 0) {
                return
            }
            MMKV.defaultMMKV().putLong(WAKE_SCREEN_SECOND, millisSec)
        }

    var statueOfProximity: Int
        get() {
            return MMKV.defaultMMKV().getInt(PROXIMITY_STATUS, DEFAULT_VALUE_OF_PROXIMITY)
        }
        set(state) {
            MMKV.defaultMMKV().putInt(PROXIMITY_STATUS, state)
        }


    var switchOfProximity: Boolean
        get() {
            return MMKV.defaultMMKV().getBoolean(PROXIMITY_SWITCH, DEFAULT_SWITCH_OF_PROXIMITY)
        }
        set(switch) {
            MMKV.defaultMMKV().putBoolean(PROXIMITY_SWITCH, switch)
        }

    var fakeSwitchOfBatterySaver: Boolean
        get() {
            return MMKV.defaultMMKV().getBoolean(BATTERY_SAVER_FAKE_SWITCH, DEFAULT_BATTERY_SAVER)
        }
        set(value) {
            MMKV.defaultMMKV().putBoolean(BATTERY_SAVER_FAKE_SWITCH, value)
        }

    var switchOfDebugMode: Boolean
        get() {
            return MMKV.defaultMMKV().getBoolean(DEBUG_MODE_SWITCH, DEFAULT_SWITCH_OF_DEBUG_MODE)
        }
        set(value) {
            MMKV.defaultMMKV().putBoolean(DEBUG_MODE_SWITCH, value)
        }

    var modeOfCurrent: CurrentMode
        get() {
            return CurrentMode.getModeFromValue(
                MMKV.defaultMMKV().getInt(
                    APP_NOTIFY_MODE,
                    DEFAULT_APP_NOTIFY_MODE
                )
            )

        }
        set(value) {
            MMKV.defaultMMKV().putInt(APP_NOTIFY_MODE, value.ordinal)
        }

    var appWhiteListStringOfNotify: String
        get() {
            return MMKV.defaultMMKV().getString(
                APP_FILTER_WHITE_LIST_STRING,
                DEFAULT_APP_WHITE_LIST_STRING
            ) ?: ""
        }
        set(value) {
            MMKV.defaultMMKV().putString(APP_FILTER_WHITE_LIST_STRING, value)
        }

    var appBlackListStringOfNotify: String
        get() {
            return MMKV.defaultMMKV().getString(
                APP_FILTER_BLACK_LIST_STRING,
                DEFAULT_APP_BLACK_LIST_STRING
            ) ?: ""
        }
        set(value) {
            MMKV.defaultMMKV().putString(APP_FILTER_BLACK_LIST_STRING, value)
        }

    var appListUpdateFlag: Long
        get() {
            return MMKV.defaultMMKV().getLong(APP_FILTER_LIST_FLAG, DEFAULT_APP_WHITE_LIST_FLAG)
        }
        set(value) {
            MMKV.defaultMMKV().putLong(APP_FILTER_LIST_FLAG, value)
        }

    var ongoingOptimize: Boolean
        get() {
            return MMKV.defaultMMKV()
                .getBoolean(ONGOING_STATUS_DETECT, DEFAULT_ONGOING_STATUS_DETECT)
        }
        set(value) {
            MMKV.defaultMMKV().putBoolean(ONGOING_STATUS_DETECT, value)
        }


    var languageSelected: LanguageInfo
        get() {
            return LanguageInfo.getModeFromValue(
                MMKV.defaultMMKV().getInt(
                    LANGUAGE_SELECTED,
                    DEFAULT_LANGUAGE_SELECTED
                )
            )
        }
        set(value) {
            MMKV.defaultMMKV().putInt(LANGUAGE_SELECTED, value.ordinal)
        }

    var sleepModeBoolean: Boolean
        get() {
            return MMKV.defaultMMKV().getBoolean(SLEEP_MODE_BOOLEAN, DEFAULT_SLEEP_MODE_BOOLEAN)
        }
        set(value) {
            MMKV.defaultMMKV().putBoolean(SLEEP_MODE_BOOLEAN, value)
        }

}