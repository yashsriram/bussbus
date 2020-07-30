package com.yashasvisriram.bussbus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

class StopDeparturesAdapter(
    private val departures: List<StopDeparture>,
    private val context: Context
) :
    RecyclerView.Adapter<StopDeparturesAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        var view: View
        var dueDescription: TextView
        var routeAndTerminal: TextView

        init {
            v.setOnClickListener(this)
            view = v
            dueDescription = v.findViewById(R.id.dueDescription)
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
        holder.dueDescription.text = padOrTruncateString(" ${departure.departureText!!}", 7)
        holder.routeAndTerminal.text = padOrTruncateString("${departure.route}${departure.terminal} ", 3)
    }
}
