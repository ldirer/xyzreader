package com.example.xyzreader;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;

public class MyApplication extends Application {


    private static final String LOG_TAG = MyApplication.class.getSimpleName();

    public void onCreate(){
        super.onCreate();
        Log.d(LOG_TAG, "USING MY CUSTOM APPLICATION CLASS");
        Stetho.initializeWithDefaults(this);
    }

}
