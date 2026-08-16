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

        fun registerListener(context: Context?) {
            if (context == null) {
                return
            }
            if (sensorManager == null) {
                sensorManager =
                    context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            }
            if (isRegistered()) {
                sensorManager?.unregisterListener(faceDownListener)
            }
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager?.registerListener(
                faceDownListener,
                accelerometer, SensorManager.SENSOR_DELAY_NORMAL
            )
        }

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

        fun isRegistered(): Boolean {
            return accelerometer != null
        }
    }
}
