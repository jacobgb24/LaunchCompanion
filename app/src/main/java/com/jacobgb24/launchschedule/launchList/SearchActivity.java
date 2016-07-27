package com.jacobgb24.launchschedule.launchList;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.jacobgb24.launchschedule.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import util.DividerItemDecoration;

/**
 * Created by jacob_000 on 7/26/2016.
 */
public class SearchActivity extends AppCompatActivity {
    private List<Launch> launchList;
    private LaunchListAdapter adapter;
    private RecyclerView recyclerView;
    private EditText search;


    @Override
    protected void onCreate(Bundle savedInstances){
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false))
            setTheme(R.style.AppThemeDarkNoAB);
        else
            setTheme(R.style.AppThemeNoAB);
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        search = (EditText) findViewById(R.id.search_view);
        ImageView closeButt = (ImageView) findViewById(R.id.search_clear);
        launchList = getIntent().getParcelableArrayListExtra("list_data");
        setSupportActionBar(toolbar);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new LaunchListAdapter(launchList, this);
        // adapter.setHasStableIds(true); //adds animation which looks bad with divider item
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);

        search.addTextChangedListener(new TextListener());

    }

    private class TextListener implements TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final List<Launch> filteredModelList = filter(launchList, s.toString());
            adapter.setFilter(filteredModelList);
            recyclerView.scrollToPosition(0);

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
    private List<Launch> filter(List<Launch> list, String query) {
        boolean checkMission = false, checkRocket = false, checkDate = false, checkLocation = false;
        query = query.toLowerCase();
        Set<String> settings = PreferenceManager.getDefaultSharedPreferences(this).getStringSet("pref_searchParams", null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_clear:
                if(search.length()==0)
                    finish();
                else
                    search.setText("");
                return true;
        }
        return false;
    }
}
