package com.symeonchen.wakeupscreen

import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.symeonchen.wakeupscreen.base.ITagProvider

/**
 * Created by SymeonChen on 2019-10-27.
 */
open class ScBaseActivity : AppCompatActivity(), ITagProvider {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Content does not draw under the status bar, so the bar is painted to
        // match whatever sits directly below it. Second-level pages start with
        // a surfaceContainer toolbar; screens whose top is a deep colour in
        // both modes override both calls.
        applyStatusBar(R.color.surface_container, light = !isNightMode())
    }

    /**
     * [light] means dark glyphs, i.e. the bar sits on a light background.
     */
    protected fun applyStatusBar(@ColorRes colorRes: Int, light: Boolean) {
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, colorRes)
        applyLightStatusBarIcons(light)
    }

    /**
     * True when the app is currently rendering dark. Read from the resources
     * rather than the system setting, so the in-app dark mode override counts.
     */
    protected fun isNightMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

    /** [light] means dark glyphs, i.e. the bar sits on a light background. */
    protected fun applyLightStatusBarIcons(light: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = light
    }

    /**
     * @see ITagProvider
     */
    override fun getDefaultTag(): String {
        return this::class.java.simpleName
    }

}