package com.symeonchen.wakeupscreen

import androidx.appcompat.app.AppCompatActivity

open class ScBaseActivity : AppCompatActivity() {
    companion object {
        var TAG: String = this::class.java.simpleName
    }
}