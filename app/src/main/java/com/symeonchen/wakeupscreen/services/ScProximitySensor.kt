package com.symeonchen.wakeupscreen.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Keeps [DataInjection.statueOfProximity] current, the same way the
 * accelerometer listener maintains the face-down state: the condition chain
 * then answers from the stored posture instead of waiting on a sensor at
 * notification time.
 */
class ScProximitySensor : SensorEventListener {

    private val handler = Handler(Looper.getMainLooper())
    private var state = ProximityVerdict.none()

    val hasReading: Boolean get() = state.hasReading

    fun reset() {
        handler.removeCallbacks(commitUncover)
        state = ProximityVerdict.none()
    }

    private val commitUncover = Runnable {
        val next = ProximityDetector.onHoldElapsed(state, SystemClock.elapsedRealtime())
        if (next != state) {
            apply(next, SystemClock.elapsedRealtime())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_PROXIMITY) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        val next = ProximityDetector.onReading(
            current = state,
            distance = event.values[0],
            maxRange = event.sensor.maximumRange,
            nowElapsedMs = now,
        )
        apply(next, now)
    }

    private fun apply(next: ProximityVerdict, nowElapsedMs: Long) {
        state = next
        if (next.hasReading) {
            DataInjection.statueOfProximity = if (next.covered) 0 else 1
        }
        handler.removeCallbacks(commitUncover)
        val pending = next.pendingUncoverAtElapsedMs ?: return
        handler.postDelayed(commitUncover, (pending - nowElapsedMs).coerceAtLeast(0L))
    }
}
