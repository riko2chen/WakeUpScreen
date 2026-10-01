package com.symeonchen.wakeupscreen.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import org.junit.Assert.*
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class PocketModeInstrumentedTest {
    private val context get() = InstrumentationRegistry.getTargetContext()
    private fun sensor(): Sensor? =
        (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
            .getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private fun event(sensor: Sensor, vararg values: Float): SensorEvent =
        SensorEvent::class.java.getDeclaredConstructor(Int::class.javaPrimitiveType).run {
            isAccessible = true
            newInstance(values.size).apply {
                this.sensor = sensor
                values.copyInto(this.values)
            }
        }

    @Test fun callbackKeepsBothPoliciesAndInvalidatesNearOnEmptyOrInvalidReadings() {
        val sensor = sensor()
        assumeNotNull(sensor)
        sensor!!
        assertTrue(sensor.maximumRange > 0f)
        val changes = mutableListOf<ProximityReadings>()
        val listener = ScProximitySensor { changes += it }
        listener.onSensorChanged(event(sensor, 0f))
        assertEquals(ProximityReadings(ProximityReading.NEAR, ProximityReading.NEAR), changes.last())
        val nonZeroNear = sensor.maximumRange / 2f
        listener.onSensorChanged(event(sensor, nonZeroNear))
        assertEquals(ProximityReadings(ProximityReading.FAR, ProximityReading.NEAR), changes.last())
        listener.onSensorChanged(event(sensor, nonZeroNear))
        assertEquals(2, changes.size)
        listener.onSensorChanged(event(sensor))
        assertEquals(ProximityReadings(), changes.last())
        listener.onSensorChanged(event(sensor, 0f))
        listener.onSensorChanged(event(sensor, Float.NaN))
        assertEquals(ProximityReadings(), changes.last())
        listener.onSensorChanged(event(sensor, sensor.maximumRange))
        assertEquals(ProximityReadings(ProximityReading.FAR, ProximityReading.FAR), changes.last())
    }

    @Test fun repeatedRegistrationKeepsListenerAndRestartCreatesFreshUnknownSession() {
        assumeNotNull(sensor())
        val listenerField = ProximitySensorState::class.java.getDeclaredField("proximityListener")
            .apply { isAccessible = true }
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val wasRegistered = ProximitySensorState.isRegistered()
            try {
                ProximitySensorState.unRegisterListener(context)
                assertEquals(ProximityReading.UNKNOWN, ProximitySensorState.currentReading())
                assertTrue(ProximitySensorState.registerListener(context))
                val firstListener = listenerField.get(null)
                assertTrue(ProximitySensorState.registerListener(context))
                assertSame(firstListener, listenerField.get(null))
                ProximitySensorState.unRegisterListener(context)
                assertFalse(ProximitySensorState.isRegistered())
                assertTrue(ProximitySensorState.registerListener(context))
                assertNotSame(firstListener, listenerField.get(null))
                // Runs before the main looper can dispatch the first sensor event.
                assertEquals(ProximityReading.UNKNOWN, ProximitySensorState.currentReading(false))
                assertEquals(ProximityReading.UNKNOWN, ProximitySensorState.currentReading(true))
                assertFalse(PocketModePolicy.shouldBlock(true, ProximitySensorState.currentReading()))
            } finally {
                ProximitySensorState.unRegisterListener(context)
                if (wasRegistered) ProximitySensorState.registerListener(context)
            }
        }
    }
}
