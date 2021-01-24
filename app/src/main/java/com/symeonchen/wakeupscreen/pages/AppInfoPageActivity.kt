package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import kotlinx.android.synthetic.main.activity_app_info.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AppInfoPageActivity : ScBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)
        setListener()
    }

    private fun setListener() {
        iv_back.setOnClickListener { finish() }
    }
}
