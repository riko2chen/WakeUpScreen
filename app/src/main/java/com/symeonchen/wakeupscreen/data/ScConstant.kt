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
    const val DEBUG_MODE_SWITCH = "debug_mode_switch"
    const val APP_NOTIFY_MODE = "app_white_list_switch"
    const val APP_FILTER_WHITE_LIST_STRING = "white_list_app"
    const val APP_FILTER_BLACK_LIST_STRING = "black_list_app"
    const val APP_FILTER_LIST_FLAG = "white_list_flag"
    const val ONGOING_STATUS_DETECT = "ongoing_status_detect"
    const val LANGUAGE_SELECTED = "language_selected"
    const val SLEEP_MODE_BOOLEAN = "sleep_mode_boolean"
    const val SLEEP_MODE_TIME_BEGIN = "sleep_mode_time_begin"
    const val SLEEP_MODE_TIME_END = "sleep_mode_time_end"

    const val DEFAULT_SWITCH_OF_APP: Boolean = true
    const val DEFAULT_SWITCH_OF_PROXIMITY: Boolean = true
    const val DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS: Long = 2000
    const val DEFAULT_VALUE_OF_PROXIMITY: Int = 1
    const val DEFAULT_BATTERY_SAVER: Boolean = false
    const val DEFAULT_SWITCH_OF_DEBUG_MODE: Boolean = false
    const val DEFAULT_APP_NOTIFY_MODE: Int = 0
    const val DEFAULT_APP_WHITE_LIST_STRING: String = ""
    const val DEFAULT_APP_BLACK_LIST_STRING: String = ""
    const val DEFAULT_APP_WHITE_LIST_FLAG = 0L
    const val DEFAULT_ONGOING_STATUS_DETECT = true
    const val DEFAULT_LANGUAGE_SELECTED: Int = 0
    const val DEFAULT_SLEEP_MODE_BOOLEAN: Boolean = false
    const val DEFAULT_SLEEP_MODE_TIME_BEGIN_HOUR = 2
    const val DEFAULT_SLEEP_MODE_TIME_END_HOUR = 4

    const val AUTHOR_MAIL = "symeonchen@gmail.com"
    const val DEFAULT_MAIL_HEAD = "【问题反馈】【通知亮屏】在此输入标题"
    const val DEFAULT_MAIL_BODY = ""

}
