package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * A phone lying screen-down on the table is a deliberate "not now": waking the
 * display would light the table. The posture itself comes from the stored
 * accelerometer state, the same arrangement pocket mode has with the proximity
 * sensor.
 */
class FaceDownCondition : LimitedCondition.NoParamCondition() {

    override val key = BlockReason.FACE_DOWN

    override fun provideResult(): ConditionState {
        if (DataInjection.switchOfFaceDown && DataInjection.statusOfFaceDown) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean = DataInjection.switchOfFaceDown

    override fun wouldBlockNow(application: Application?): Boolean =
        DataInjection.switchOfFaceDown && DataInjection.statusOfFaceDown
}
