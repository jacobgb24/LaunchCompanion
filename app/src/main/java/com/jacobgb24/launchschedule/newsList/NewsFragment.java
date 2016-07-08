package com.jacobgb24.launchschedule.newsList;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jacobgb24.launchschedule.R;
import com.jacobgb24.launchschedule.SettingsActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import supportClasses.DividerItemDecoration;

/**
 * Created by jacob_000 on 9/16/2015.
 */
public class NewsFragment extends android.support.v4.app.Fragment {

    private RecyclerView rv;
    private NewsAdapter adapter;
    private List<FeedParser.Entry> rssItemList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
                    try {
                        swipeRefreshLayout.setRefreshing(true);
                        new DownloadFile().execute("http://spacenews.com/feed/");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.open_sfn:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spacenews.com/segment/news"));
                startActivity(browserIntent);
                return true;
        }
        return false;
    }

}
