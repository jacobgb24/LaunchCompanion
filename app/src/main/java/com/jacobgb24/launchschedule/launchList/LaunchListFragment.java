package com.jacobgb24.launchschedule.launchList;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.jacobgb24.launchschedule.util.DividerItemDecoration;
import com.jacobgb24.launchschedule.util.Util;


/**
 * Created by jacob_000 on 9/15/2015.
 */
public class LaunchListFragment extends Fragment {
    private Context context;
    private RecyclerView rv;
    private LaunchListAdapter adapter;
    private List<Launch> launchList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        launchList = new ArrayList<>();
        setHasOptionsMenu(true);
        context = getContext();

        View view = inflater.inflate(R.layout.fragment_launchlist, viewGroup, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_material_light);
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
        rv = (RecyclerView) view.findViewById(R.id.list);
        rv.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        refreshListener.onRefresh();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new LaunchListAdapter(launchList, getActivity());
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);
        loadData();
        super.onViewCreated(view, savedInstanceState);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        new DownloadFile().execute("https://spaceflightnow.com/launch-schedule/");
                }
            });
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        try {
            FileOutputStream fileOut = new FileOutputStream(new File(getActivity().getFilesDir(), "Launch Data"));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(launchList);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            FileInputStream fileIn = new FileInputStream(new File(getActivity().getFilesDir(), "Launch Data"));
            ObjectInputStream in = new ObjectInputStream(fileIn);
            launchList = (ArrayList<Launch>) in.readObject();

            in.close();
            fileIn.close();
            adapter.setList(launchList);
            (rv.getAdapter()).notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toast(final String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            InputStream input;
            String strdata = null;
            StringBuilder stringBuilder = new StringBuilder();
            int count;
            try {
                URL url = new URL(params[0]);
                // download the file
                input = new BufferedInputStream(url.openStream());
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    stringBuilder.append(new String(data, 0, count));
                }
                strdata = stringBuilder.toString();
                strdata = strdata.replaceAll("\r\n", "\n");
                input.close();

            } catch (IOException e) {
                e.printStackTrace();
                if(Util.isNetworkConnected(context)) {
                    FirebaseCrashlytics.getInstance().log("Error loading launches");
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
            return strdata;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    launchList.clear();
                    launchList = DataParser.parseData(result, getContext());
                    adapter.setList(launchList);
                    (rv.getAdapter()).notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().log("Error parsing launch list");
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else if (launchList.isEmpty())
                toast("Error downloading data");
            else
                toast("Could not refresh list");
            swipeRefreshLayout.setRefreshing(false);
            super.onPostExecute(result);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_launch_fragment, menu);
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spaceflightnow.com/launch-schedule"));
                startActivity(browserIntent);
                return true;
            case R.id.action_search:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                searchIntent.putExtra("list_data", (ArrayList) launchList);
                startActivity(searchIntent);
        }
        return false;
    }
}
