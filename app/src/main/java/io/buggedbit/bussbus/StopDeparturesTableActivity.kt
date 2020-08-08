package io.buggedbit.bussbus

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    // Periodically display lastest sync status
    private var lastSyncTimestamp = System.currentTimeMillis()
    private val uiThreadHandler = Handler(Looper.getMainLooper())
    private val displayLatestSyncStatusPeriodically = object : Runnable {
        override fun run() {
            updateSyncStatus()
            uiThreadHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_departures_table)
        title = "Departures per stop"

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

        // Update time since last sync
        uiThreadHandler.post(displayLatestSyncStatusPeriodically)
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
        uiThreadHandler.removeCallbacks(displayLatestSyncStatusPeriodically)
    }

    private fun reload(apiService: ApiService) {
        // If not connected to network, prompt user to connect
        if ((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetwork == null) {
            AlertDialog.Builder(this).setMessage("Please connect to internet.")
                .setPositiveButton("Ok") { _, _ -> }.create().show()
            return
        }
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
        // No stops => Prompt user to add
        if (sp.all.entries.size == 0) {
            AlertDialog.Builder(this).setMessage("Please add some stops by clicking on + icon.")
                .setPositiveButton("Ok") { _, _ -> }.create().show()
        }
        // Update last sync state
        lastSyncTimestamp = System.currentTimeMillis()
        updateSyncStatus()
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
                        StopDeparturesAdapter(
                            stopDepartures,
                            this@StopDeparturesTableActivity
                        )
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

    private fun updateSyncStatus() {
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
            true -> ContextCompat.getDrawable(context,
                R.drawable.live_departure
            )
            false -> ContextCompat.getDrawable(context,
                R.drawable.normal_departure
            )
        }
        holder.view.setPadding(40, 20, 40, 20)
        holder.view.due.text = departure.departureText!!.padOrTruncateString(7)
        holder.view.route.text = departure.route!!.padOrTruncateString(1)
        holder.view.terminal.text = departure.terminal!!.padOrTruncateString(1)
    }
}