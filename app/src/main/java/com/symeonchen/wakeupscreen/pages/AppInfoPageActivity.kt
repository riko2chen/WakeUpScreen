package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.databinding.ActivityAppInfoBinding

/**
 * Created by SymeonChen on 2019-10-27.
 */
class AppInfoPageActivity : ScBaseActivity() {

    private val binding by lazy { ActivityAppInfoBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListener()
    }

    private fun setListener() {
        binding.ivBack.setOnClickListener { finish() }
    }
}
