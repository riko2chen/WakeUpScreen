package com.symeonchen.uicomponent.views

import android.content.Context
import android.view.ViewGroup


/**
 * Created by SymeonChen on 2020/5/23.
 */
class UiTools private constructor() {
    companion object {
        val instance: UiTools by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UiTools()
        }
    }

    private var scLoadingItem: SCLoadingItem? = null

    @Synchronized
    fun showLoading(context: Context, container: ViewGroup? = null) {
        scLoadingItem = SCLoadingItem(context)
        scLoadingItem?.showLoading(container)
    }

    fun hideLoading() {
        scLoadingItem?.hideLoading()
    }


}