package com.symeonchen.wakeupscreen.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Turns a stream of accelerometer readings into a face-down verdict.
 *
 * Pure arithmetic, separated from the listener so the hysteresis can be tested
 * without a device. Gravity on the z axis is ~+9.8 m/s² with the screen up and
 * ~-9.8 face down; the two thresholds deliberately do not meet, so a phone
 * balanced near the tipping point cannot flap the state on every reading.
 */
object FaceDownDetector {

    private const val FACE_DOWN_ENTER_Z = -7.0f
    private const val FACE_DOWN_EXIT_Z = -5.0f

    fun next(currentlyFaceDown: Boolean, zAcceleration: Float): Boolean = when {
        zAcceleration <= FACE_DOWN_ENTER_Z -> true
        zAcceleration >= FACE_DOWN_EXIT_Z -> false
        else -> currentlyFaceDown
    }
}

/**
 * Keeps [DataInjection.statusOfFaceDown] current, the same way the proximity
 * listener maintains the pocket state: the condition chain then answers from
 * the stored posture instead of waiting on a sensor at notification time.
 */
class ScFaceDownSensor : SensorEventListener {

    private var isFaceDown = DataInjection.statusOfFaceDown

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }
        val next = FaceDownDetector.next(isFaceDown, event.values[2])
        if (next != isFaceDown) {
            isFaceDown = next
            DataInjection.statusOfFaceDown = next
        }
    }
}
