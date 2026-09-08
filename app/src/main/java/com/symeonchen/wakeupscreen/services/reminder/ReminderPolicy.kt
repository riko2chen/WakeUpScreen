package com.symeonchen.wakeupscreen.services.reminder

import com.symeonchen.wakeupscreen.services.notification.BlockReason

/** One batch lasts until its last relevant notification is dismissed or reminders are disabled. */
object ReminderPolicy {
    /** Only successful wakes consume rounds; zero is unlimited. */
    fun hasRoundsRemaining(rounds: Int, maximum: Int): Boolean = maximum == 0 || rounds < maximum

    /** Notification-specific failures must not hide another candidate. Other gates apply to all. */
    fun shouldTryAnother(blockReason: String?): Boolean = blockReason in setOf(
        BlockReason.FILTER_LIST, BlockReason.LOW_IMPORTANCE, BlockReason.ONGOING,
    )

    /** Keep an existing deadline even when more messages arrive or existing messages update. */
    fun nextDeadline(existing: Long, now: Long, intervalMillis: Long): Long =
        if (existing > 0L) existing else now + intervalMillis

    /** Returns either an eligible candidate or the first globally blocked / last blocked candidate. */
    fun <T, R> evaluate(candidates: List<T>, check: (T) -> R, reason: (R) -> String?): Pair<T, R>? {
        var last: Pair<T, R>? = null
        for (candidate in candidates) {
            val result = check(candidate)
            last = candidate to result
            if (!shouldTryAnother(reason(result))) return last
        }
        return last
    }
}
