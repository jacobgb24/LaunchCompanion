package util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

