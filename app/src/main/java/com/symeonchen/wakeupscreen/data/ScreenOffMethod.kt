package com.symeonchen.wakeupscreen.data

import android.os.Build

/**
 * How the display is put back to sleep once a precise screen-on window ends.
 *
 * Releasing the wake lock alone is not enough: the display simply falls back to
 * the system timeout, which is usually 30 seconds or more. To honour a 5-second
 * window the app has to blank the screen itself.
 *
 * Two of the four approaches the reference implementation offers are absent by
 * design. Simulating the power key needs root, and locking through a device
 * admin costs the user Smart Lock and makes the app awkward to uninstall.
 */
enum class ScreenOffMethod {

    /**
     * Show a black, zero-brightness activity that asks the system for the
     * shortest possible display timeout.
     *
     * Needs no permission, but leans on `userActivityTimeout`, a hidden window
     * attribute the window manager only forwards for privileged windows on
     * newer releases. Measured on Android 13 / One UI: the field is set without
     * complaint and then quietly ignored, leaving the screen black but awake
     * until the system timeout. Treat it as a fallback for older devices, not
     * as something to rely on.
     */
    ENFORCE,

    /**
     * `GLOBAL_ACTION_LOCK_SCREEN` from an accessibility service. This is a real
     * lock, identical to pressing the power button, and is the only method that
     * is exact on every device — at the cost of the user granting accessibility
     * access. Requires Android 9 (API 28).
     */
    ACCESSIBILITY;

    companion object {

        fun fromValue(referenceNum: Int): ScreenOffMethod {
            return values().getOrNull(referenceNum) ?: defaultForThisDevice()
        }

        /**
         * [ACCESSIBILITY] wherever the API exists, matching how the reference
         * implementation picks its default. It costs a permission the user has
         * to grant, but it is the only method that actually works on a modern
         * build — and until it is granted the fallback behaves no worse than
         * leaving the feature switched off.
         */
        fun defaultForThisDevice(): ScreenOffMethod {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ACCESSIBILITY else ENFORCE
        }
    }
}
