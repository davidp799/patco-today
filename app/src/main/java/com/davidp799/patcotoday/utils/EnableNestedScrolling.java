package com.davidp799.patcotoday.utils;

import androidx.recyclerview.widget.RecyclerView;

public class EnableNestedScrolling {
    public static void enable(RecyclerView myRecyclerView) {
        myRecyclerView.setNestedScrollingEnabled(true);
    }
}