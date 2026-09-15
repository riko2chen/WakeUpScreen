package com.symeonchen.wakeupscreen.states

import android.content.Context
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Keeps the two posture listeners attached for as long as their switches are
 * on — not only while the settings screen is in front.
 *
 * Pocket mode used to register the proximity sensor from the home fragment
 * and the settings toggle alone. After hours in a pocket the process is
 * typically gone; a streaming app then posts a notification, the listener
 * service comes back, and the last stored proximity value is still "far"
 * from before the phone went in. The screen wakes against fabric, which is
 * how random lock-screen input happens. Syncing from the application and
 * the notification listener closes that gap.
 */
object PostureSensorCoordinator {

    fun sync(context: Context?) {
        if (DataInjection.switchOfProximity) {
            if (!ProximitySensorState.isRegistered()) {
                ProximitySensorState.registerListener(context)
            }
        } else if (ProximitySensorState.isRegistered()) {
            ProximitySensorState.unRegisterListener(context)
        }

        if (DataInjection.switchOfFaceDown) {
            if (!FaceDownSensorState.isRegistered()) {
                FaceDownSensorState.registerListener(context)
            }
        } else if (FaceDownSensorState.isRegistered()) {
            FaceDownSensorState.unRegisterListener(context)
        }
    }
}
