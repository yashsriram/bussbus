package com.yashasvisriram.bussbus

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        // REST service setup
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // UI setup
        stopDeparturesRecyclerView1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        stopDeparturesRecyclerView2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        stopDeparturesRecyclerView3.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        stopDeparturesRecyclerView4.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initial REST calls
        restCall(apiService, "16154", "RecWell", stopHintView1, stopDeparturesRecyclerView1)
        restCall(apiService, "13209", "CMU", stopHintView2, stopDeparturesRecyclerView2)
        restCall(apiService, "56699", "2 Stop", stopHintView3, stopDeparturesRecyclerView3)
        restCall(apiService, "16132", "6 Stop", stopHintView4, stopDeparturesRecyclerView4)

        // Sync setup
        refreshView.setOnClickListener {
            restCall(apiService, "16154", "RecWell", stopHintView1, stopDeparturesRecyclerView1)
            restCall(apiService, "13209", "CMU", stopHintView2, stopDeparturesRecyclerView2)
            restCall(apiService, "56699", "2 Stop", stopHintView3, stopDeparturesRecyclerView3)
            restCall(apiService, "16132", "6 Stop", stopHintView4, stopDeparturesRecyclerView4)
        }

        // Time since last sync setup
        lastSyncRunnable.run()
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
                    stopHintView.text = stopDescription
                    stopDeparturesRecyclerView.adapter =
                        StopDeparturesAdapter(stopDepartures, this@MainActivity)
                    lastSyncTimestamp = System.currentTimeMillis()
                    updateTimeSinceLastSync()
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {
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
