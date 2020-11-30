package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.databinding.ActivityAppInfoBinding

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AppInfoPageActivity : ScBaseActivity() {

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_app_info)
        setListener()
    }

    private fun setListener() {
        binding.ivBack.setOnClickListener { finish() }
    }
}
