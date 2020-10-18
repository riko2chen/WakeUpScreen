package com.symeonchen.wakeupscreen.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.LogUtils
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory


/**
 * Created by SymeonChen on 2020/10/17.
 */
class PlayStoreTools private constructor() {


    private var context: Context? = null
    private var manager: ReviewManager? = null
    private var result: ReviewInfo? = null
    private var requesting: Boolean = false

    companion object {
        val TAG = this::class.java.simpleName

        // 120 * 24 * 60 * 60 * 1000 = 10368000000
        const val IN_APP_REVIEW_FREEZE_TIME: Long = 10368000000

        @Volatile
        private var instance: PlayStoreTools? = null

        fun newInstance(context: Context?): PlayStoreTools? {
            if (instance == null) {
                synchronized(PlayStoreTools::class.java) {
                    instance = PlayStoreTools()
                    instance?.context = context
                    return instance
                }
            }
            return instance
        }
    }


    fun prepare() {
        if (requesting) {
            LogUtils.w(TAG, "Review flow is in processing. ")
            return
        }
        requesting = true
        val currentTime: Long = System.currentTimeMillis()
        if (currentTime - DataInjection.lastInAppReviewTime.toLong() < IN_APP_REVIEW_FREEZE_TIME) {
            LogUtils.w(TAG, "Not exceeding the prescribed time limit. ")
            return
        }
        manager = ReviewManagerFactory.create(context)
        val request = manager?.requestReviewFlow()
        request?.addOnCompleteListener { callback ->
            requesting = false
            if (callback.isSuccessful) {
                this.result = callback.result
            }
        }
    }

    fun openPlayStore(activity: Activity?) {
        if (activity == null) {
            openPlayStoreWithUrl()
            return
        }
        val localResult = this.result
        if (localResult != null) {
            DataInjection.lastInAppReviewTime = System.currentTimeMillis().toString()
            manager?.launchReviewFlow(activity, localResult)
            this.result = null
        } else {
            openPlayStoreWithUrl()
        }
    }

    private fun openPlayStoreWithUrl() {
        val uri = "https://play.google.com/store/apps/details?id=com.symeonchen.wakeupscreen"
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uri)
                setPackage("com.android.vending")
            }
            context?.startActivity(intent)
        } catch (ignore: ActivityNotFoundException) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(uri)
            context?.startActivity(i)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}