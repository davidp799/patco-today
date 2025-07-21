package com.davidp799.patcotoday.ui.info

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.davidp799.patcotoday.R

class InfoRecyclerAdapter(val mContext: Context, val resource: Int, val mObjects: Array<String>) : RecyclerView.Adapter<InfoRecyclerAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the info_adapter_view_layout view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.info_item_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val image: ImageView = holder.imageView.findViewById<View>(R.id.item_image) as ImageView
        val entry: TextView = holder.textView.findViewById<View>(R.id.item_text) as TextView

        // sets the text to the textview from our itemHolder class
        entry.text = mObjects[position]

        // sets the image to the imageview from our itemHolder class
        when (position) {
            0 -> image.setImageResource(R.drawable.ic_info_fares_tt)
            1 -> image.setImageResource(R.drawable.ic_info_reload_tt)
            2 -> image.setImageResource(R.drawable.ic_info_twitter_tt)
            3 -> image.setImageResource(R.drawable.ic_info_call_tt)
            4 -> image.setImageResource(R.drawable.ic_info_email_tt)
            5 -> image.setImageResource(R.drawable.ic_info_website_tt)
            6 -> image.setImageResource(R.drawable.ic_info_special_tt)
            7 -> image.setImageResource(R.drawable.ic_info_accessibility_new_tt)
            8 -> image.setImageResource(R.drawable.ic_info_faq_tt)
            9 -> image.setImageResource(R.drawable.ic_info_safety_tt)
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mObjects.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)

    }
}