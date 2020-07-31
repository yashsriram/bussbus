package com.yashasvisriram.bussbus

import android.content.Context
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
            val sp =
                getSharedPreferences(
                    getString(R.string.stop_id_stop_nickname_sp),
                    Context.MODE_PRIVATE
                )
            val stopId = activityAddStop.stopId.text.toString()
            // len == 5
            if (stopId.length == 5) {
                Toast.makeText(
                    this,
                    "Stop Id has to be 5 chars long",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            // Uniqueness
            if (sp.getString(stopId, null) != null) {
                Toast.makeText(
                    this,
                    "This stop is already added.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val stopNickname = activityAddStop.stopNickname.text.toString()
            // 0 < len
            if (stopNickname.isEmpty()) {
                Toast.makeText(
                    this,
                    "Stop Nickname can not be empty",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            // Uniqueness
            for ((_, nickname) in sp.all.entries) {
                if (nickname == stopNickname) {
                    Toast.makeText(
                        this,
                        "This nickname is already used. Please use something else.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }
            with(sp.edit()) {
                putString(stopId, stopNickname)
                commit()
            }
            finish()
        }
    }
}