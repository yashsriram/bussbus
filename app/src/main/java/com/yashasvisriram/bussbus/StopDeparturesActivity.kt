package com.yashasvisriram.bussbus

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_stop_departures.*
import kotlinx.android.synthetic.main.activity_stop_departures.view.*
import kotlinx.android.synthetic.main.stop_departures_row.view.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class StopDeparturesActivity : AppCompatActivity() {

    // REST service
    private val baseUrl = "https://svc.metrotransit.org/"
    private val compositeDisposable = CompositeDisposable()

    // Keeping track of last synced time
    private var lastSyncTimestamp = System.currentTimeMillis()
    private val checkInterval = 5000L
    private val lastSyncHandler: Handler = Handler()
    private val lastSyncRunnable: Runnable = object : Runnable {
        override fun run() {
            try {
                updateTimeSinceLastSync()
            } finally {
                lastSyncHandler.postDelayed(this, checkInterval)
            }
        }
    }

    // UI
    private val stopNameLen = 7

    // Mock db
    private val stopsFile = arrayListOf(
        arrayListOf("13209", "CMU1"),
        arrayListOf("16154", "RecWell"),
        arrayListOf("16157", "Armory"),
        arrayListOf("56699", "2@Home"),
        arrayListOf("16132", "6@Home")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_departures)
        title = "Departures per stop"

        // Pick a random backdrop
        val backgrounds = arrayListOf(
            R.drawable.backdrop1,
            R.drawable.backdrop2,
            R.drawable.backdrop3,
            R.drawable.backdrop4,
            R.drawable.backdrop5,
            R.drawable.backdrop6,
            R.drawable.backdrop7,
            R.drawable.backdrop8
        )
        activityMainLayout.background =
            ContextCompat.getDrawable(this, backgrounds.getRandomElement())

        // UI setup
        for (entry in stopsFile) {
            val row = LayoutInflater.from(this)
                .inflate(R.layout.stop_departures_row, activityMainLayout.table, false)
            row.departuresList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            activityMainLayout.table.addView(row)
        }

        // REST service setup
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Initial REST call
        sync(apiService)

        // Sync setup
        refreshView.setOnClickListener {
            sync(apiService)
        }

        // Time since last sync setup
        lastSyncRunnable.run()
    }

    private fun sync(apiService: ApiService) {
        for (i in 0 until stopsFile.size) {
            restCall(
                apiService,
                stopsFile[i][0],
                stopsFile[i][1],
                activityMainLayout.table.getChildAt(i).name,
                activityMainLayout.table.getChildAt(i).departuresList
            )
        }
    }

    private fun restCall(
        apiService: ApiService,
        stopId: String,
        stopDescription: String,
        stopHintView: TextView,
        stopDeparturesRecyclerView: RecyclerView
    ) {
        val stopDepartures = apiService.getStopDepartures(stopId)
        stopDepartures!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<StopDeparture>?> {
                override fun onSuccess(stopDepartures: List<StopDeparture>) {
                    stopHintView.text = "${padOrTruncateString(stopDescription, stopNameLen)} ᐅ "
                    stopDeparturesRecyclerView.adapter =
                        StopDeparturesAdapter(stopDepartures, this@StopDeparturesActivity)
                    lastSyncTimestamp = System.currentTimeMillis()
                    updateTimeSinceLastSync()
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {
                    stopHintView.text = "${padOrTruncateString(stopDescription, stopNameLen)} ᐅ "
                    Snackbar.make(
                        stopDeparturesRecyclerView,
                        "Could not get departures from $stopDescription (Stop #$stopId)",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateTimeSinceLastSync() {
        val timeSinceLastSyncInMillis = System.currentTimeMillis() - lastSyncTimestamp
        refreshView.text = "${timeSinceLastSyncInMillis / 1000 / 60} min ago"
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
        lastSyncHandler.removeCallbacks(lastSyncRunnable)
    }
}
