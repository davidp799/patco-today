package com.davidp799.patcotoday.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

public class CheckNetwork {
    private final Executor executor;
    private ConnectivityManager connectivityManager;
    private boolean status;
    private Context context;


    public CheckNetwork(Context context, Executor executor) {
        this.executor = executor;
        this.context = context;
    }
    public void makeNetworkRequest() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                status = makeSynchronousNetworkRequest(context);
            }
        });


    }
    public boolean makeSynchronousNetworkRequest(Context context) {
        try {
            this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // use new connectivity manager mode if Android N
                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        status = true;
                    }
                    @Override
                    public void onLost(@NonNull Network network) {
                        status = false;
                    }
                });
                return status;
            } else { // otherwise, ping server
                try {
                    String command = "ping -c 1 www.ridepatco.org";
                    return (Runtime.getRuntime().exec(command).waitFor() == 0);
                } catch (Exception e) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
}
