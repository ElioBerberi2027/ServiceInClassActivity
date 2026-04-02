package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var bound = false
    private lateinit var countdownText: TextView

    private val timerHandler = Handler(Looper.getMainLooper()) { msg: Message ->
        countdownText.text = msg.what.toString()
        true
    }

    private var connection = object :  ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("MainActivity", "service connected")
            timerBinder = p1 as TimerService.TimerBinder
            timerBinder?.setHandler(timerHandler)
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

        countdownText = findViewById(R.id.textView)

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