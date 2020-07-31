package com.yashasvisriram.bussbus

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_remove_stop.*
import kotlinx.android.synthetic.main.activity_remove_stop.view.*
import kotlinx.android.synthetic.main.stop_remove.view.*

class RemoveStopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_stop)
        title = "Remove a stop"

        // Get stops from persistent storage
        val sp = getSharedPreferences(
            getString(R.string.stop_id_stop_nickname_sp),
            Context.MODE_PRIVATE
        )
        activityRemoveStop.table.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        activityRemoveStop.table.adapter =
            StopsAdapter(sp.all.map { Pair(it.key!!, it.value!!.toString()) }, this)

    }
}

private class StopsAdapter(
    private val stops: List<Pair<String, String>>,
    private val context: Context
) :
    RecyclerView.Adapter<StopsAdapter.Holder>() {

    private class Holder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {}
    }

    override fun getItemCount() = stops.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflatedView =
            LayoutInflater.from(context).inflate(R.layout.stop_remove, parent, false)
        return Holder(inflatedView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (id, nickname) = stops[position]
        holder.view.stopId.text = id
        holder.view.stopNickname.text = nickname
    }
}