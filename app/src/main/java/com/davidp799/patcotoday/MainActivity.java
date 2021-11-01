package com.davidp799.patcotoday;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override

    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else { super.onBackPressed(); }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://www.ridepatco.org/schedules/schedules.asp");

    }

}