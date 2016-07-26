package com.jacobgb24.launchschedule;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import util.ChromeCustomTabs;
import util.CustomViewPager;
import util.TabAdapter;


public class MainActivity extends AppCompatActivity {

    public static Context context;
    private boolean darkThemeUsed=false;
    private FirebaseAnalytics firebaseAnalytics;
    private MaterialSearchView searchView;

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
        MainActivity.context = getApplicationContext();
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        recordUserProps();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(darkThemeUsed)
            toolbar.setPopupTheme(R.style.PopupMenu_Dark);
        setSupportActionBar(toolbar);
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        final CustomViewPager viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager.setAdapter(tabAdapter);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        searchView= (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
                firebaseAnalytics.logEvent("search_opened", new Bundle());
                viewPager.setPagingEnabled(false);
                tabLayout.setVisibility(View.GONE);
            }
            @Override
            public void onSearchViewClosed() {
                viewPager.setPagingEnabled(true);
                tabLayout.setVisibility(View.VISIBLE);
            }
        });
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==1 && searchView.isShown()) {
                       //     searchView.closeSearch();
                }
                super.onTabSelected(tab);
            }
        });

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
    @Override
    public void onBackPressed() {
        if (searchView.isShown()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
