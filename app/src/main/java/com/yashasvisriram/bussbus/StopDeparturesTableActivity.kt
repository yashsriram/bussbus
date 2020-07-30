package com.yashasvisriram.bussbus

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.activity_stop_departures_table.*
import kotlinx.android.synthetic.main.activity_stop_departures_table.view.*
import kotlinx.android.synthetic.main.stop_departures_row.view.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class StopDeparturesTableActivity : AppCompatActivity() {

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

    // Mock db
    private val file = arrayListOf(
        arrayListOf("13209", "CMU1"),
        arrayListOf("16154", "RecWell"),
        arrayListOf("16157", "Armory"),
        arrayListOf("56699", "2@Home"),
        arrayListOf("16132", "6@Home")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_departures_table)
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
        activityStopDepartures.background =
            ContextCompat.getDrawable(this, backgrounds.random())

        // UI setup
        for (entry in file) {
            val row = LayoutInflater.from(this)
                .inflate(R.layout.stop_departures_row, activityStopDepartures.table, false)
            row.departuresList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            activityStopDepartures.table.addView(row)
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
        activityStopDepartures.sync.setOnClickListener {
            sync(apiService)
        }

        // Time since last sync setup
        lastSyncRunnable.run()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_stop_departures_table_menu, menu)
        return true
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
        lastSyncHandler.removeCallbacks(lastSyncRunnable)
    }

    private fun sync(apiService: ApiService) {
        for (i in 0 until file.size) {
            restCall(
                apiService,
                file[i][0],
                activityStopDepartures.table.getChildAt(i).departuresList,
                "${file[i][1].padOrTruncateString(7)} ᐅ ",
                activityStopDepartures.table.getChildAt(i).name
            )
        }
    }

    private fun restCall(
        apiService: ApiService,
        stopId: String,
        stopDeparturesListView: RecyclerView,
        stopName: String,
        stopNameView: TextView
    ) {
        stopNameView.text = stopName
        val stopDeparturesListFuture = apiService.getStopDepartures(stopId)
        stopDeparturesListFuture!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<StopDeparture>?> {
                override fun onSuccess(stopDepartures: List<StopDeparture>) {
                    stopDeparturesListView.adapter =
                        StopDeparturesAdapter(stopDepartures, this@StopDeparturesTableActivity)
                    lastSyncTimestamp = System.currentTimeMillis()
                    updateTimeSinceLastSync()
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onError(e: Throwable) {
                    Snackbar.make(
                        stopDeparturesListView,
                        "Could not get departures from $stopName (Stop #$stopId)",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun updateTimeSinceLastSync() {
        val timeSinceLastSyncInMillis = System.currentTimeMillis() - lastSyncTimestamp
        activityStopDepartures.sync.text = "${timeSinceLastSyncInMillis / 1000 / 60} min ago"
    }
}

private class StopDeparturesAdapter(
    private val departures: List<StopDeparture>,
    private val context: Context
) :
    RecyclerView.Adapter<StopDeparturesAdapter.Holder>() {

    private class Holder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var view: View
        var due: TextView
        var routeAndTerminal: TextView

        init {
            v.setOnClickListener(this)
            view = v
            due = v.findViewById(R.id.due)
            routeAndTerminal = v.findViewById(R.id.routeAndTerminal)
        }

        override fun onClick(v: View) {
        }
    }

    override fun getItemCount() = departures.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView =
            LayoutInflater.from(context).inflate(R.layout.stop_departure, parent, false)
        return Holder(inflatedView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val departure = departures[position]
        holder.view.background = when (departure.actual) {
            true -> ContextCompat.getDrawable(context, R.drawable.live_departure)
            false -> ContextCompat.getDrawable(context, R.drawable.normal_departure)
        }
        holder.view.setPadding(20, 20, 20, 20)
        holder.due.text = " ${departure.departureText!!}".padOrTruncateString(7)
        holder.routeAndTerminal.text =
            "${departure.route}${departure.terminal} ".padOrTruncateString(3)
    }
}