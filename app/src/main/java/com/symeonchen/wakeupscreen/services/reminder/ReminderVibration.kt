package com.symeonchen.wakeupscreen.services.reminder

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.symeonchen.wakeupscreen.utils.DataInjection

/** Called only after the reminder engine accepts and requests a screen wake. */
internal object ReminderVibration {
    internal const val DURATION_MS = 150L

    internal fun notificationAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    internal fun systemAllowsVibration(interruptionFilter: Int, ringerMode: Int): Boolean =
        interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL &&
            (ringerMode == AudioManager.RINGER_MODE_NORMAL || ringerMode == AudioManager.RINGER_MODE_VIBRATE)

    @Suppress("DEPRECATION")
    fun vibrateIfAllowed(context: Context): Boolean {
        if (!DataInjection.repeatReminderVibration || !DataInjection.switchOfApp ||
            !DataInjection.repeatReminderSwitch) return false
        return try {
            val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return false
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
            // The legacy service is public from API 1 and remains compatible on API 31+.
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return false
            if (!ReminderVibrationPolicy.allows(
                    enabled = DataInjection.repeatReminderVibration,
                    dndOff = notifications.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL,
                    ringerAllowsVibration = systemAllowsVibration(notifications.currentInterruptionFilter, audio.ringerMode),
                    hasVibrator = vibrator.hasVibrator(),
                )) return false
            val attributes = notificationAttributes()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE), attributes)
            } else {
                vibrator.vibrate(DURATION_MS, attributes)
            }
            true
        } catch (_: SecurityException) {
            // An OEM or policy may reject vibration; this must not interrupt reminder scheduling.
            false
        }
    }
}
