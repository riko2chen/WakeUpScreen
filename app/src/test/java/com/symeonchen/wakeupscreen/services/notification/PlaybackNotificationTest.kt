package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackNotificationTest {

    @Test
    fun `platform constants match the strings the detector uses`() {
        assertEquals(Notification.CATEGORY_TRANSPORT, PlaybackNotification.CATEGORY_TRANSPORT)
        assertEquals(Notification.EXTRA_MEDIA_SESSION, PlaybackNotification.EXTRA_MEDIA_SESSION)
    }

    @Test
    fun `a transport category is playback`() {
        assertTrue(
            PlaybackNotification.isPlayback(
                category = Notification.CATEGORY_TRANSPORT,
                hasMediaSession = false,
                template = null,
            )
        )
    }

    @Test
    fun `a media session extra is playback`() {
        assertTrue(
            PlaybackNotification.isPlayback(
                category = null,
                hasMediaSession = true,
                template = null,
            )
        )
    }

    @Test
    fun `a MediaStyle template is playback`() {
        assertTrue(
            PlaybackNotification.isPlayback(
                category = null,
                hasMediaSession = false,
                template = "android.app.Notification\$MediaStyle",
            )
        )
    }

    @Test
    fun `an ordinary message is not playback`() {
        assertFalse(
            PlaybackNotification.isPlayback(
                category = Notification.CATEGORY_MESSAGE,
                hasMediaSession = false,
                template = null,
            )
        )
    }
}
