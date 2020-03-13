package com.ronal.nearestprinting.util;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class NearestPrinting extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
