package com.jacobgb24.launchschedule.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsService;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

import com.jacobgb24.launchschedule.MainActivity;
import com.jacobgb24.launchschedule.R;
import com.jacobgb24.launchschedule.newsList.NewsArticleActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob_000 on 7/25/2016.
 */
public class ChromeCustomTabs {
    private static CustomTabsClient client;
    private static CustomTabsSession customTabsSession;
    private static CustomTabsIntent customTabsIntent;
    private static String tabsPackage="";

    public static void customTabsWarmUp(final MainActivity mainActivity){
        CustomTabsServiceConnection customTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                //Pre-warming
                client = customTabsClient;
                client.warmup(0L);
                customTabsSession = client.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                client = null;
            }
        };

        if(isChromeCustomTabsSupported(mainActivity)) {
            CustomTabsClient.bindCustomTabsService(mainActivity, tabsPackage, customTabsServiceConnection);
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabsSession);
            customTabsIntent = builder.build();
            builder.setCloseButtonIcon(BitmapFactory.decodeResource(mainActivity.getResources(), R.mipmap.ic_arrow_back_white_24dp));
            builder.setToolbarColor(ContextCompat.getColor(mainActivity.getApplicationContext(), android.R.color.background_dark));
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + MainActivity.context.getPackageName()));
        }
    }

    public static boolean isChromeCustomTabsSupported(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://test.com"));
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        if(!(packagesSupportingCustomTabs.isEmpty())) {
            tabsPackage = packagesSupportingCustomTabs.get(0);
            return true;
        }
        else
            return false;
    }

    public static void loadURL(Activity activity, String url){
        if(isChromeCustomTabsSupported(activity) && !PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("pref_noCustTabs", false)) {
            customTabsIntent.launchUrl(activity, Uri.parse(url));
        }
        else {
            Intent intent = new Intent(activity, NewsArticleActivity.class);
            intent.putExtra("ARTICLE_LINK", url);
            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }
}
