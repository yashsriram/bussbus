package com.yashasvisriram.bussbus

import android.os.Bundle
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI setup
        stopDepartures1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

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
    }

    private fun restCall(apiService: ApiService, stopId: String, stopDescription: String) {
        val stopDepartures = apiService.getStopDepartures(stopId)
        stopDepartures!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<StopDeparture>?> {
                override fun onSuccess(stopDepartures: List<StopDeparture>) {
                    stopDepartures1.adapter = StopDeparturesAdapter(stopDepartures)
                    stopHint1.text = stopDescription
                    swipeContainer.isRefreshing = false
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {
                    Snackbar.make(
                        stopDepartures1,
                        "Could not get departures from $stopDescription (Stop #$stopId)",
                        Snackbar.LENGTH_LONG
                    ).show()
                    swipeContainer.isRefreshing = false
                }
            })
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
    }
}
