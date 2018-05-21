package com.zendrive.touchnow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.awareness.fence.FenceState
import com.zendrive.touchnow.misc.AppUtil

class HeadPhoneFenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fenceState = FenceState.extract(intent)
        if (fenceState.currentState == FenceState.TRUE) {
            AppUtil.handleFenceCallback(fenceState, context)
        }
    }
}