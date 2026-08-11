package com.symeonchen.wakeupscreen.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.symeonchen.wakeupscreen.utils.ScLog

/**
 * Locks the screen through `GLOBAL_ACTION_LOCK_SCREEN`.
 *
 * This is the only way an unprivileged app can put the display to sleep as
 * decisively as the power button, which is what makes a short screen-on window
 * exact rather than approximate. It costs the user an explicit grant, so it is
 * opt-in and the app always has the permission-free fallback to fall back on.
 *
 * The service subscribes to no accessibility events and reads nothing from the
 * screen; it exists purely to perform that one global action.
 */
class ScLockScreenAccessibilityService : AccessibilityService() {

    companion object {
        private const val MODULE = "Accessibility"

        @Volatile
        private var instance: ScLockScreenAccessibilityService? = null

        /** True once the system has bound and connected the service. */
        fun isConnected(): Boolean = instance != null

        /**
         * Performs the lock. Returns false when the service is not connected or
         * the platform refused the action, so the caller can fall back.
         */
        fun lockScreen(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return false
            }
            val service = instance ?: return false
            return try {
                val performed = service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                ScLog.i(MODULE, "GLOBAL_ACTION_LOCK_SCREEN performed=$performed")
                performed
            } catch (e: Exception) {
                ScLog.w(MODULE, "GLOBAL_ACTION_LOCK_SCREEN failed", e)
                false
            }
        }

        /**
         * Whether the user has enabled the service in system settings.
         *
         * Read from settings rather than from [isConnected] because the UI has
         * to show the right state before the system has got round to binding.
         */
        fun isEnabledInSettings(context: Context): Boolean {
            val expected = "${context.packageName}/${ScLockScreenAccessibilityService::class.java.name}"
            val enabled = try {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                )
            } catch (e: Exception) {
                ScLog.w(MODULE, "failed to read enabled accessibility services", e)
                null
            } ?: return false

            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabled)
            for (entry in splitter) {
                if (entry.equals(expected, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        ScLog.i(MODULE, "service connected")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        ScLog.i(MODULE, "service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally empty: the service subscribes to no events.
    }

    override fun onInterrupt() {
        // Nothing to interrupt.
    }
}
