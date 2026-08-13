package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * The decision alone, kept apart from the Android battery APIs so it can be
 * pinned down in a plain unit test.
 */
object BatteryLevelPolicy {

    /**
     * Blocks only while the battery is actually draining. The gate exists to
     * protect a low battery; once the charger is in, waking the screen no
     * longer costs anything worth guarding, and a phone sitting at 15% on the
     * bedside charger should behave normally.
     *
     * An unreadable level (null) passes: a broken reading must not silence
     * every notification.
     */
    fun shouldBlock(enabled: Boolean, levelPercent: Int?, isCharging: Boolean, thresholdPercent: Int): Boolean {
        if (!enabled || isCharging) return false
        levelPercent ?: return false
        return levelPercent < thresholdPercent
    }
}

class BatteryLevelCondition : LimitedCondition.AppContextCondition() {

    override val key = BlockReason.BATTERY_LEVEL

    override fun provideResult(application: Application?): ConditionState {
        return if (wouldBlock(application) == true) ConditionState.BLOCK else ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean = DataInjection.batteryLevelSwitch

    override fun wouldBlockNow(application: Application?): Boolean? {
        if (!DataInjection.batteryLevelSwitch) {
            return false
        }
        application ?: return null
        return wouldBlock(application)
    }

    private fun wouldBlock(application: Application?): Boolean? {
        val status = application?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return null
        val level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else null
        val chargeStatus = status.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = chargeStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            chargeStatus == BatteryManager.BATTERY_STATUS_FULL
        return BatteryLevelPolicy.shouldBlock(
            enabled = DataInjection.batteryLevelSwitch,
            levelPercent = percent,
            isCharging = isCharging,
            thresholdPercent = DataInjection.batteryLevelThreshold,
        )
    }
}
