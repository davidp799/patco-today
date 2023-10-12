package com.davidp799.patcotoday.ui.schedules;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.utils.Arrival;

import java.util.ArrayList;
import java.util.Objects;

public class SchedulesListAdapter extends ArrayAdapter<Arrival> {

    private final Context mContext;
    private final int mResource;
    private final int mScrollValue;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView arrives;
        TextView travels;
    }

    /**
     * Default constructor for the PersonListAdapter
     */
    public SchedulesListAdapter(Context context, int resource, ArrayList<Arrival> objects, int scrollValue) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mScrollValue = scrollValue;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        //get the arrival information
        String arrivalTime = Objects.requireNonNull(getItem(position)).getArrivalTime();
        String travelTime = Objects.requireNonNull(getItem(position)).getTravelTime();
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
            holder.arrives = convertView.findViewById(R.id.textView1);
            holder.travels = convertView.findViewById(R.id.textView2);

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

        // TODO: fix this??? partially working
        if (position == mScrollValue && mScrollValue != 0) {
            holder.arrives.setTypeface(holder.arrives.getTypeface(), Typeface.BOLD);
            holder.travels.setTypeface(holder.travels.getTypeface(), Typeface.BOLD);
        }

        return convertView;
    }
}