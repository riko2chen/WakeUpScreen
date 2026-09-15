package com.symeonchen.wakeupscreen.services

/**
 * Turns a stream of proximity readings into a pocket verdict.
 *
 * Pure arithmetic, separated from the listener so the near/far line and the
 * uncover hold can be tested without a device. The proximity sensor is
 * documented as reporting its maximum range when far and a lesser value when
 * near — including binary sensors that only ever emit `0` and `maxRange`.
 *
 * Uncovering is delayed on purpose. Pocket fabric, under-display sensors and
 * Always On Display (a now-playing overlay while music runs) produce brief
 * "far" spikes that would otherwise punch a hole in pocket mode and light the
 * lock screen against the user's leg.
 */
object ProximityDetector {

    /**
     * How long a far reading must last before the phone is treated as out of
     * the pocket. Near is applied immediately: waking against fabric is the
     * expensive mistake, a 1.5 s pause after taking the phone out is not.
     */
    const val UNCOVER_HOLD_MS = 1500L

    fun isNear(distance: Float, maxRange: Float): Boolean {
        if (maxRange <= 0f) return distance <= 0f
        return distance < maxRange
    }

    fun onReading(
        current: ProximityVerdict,
        distance: Float,
        maxRange: Float,
        nowElapsedMs: Long,
    ): ProximityVerdict {
        if (isNear(distance, maxRange)) {
            return ProximityVerdict(
                covered = true,
                hasReading = true,
                pendingUncoverAtElapsedMs = null,
            )
        }
        // First reading, or already uncovered: a far value is trusted at once
        // so a phone sitting on a desk after a process restart still wakes.
        if (!current.hasReading || !current.covered) {
            return ProximityVerdict(
                covered = false,
                hasReading = true,
                pendingUncoverAtElapsedMs = null,
            )
        }
        val pending = current.pendingUncoverAtElapsedMs ?: (nowElapsedMs + UNCOVER_HOLD_MS)
        if (nowElapsedMs >= pending) {
            return ProximityVerdict(
                covered = false,
                hasReading = true,
                pendingUncoverAtElapsedMs = null,
            )
        }
        return ProximityVerdict(
            covered = true,
            hasReading = true,
            pendingUncoverAtElapsedMs = pending,
        )
    }

    fun onHoldElapsed(current: ProximityVerdict, nowElapsedMs: Long): ProximityVerdict {
        val pending = current.pendingUncoverAtElapsedMs ?: return current
        if (nowElapsedMs < pending) {
            return current
        }
        return ProximityVerdict(
            covered = false,
            hasReading = current.hasReading,
            pendingUncoverAtElapsedMs = null,
        )
    }
}

/**
 * The pocket state the proximity listener keeps. [hasReading] is false until
 * the sensor has spoken at least once after registration; pocket mode treats
 * that window as covered so a streaming notification cannot race the first
 * event.
 */
data class ProximityVerdict(
    val covered: Boolean,
    val hasReading: Boolean,
    val pendingUncoverAtElapsedMs: Long? = null,
) {
    companion object {
        fun none() = ProximityVerdict(
            covered = false,
            hasReading = false,
            pendingUncoverAtElapsedMs = null,
        )
    }
}
