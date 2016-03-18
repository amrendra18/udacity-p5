package com.example.xyzreader;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Amrendra Kumar on 18/03/16.
 */
public class XYZApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
