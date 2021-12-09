package com.davidp799.patcotoday.ui.schedules;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SchedulesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SchedulesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is schedules fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}