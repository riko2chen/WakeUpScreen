package com.symeonchen.wakeupscreen.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.FrameLayout
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.utils.ScLog

/**
 * The permission-free way to end a precise screen-on window.
 *
 * It cannot switch the display off directly — no unprivileged app can — so it
 * does the next best thing: covers the screen with an opaque black window at
 * zero brightness and asks the system for the shortest display timeout it will
 * accept. The panel looks off immediately and the system powers it down a
 * moment later.
 *
 * The timeout is a hidden window attribute, so on builds that hide it the
 * screen only goes black and waits out the normal system timeout. That is the
 * reason the accessibility method exists.
 */
class ScreenOffActivity : Activity() {

    companion object {
        private const val MODULE = "ScreenOffActivity"

        fun start(context: Context) {
            val intent = Intent(context, ScreenOffActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NO_ANIMATION or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                ScLog.w(MODULE, "failed to start", e)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val finishSelf = Runnable {
        ScLog.i(MODULE, "safety timeout reached, closing")
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showOverKeyguard()

        val attributes = window.attributes
        // FLAG_ALLOW_LOCK_WHILE_SCREEN_ON is the important one: without it the
        // presence of a foreground window keeps the keyguard from re-arming.
        // FLAG_KEEP_SCREEN_ON is pointedly absent, and so is FLAG_DIM_BEHIND —
        // the window is already opaque black, and dimming behind would stop it
        // counting as the top fullscreen opaque window, which is the only kind
        // whose userActivityTimeout the window manager passes on.
        attributes.flags = attributes.flags or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        attributes.format = PixelFormat.OPAQUE
        attributes.screenBrightness = 0.0f
        attributes.buttonBrightness = 0.0f
        applyShortestUserActivityTimeout(attributes)
        window.attributes = attributes

        setContentView(FrameLayout(this).apply { setBackgroundColor(Color.BLACK) })

        handler.postDelayed(finishSelf, ScConstant.SCREEN_OFF_ACTIVITY_SAFETY_TIMEOUT_MS)
        ScLog.i(MODULE, "shown, safety timeout ${ScConstant.SCREEN_OFF_ACTIVITY_SAFETY_TIMEOUT_MS}ms")
    }

    /**
     * The window has to sit on top of the lock screen; the wake that started
     * this window left the keyguard up, and an activity behind it would blank
     * nothing.
     */
    @Suppress("DEPRECATION")
    private fun showOverKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
    }

    /**
     * `WindowManager.LayoutParams.userActivityTimeout` is the switch that makes
     * this work: a value of 1 tells the system this window wants the display to
     * time out as good as immediately. It has no public accessor, so it is set
     * reflectively and the failure is logged rather than thrown — the black
     * window on its own is still an acceptable, if slower, result.
     */
    private fun applyShortestUserActivityTimeout(attributes: WindowManager.LayoutParams) {
        try {
            val field = attributes.javaClass.getDeclaredField("userActivityTimeout")
            field.isAccessible = true
            field.setLong(attributes, 1L)
            ScLog.i(MODULE, "userActivityTimeout set to 1ms")
        } catch (e: Throwable) {
            ScLog.w(MODULE, "userActivityTimeout unavailable on this build", e)
        }
    }

    /** Once the display is off there is nothing left to show. */
    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacks(finishSelf)
        super.onDestroy()
    }
}
