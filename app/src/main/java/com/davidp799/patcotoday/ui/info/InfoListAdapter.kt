package com.davidp799.patcotoday.ui.info

import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.davidp799.patcotoday.R
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class InfoListAdapter(
    private val items: Array<String>,
    private val onItemClick: (Int, View) -> Unit
) : RecyclerView.Adapter<InfoListAdapter.InfoViewHolder>() {
    class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.item_image)
        val entry: TextView = view.findViewById(R.id.item_text)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.info_item_layout, parent, false)
        return InfoViewHolder(view)
    }
    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.entry.text = items[position]
        ViewCompat.setTransitionName(holder.itemView, "info_${items[position]}")

        when (position) {
            0 -> holder.image.setImageResource(R.drawable.ic_info_fares_tt)
            1 -> holder.image.setImageResource(R.drawable.ic_info_reload_tt)
            2 -> holder.image.setImageResource(R.drawable.ic_info_twitter_tt)
            3 -> holder.image.setImageResource(R.drawable.ic_info_call_tt)
            4 -> holder.image.setImageResource(R.drawable.ic_info_email_tt)
            5 -> holder.image.setImageResource(R.drawable.ic_info_website_tt)
            6 -> holder.image.setImageResource(R.drawable.ic_info_special_tt)
            7 -> holder.image.setImageResource(R.drawable.ic_info_accessibility_new_tt)
            8 -> holder.image.setImageResource(R.drawable.ic_info_faq_tt)
            9 -> holder.image.setImageResource(R.drawable.ic_info_safety_tt)
        }
        holder.itemView.setOnClickListener { onItemClick(position, holder.itemView) }
    }
    override fun getItemCount(): Int = items.size
}
