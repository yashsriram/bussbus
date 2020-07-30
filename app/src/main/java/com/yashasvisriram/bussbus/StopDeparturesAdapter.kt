package com.yashasvisriram.bussbus

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class StopDeparturesAdapter(
    private val departures: List<StopDeparture>,
    private val context: Context
) :
    RecyclerView.Adapter<StopDeparturesAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
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
        val inflatedView = parent.inflate(R.layout.stop_departure, false)
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
