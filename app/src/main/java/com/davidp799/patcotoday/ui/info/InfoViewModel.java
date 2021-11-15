package com.davidp799.patcotoday.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public InfoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is info fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}