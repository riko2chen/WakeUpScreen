package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification

/**
 * Notifications that represent media currently playing rather than a new
 * message: a MediaStyle now-playing card, a transport-category update, or
 * anything carrying a media session token.
 *
 * Streaming apps refresh these on every track (and some omit
 * `FLAG_ONGOING_EVENT`), which is how a hours-long YouTube Music session
 * used to keep asking the screen to wake while the phone sat in a pocket.
 *
 * The category and extra keys are the platform string constants; the unit
 * tests feed the real `Notification` fields in so the two cannot drift apart
 * without a test failing.
 */
object PlaybackNotification {

    /** `Notification.CATEGORY_TRANSPORT`. */
    const val CATEGORY_TRANSPORT = "transport"

    /** `Notification.EXTRA_MEDIA_SESSION`. */
    const val EXTRA_MEDIA_SESSION = "android.mediaSession"

    /** Token inside `Notification.EXTRA_TEMPLATE` for `Notification.MediaStyle`. */
    const val MEDIA_STYLE_TOKEN = "MediaStyle"

    fun isPlayback(notification: Notification?): Boolean {
        notification ?: return false
        return isPlayback(
            category = notification.category,
            hasMediaSession = notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION),
            template = notification.extras.getString(Notification.EXTRA_TEMPLATE),
        )
    }

    fun isPlayback(
        category: String?,
        hasMediaSession: Boolean,
        template: String?,
    ): Boolean {
        if (category == CATEGORY_TRANSPORT) return true
        if (hasMediaSession) return true
        return template?.contains(MEDIA_STYLE_TOKEN) == true
    }
}
