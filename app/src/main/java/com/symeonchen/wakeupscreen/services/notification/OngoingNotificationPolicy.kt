package com.symeonchen.wakeupscreen.services.notification

/**
 * Which notifications the ongoing / non-clearable gates drop.
 *
 * Playback updates are treated as ongoing even when the app forgot the flag:
 * a media session that is still playing is "something in progress", the same
 * as a call or a navigation prompt, and every track change would otherwise be
 * a chance to wake the screen.
 */
object OngoingNotificationPolicy {

    fun shouldBlock(
        isOngoing: Boolean,
        isClearable: Boolean,
        isPlayback: Boolean,
        blockOngoing: Boolean,
        blockNonClearable: Boolean,
    ): Boolean {
        if (blockNonClearable && !isClearable) {
            return true
        }
        if (blockOngoing && (isOngoing || isPlayback)) {
            return true
        }
        return false
    }
}
