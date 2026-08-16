package com.symeonchen.wakeupscreen.utils

/**
 * How long ago something happened, reduced to the coarsest unit a person would
 * actually say. The mapping to words happens in the UI layer, where the string
 * resources live; keeping the arithmetic here makes it testable without Android.
 */
sealed class ElapsedTimeBucket {
    object JustNow : ElapsedTimeBucket()
    data class Minutes(val minutes: Long) : ElapsedTimeBucket()
    data class Hours(val hours: Long) : ElapsedTimeBucket()
    data class Days(val days: Long) : ElapsedTimeBucket()

    companion object {
        private const val MINUTE_MS = 60_000L
        private const val HOUR_MS = 60 * MINUTE_MS
        private const val DAY_MS = 24 * HOUR_MS

        /**
         * A timestamp ahead of `now` — a clock that was set back — reads as
         * [JustNow] rather than a negative age.
         */
        fun between(thenMs: Long, nowMs: Long): ElapsedTimeBucket {
            val elapsed = nowMs - thenMs
            return when {
                elapsed < MINUTE_MS -> JustNow
                elapsed < HOUR_MS -> Minutes(elapsed / MINUTE_MS)
                elapsed < DAY_MS -> Hours(elapsed / HOUR_MS)
                else -> Days(elapsed / DAY_MS)
            }
        }
    }
}
