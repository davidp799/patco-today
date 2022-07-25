package com.davidp799.patcotoday.ui.map

import android.content.Context
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.davidp799.patcotoday.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MapListAdapter(private val mContext: Context, resource: Int, private val mObjects: Array<String>): ArrayAdapter<String>(mContext, resource, mObjects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(mContext)
        val row: View = inflater.inflate(R.layout.map_adapter_view_layout, parent, false)
        val image: ImageView = row.findViewById<View>(R.id.item_image) as ImageView
        val entry: TextView = row.findViewById<View>(R.id.item_text) as TextView
        entry.text = mObjects[position]
        if (position == 0) {
            image.setImageResource(R.drawable.ic_timeline_start_tt)
        } else if (position == mObjects.size-1) {
            image.setImageResource(R.drawable.ic_timeline_end_tt)
        }
        return row
    }
}