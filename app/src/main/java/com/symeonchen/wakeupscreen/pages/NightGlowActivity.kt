package com.symeonchen.wakeupscreen.pages

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.services.screen.ScreenOffController
import com.symeonchen.wakeupscreen.utils.ScLog

/**
 * The night version of a wake: instead of the full lock screen at full
 * brightness, a black window at minimum brightness with one small red glow.
 *
 * Red because it is the colour dark-adapted eyes forgive — the reason
 * flashlights on night watches and astronomy apps use it — and small because
 * the point is "something arrived", not reading the message. The window turns
 * the screen on itself, stays [ScConstant.NIGHT_GLOW_DURATION_MS], then asks
 * [ScreenOffController] to put the display back out the same way the precise
 * screen-on window does.
 */
class NightGlowActivity : Activity() {

    companion object {
        private const val MODULE = "NightGlow"

        fun start(context: Context) {
            val intent = Intent(context, NightGlowActivity::class.java).apply {
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

        /** Dark red, far below full channel value, on top of a pure black window. */
        private const val GLOW_COLOR = 0xFF8B0000.toInt()
        private const val GLOW_SIZE_DP = 48
    }

    private val handler = Handler(Looper.getMainLooper())
    private val endGlow = Runnable {
        ScLog.i(MODULE, "glow window over, requesting screen off")
        ScreenOffController.requestScreenOff(this)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showOverKeyguardAndTurnScreenOn()

        val attributes = window.attributes
        attributes.flags = attributes.flags or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        attributes.format = PixelFormat.OPAQUE
        // The minimum the panel accepts; on AMOLED the black stays unlit and
        // only the glow's own pixels draw power.
        attributes.screenBrightness = 0.01f
        attributes.buttonBrightness = 0.0f
        window.attributes = attributes

        val density = resources.displayMetrics.density
        val glowSize = (GLOW_SIZE_DP * density).toInt()
        val glow = ImageView(this).apply {
            setImageDrawable(GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(GLOW_COLOR)
            })
            layoutParams = FrameLayout.LayoutParams(glowSize, glowSize, Gravity.CENTER)
        }
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(glow)
        })

        handler.postDelayed(endGlow, ScConstant.NIGHT_GLOW_DURATION_MS)
        ScLog.i(MODULE, "shown for ${ScConstant.NIGHT_GLOW_DURATION_MS}ms")
    }

    @Suppress("DEPRECATION")
    private fun showOverKeyguardAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(endGlow)
        super.onDestroy()
    }
}
