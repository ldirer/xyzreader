package com.example.xyzreader.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    private static final String LOG_TAG = Config.class.getSimpleName();

    static {
        URL url = null;
        try {
            url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        BASE_URL = url;
    }
}
