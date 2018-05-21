package com.zendrive.touchnow.misc

import android.app.job.JobParameters
import android.app.job.JobService
import com.zendrive.touchnow.managers.ActivityRecognitionManager
import com.zendrive.touchnow.managers.HeadPhoneFenceManager
import com.zendrive.touchnow.managers.ProximitySensorManager
import com.zendrive.touchnow.misc.AppUtil

class TouchAppService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val proximitySensorManager = ProximitySensorManager.getProximityManager(this)
        if (!proximitySensorManager.isStarted) {
            proximitySensorManager.start(this)
        }

        val recognitionManager = ActivityRecognitionManager.getInstance(this)
        if (!recognitionManager.isStarted) {
            recognitionManager.start()
        }

        val headPhoneFenceManager = HeadPhoneFenceManager.getInstance(this)
        if (!headPhoneFenceManager.isStarted) {
            headPhoneFenceManager.start()
        }
        AppUtil.scheduleJob(this)
        return false
    }
}