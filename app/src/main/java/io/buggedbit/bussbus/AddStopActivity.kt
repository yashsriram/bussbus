package io.buggedbit.bussbus

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
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
            // (len == 5)?
            if (stopId.length != 5) {
                Snackbar.make(
                    activityAddStop.add,
                    "Stop Id has to be 5 chars long.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            // Unique?
            if (sp.getString(stopId, null) != null) {
                Snackbar.make(
                    activityAddStop.add,
                    "This stop is already added.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val stopNickname = activityAddStop.stopNickname.text.toString()
            // (0 < len)?
            if (stopNickname.isEmpty()) {
                Snackbar.make(
                    activityAddStop.add,
                    "Stop Nickname can not be empty.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            // Unique?
            for ((_, nickname) in sp.all.entries) {
                if (nickname == stopNickname) {
                    Snackbar.make(
                        activityAddStop.add,
                        "This nickname is already used. Please use something else.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }
            val isAdded = sp.edit().putString(stopId, stopNickname).commit()
            // Added?
            if (!isAdded) {
                Snackbar.make(
                    activityAddStop.add,
                    "Could not add stop.",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            finish()
        }
    }
}