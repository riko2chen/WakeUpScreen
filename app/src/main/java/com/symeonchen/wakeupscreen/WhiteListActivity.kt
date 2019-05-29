package com.symeonchen.wakeupscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_debug_page.*

class WhiteListActivity : ScBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_white_list)
        setListener()
    }

    companion object {
        fun actionStart(context: Context?) {
            context ?: return
            context.startActivity(Intent(context, WhiteListActivity::class.java))
        }
    }


    private fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    private fun updateView() {
    }


}
