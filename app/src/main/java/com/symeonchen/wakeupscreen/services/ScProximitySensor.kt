package com.symeonchen.wakeupscreen.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.utils.DataInjection

class ScProximitySensor : SensorEventListener {

    private var isProximityNear = 1.0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val accuracy = event.values[0]
            LogUtils.d(accuracy)
            if (accuracy == 0f && isProximityNear != accuracy) {
                isProximityNear = accuracy
                DataInjection.statueOfProximity = isProximityNear.toInt()
            }
            if (accuracy > 0f && isProximityNear == 0f) {
                isProximityNear = accuracy
                DataInjection.statueOfProximity = isProximityNear.toInt()
            }
        }
    }
}