package com.symeonchen.wakeupscreen.utils

import android.content.Context
import com.symeonchen.wakeupscreen.BuildConfig
import com.symeonchen.wakeupscreen.data.ChangelogCatalog
import com.symeonchen.wakeupscreen.data.ChangelogVersion
import com.symeonchen.wakeupscreen.data.FeatureBadge

/**
 * Decides, once per update, whether the What's New sheet appears and which
 * NEW badges are still owed. All state lives in [DataInjection]; none of it
 * travels in the settings backup, because "has read the 4.0.0 notes" is a fact
 * about this device's user, not a preference worth restoring elsewhere.
 */
object WhatsNewTracker {

    /**
     * The versions to present on this launch, newest first; empty when there
     * is nothing to show. Consuming: the last-seen version is advanced
     * immediately, so a configuration change (or the next launch) shows
     * nothing again. A user who swipes the sheet away half-read can reopen it
     * from Settings → About at any time.
     */
    fun consumePendingWhatsNew(context: Context): List<ChangelogVersion> {
        val current = BuildConfig.VERSION_CODE
        val lastSeen = DataInjection.lastSeenVersionCode
        if (lastSeen == current) {
            return emptyList()
        }
        if (lastSeen > current) {
            // A sideloaded downgrade. Resync quietly; the next real update
            // will compare against this build.
            DataInjection.lastSeenVersionCode = current
            return emptyList()
        }
        if (lastSeen == 0 && isFreshInstall(context)) {
            // A new user has no "update" to read about; showing release notes
            // for versions they never ran would only be noise.
            DataInjection.updatedFromVersionCode = current
            DataInjection.lastSeenVersionCode = current
            return emptyList()
        }
        // lastSeen == 0 on an updated install means the previous build
        // predates the tracker, so it can only be PRE_TRACKER or older.
        val updatedFrom = if (lastSeen == 0) ChangelogCatalog.PRE_TRACKER_VERSION_CODE else lastSeen
        DataInjection.updatedFromVersionCode = updatedFrom
        DataInjection.lastSeenVersionCode = current
        return ChangelogCatalog.versionsAfter(updatedFrom)
    }

    /**
     * Errs toward "fresh" because the failure modes differ: skipping the sheet
     * for one updater is a shrug, greeting every new user with old release
     * notes is a bad first impression.
     */
    private fun isFreshInstall(context: Context): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.firstInstallTime == info.lastUpdateTime
        } catch (_: Exception) {
            true
        }
    }

    fun isBadgeVisible(badge: FeatureBadge): Boolean {
        return badge.sinceVersionCode > DataInjection.updatedFromVersionCode &&
            !clickedBadgeKeys().contains(badge.key)
    }

    fun acknowledgeBadge(badge: FeatureBadge) {
        val clicked = clickedBadgeKeys()
        if (clicked.contains(badge.key)) {
            return
        }
        DataInjection.clickedFeatureBadges = (clicked + badge.key).joinToString(",")
    }

    private fun clickedBadgeKeys(): Set<String> =
        DataInjection.clickedFeatureBadges.split(",").filter { it.isNotEmpty() }.toSet()
}
