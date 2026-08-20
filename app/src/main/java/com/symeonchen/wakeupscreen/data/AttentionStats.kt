package com.symeonchen.wakeupscreen.data

import org.json.JSONObject

/**
 * What one app did with its wakes over the window: how often it lit the screen,
 * and how often the user actually unlocked shortly after.
 */
data class AppAttention(
    val packageName: String,
    val wakes: Int,
    val followed: Int,
) {
    val neverFollowed: Boolean get() = wakes > 0 && followed == 0
}

/**
 * The arithmetic of the attention statistics, kept free of Android and disk.
 *
 * The store is a JSON object of per-package day buckets:
 * `{"com.app": {"19700": [wakes, followed], ...}, ...}` with the day as epoch
 * days. Buckets outside the retention window are pruned on every write, so the
 * store cannot grow beyond (apps × retention days).
 *
 * Everything is counted locally from two facts the app already observes — "the
 * screen was woken for this package" and "the device was unlocked" — never from
 * notification content.
 */
object AttentionStats {

    const val RETENTION_DAYS = 30

    /** An unlock this soon after a wake counts as the wake being followed up. */
    const val FOLLOW_WINDOW_MS = 30_000L

    /** Suggest blacklisting only past this many ignored wakes; fewer is noise. */
    const val SUGGEST_MIN_WAKES = 10

    fun recordWake(store: JSONObject, packageName: String, epochDay: Long): JSONObject =
        record(store, packageName, epochDay, wakeDelta = 1, followDelta = 0)

    /**
     * Follow-ups are attributed by the caller, which knows which wakes fell
     * inside [FOLLOW_WINDOW_MS]; this only does the bookkeeping.
     */
    fun recordFollow(store: JSONObject, packageName: String, epochDay: Long): JSONObject =
        record(store, packageName, epochDay, wakeDelta = 0, followDelta = 1)

    private fun record(
        store: JSONObject,
        packageName: String,
        epochDay: Long,
        wakeDelta: Int,
        followDelta: Int,
    ): JSONObject {
        val pruned = prune(store, epochDay - RETENTION_DAYS + 1)
        val app = pruned.optJSONObject(packageName) ?: JSONObject()
        val bucket = app.optJSONArray(epochDay.toString())
        val wakes = (bucket?.optInt(0) ?: 0) + wakeDelta
        val followed = (bucket?.optInt(1) ?: 0) + followDelta
        app.put(epochDay.toString(), org.json.JSONArray().put(wakes).put(followed))
        pruned.put(packageName, app)
        return pruned
    }

    /** Drops every bucket before [firstKeptDay], and apps left with none. */
    fun prune(store: JSONObject, firstKeptDay: Long): JSONObject {
        val result = JSONObject()
        for (pkg in store.keys()) {
            val app = store.optJSONObject(pkg) ?: continue
            val kept = JSONObject()
            for (day in app.keys()) {
                val dayNumber = day.toLongOrNull() ?: continue
                if (dayNumber >= firstKeptDay) {
                    kept.put(day, app.optJSONArray(day))
                }
            }
            if (kept.length() > 0) {
                result.put(pkg, kept)
            }
        }
        return result
    }

    /** Totals per app over the window, most wakes first. */
    fun summarize(store: JSONObject, firstKeptDay: Long): List<AppAttention> {
        val result = mutableListOf<AppAttention>()
        for (pkg in store.keys()) {
            val app = store.optJSONObject(pkg) ?: continue
            var wakes = 0
            var followed = 0
            for (day in app.keys()) {
                val dayNumber = day.toLongOrNull() ?: continue
                if (dayNumber < firstKeptDay) continue
                val bucket = app.optJSONArray(day) ?: continue
                wakes += bucket.optInt(0)
                followed += bucket.optInt(1)
            }
            if (wakes > 0 || followed > 0) {
                result.add(AppAttention(pkg, wakes, followed))
            }
        }
        return result.sortedByDescending { it.wakes }
    }

    /**
     * The apps worth suggesting for the blacklist: enough wakes to be a real
     * pattern, and not one of them followed up.
     */
    fun suggestions(summary: List<AppAttention>): List<AppAttention> =
        summary.filter { it.neverFollowed && it.wakes >= SUGGEST_MIN_WAKES }
}
