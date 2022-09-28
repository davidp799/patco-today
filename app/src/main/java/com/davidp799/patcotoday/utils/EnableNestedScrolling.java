package com.davidp799.patcotoday.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class EnableNestedScrolling {
    public static void enable(ListView myListView) {
        myListView.setNestedScrollingEnabled(true);
    }
}