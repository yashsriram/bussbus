package com.yashasvisriram.bussbus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

class StopDeparturesAdapter(private val departures: List<StopDeparture>) :
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
        if (departure.actual) {
            holder.view.setBackgroundColor(Color.GREEN)
        }
        holder.dueDescription.text = departure.departureText
        holder.routeAndTerminal.text = "${departure.route}${departure.terminal}"
    }
}
