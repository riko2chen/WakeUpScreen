package com.symeonchen.wakeupscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by SymeonChen on 2019-10-27.
 */
open class ScBaseFragment : Fragment() {
    companion object {
        var TAG: String = this::class.java.simpleName
    }

    protected var mCompositeDisposable: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCompositeDisposable = CompositeDisposable()
    }

    override fun onDestroy() {
        mCompositeDisposable?.clear()
        super.onDestroy()
    }
}