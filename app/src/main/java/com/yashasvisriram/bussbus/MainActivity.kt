package com.yashasvisriram.bussbus

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://svc.metrotransit.org/"
    private val compositeDisposable = CompositeDisposable()

    // Fields for keeping track of last synced time
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI setup
        stopDeparturesView1.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // REST service setup
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Initial REST call
        restCall(apiService, "16154", "RecWell")

        // Swipe refresh setup
        swipeContainer.setOnRefreshListener {
            restCall(apiService, "16154", "RecWell")
        }

        // Time since last sync setup
        lastSyncRunnable.run()
    }

    private fun restCall(apiService: ApiService, stopId: String, stopDescription: String) {
        val stopDepartures = apiService.getStopDepartures(stopId)
        stopDepartures!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<StopDeparture>?> {
                override fun onSuccess(stopDepartures: List<StopDeparture>) {
                    stopHintView1.text = stopDescription
                    stopDeparturesView1.adapter = StopDeparturesAdapter(stopDepartures)
                    swipeContainer.isRefreshing = false
                    lastSyncTimestamp = System.currentTimeMillis()
                    updateTimeSinceLastSync()
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {
                    Snackbar.make(
                        stopDeparturesView1,
                        "Could not get departures from $stopDescription (Stop #$stopId)",
                        Snackbar.LENGTH_LONG
                    ).show()
                    swipeContainer.isRefreshing = false
                }
            })
    }

    private fun updateTimeSinceLastSync() {
        val timeSinceLastSyncInMillis = System.currentTimeMillis() - lastSyncTimestamp
        timeSinceLastSyncView.text = "${timeSinceLastSyncInMillis / 1000 / 60} min since last sync"
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
        lastSyncHandler.removeCallbacks(lastSyncRunnable)
    }
}
