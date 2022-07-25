package com.davidp799.patcotoday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.davidp799.patcotoday.utils.Arrival;

import java.util.ArrayList;

/**
 *
 */

public class SchedulesListAdapter extends ArrayAdapter<Arrival> {

    private static final String TAG = "SchedulesListAdapter";

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView arrives;
        TextView travels;
    }

    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param objects
     */
    public SchedulesListAdapter(Context context, int resource, ArrayList<Arrival> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        String arrivalTime = getItem(position).getArrivalTime();
        String travelTime = getItem(position).getTravelTime();
        //Create the person object with the information
        Arrival arrival = new Arrival(arrivalTime,travelTime);
        //create the view result for showing the animation
        final View result;
        //ViewHolder object
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.arrives = (TextView) convertView.findViewById(R.id.textView1);
            holder.travels = (TextView) convertView.findViewById(R.id.textView2);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        /* Animations: fade in fast */
        Animation fadeInAnimation = new AlphaAnimation(0,1);
        fadeInAnimation.setInterpolator(new DecelerateInterpolator());
        fadeInAnimation.setDuration(100);
        result.startAnimation(fadeInAnimation);

        holder.arrives.setText(arrival.getArrivalTime());
        holder.travels.setText(arrival.getTravelTime());

        return convertView;
    }
}