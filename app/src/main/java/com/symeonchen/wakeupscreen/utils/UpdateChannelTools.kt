package com.symeonchen.wakeupscreen.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri


/**
 * Opens an external app or web page so the user can check for / install
 * updates. Everything here is an outbound [Intent] — the app never performs
 * any network request itself, so no INTERNET permission is required.
 */
object UpdateChannelTools {

    private const val FDROID_PACKAGE = "org.fdroid.fdroid"
    private const val PLAY_STORE_PACKAGE = "com.android.vending"
    private const val GITHUB_RELEASES_URL =
        "https://github.com/riko2chen/WakeUpScreen/releases/latest"

    /** Open the app's Google Play listing, falling back to the browser. */
    fun openPlayStore(context: Context?) {
        context ?: return
        val webUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).setPackage(PLAY_STORE_PACKAGE)
            )
        } catch (_: ActivityNotFoundException) {
            openInBrowser(context, webUrl)
        }
    }

    /** Open the app's F-Droid page, preferring the F-Droid client app. */
    fun openFDroid(context: Context?) {
        context ?: return
        val webUrl = "https://f-droid.org/packages/${context.packageName}/"
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).setPackage(FDROID_PACKAGE)
            )
        } catch (_: ActivityNotFoundException) {
            openInBrowser(context, webUrl)
        }
    }

    /** Open the GitHub "latest release" page in the browser. */
    fun openGitHubReleases(context: Context?) {
        openInBrowser(context, GITHUB_RELEASES_URL)
    }

    private fun openInBrowser(context: Context?, url: String) {
        context ?: return
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
