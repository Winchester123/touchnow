package com.zendrive.touchnow.managers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.fence.HeadphoneFence
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.zendrive.touchnow.receivers.HeadPhoneFenceReceiver

private const val REQUEST_CODE = 44

// TODO: handle if the task fails
class HeadPhoneFenceManager(private val context: Context): OnCompleteListener<Void> {

    private val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(context,
            HeadPhoneFenceReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
    var isStarted = false

    fun start() {
        val headphoneFencePluggedIn = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)
        val headphoneFenceUnplugged = HeadphoneFence.during(HeadphoneState.UNPLUGGED)
        val fencingRequest = FenceUpdateRequest.Builder()
                .addFence(HEADPHONE_PLUGIN_FENCE_KEY, headphoneFencePluggedIn, pendingIntent)
                .addFence(HEADPHONE_UNPLUG_FENCE_KEY, headphoneFenceUnplugged, pendingIntent)
                .build()

        Awareness.getFenceClient(context)
                .updateFences(fencingRequest)
                .addOnCompleteListener(this)
        isStarted = true
    }

    fun stopMonitoring() {
        Awareness.getFenceClient(context)
                .updateFences(FenceUpdateRequest.Builder()
                        .removeFence(pendingIntent).build())
                .addOnCompleteListener(this)
        isStarted = false
    }

    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            Log.d("Tag", "Fence Task successful")
        } else {
            Log.d("Tag", "Fence Task Failed")
        }
    }

    companion object {
        const val HEADPHONE_PLUGIN_FENCE_KEY = "headphone_plugin"
        const val HEADPHONE_UNPLUG_FENCE_KEY = "headphone_unplug"

        @SuppressLint("StaticFieldLeak")
        private var headPhoneFenceManager: HeadPhoneFenceManager? = null
        fun getInstance(context: Context) = headPhoneFenceManager
                ?: HeadPhoneFenceManager(context).let {
            headPhoneFenceManager = it
            it
        }
    }
}