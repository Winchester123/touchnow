package com.zendrive.touchnow.managers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.zendrive.touchnow.receivers.ActivityRecognitionReceiver

private const val REQUEST_CODE = 42
private const val FREQ_IN_SECS = 30 * 1000L

// TODO: handle if the task fails
class ActivityRecognitionManager(private val context: Context) : OnCompleteListener<Void> {
    private var pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
            Intent(context, ActivityRecognitionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)
    var isStarted = false

    fun start() {
        ActivityRecognition.getClient(context)
                .requestActivityUpdates(FREQ_IN_SECS, pendingIntent)
                .addOnCompleteListener(this)
        isStarted = true

    }

    fun stop() {
        ActivityRecognition.getClient(context).removeActivityUpdates(pendingIntent)
    }

    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            Log.d("Tag", "Activity Task Successful")
        } else {
            Log.d("Tag", "Activity Task Failed")
        }
    }

    companion object {
        const val LAST_ACTIVITY_KEY = "first_activity"
        const val SECOND_LAST_ACTIVITY_KEY = "second_activity"
        const val THIRD_LAST_ACTIVITY_KEY = "third_activity"

        @SuppressLint("StaticFieldLeak")
        private var instance: ActivityRecognitionManager? = null
        fun getInstance(context: Context) = instance
                ?: ActivityRecognitionManager(context).let {
            instance = it
            it
        }
    }
}