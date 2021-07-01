package com.symeonchen.wakeupscreen.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding

/**
 * Created by SymeonChen on 2021/06/07.
 */

fun <T : ViewBinding> Activity.inflate(inflater: (LayoutInflater) -> T) = lazy {
    inflater(layoutInflater)
}

fun <T : ViewBinding> Fragment.inflate(
    layoutInflater: LayoutInflater,
    parent: ViewGroup?,
    attachToParent: Boolean,
    inflater: (LayoutInflater, ViewGroup?, Boolean) -> T
) = lazy {
    var binding: T? = inflater(layoutInflater, parent, attachToParent)
    this.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreateView() {
            binding = inflater(layoutInflater, parent, attachToParent)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyView() {
            this@inflate.viewLifecycleOwner.lifecycle.removeObserver(this)
            binding = null
        }
    })
    binding!!
}

fun <T : ViewBinding> ViewGroup.viewInflateMerge(
    inflater: (LayoutInflater, ViewGroup) -> T
) = lazy {
    inflater(LayoutInflater.from(context), this)
}


fun <T : ViewBinding> ViewGroup.viewInflate(
    inflater: (LayoutInflater, ViewGroup, Boolean) -> T
) = lazy {
    inflater(LayoutInflater.from(context), this, true)
}

fun <T : ViewBinding> ViewGroup.viewInflate(
    attachToParent: Boolean,
    inflater: (LayoutInflater, ViewGroup, Boolean) -> T
) = lazy {
    inflater(LayoutInflater.from(context), this, attachToParent)
}

