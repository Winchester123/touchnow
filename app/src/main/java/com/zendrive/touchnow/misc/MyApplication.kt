package com.zendrive.touchnow.misc

import android.app.Application
import com.zendrive.touchnow.misc.AppUtil

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppUtil.scheduleJob(this.applicationContext)
    }
}