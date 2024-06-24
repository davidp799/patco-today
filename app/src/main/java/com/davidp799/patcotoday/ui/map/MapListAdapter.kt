package com.davidp799.patcotoday.ui.map

import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.davidp799.patcotoday.R
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class MapListAdapter(
    private val stations: Array<String>,
    private val onItemClick: (String, View) -> Unit
) : RecyclerView.Adapter<MapListAdapter.StationViewHolder>() {
    class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.item_image)
        val entry: TextView = view.findViewById(R.id.item_text)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.map_adapter_view_layout, parent, false)
        return StationViewHolder(view)
    }
    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val stationName = stations[position]
        ViewCompat.setTransitionName(holder.itemView, "station_${stationName}")
        holder.entry.text = stationName

        if (position == 0) {
            holder.image.setImageResource(R.drawable.ic_timeline_start_tt)
        } else if (position == stations.size - 1) {
            holder.image.setImageResource(R.drawable.ic_timeline_end_tt)
        }

        holder.itemView.setOnClickListener { onItemClick(stationName, holder.itemView) }
    }
    override fun getItemCount(): Int = stations.size
}
