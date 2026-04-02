package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var bound = false

    private var connection = object :  ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("MainActivity", "service connected")
            timerBinder = p1 as TimerService.TimerBinder
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d("MainActivity", "service disconnected")
            timerBinder = null
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, TimerService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (bound) {
                timerBinder?.start(10)
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (bound) {
                timerBinder?.stop()
            }
        }
    }
}