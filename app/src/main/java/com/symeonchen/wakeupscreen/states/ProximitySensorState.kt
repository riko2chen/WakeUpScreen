package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.symeonchen.wakeupscreen.services.ScProximitySensor

/**
 * Registration bookkeeping for the pocket-mode proximity listener.
 */
class ProximitySensorState {
    companion object {
        private var proximityListener = ScProximitySensor()
        private var proximitySensor: Sensor? = null
        private var sensorManager: SensorManager? = null

        fun registerListener(context: Context?) {
            if (context == null) {
                return
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }

            if (isRegistered()) {
                sensorManager?.unregisterListener(proximityListener)
            }
            proximityListener.reset()
            val sensor = wakeupProximity() ?: return
            val registered = sensorManager?.registerListener(
                proximityListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            ) == true
            proximitySensor = if (registered) sensor else null
        }

        fun unRegisterListener(context: Context?) {
            if (context == null) {
                return
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            sensorManager?.unregisterListener(proximityListener)
            proximityListener.reset()
            proximitySensor = null
        }

        fun isRegistered(): Boolean {
            return proximitySensor != null
        }

        /** False until the wakeup sensor has delivered at least one reading. */
        fun hasReading(): Boolean = proximityListener.hasReading

        /**
         * Prefer the wake-up proximity sensor so covering is delivered while
         * the SoC is asleep. The non-wake-up copy is what used to freeze the
         * last "far" value for hours in a pocket.
         */
        private fun wakeupProximity(): Sensor? {
            val manager = sensorManager ?: return null
            return manager.getDefaultSensor(Sensor.TYPE_PROXIMITY, true)
                ?: manager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        }
    }
}
