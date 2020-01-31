package com.yashasvisriram.bussbus

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
        var description: TextView

        init {
            v.setOnClickListener(this)
            description = v.findViewById(R.id.description)
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
        holder.description.text = departure.departureText
    }
}
