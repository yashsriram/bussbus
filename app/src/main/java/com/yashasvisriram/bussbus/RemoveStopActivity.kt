package com.yashasvisriram.bussbus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class RemoveStopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_stop)
        title = "Remove a stop"
    }
}