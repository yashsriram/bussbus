package com.yashasvisriram.bussbus

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_stop.*
import kotlinx.android.synthetic.main.activity_add_stop.view.*

class AddStopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_stop)
        title = "Add a stop"

        activityAddStop.add.setOnClickListener {
            val stopId = activityAddStop.stopId.text.toString()
            if (stopId.isEmpty()) {
                Toast.makeText(
                    this,
                    "Stop Id can not be empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (stopId.length > 6) {
                Toast.makeText(
                    this,
                    "Stop Id can not be more than 6 digits long",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val stopNickname = activityAddStop.stopNickname.text.toString()
            if (stopNickname.isEmpty()) {
                Toast.makeText(
                    this,
                    "Stop Nickname can not be empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            println("hesoyam ${stopId} ${stopNickname}")
        }
    }
}