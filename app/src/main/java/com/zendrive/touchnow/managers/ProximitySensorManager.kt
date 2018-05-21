package com.zendrive.touchnow.managers

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit

class ProximitySensorManager(private val context: Context) : SensorEventListener {
    private val period = 1000
    private lateinit var sensorManager: SensorManager

    var isStarted = false

    fun start(context: Context) {
        isStarted = true
        sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                period)
        Log.d("Tag", "started proximity values")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Tag", "Accuracy changed")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            Log.d("Tag", "Proximity Value: ${event.values[0]}")
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putFloat(LAST_PROXIMITY_VALUE_KEY, event.values[0])
            }
        }
    }

    companion object {
        const val LAST_PROXIMITY_VALUE_KEY = "last_proximity_value"
        const val MIN_CLOSE_PROXIMITY = 10.0

        @SuppressLint("StaticFieldLeak")
        private var instance: ProximitySensorManager? = null
        fun getProximityManager(context: Context): ProximitySensorManager = instance
                ?: ProximitySensorManager(context).let {
                    instance = it
                    it
                }
    }
}