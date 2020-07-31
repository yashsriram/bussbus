package com.yashasvisriram.bussbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_stop_departures_table.*
import kotlinx.android.synthetic.main.activity_stop_departures_table.view.*
import kotlinx.android.synthetic.main.stop_departure.view.*
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

        // REST service setup
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Initial REST call
        reload(apiService)

        // Sync setup
        activityStopDepartures.sync.setOnClickListener {
            reload(apiService)
        }

        // Time since last sync setup
        lastSyncRunnable.run()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_stop_departures_table_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.addStop -> {
            val intent = Intent(this, AddStopActivity::class.java)
            startActivityForResult(intent, 0)
            true
        }
        R.id.removeStop -> {
            val intent = Intent(this, RemoveStopActivity::class.java)
            startActivityForResult(intent, 0)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        reload(apiService)
    }

    override fun onDestroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onDestroy()
        lastSyncHandler.removeCallbacks(lastSyncRunnable)
    }

    private fun reload(apiService: ApiService) {
        // Get stops from persistent storage
        val sp =
            getSharedPreferences(
                getString(R.string.stop_id_stop_nickname_sp),
                Context.MODE_PRIVATE
            )
        // Clear all views
        activityStopDepartures.table.removeAllViews()
        for ((id, nickname) in sp.all.entries) {
            // UI setup
            val row = LayoutInflater.from(this)
                .inflate(R.layout.stop_departures_row, activityStopDepartures.table, false)
            row.departuresList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            activityStopDepartures.table.addView(row)
            // Fill information
            restCall(
                apiService,
                id,
                row.departuresList,
                "${nickname.toString().padOrTruncateString(7)} ᐅ ",
                row.nickname
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
                    Toast.makeText(
                        this@StopDeparturesTableActivity,
                        "Could not get departures from $stopName (Stop #$stopId).",
                        Toast.LENGTH_LONG
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

    private class Holder(val view: View) : RecyclerView.ViewHolder(view)

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
        holder.view.setPadding(40, 20, 40, 20)
        holder.view.due.text = departure.departureText!!.padOrTruncateString(7)
        holder.view.route.text = departure.route!!.padOrTruncateString(1)
        holder.view.terminal.text = departure.terminal!!.padOrTruncateString(1)
    }
}