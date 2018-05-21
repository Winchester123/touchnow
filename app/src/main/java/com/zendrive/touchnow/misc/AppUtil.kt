package com.zendrive.touchnow.misc

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.location.DetectedActivity
import com.zendrive.touchnow.managers.ActivityRecognitionManager
import com.zendrive.touchnow.managers.HeadPhoneFenceManager
import com.zendrive.touchnow.managers.ProximitySensorManager
import com.zendrive.touchnow.ui.MainActivity
import java.util.concurrent.TimeUnit

object AppUtil {
    private const val JOB_PERIOD_IN_SECONDS = 5L
    private const val JOB_ID = 42
    private const val HEADPHONE_FENCE_PLUGIN_CALLBACK_TIME_KEY = "fence_plugin_key"
    private const val MAX_HEADPHONE_CALLBACK_DELAY = 2 * 60 * 1000
    private var latestActivity = DetectedActivity.STILL

    fun scheduleJob(context: Context) {
        val sensorCheckJob = JobInfo.Builder(JOB_ID,
                ComponentName(context.applicationContext, TouchAppService::class.java))
                .setMinimumLatency(TimeUnit.SECONDS.toMillis(JOB_PERIOD_IN_SECONDS))
                .setOverrideDeadline(TimeUnit.SECONDS.toMillis(JOB_PERIOD_IN_SECONDS)).build()
        val jobScheduler = context.applicationContext
                .getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(sensorCheckJob)
    }

    fun handleActivityPoint(context: Context, activity: Int) {
        latestActivity = activity
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MainActivity
                        .KEEP_ACTIVITY_ON_KEY, false)) {
            if (!MainActivity.isActivityVisible()) {
                // restart the activity if it was killed
                context.startActivity(Intent(context, MainActivity::class.java))
            }
            return
        }

        if (!MainActivity.isActivityVisible() && shouldStartActivity(context)) {
            context.startActivity(Intent(context, MainActivity::class.java))
        } else if (shouldStopActivity(context)) {
            context.sendBroadcast(Intent(MainActivity.STOP_ACTIVITY_ACTION))
        }
    }

    fun getLatestActivityName(): String {
        when (latestActivity) {
            DetectedActivity.STILL -> return "STILL"
            DetectedActivity.RUNNING -> return "RUNNING"
            DetectedActivity.ON_FOOT -> return "ON_FOOT"
            DetectedActivity.IN_VEHICLE -> return "IN_VEHICLE"
            DetectedActivity.WALKING -> return "WALKING"
            DetectedActivity.UNKNOWN -> return "UNKNOWN"
            DetectedActivity.TILTING -> return "TILTING"
            DetectedActivity.ON_BICYCLE -> return "ON_BICYCLING"
            -1 -> return "PROXIMITY"
        }
        return "NO IDEA!!"
    }

    fun handleFenceCallback(fenceState: FenceState, context: Context) {
        if (fenceState.fenceKey == HeadPhoneFenceManager.HEADPHONE_PLUGIN_FENCE_KEY) {
            handleHeadPhonePluginCallback(context)
        } else if (fenceState.fenceKey == HeadPhoneFenceManager.HEADPHONE_UNPLUG_FENCE_KEY) {
            // stop the activity no matter what, if it is shown
            context.sendBroadcast(Intent(MainActivity.STOP_ACTIVITY_ACTION))
        }
    }

    private fun handleHeadPhonePluginCallback(context: Context) {
        val currentTime = System.currentTimeMillis()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (checkNonStillActivityPoints(context) && !MainActivity.isActivityVisible() &&
                preferences.getFloat(ProximitySensorManager.LAST_PROXIMITY_VALUE_KEY, -1.0f)
                < ProximitySensorManager.MIN_CLOSE_PROXIMITY) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
        preferences.edit {
            putLong(HEADPHONE_FENCE_PLUGIN_CALLBACK_TIME_KEY, currentTime)
        }
    }

    private fun checkNonStillActivityPoints(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getInt(ActivityRecognitionManager.LAST_ACTIVITY_KEY, -1) !=
                DetectedActivity.STILL ||
                sharedPreferences.getInt(ActivityRecognitionManager.SECOND_LAST_ACTIVITY_KEY,
                        -1) != DetectedActivity.STILL ||
                sharedPreferences.getInt(ActivityRecognitionManager.THIRD_LAST_ACTIVITY_KEY, -1)
                != DetectedActivity.STILL) {
            return true
        }
        return false
    }

    private fun shouldStartActivity(context: Context): Boolean {
        // show if last two points are not still and we got the headphone callback within last 2
        // mins
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val headPhoneTime = preferences.getLong(HEADPHONE_FENCE_PLUGIN_CALLBACK_TIME_KEY, -1)
        if (System.currentTimeMillis() - headPhoneTime < MAX_HEADPHONE_CALLBACK_DELAY) {
            if (preferences.getInt(ActivityRecognitionManager.LAST_ACTIVITY_KEY, -1)
                    != DetectedActivity.STILL
                    && preferences.getInt(ActivityRecognitionManager.SECOND_LAST_ACTIVITY_KEY,
                            -1) != DetectedActivity.STILL) {
                return true
            }
        }
        return false
    }

    private fun shouldStopActivity(context: Context): Boolean {
        // if last known proximity value is more than 10 and have seen three still points, stop
        // the activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences.getFloat(ProximitySensorManager.LAST_PROXIMITY_VALUE_KEY, -1.0f)
                > ProximitySensorManager.MIN_CLOSE_PROXIMITY &&
                preferences.getInt(ActivityRecognitionManager.LAST_ACTIVITY_KEY, -1)
                == DetectedActivity.STILL &&
                preferences.getInt(ActivityRecognitionManager.SECOND_LAST_ACTIVITY_KEY, -1)
                == DetectedActivity.STILL &&
                preferences.getInt(ActivityRecognitionManager.THIRD_LAST_ACTIVITY_KEY, -1)
                == DetectedActivity.STILL) {
            return true
        }
        return false
    }
}