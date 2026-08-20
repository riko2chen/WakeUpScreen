package com.symeonchen.wakeupscreen.data

import androidx.annotation.StringRes
import com.symeonchen.wakeupscreen.R

/**
 * One released version as the in-app What's New sheet presents it: a handful
 * of short bullets, not the full changelog. The complete history stays in
 * docs/CHANGELOG*.md, which the sheet links to.
 */
data class ChangelogVersion(
    val versionCode: Int,
    val versionName: String,
    @StringRes val bulletRes: List<Int>,
)

/**
 * A settings row introduced by an update. Its dot shows until the row is
 * visited, judged against the version the user updated from — so it survives
 * dismissing the What's New sheet, and never shows on a fresh install.
 */
enum class FeatureBadge(val key: String, val sinceVersionCode: Int) {
    ATTENTION_STATS("attention_stats", 40000),
    BACKUP_EXPORT("backup_export", 40000),
    BACKUP_IMPORT("backup_import", 40000),
    BATTERY_LEVEL("battery_level", 40000),
    FACE_DOWN("face_down", 40000),
    NIGHT_GLOW("night_glow", 40000),
    SLEEP_WEEKDAY("sleep_weekday", 40000),
}

object ChangelogCatalog {

    /**
     * The last release that shipped without the tracker. An update whose stored
     * last-seen version is absent can only come from here or earlier, so the
     * sheet shows everything after this rather than guessing.
     */
    const val PRE_TRACKER_VERSION_CODE = 31100

    /**
     * Newest first, the order the sheet lists them. 3.2.0 never shipped under
     * its own versionCode (its work went out inside 4.0.0), but it keeps its
     * own section here so an updater still sees those changes listed under the
     * version the changelog files use; 32000 merely slots it between 3.1.1 and
     * 4.0.0 for the "newer than last seen" filter.
     */
    val versions = listOf(
        ChangelogVersion(
            41000, "4.1.0",
            listOf(
                R.string.changelog_v41000_1,
                R.string.changelog_v41000_2,
                R.string.changelog_v41000_3,
                R.string.changelog_v41000_4,
            ),
        ),
        ChangelogVersion(
            40000, "4.0.0",
            listOf(
                R.string.changelog_v40000_1,
                R.string.changelog_v40000_2,
                R.string.changelog_v40000_3,
                R.string.changelog_v40000_4,
                R.string.changelog_v40000_5,
                R.string.changelog_v40000_6,
                R.string.changelog_v40000_7,
            ),
        ),
        ChangelogVersion(
            32000, "3.2.0",
            listOf(
                R.string.changelog_v32000_1,
                R.string.changelog_v32000_2,
                R.string.changelog_v32000_3,
                R.string.changelog_v32000_4,
            ),
        ),
        ChangelogVersion(
            31100, "3.1.1",
            listOf(
                R.string.changelog_v31100_1,
                R.string.changelog_v31100_2,
            ),
        ),
        ChangelogVersion(
            31000, "3.1.0",
            listOf(
                R.string.changelog_v31000_1,
                R.string.changelog_v31000_2,
            ),
        ),
        ChangelogVersion(
            30600, "3.0.6",
            listOf(
                R.string.changelog_v30600_1,
            ),
        ),
        ChangelogVersion(
            30500, "3.0.5",
            listOf(
                R.string.changelog_v30500_1,
                R.string.changelog_v30500_2,
            ),
        ),
    )

    fun versionsAfter(versionCode: Int): List<ChangelogVersion> =
        versions.filter { it.versionCode > versionCode }
}
