package com.zendrive.touchnow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.zendrive.touchnow.managers.ActivityRecognitionManager
import com.zendrive.touchnow.misc.AppUtil

private const val MIN_CONFIDENCE_TO_SAVE = 50

class ActivityRecognitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val res = ActivityRecognitionResult.extractResult(intent)
        Log.d("Tag", "Detected Activity: $res.mostProbableActivity.toString()")
        when (res.mostProbableActivity.type) {
            DetectedActivity.WALKING, DetectedActivity.ON_FOOT,
            DetectedActivity.RUNNING, DetectedActivity.STILL, DetectedActivity.IN_VEHICLE -> {
                saveActivityPoint(res, context)
                AppUtil.handleActivityPoint(context, res.mostProbableActivity.type)
            }
        }
    }

    private fun saveActivityPoint(result: ActivityRecognitionResult?, context: Context) {
        result ?: return
        if (result.mostProbableActivity.confidence > MIN_CONFIDENCE_TO_SAVE) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)

            preferences.edit {
                putInt(ActivityRecognitionManager.THIRD_LAST_ACTIVITY_KEY,
                        preferences.getInt(ActivityRecognitionManager.SECOND_LAST_ACTIVITY_KEY,
                                -1))
                putInt(ActivityRecognitionManager.SECOND_LAST_ACTIVITY_KEY,
                        preferences.getInt(ActivityRecognitionManager.LAST_ACTIVITY_KEY,
                                -1))
                putInt(ActivityRecognitionManager.LAST_ACTIVITY_KEY,
                        result.mostProbableActivity.type)
            }
        }
    }
}