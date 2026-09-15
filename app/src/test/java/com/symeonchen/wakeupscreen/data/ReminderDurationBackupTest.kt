package com.symeonchen.wakeupscreen.data

import android.content.SharedPreferences
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.ReminderDurationPolicy
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

/** Exercises the real backup catalog and preference boundary with an in-memory Android store. */
class ReminderDurationBackupTest {
    private val values = mutableMapOf<String, Any>()
    private val settingsField = ScStore::class.java.getDeclaredField("settings").apply { isAccessible = true }
    private var original: Any? = null

    @Before fun installStore() {
        original = settingsField.get(null)
        lateinit var editor: SharedPreferences.Editor
        editor = Proxy.newProxyInstance(
            javaClass.classLoader, arrayOf(SharedPreferences.Editor::class.java),
        ) { _, method, args ->
            when {
                method.name.startsWith("put") -> {
                    values[args!![0] as String] = args[1]
                    editor
                }
                method.name == "apply" -> null
                method.name == "commit" -> true
                else -> error("Unexpected editor call: ${method.name}")
            }
        } as SharedPreferences.Editor
        val preferences = Proxy.newProxyInstance(
            javaClass.classLoader, arrayOf(SharedPreferences::class.java),
        ) { _, method, args ->
            when {
                method.name == "edit" -> editor
                method.name == "contains" -> values.containsKey(args!![0])
                method.name.startsWith("get") -> values[args!![0]] ?: args[1]
                else -> error("Unexpected preference call: ${method.name}")
            }
        } as SharedPreferences
        settingsField.set(null, preferences)
    }

    @After fun restoreStore() { settingsField.set(null, original) }

    @Test fun freshInstallAndOldBackupPreserveInheritedBehavior() {
        assertEquals(ReminderDurationPolicy.INHERIT, DataInjection.repeatReminderScreenOnSeconds)
        val old = BackupEnvelope.wrap(JSONObject().put(ScConstant.REPEAT_REMINDER_MAX_ROUNDS, 3), 0).toString()
        SettingsBackup.import(old)
        assertEquals(ReminderDurationPolicy.INHERIT, DataInjection.repeatReminderScreenOnSeconds)
        assertFalse(JSONObject(SettingsBackup.export()).getJSONObject("settings")
            .has(ScConstant.REPEAT_REMINDER_SCREEN_ON_SECONDS))
    }

    @Test fun customAndInheritedPreferencesRoundTripWithoutChangingInitialSettings() {
        DataInjection.preciseScreenOnSwitch = false
        DataInjection.milliSecondOfWakeUpScreen = 10_000
        for (duration in listOf(5L, 30L, 0L)) {
            DataInjection.repeatReminderScreenOnSeconds = duration
            val exported = SettingsBackup.export()
            values.clear()
            assertTrue(SettingsBackup.import(exported) is SettingsBackup.ImportResult.Success)
            assertEquals(duration, DataInjection.repeatReminderScreenOnSeconds)
            assertFalse(DataInjection.preciseScreenOnSwitch)
            assertEquals(10_000L, DataInjection.milliSecondOfWakeUpScreen)
        }
    }

    @Test fun customDurationRemainsIndependentWhenInitialPreciseSwitchChanges() {
        DataInjection.repeatReminderScreenOnSeconds = 5
        DataInjection.milliSecondOfWakeUpScreen = 10_000
        for (initialEnabled in listOf(false, true)) {
            DataInjection.preciseScreenOnSwitch = initialEnabled
            assertEquals(ReminderDurationPolicy.Wake.Precise(5), ReminderDurationPolicy.resolve(
                DataInjection.repeatReminderScreenOnSeconds, true, true,
            ))
            assertEquals(10L, DataInjection.preciseScreenOnSecond)
            assertEquals(initialEnabled, DataInjection.preciseScreenOnSwitch)
        }
    }

    @Test fun missingNewSettingKeepsExistingCustomChoiceAndImportedOutliersAreClamped() {
        DataInjection.repeatReminderScreenOnSeconds = 7
        SettingsBackup.import(BackupEnvelope.wrap(JSONObject(), 0).toString())
        assertEquals(7L, DataInjection.repeatReminderScreenOnSeconds)
        SettingsBackup.import(BackupEnvelope.wrap(JSONObject()
            .put(ScConstant.REPEAT_REMINDER_SCREEN_ON_SECONDS, Long.MAX_VALUE), 0).toString())
        assertEquals(30L, DataInjection.repeatReminderScreenOnSeconds)
        DataInjection.repeatReminderScreenOnSeconds = -5
        assertEquals(0L, DataInjection.repeatReminderScreenOnSeconds)
    }
}
