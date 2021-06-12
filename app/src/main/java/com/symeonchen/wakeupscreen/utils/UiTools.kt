package com.symeonchen.wakeupscreen.utils

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.blankj.utilcode.util.ActivityUtils
import com.symeonchen.uicomponent.views.SCLoadingItem


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
        val rootView =
            if (container == null) {
                val currActivity: Activity? = ActivityUtils.getActivityByContext(context)
                currActivity?.findViewById(android.R.id.content)
            } else {
                container
            }
        scLoadingItem?.showLoading(rootView)

    }

    fun hideLoading() {
        scLoadingItem?.hideLoading()
    }


}