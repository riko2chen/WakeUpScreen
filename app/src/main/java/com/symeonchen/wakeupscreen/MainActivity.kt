package com.symeonchen.wakeupscreen

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.symeonchen.wakeupscreen.databinding.ActivityMainBinding
import com.symeonchen.wakeupscreen.pages.ScMainFragment
import com.symeonchen.wakeupscreen.pages.ScSettingFragment

/**
 * Created by SymeonChen on 2019-10-27.
 */
class MainActivity : ScBaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragmentList = listOf<Fragment>(ScMainFragment(), ScSettingFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        // Edge-to-edge is enforced on Android 15+, so keep the bottom nav above
        // the gesture / navigation bar instead of being hidden behind it.
        ViewCompat.setOnApplyWindowInsetsListener(binding.bnvMain) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }

        binding.vpMain.adapter = MainViewPagerAdapter(this, fragmentList)

        binding.vpMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bnvMain.menu.getItem(position).isChecked = true
            }
        })

        binding.bnvMain.setOnItemSelectedListener {
            binding.vpMain.currentItem = it.order
            true
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
