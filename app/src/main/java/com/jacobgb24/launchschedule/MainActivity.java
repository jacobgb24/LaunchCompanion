/*
 * Copyright 2013 Mark Injerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jacobgb24.launchschedule;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


import com.jacobgb24.launchschedule.launchList.LaunchListFragment;
import com.jacobgb24.launchschedule.newsList.NewsArticleActivity;
import com.jacobgb24.launchschedule.newsList.NewsFragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static Context context;
    private CustomTabsServiceConnection customTabsServiceConnection;
    private CustomTabsClient client;
    private CustomTabsSession customTabsSession;
    private String tabsPackage="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false))
            setTheme(R.style.AppThemeDarkNoAB);
        else
            setTheme(R.style.AppThemeNoAB);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager.setAdapter(tabAdapter);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

    }

    public static class TabAdapter extends FragmentPagerAdapter {
        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return new LaunchListFragment();
            else
                return new NewsFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return "Schedule";
            else return "News";
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        customTabsServiceConnection = new CustomTabsServiceConnection() {
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
    }

    public void loadURL(String url){
        if(isChromeCustomTabsSupported() && !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_noCustTabs", false)) {
            CustomTabsClient.bindCustomTabsService(MainActivity.this, tabsPackage, customTabsServiceConnection);
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabsSession).build();
            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
        }
        else {
            Intent intent = new Intent(getApplicationContext(), NewsArticleActivity.class);
            intent.putExtra("ARTICLE_LINK", url);
            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
            startActivity(intent);
        }
    }

    private boolean isChromeCustomTabsSupported() {
        PackageManager pm = context.getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.test-url.com"));
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
}