package com.symeonchen.wakeupscreen

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

open class ScBaseActivity : AppCompatActivity() {
    var mCompositeDisposable = CompositeDisposable()

    companion object {
        var TAG: String = this::class.java.simpleName
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }
}