package com.symeonchen.wakeupscreen

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jaeger.library.StatusBarUtil
import com.symeonchen.wakeupscreen.pages.ScMainFragment
import com.symeonchen.wakeupscreen.pages.ScSettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ScBaseActivity(), ViewPager.OnPageChangeListener {

    private var fragmentList = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorPrimary))
        initView()
        setListener()
    }

    private fun initView() {
        fragmentList.add(ScMainFragment())
        fragmentList.add(ScSettingFragment())
    }

    private fun setListener() {
        vp_main.adapter = MainViewPagerAdapter(supportFragmentManager, fragmentList)
        vp_main.addOnPageChangeListener(this)
        bnv_main.setOnNavigationItemSelectedListener {
            vp_main.currentItem = it.order
            return@setOnNavigationItemSelectedListener true
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        bnv_main.menu.getItem(position).isChecked = true
    }

    inner class MainViewPagerAdapter(fm: FragmentManager, fragmentList: ArrayList<Fragment>) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var fragmentList = ArrayList<Fragment>()

        init {
            this.fragmentList = fragmentList
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }
    }
}
