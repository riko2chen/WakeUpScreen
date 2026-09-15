package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.symeonchen.wakeupscreen.services.ProximityReading
import com.symeonchen.wakeupscreen.services.ScProximitySensor

/**
 * Created by SymeonChen on 2019-10-27.
 */
class ProximitySensorState {
    companion object {
        @Volatile
        private var reading = ProximityReading.UNKNOWN

        private var proximityListener = newListener()
        private var proximitySensor: Sensor? = null
        private var sensorManager: SensorManager? = null

        /**
         * Starts a fresh session. No value persisted by an older process is
         * trusted: UNKNOWN blocks briefly until this listener receives the
         * current reading.
         */
        @Synchronized
        fun registerListener(context: Context?): Boolean {
            if (context == null) {
                reading = ProximityReading.UNAVAILABLE
                return false
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }

            if (isRegistered()) {
                return true
            }

            val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            if (sensor == null) {
                reading = ProximityReading.UNAVAILABLE
                return false
            }

            reading = ProximityReading.UNKNOWN
            // The listener suppresses duplicate readings. A new registration
            // therefore needs a new listener too, otherwise a FAR -> stop ->
            // FAR sequence would leave this session stuck at UNKNOWN.
            proximityListener = newListener()
            val registered = sensorManager?.registerListener(
                proximityListener,
                sensor, SensorManager.SENSOR_DELAY_NORMAL
            ) == true
            if (!registered) {
                reading = ProximityReading.UNAVAILABLE
                return false
            }
            proximitySensor = sensor
            return true
        }

        @Synchronized
        fun unRegisterListener(context: Context?) {
            if (context == null) {
                return
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            sensorManager?.unregisterListener(proximityListener)
            proximitySensor = null
            reading = ProximityReading.UNKNOWN
        }

        @Synchronized
        fun isRegistered(): Boolean = proximitySensor != null

        fun currentReading(): ProximityReading = reading

        private fun newListener() = ScProximitySensor { newReading ->
            reading = newReading
        }
    }

}
