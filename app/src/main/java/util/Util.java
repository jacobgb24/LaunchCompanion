package util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by jacob_000 on 7/29/2016.
 */
public class Util {
    public static boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
