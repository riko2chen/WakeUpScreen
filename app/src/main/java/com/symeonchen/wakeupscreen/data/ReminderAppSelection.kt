package com.symeonchen.wakeupscreen.data

/** An additional restriction; never an override of the initial-wake filter. */
data class ReminderAppSelection(val custom: Boolean = false, val packages: Set<String> = emptySet()) {
    fun allows(packageName: String, initialWakeAllowed: Boolean): Boolean =
        initialWakeAllowed && (!custom || packageName in packages)

    fun save() {
        ScStore.putString(ScConstant.REMINDER_APP_PACKAGES, encode(packages))
        ScStore.putBoolean(ScConstant.REMINDER_CUSTOM_APPS, custom)
    }

    companion object {
        fun load() = ReminderAppSelection(
            ScStore.getBoolean(ScConstant.REMINDER_CUSTOM_APPS, false),
            decode(ScStore.getString(ScConstant.REMINDER_APP_PACKAGES, "").orEmpty()),
        )
        fun decode(raw: String): Set<String> = raw.split(',').map { it.trim() }
            .filter { it.isNotEmpty() }.toSet()
        fun encode(packages: Set<String>): String = packages.sorted().joinToString(",")
    }
}
