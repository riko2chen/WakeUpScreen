package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import android.app.NotificationManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rule that decides whether a notification counts as silent. Getting it
 * wrong in either direction is a bug the user feels: too strict and real
 * messages stop waking the screen, too loose and the feature does nothing.
 */
@Suppress("DEPRECATION") // Notification.PRIORITY_*: still what pre-Android 8 carries.
class ImportancePolicyTest {

    @Test
    fun `none min and low importance are silent`() {
        listOf(
            NotificationManager.IMPORTANCE_NONE,
            NotificationManager.IMPORTANCE_MIN,
            NotificationManager.IMPORTANCE_LOW,
        ).forEach {
            assertTrue("importance $it", ImportancePolicy.isSilent(it))
        }
    }

    @Test
    fun `default and above are not silent`() {
        listOf(
            NotificationManager.IMPORTANCE_DEFAULT,
            NotificationManager.IMPORTANCE_HIGH,
            NotificationManager.IMPORTANCE_MAX,
        ).forEach {
            assertFalse("importance $it", ImportancePolicy.isSilent(it))
        }
    }

    @Test
    fun `an unresolved channel falls back to the notification priority`() {
        // Below Android 8 there are no channels, so this is the only signal.
        assertTrue(
            ImportancePolicy.isSilent(
                ImportancePolicy.IMPORTANCE_UNRESOLVED,
                Notification.PRIORITY_MIN,
            )
        )
        assertTrue(
            ImportancePolicy.isSilent(
                ImportancePolicy.IMPORTANCE_UNRESOLVED,
                Notification.PRIORITY_LOW,
            )
        )
        assertFalse(
            ImportancePolicy.isSilent(
                ImportancePolicy.IMPORTANCE_UNRESOLVED,
                Notification.PRIORITY_DEFAULT,
            )
        )
        assertFalse(
            ImportancePolicy.isSilent(
                ImportancePolicy.IMPORTANCE_UNRESOLVED,
                Notification.PRIORITY_HIGH,
            )
        )
    }

    @Test
    fun `importance wins over priority when both are known`() {
        // A channel the user muted stays muted however the app labelled the
        // individual notification, and vice versa: on Android 8+ the channel is
        // the user's decision and priority is only the app's suggestion.
        assertTrue(
            ImportancePolicy.isSilent(
                NotificationManager.IMPORTANCE_LOW,
                Notification.PRIORITY_MAX,
            )
        )
        assertFalse(
            ImportancePolicy.isSilent(
                NotificationManager.IMPORTANCE_HIGH,
                Notification.PRIORITY_MIN,
            )
        )
    }

    @Test
    fun `nothing known means nothing is dropped`() {
        // Fail open: never suppress a notification on the strength of a value
        // that could not be read.
        assertFalse(ImportancePolicy.isSilent(ImportancePolicy.IMPORTANCE_UNRESOLVED, null))
        assertFalse(ImportancePolicy.isSilent(NotificationManager.IMPORTANCE_UNSPECIFIED, null))
    }

    @Test
    fun `unspecified importance still consults the priority`() {
        assertTrue(
            ImportancePolicy.isSilent(
                NotificationManager.IMPORTANCE_UNSPECIFIED,
                Notification.PRIORITY_MIN,
            )
        )
    }
}
