package com.symeonchen.wakeupscreen.services.reminder

/** Extra restrictions on a reminder that has already passed the wake rules. */
internal object ReminderVibrationPolicy {
    fun allows(enabled: Boolean, dndOff: Boolean, ringerAllowsVibration: Boolean, hasVibrator: Boolean): Boolean =
        enabled && dndOff && ringerAllowsVibration && hasVibrator
}
