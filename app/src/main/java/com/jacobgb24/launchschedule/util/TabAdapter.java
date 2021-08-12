package com.jacobgb24.launchschedule.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.jacobgb24.launchschedule.launchList.LaunchListFragment;
import com.jacobgb24.launchschedule.newsList.NewsFragment;

/**
 * Created by jacob_000 on 7/25/2016.
 */
//Used in MainActivity
public class TabAdapter extends FragmentPagerAdapter {

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

