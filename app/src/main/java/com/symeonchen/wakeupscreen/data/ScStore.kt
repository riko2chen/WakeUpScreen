package com.symeonchen.wakeupscreen.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Every value the app persists.
 *
 * SharedPreferences replaced MMKV in 4.1.0. MMKV ships a native library that
 * crashes on 32-bit ABIs, and the releases that align it for 16 KB page sizes
 * are exactly the ones that crash — the two requirements could not be met at
 * once. SharedPreferences is pure Java, so the app carries no `.so` of its own
 * and both problems disappear. What MMKV left on disk is carried over once by
 * [LegacyMmkvStore].
 *
 * Reads fall back to the caller's default until [init] has run, so a read that
 * somehow beats `Application.onCreate` returns a default instead of crashing.
 */
object ScStore {

    private const val SETTINGS_FILE = "wake_up_screen_settings"
    private const val HISTORY_FILE = "wake_up_screen_history"

    @Volatile
    private var settings: SharedPreferences? = null

    /**
     * The notification log and the attention statistics: two JSON blobs
     * rewritten on every notification. They live in their own file so that
     * churn does not rewrite the settings alongside them.
     */
    @Volatile
    private var history: SharedPreferences? = null

    fun init(context: Context) {
        val app = context.applicationContext
        val settingsPrefs = app.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
        val historyPrefs = app.getSharedPreferences(HISTORY_FILE, Context.MODE_PRIVATE)
        settings = settingsPrefs
        history = historyPrefs
        LegacyMmkvStore.migrateOnce(app, settingsPrefs, historyPrefs)
    }

    /** Whether the key was ever written. Absent is not the same as "holds the default". */
    fun containsKey(key: String): Boolean = settings?.contains(key) == true

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        settings?.getBoolean(key, defaultValue) ?: defaultValue

    fun putBoolean(key: String, value: Boolean) {
        settings?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getInt(key: String, defaultValue: Int): Int =
        settings?.getInt(key, defaultValue) ?: defaultValue

    fun putInt(key: String, value: Int) {
        settings?.edit()?.putInt(key, value)?.apply()
    }

    fun getLong(key: String, defaultValue: Long): Long =
        settings?.getLong(key, defaultValue) ?: defaultValue

    fun putLong(key: String, value: Long) {
        settings?.edit()?.putLong(key, value)?.apply()
    }

    /**
     * A null default is meaningful: it is how a caller tells "never written"
     * apart from a stored empty string.
     */
    fun getString(key: String, defaultValue: String?): String? =
        settings?.getString(key, defaultValue) ?: defaultValue

    fun putString(key: String, value: String) {
        settings?.edit()?.putString(key, value)?.apply()
    }

    fun getHistoryString(key: String, defaultValue: String): String =
        history?.getString(key, defaultValue) ?: defaultValue

    fun putHistoryString(key: String, value: String) {
        history?.edit()?.putString(key, value)?.apply()
    }
}
