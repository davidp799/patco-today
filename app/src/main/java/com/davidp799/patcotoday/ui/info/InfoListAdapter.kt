package com.davidp799.patcotoday.ui.info

import android.content.Context
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.davidp799.patcotoday.R
import android.widget.TextView

class InfoListAdapter(private val mContext: Context, resource: Int, private val mObjects: Array<String>): ArrayAdapter<String>(mContext, resource, mObjects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(mContext)
        val row: View = inflater.inflate(R.layout.info_adapter_view_layout, parent, false)
        val image: ImageView = row.findViewById<View>(R.id.item_image) as ImageView
        val entry: TextView = row.findViewById<View>(R.id.item_text) as TextView
        entry.text = mObjects[position]
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
        return row
    }
}