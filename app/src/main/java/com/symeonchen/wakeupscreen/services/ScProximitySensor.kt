package com.symeonchen.wakeupscreen.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

/**
 * The state of the proximity sensor in this process.
 *
 * UNKNOWN is deliberately distinct from FAR. A newly rebound notification
 * listener must not assume that the phone is outside a pocket while it is
 * still waiting for the sensor's first reading.
 */
enum class ProximityReading {
    UNKNOWN,
    NEAR,
    FAR,
    UNAVAILABLE,
}

/** Pure proximity arithmetic, kept outside Android callbacks for unit tests. */
object ProximityDetector {

    /**
     * Android only promises that a near reading is less than maximumRange; it
     * does not promise that near is exactly zero. That distinction matters for
     * virtual and under-display proximity sensors used by newer phones.
     */
    fun classify(distance: Float, maximumRange: Float): ProximityReading {
        if (!distance.isFinite() || !maximumRange.isFinite() ||
            distance < 0f || maximumRange <= 0f) {
            return ProximityReading.UNKNOWN
        }
        return if (distance < maximumRange) {
            ProximityReading.NEAR
        } else {
            ProximityReading.FAR
        }
    }
}

/** The fail-safe rule applied while pocket mode is enabled. */
object PocketModePolicy {
    fun shouldBlock(enabled: Boolean, reading: ProximityReading): Boolean =
        enabled && when (reading) {
            ProximityReading.NEAR,
            ProximityReading.UNKNOWN -> true

            ProximityReading.FAR,
            ProximityReading.UNAVAILABLE -> false
        }
}

/**
 * Created by SymeonChen on 2019-10-27.
 */
class ScProximitySensor(
    private val onReadingChanged: (ProximityReading) -> Unit,
) : SensorEventListener {

    private var lastReading = ProximityReading.UNKNOWN

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_PROXIMITY || event.values.isEmpty()) {
            return
        }
        val reading = ProximityDetector.classify(
            distance = event.values[0],
            maximumRange = event.sensor.maximumRange,
        )
        if (reading != lastReading) {
            lastReading = reading
            onReadingChanged(reading)
        }
    }
}
