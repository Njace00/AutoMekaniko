package com.example.automakaniko

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var rpm: TextView
    private lateinit var speed: TextView
    private lateinit var temp: TextView
    private lateinit var throttle: TextView
    private lateinit var load: TextView
    private lateinit var status: TextView
    private lateinit var connectBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        rpm = findViewById(R.id.rpm)
        speed = findViewById(R.id.speed)
        temp = findViewById(R.id.temp)
        throttle = findViewById(R.id.throttle)
        load = findViewById(R.id.load)
        status = findViewById(R.id.statusCard)
        connectBtn = findViewById(R.id.connectBtn)

        // Fake initial values
        setStaticData()

        // Button click simulation
        connectBtn.setOnClickListener {
            simulateConnection()
        }
    }

    private fun setStaticData() {
        rpm.text = "850"
        speed.text = "0"
        temp.text = "72"
        throttle.text = "12"
        load.text = "18"
        status.text = "Disconnected"
    }

    private fun simulateConnection() {
        status.text = "Connected to OBD"

        // Simulated live values
        rpm.text = "2150"
        speed.text = "48"
        temp.text = "90"
        throttle.text = "35"
        load.text = "52"
    }
}