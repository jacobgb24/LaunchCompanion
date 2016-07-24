package com.jacobgb24.launchschedule.launchList;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.google.firebase.crash.FirebaseCrash;
import com.jacobgb24.launchschedule.R;
import com.jacobgb24.launchschedule.SettingsActivity;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

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
import java.util.Set;
import java.util.regex.Pattern;

import supportClasses.DividerItemDecoration;


/**
 * Created by jacob_000 on 9/15/2015.
 */
public class LaunchListFragment extends android.support.v4.app.Fragment {
    private RecyclerView rv;
    private LaunchListAdapter adapter;
    private List<Launch> launchList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialSearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        launchList = new ArrayList<>();
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_launchlist, viewGroup, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_material_light);
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
        rv = (RecyclerView) view.findViewById(R.id.list);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        refreshListener.onRefresh();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new LaunchListAdapter(launchList, getActivity());
        adapter.setHasStableIds(true);
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
                        new DownloadFile().execute("http://spaceflightnow.com/launch-schedule/");
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
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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
                FirebaseCrash.log("Error loading launches");
                FirebaseCrash.report(e);
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
                    launchList = DataParser.parseData(result);
                    adapter.setList(launchList);
                    (rv.getAdapter()).notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
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
        searchView = (MaterialSearchView) getActivity().findViewById(R.id.search_view);
        searchView.setMenuItem(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(listener);
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
        }
        return false;
    }
    MaterialSearchView.OnQueryTextListener listener = new MaterialSearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String input) {
            final List<Launch> filteredModelList = filter(launchList, input);
            adapter.setFilter(filteredModelList);
            rv.scrollToPosition(0);
            return true;
        }

        public boolean onQueryTextSubmit(String input) {
            searchView.hideKeyboard(getView());
            return true;
        }
    };
    private List<Launch> filter(List<Launch> list, String query) {
        boolean checkMission = false, checkRocket = false, checkDate = false, checkLocation = false;
        query = query.toLowerCase();
        Set<String> settings = PreferenceManager.getDefaultSharedPreferences(getActivity()).getStringSet("pref_searchParams", null);
        String[] values = settings.toArray(new String[settings.size()]);
        final List<Launch> filteredModelList = new ArrayList<>();
        for (String param : values) {
            switch (param) {
                case "Mission":
                    checkMission = true;
                    break;
                case "Rocket":
                    checkRocket = true;
                    break;
                case "Date":
                    checkDate = true;
                    break;
                case "Location":
                    checkLocation = true;
                    break;
            }
        }

        for (Launch launch : list) {
            String simpleLoc = launch.getLocation();
            simpleLoc=simpleLoc.substring(simpleLoc.lastIndexOf(",")+1,simpleLoc.length()).trim();
            // regex to match the start of words
            Pattern p = Pattern.compile("\\b" + Pattern.quote(query.toString()), Pattern.CASE_INSENSITIVE) ;
            if(checkMission && p.matcher(launch.getMission()).find())
                filteredModelList.add(launch);
            else if(checkRocket && p.matcher(launch.getVehicle()).find())
                filteredModelList.add(launch);
            else if(checkLocation && p.matcher(simpleLoc).find())
                filteredModelList.add(launch);
            else if(checkDate && p.matcher(launch.getDate()).find())
                filteredModelList.add(launch);
        }
        return filteredModelList;
    }
}
