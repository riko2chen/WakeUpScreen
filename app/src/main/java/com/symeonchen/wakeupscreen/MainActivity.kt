package com.symeonchen.wakeupscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.symeonchen.wakeupscreen.compose.WhatsNewSheet
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.databinding.ActivityMainBinding
import com.symeonchen.wakeupscreen.pages.ScMainFragment
import com.symeonchen.wakeupscreen.pages.ScSettingFragment
import com.symeonchen.wakeupscreen.utils.WhatsNewTracker

/**
 * Created by SymeonChen on 2019-10-27.
 */
class MainActivity : ScBaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragmentList = listOf<Fragment>(ScMainFragment(), ScSettingFragment())

    private companion object {
        const val HOME_PAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // On API 29 and earlier, a Compose child consuming insets must not
        // prevent its sibling bottom navigation from receiving them.
        ViewGroupCompat.installCompatInsetsDispatch(binding.root)
        setContentView(binding.root)
        initView()
        maybeShowWhatsNew()
    }

    /**
     * After an update, the first launch gets the What's New sheet, listing
     * every version between the one the user left and this one. Consuming the
     * pending state advances the stored version immediately, so a rotation or
     * the next launch shows nothing; the same content stays reachable under
     * Settings → About → Changelog.
     *
     * The sheet is Compose but this activity is view-based, so a zero-sized
     * ComposeView hosts it: ModalBottomSheet renders in its own window, and
     * the host only anchors the composition.
     */
    private fun maybeShowWhatsNew() {
        val versions = WhatsNewTracker.consumePendingWhatsNew(this)
        if (versions.isEmpty()) {
            return
        }
        val host = ComposeView(this)
        binding.root.addView(host, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        host.setContent {
            WakeUpScreenTheme {
                var show by remember { mutableStateOf(true) }
                if (show) {
                    WhatsNewSheet(
                        versions = versions,
                        onDismiss = {
                            show = false
                            binding.root.removeView(host)
                        },
                        onViewFullChangelog = {
                            try {
                                startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.changelog_url)))
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    )
                }
            }
        }
    }

    private fun initView() {
        // This listener owns the bottom navigation insets; Compose pages own
        // their top and horizontal safe areas inside the pager.
        val baseLeft = binding.bnvMain.paddingLeft
        val baseRight = binding.bnvMain.paddingRight
        val baseBottom = binding.bnvMain.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bnvMain) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(
                left = baseLeft + bars.left,
                right = baseRight + bars.right,
                bottom = baseBottom + bars.bottom,
            )
            insets
        }

        binding.vpMain.adapter = MainViewPagerAdapter(this, fragmentList)

        binding.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bnvMain.menu.getItem(position).isChecked = true
                applyStatusBarForPage(position)
            }
        })

        binding.bnvMain.setOnItemSelectedListener {
            binding.vpMain.currentItem = it.order
            true
        }

        // The callback above only fires on a change, so the first page needs
        // its status bar treatment applied here.
        applyStatusBarForPage(binding.vpMain.currentItem)
    }

    /**
     * Page backgrounds draw behind the transparent status bar. Match its
     * glyphs to the home gradient or the settings surface.
     */
    private fun applyStatusBarForPage(position: Int) {
        if (position == HOME_PAGE) {
            applyLightStatusBarIcons(false)
        } else {
            applyLightStatusBarIcons(!isNightMode())
        }
    }

    private class MainViewPagerAdapter(
        activity: MainActivity,
        private val fragmentList: List<Fragment>
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = fragmentList.size

        override fun createFragment(position: Int): Fragment = fragmentList[position]
    }
}
