package com.jacobgb24.launchschedule;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.jacobgb24.launchschedule.util.ChromeCustomTabs;
import com.jacobgb24.launchschedule.util.TabAdapter;


public class MainActivity extends AppCompatActivity {

    private boolean darkThemeUsed=false;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false)) {
            setTheme(R.style.AppThemeDarkNoAB);
            darkThemeUsed=true;
        }
        else
            setTheme(R.style.AppThemeNoAB);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        recordUserProps();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(darkThemeUsed && toolbar!=null)
            toolbar.setPopupTheme(R.style.PopupMenu_Dark);
        setSupportActionBar(toolbar);

        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager.setAdapter(tabAdapter);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        ChromeCustomTabs.customTabsWarmUp(this);
    }

    private void recordUserProps(){
        firebaseAnalytics.setUserProperty("setting_dark_theme", ""+PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false));
        firebaseAnalytics.setUserProperty("setting_disable_images", ""+PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_noImages", false));
        firebaseAnalytics.setUserProperty("setting_disable_cct", ""+PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_noCustTabs", false));
        firebaseAnalytics.setUserProperty("cct_supported", ""+ChromeCustomTabs.isChromeCustomTabsSupported(this));
    }
}
