package com.davidp799.patcotoday.ui.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.databinding.FragmentMapBinding;
import com.google.android.material.transition.MaterialFadeThrough;

public class MapFragment extends Fragment {

    private MapViewModel MapViewModel;
    private FragmentMapBinding binding;

    // List Item Links
    String[] stationLinks = {"http://www.ridepatco.org/stations/15th.asp", "http://www.ridepatco.org/stations/12th.asp",
                             "http://www.ridepatco.org/stations/9th.asp", "http://www.ridepatco.org/stations/8th.asp",
                             "http://www.ridepatco.org/stations/cityhall.asp", "http://www.ridepatco.org/stations/broadway.asp",
                             "http://www.ridepatco.org/stations/ferryave.asp", "http://www.ridepatco.org/stations/collingswood.asp",
                             "http://www.ridepatco.org/stations/westmont.asp", "http://www.ridepatco.org/stations/haddonfield.asp",
                             "http://www.ridepatco.org/stations/woodcrest.asp", "http://www.ridepatco.org/stations/ashland.asp",
                             "http://www.ridepatco.org/stations/lindenwold.asp"};

    // Icon List

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setEnterTransition(new MaterialFadeThrough());


        // image view lineMap
        ImageView image = (ImageView) root.findViewById(R.id.lineMap);
        image.setImageResource(R.drawable.patco_linemap);

        String[]  stationsList = {"15/16th & Locust", "12/13th & Locust", "9/10th & Locust", "8th & Market",
                             "City Hall", "Broadway", "Ferry Avenue", "Collingswood", "Westmont",
                             "Haddonfield", "Woodcrest", "Ashland", "Lindenwold"};

        // FIRST LISTVIEW ITEM
        ListView stationsMap = (ListView) root.findViewById(R.id.stationsMap);
        ArrayAdapter<String> listGeneralAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                stationsList
        );
        stationsMap.setTransitionGroup(true);
        stationsMap.setAdapter(listGeneralAdapter);
        stationsMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View  view, int position, long id)
            {
                Intent openLinksIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stationLinks[position]));
                getContext().startActivity(openLinksIntent);
            }
        });

        stationsMap.setAdapter(listGeneralAdapter);
        stationsMap.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View  view, int position, long id)
            {
                Intent openLinksIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stationLinks[position]));
                getContext().startActivity(openLinksIntent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}