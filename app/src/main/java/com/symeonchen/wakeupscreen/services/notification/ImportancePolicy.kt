package com.symeonchen.wakeupscreen.services.notification

/**
 * What counts as a silent notification.
 *
 * The rule follows the system's own idea of silent rather than inventing one.
 * On Android 8+ that is the channel importance the user (or the app) chose:
 * `LOW` and below is exactly the set that makes no sound and never peeks, which
 * is what makes a wake-up confusing — the screen lights up on an empty lock
 * screen. Below Android 8 there are no channels, so the deprecated per-
 * notification priority is the only equivalent signal available.
 *
 * Holds no Android references at all. The thresholds are written out as the
 * numbers the platform defines rather than as `NotificationManager.IMPORTANCE_*`
 * / `Notification.PRIORITY_*`, because those fields postdate this app's minimum
 * SDK and would only ever be inlined to these same values anyway. The unit
 * tests feed the real platform constants in, so the two cannot drift apart
 * without a test failing.
 */
object ImportancePolicy {

    /**
     * Importance value meaning "we could not find out" — the default carried by
     * [com.symeonchen.wakeupscreen.utils.ChannelLogInfo] before Android 8 and
     * whenever the channel cannot be resolved.
     */
    const val IMPORTANCE_UNRESOLVED = -1

    /** `NotificationManager.IMPORTANCE_NONE`: the lowest real importance. */
    private const val IMPORTANCE_NONE = 0

    /** `NotificationManager.IMPORTANCE_LOW`: audible importance starts above this. */
    private const val IMPORTANCE_LOW = 2

    /** `Notification.PRIORITY_LOW`: the legacy equivalent of the line above. */
    private const val PRIORITY_LOW = -1

    /**
     * Whether a notification with these channel/notification values should be
     * treated as silent.
     *
     * Fails open on purpose: an unresolved importance with no usable priority
     * returns `false`, so a notification is never dropped on the strength of a
     * value the app could not read. Losing a real message costs more than one
     * unnecessary wake-up.
     */
    fun isSilent(importance: Int, priority: Int? = null): Boolean {
        if (importance >= IMPORTANCE_NONE) {
            return importance <= IMPORTANCE_LOW
        }
        // IMPORTANCE_UNSPECIFIED / IMPORTANCE_UNRESOLVED land here as well as
        // pre-Android 8 notifications: fall back to the legacy priority.
        priority ?: return false
        return priority <= PRIORITY_LOW
    }
}
