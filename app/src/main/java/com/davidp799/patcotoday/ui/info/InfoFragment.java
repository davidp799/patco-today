package com.davidp799.patcotoday.ui.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.davidp799.patcotoday.R;
import com.davidp799.patcotoday.databinding.FragmentInfoBinding;
import com.google.android.material.transition.MaterialFadeThrough;

public class InfoFragment extends Fragment {

    private InfoViewModel InfoViewModel;
    private FragmentInfoBinding binding;

    // List Item Links
    String[] urls = {"http://www.ridepatco.org/schedules/fares.html",
            "https://www.patcofreedomcard.org/front/account/login.jsp",
            "https://twitter.com/RidePATCO", "tel:+1-856-772-6900",
            "patco@ridepatco.org", "http://www.ridepatco.org/index.asp",
            "david.r.pape@gmail.com", "http://www.ridepatco.org/schedules/alerts_more.asp?page=25",
            "http://www.ridepatco.org/travel/faqs.html", "http://www.ridepatco.org/safety/how_do_i.html"};

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        InfoViewModel = new ViewModelProvider(this).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setEnterTransition(new MaterialFadeThrough());

        String[]  general = {"Fares", "Reload Freedom Card", "Twitter", "Call", "Email", "Website",
                             "Email", "Elevator & Escalator Availability", "FAQ's", "Safety & Security"};

        // FIRST LISTVIEW ITEM
        ListView listView = (ListView) root.findViewById(R.id.listView);
        ArrayAdapter<String> listGeneralAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                general
        );

        listView.setAdapter(listGeneralAdapter);
        listView.setTransitionGroup(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View  view, int position, long id)
            {
                Intent openLinksIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[position]));
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