package com.davidp799.patcotoday.ui.map

import android.content.Context
import android.widget.ArrayAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.davidp799.patcotoday.R
import android.widget.TextView

class MapListAdapter(private val mContext: Context, resource: Int, private val mObjects: Array<String>): ArrayAdapter<String>(mContext, resource, mObjects) {
    class ViewHolder(view: View) {
        val image: ImageView = view.findViewById<View>(R.id.item_image) as ImageView
        val entry: TextView = view.findViewById<View>(R.id.item_text) as TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val inflater = LayoutInflater.from(mContext)
        view = inflater.inflate(R.layout.map_adapter_view_layout, parent, false)
        val viewHolder = ViewHolder(view)
        view.tag = viewHolder

        viewHolder.entry.text = mObjects[position]
        if (position == 0) {
            viewHolder.image.setImageResource(R.drawable.ic_timeline_start_tt)
        } else if (position == mObjects.size-1) {
            viewHolder.image.setImageResource(R.drawable.ic_timeline_end_tt)
        }
        return view
    }
}
