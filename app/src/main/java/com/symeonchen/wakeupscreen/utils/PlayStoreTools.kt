package com.symeonchen.wakeupscreen.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri


/**
 * Created by SymeonChen on 2020/10/17.
 */
class PlayStoreTools {

    companion object {

        fun openPlayStoreWithUrl(context: Context?) {
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

}