package com.symeonchen.wakeupscreen.states

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.symeonchen.wakeupscreen.services.ScFaceDownSensor
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Registration bookkeeping for the face-down accelerometer listener, mirroring
 * [ProximitySensorState] so the two posture sensors are managed the same way.
 */
class FaceDownSensorState {
    companion object {
        private var faceDownListener = ScFaceDownSensor()
        private var accelerometer: Sensor? = null
        private var sensorManager: SensorManager? = null

        @Synchronized
        fun registerListener(context: Context?): Boolean {
            if (context == null) {
                DataInjection.statusOfFaceDown = false
                return false
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            if (isRegistered()) {
                return true
            }
            val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (sensor == null) {
                DataInjection.statusOfFaceDown = false
                return false
            }
            // Reset both halves of the cached state for this registration.
            // Otherwise toggling the feature off and back on while still face
            // down can suppress the first identical accelerometer verdict.
            DataInjection.statusOfFaceDown = false
            faceDownListener = ScFaceDownSensor()
            val registered = sensorManager?.registerListener(
                faceDownListener,
                sensor, SensorManager.SENSOR_DELAY_NORMAL
            ) == true
            if (!registered) {
                DataInjection.statusOfFaceDown = false
                return false
            }
            accelerometer = sensor
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
            sensorManager?.unregisterListener(faceDownListener)
            accelerometer = null
            // A stale "face down" from before the listener stopped would keep
            // blocking forever; without a sensor feeding it, the safe answer
            // is "not face down".
            DataInjection.statusOfFaceDown = false
        }

        @Synchronized
        fun isRegistered(): Boolean = accelerometer != null
    }
}
