package util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import com.jacobgb24.launchschedule.MainActivity;

/**
 * Created by jacob_000 on 7/29/2016.
 */
public class Util {
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
