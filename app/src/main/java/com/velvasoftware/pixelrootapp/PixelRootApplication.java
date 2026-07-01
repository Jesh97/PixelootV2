package com.velvasoftware.pixelrootapp;

import android.app.Application;

import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;

public class PixelRootApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(getApplicationContext());
    }
}