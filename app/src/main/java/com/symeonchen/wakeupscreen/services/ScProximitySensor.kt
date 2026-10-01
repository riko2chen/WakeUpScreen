package com.symeonchen.wakeupscreen.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

/** Runtime state only: missing readings must not prevent a notification wake. */
enum class ProximityReading {
    UNKNOWN,
    NEAR,
    FAR,
    UNAVAILABLE,
}

/** Keep both interpretations so changing the setting needs no new sensor event. */
data class ProximityReadings(
    val legacy: ProximityReading = ProximityReading.UNKNOWN,
    val experimental: ProximityReading = ProximityReading.UNKNOWN,
) {
    fun selected(tryNewVersion: Boolean): ProximityReading =
        if (tryNewVersion) experimental else legacy
}

/** Pure proximity arithmetic, kept outside Android callbacks for unit tests. */
object ProximityDetector {
    fun classify(
        distance: Float,
        maximumRange: Float,
        tryNewVersion: Boolean = false,
    ): ProximityReading {
        if (!distance.isFinite() || distance < 0f) {
            return ProximityReading.UNKNOWN
        }
        // Restore the old zero-only threshold without persisting sensor state
        // or truncating positive fractional distances to zero.
        if (!tryNewVersion) {
            return if (distance == 0f) ProximityReading.NEAR else ProximityReading.FAR
        }
        if (!maximumRange.isFinite() || maximumRange <= 0f) {
            return ProximityReading.UNKNOWN
        }
        return if (distance < maximumRange) ProximityReading.NEAR else ProximityReading.FAR
    }

    fun readings(distance: Float, maximumRange: Float) = ProximityReadings(
        legacy = classify(distance, maximumRange),
        experimental = classify(distance, maximumRange, tryNewVersion = true),
    )
}

/** Only a known near reading blocks, for both legacy and experimental detection. */
object PocketModePolicy {
    fun shouldBlock(enabled: Boolean, reading: ProximityReading): Boolean =
        enabled && reading == ProximityReading.NEAR
}

class ScProximitySensor(
    private val onReadingChanged: (ProximityReadings) -> Unit,
) : SensorEventListener {

    private var lastReadings = ProximityReadings()

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_PROXIMITY) return
        // An empty event invalidates the last reading instead of leaving NEAR stuck.
        val readings = ProximityDetector.readings(
            distance = event.values.firstOrNull() ?: Float.NaN,
            maximumRange = event.sensor.maximumRange,
        )
        if (readings != lastReadings) {
            lastReadings = readings
            onReadingChanged(readings)
        }
    }
}
