package com.jacobgb24.launchschedule.util;

import android.content.Context;
import android.net.ConnectivityManager;

import com.jacobgb24.launchschedule.MainActivity;

/**
 * Created by jacob_000 on 7/29/2016.
 */
public class Util {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
