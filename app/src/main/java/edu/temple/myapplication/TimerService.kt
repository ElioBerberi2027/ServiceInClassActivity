package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused = false

    private var currentVal = 0

    private val PREFS = "timer_pref"

    private val KEY_VAL = "curr_val"

    private val KEY_PAUSED = "paused"

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
            val savedVal = prefs.getInt(KEY_VAL, -1)
            val wasPaused = prefs.getBoolean(KEY_PAUSED, false)

            val valueToStart = if (wasPaused && savedVal > 0) {
                savedVal
            } else {
                startValue
            }

            this@TimerService.paused = false

            val prefsEdit = getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            prefsEdit.putBoolean(KEY_PAUSED, false)
            prefsEdit.apply()

            if (!isRunning) {
                if (::t.isInitialized) t.interrupt()
                this@TimerService.start(valueToStart)
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
                clearState()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()

        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused

            if (paused){
                saveState()
            }
        }
    }

    private fun saveState() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_VAL, currentVal)
            .putBoolean(KEY_PAUSED, true)
            .apply()
    }

    private fun clearState() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    inner class TimerThread(private val startValue: Int) : Thread() {

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {

                    while (paused);

                    currentVal = i

                    Log.d("Countdown", i.toString())

                    timerHandler?.sendEmptyMessage(i)

                    while (paused);
                    sleep(1000)

                }
                clearState()
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                if (!paused){
                    clearState()
                }
                paused = false
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }


}