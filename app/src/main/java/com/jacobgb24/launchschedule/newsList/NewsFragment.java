package com.jacobgb24.launchschedule.newsList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jacobgb24.launchschedule.R;
import com.jacobgb24.launchschedule.SettingsActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.jacobgb24.launchschedule.util.DividerItemDecoration;
import com.jacobgb24.launchschedule.util.Util;

/**
 * Created by jacob_000 on 9/16/2015.
 */
public class NewsFragment extends Fragment {

    private Context context;
    private RecyclerView rv;
    private NewsAdapter adapter;
    private List<FeedParser.Entry> rssItemList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        context = getContext();
        View view = inflater.inflate(R.layout.fragment_news, viewGroup, false);
        rv = (RecyclerView) view.findViewById(R.id.list_news);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout_news);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_material_light);
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
        rssItemList = new ArrayList<>();

        refreshListener.onRefresh();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new NewsAdapter(rssItemList, getActivity());
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        (rv.getAdapter()).notifyDataSetChanged();

        super.onViewCreated(view, savedInstanceState);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        new DownloadFile().execute("https://spacenews.com/feed/");
                }
            });
        }
    };

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                InputStream inputStream = new BufferedInputStream(url.openStream());
                rssItemList.clear();
                rssItemList = FeedParser.parse(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                if(Util.isNetworkConnected(context)) {
                    FirebaseCrashlytics.getInstance().log("Error loading articles");
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                adapter.setList(rssItemList);
                (rv.getAdapter()).notifyDataSetChanged();
                if (rssItemList.size() == 0)
                    Toast.makeText(getActivity().getApplicationContext(), "Could not load articles", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(result);
        }
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_news_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.open_sn:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spacenews.com/segment/news"));
                startActivity(browserIntent);
                return true;
        }
        return false;
    }

}
