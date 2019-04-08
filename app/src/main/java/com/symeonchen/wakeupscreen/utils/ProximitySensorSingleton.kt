package com.symeonchen.wakeupscreen.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.symeonchen.wakeupscreen.services.SCProximitySensor

object ProximitySensorSingleton {

    private var proximityListener = SCProximitySensor()
    private var proximitySensor: Sensor? = null
    private var sensorManager: SensorManager? = null


    fun initData(context: Context) {
        sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun registerListener() {

        if (isRegistered()) {
            sensorManager?.unregisterListener(proximityListener)
        }
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager?.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unRegisterListener() {
        sensorManager?.unregisterListener(proximityListener)
        proximitySensor = null
    }

    fun isRegistered(): Boolean {
        return proximitySensor != null
    }


}