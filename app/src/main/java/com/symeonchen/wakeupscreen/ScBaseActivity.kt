package com.symeonchen.wakeupscreen

import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.symeonchen.wakeupscreen.base.ITagProvider

/**
 * Created by SymeonChen on 2019-10-27.
 */
open class ScBaseActivity : AppCompatActivity(), ITagProvider {

    /**
     * @see ITagProvider
     */
    override fun getDefaultTag(): String {
        return this::class.java.simpleName
    }

}