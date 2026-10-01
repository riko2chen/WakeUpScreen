package com.symeonchen.wakeupscreen.data

import android.content.SharedPreferences
import com.symeonchen.wakeupscreen.services.ProximityDetector
import com.symeonchen.wakeupscreen.services.ProximityReadings
import com.symeonchen.wakeupscreen.services.ProximityReading
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.conditions.PocketModeCondition
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

/** Real preferences, backup catalog and condition; no Android hardware dependency. */
class PocketModeCompatibilityTest {
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


    private val readingsField = ProximitySensorState::class.java.getDeclaredField("readings")
        .apply { isAccessible = true }
    private lateinit var originalReadings: ProximityReadings

    @Before fun saveReadings() {
        originalReadings = readingsField.get(null) as ProximityReadings
        readingsField.set(null, ProximityReadings())
    }

    @After fun restoreReadings() { readingsField.set(null, originalReadings) }

    @Test fun existingInstallAndLegacyRuntimeNearValueUseLegacyModeAndAllowUnknown() {
        ScStore.putInt(ScConstant.PROXIMITY_STATUS, 0)
        assertTrue(DataInjection.switchOfProximity)
        assertFalse(DataInjection.pocketModeTryNewVersion)
        assertEquals(ConditionState.SUCCESS, PocketModeCondition().provideResult())
        assertFalse(PocketModeCondition().wouldBlockNow(null))
        assertFalse(JSONObject(SettingsBackup.export()).getJSONObject("settings")
            .has(ScConstant.PROXIMITY_STATUS))
    }

    @Test fun conditionAndPreviewSwitchImmediatelyForTheSameNonZeroSample() {
        val condition = PocketModeCondition()
        readingsField.set(null, ProximityDetector.readings(1f, 5f))
        for (experimental in listOf(false, true, false)) {
            DataInjection.pocketModeTryNewVersion = experimental
            assertEquals(if (experimental) ConditionState.BLOCK else ConditionState.SUCCESS,
                condition.provideResult())
            assertEquals(experimental, condition.wouldBlockNow(null))
        }
        DataInjection.switchOfProximity = false
        DataInjection.pocketModeTryNewVersion = true
        assertEquals(ConditionState.SUCCESS, condition.provideResult())
        assertFalse(condition.isArmed())
    }

    @Test fun conditionAllowsMissingUnavailableAndInvalidReadingsInBothModes() {
        for (readings in listOf(
            ProximityReadings(),
            ProximityReadings(ProximityReading.UNAVAILABLE, ProximityReading.UNAVAILABLE),
            ProximityDetector.readings(Float.NaN, 5f),
        )) {
            readingsField.set(null, readings)
            for (experimental in listOf(false, true)) {
                DataInjection.pocketModeTryNewVersion = experimental
                assertEquals(ConditionState.SUCCESS, PocketModeCondition().provideResult())
                assertFalse(PocketModeCondition().wouldBlockNow(null))
            }
        }
    }

    @Test fun optInRoundTripsAndOldBackupsNeverEnableItImplicitly() {
        val oldBackup = BackupEnvelope.wrap(JSONObject().put(ScConstant.PROXIMITY_SWITCH, true), 0)
            .toString()
        SettingsBackup.import(oldBackup)
        assertFalse(DataInjection.pocketModeTryNewVersion)
        for (experimental in listOf(false, true)) {
            DataInjection.pocketModeTryNewVersion = experimental
            val exported = SettingsBackup.export()
            values.clear()
            assertTrue(SettingsBackup.import(exported) is SettingsBackup.ImportResult.Success)
            assertEquals(experimental, DataInjection.pocketModeTryNewVersion)
            SettingsBackup.import(oldBackup)
            assertEquals(experimental, DataInjection.pocketModeTryNewVersion)
        }
    }
}
