package com.zendrive.touchnow.ui

import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import com.zendrive.touchnow.misc.AppUtil
import com.zendrive.touchnow.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private lateinit var receiver: BroadcastReceiver

    @Suppress( "deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleIntent(intent)
            }
        }
        this.registerReceiver(receiver, IntentFilter(STOP_ACTIVITY_ACTION))
        ScheduledThreadPoolExecutor(1).scheduleAtFixedRate({ runOnUiThread { updateContents() } },
                0, 1, TimeUnit.SECONDS)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        findViewById<Switch>(R.id.keep_on_switch).isChecked = sharedPreferences
                .getBoolean(KEEP_ACTIVITY_SWITCH_STATE_KEY, false)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.edit {
            putBoolean(KEEP_ACTIVITY_SWITCH_STATE_KEY,
                    findViewById<Switch>(R.id.keep_on_switch).isChecked)
        }
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        unregisterReceiver(receiver)
    }

    fun finishMainActivity(v: View) {
        if (sharedPreferences.getBoolean(KEEP_ACTIVITY_ON_KEY,
                        false)) {
            Toast.makeText(this.applicationContext,"Switch keep Activity On", Toast.LENGTH_SHORT).show()
            return
        }
        val switch = v as Switch
        if (switch.isChecked) {
            Toast.makeText(this.applicationContext, "Exiting the activity", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun keepMainActivity(v: View) {
        val switch = v as Switch
        if (switch.isChecked) {
            sharedPreferences.edit {
                putBoolean(KEEP_ACTIVITY_ON_KEY, true)
            }
        } else {
            sharedPreferences.edit {
                putBoolean(KEEP_ACTIVITY_ON_KEY, false)
            }
        }
    }

    private fun updateContents() {
        findViewById<TextView>(R.id.timeView).text = timeFormat.format(Date())
        findViewById<TextView>(R.id.activityText).text = AppUtil.getLatestActivityName()
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == STOP_ACTIVITY_ACTION) {
            Log.d("Tag", "Stopping activity due to intent!!!")
            this.finish()
        }
    }

    companion object {
        private var isActive = false
        private const val KEEP_ACTIVITY_SWITCH_STATE_KEY = "keep_activity_switch_state"
        const val STOP_ACTIVITY_ACTION = "stop_main_activity"
        const val KEEP_ACTIVITY_ON_KEY = "keep_main_activity_on"
        fun isActivityVisible(): Boolean {
            return isActive
        }
    }
}
