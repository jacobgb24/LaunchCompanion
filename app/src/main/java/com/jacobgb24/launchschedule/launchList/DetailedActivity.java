package com.jacobgb24.launchschedule.launchList;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jacobgb24.launchschedule.R;
import com.jacobgb24.launchschedule.SettingsActivity;

import java.text.DecimalFormat;

/**
 * Created by jacob_000 on 7/23/2015.
 */
public class DetailedActivity extends AppCompatActivity {
    private Launch launch;
    private boolean hasCountdown = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false))
            setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        Intent i = getIntent();
        launch = i.getParcelableExtra("LAUNCH_OBJ");
        setTitle(launch.getMission());
        fillView();

    }

    @SuppressWarnings("ConstantConditions")
    private void fillView() {
        TextView subtitle = (TextView) findViewById(R.id.dcard_subtitle);
        ImageView imageView = (ImageView) findViewById(R.id.dcard_img);
        TextView time = (TextView) findViewById(R.id.dcard_time);
        TextView location = (TextView) findViewById(R.id.dcard_location);
        TextView details = (TextView) findViewById(R.id.dcard_details);
        TextView countdown = (TextView) findViewById(R.id.dcard_countdown);
        LinearLayout locItem = (LinearLayout) findViewById(R.id.dcard_loc_item);
        LinearLayout timeItem = (LinearLayout) findViewById(R.id.dcard_time_item);

        if (!launch.hasCal() || launch.getCal().getTimeInMillis() < System.currentTimeMillis()) {
            countdown.setVisibility(View.GONE);
        } else {
            setCountdown(countdown);
        }

        timeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("CAL=", Long.toString(launch.getCal().getTimeInMillis()));
                if (launch.hasCal() && !(launch.getCal().getTimeInMillis() < System.currentTimeMillis())) {
                    Log.e("ATTEMP", "cal");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailedActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, 16);
                    } else
                        createReminder();
                }
            }
        });
        locItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + launch.getLocation())));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Could not open map", Toast.LENGTH_SHORT).show();
                }
            }
        });

        time.setText(launch.getDate() + " at " + launch.getTime());
        location.setText(launch.getLocation());
        details.setText(launch.getDescription());
        subtitle.setText(launch.getVehicle());

        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_disImg", false)) {
            String r = launch.getVehicle();
            String imgFalcon9 = "http://i.imgur.com/xYXarSa.jpg",
                    imgSoyuz = "http://i.imgur.com/dPKLDG1.jpg",
                    imgRockot = "http://i.imgur.com/DFfnUeH.jpg",
                    imgH2B = "http://i.imgur.com/bar7eAS.jpg",
                    imgLongMarch = "http://i.imgur.com/ouTHTvc.jpg",
                    imgDelta2 = "http://i.imgur.com/hjPGe44.jpg",
                    imgDelta4 = "http://i.imgur.com/OkWUZvb.jpg",
                    imgDelta4H = "http://i.imgur.com/KZ3sms3.jpg",
                    imgPSLV = "http://i.imgur.com/tJQguXE.jpg",
                    imgProton = "http://i.imgur.com/LVZh3te.jpg",
                    imgGSLV = "http://i.imgur.com/wN5kC9U.jpg",
                    imgAtlas5 = "http://i.imgur.com/GJt4xBO.jpg",
                    imgAriane5 = "http://i.imgur.com/sWDV4kh.jpg",
                    imgVega = "http://i.imgur.com/TSPzXUi.jpg",
                    imgMinotaur = "http://i.imgur.com/bdQWcx1.jpg",
                    imgAntares = "http://i.imgur.com/6k0cDUv.jpg";

                if (r.contains("Falcon"))
                    Glide.with(this).load(imgFalcon9).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Soyuz"))
                    Glide.with(this).load(imgSoyuz).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Rockot"))
                    Glide.with(this).load(imgRockot).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("H-2"))
                    Glide.with(this).load(imgH2B).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Long March"))
                    Glide.with(this).load(imgLongMarch).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Delta 2") || r.contains("Delta II"))
                    Glide.with(this).load(imgDelta2).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if ((r.contains("Delta 4") || r.contains("Delta IV")) && r.contains("Heavy"))
                    Glide.with(this).load(imgDelta4H).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Delta 4") || r.contains("Delta IV"))
                    Glide.with(this).load(imgDelta4).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("PSLV"))
                    Glide.with(this).load(imgPSLV).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Proton"))
                    Glide.with(this).load(imgProton).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("GSLV"))
                    Glide.with(this).load(imgGSLV).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Atlas 5"))
                    Glide.with(this).load(imgAtlas5).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Ariane 5"))
                    Glide.with(this).load(imgAriane5).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Vega"))
                    Glide.with(this).load(imgVega).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Minotaur"))
                    Glide.with(this).load(imgMinotaur).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else if (r.contains("Antares"))
                    Glide.with(this).load(imgAntares).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
                else
                    Glide.with(this).load(R.drawable.defaultimg).placeholder(R.drawable.placeholder).into(imageView);
        }
    }

    private void createReminder() {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, launch.getCal().getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, launch.getCal().getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, launch.getMission() + " launch")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, launch.getLocation())
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
            Cursor cursor = CalendarContract.Instances.query(getContentResolver(), null, launch.getCal().getTimeInMillis(), launch.getCal().getTimeInMillis());
            if (cursor.getCount() < 1) {
                startActivity(intent);
            } else
                Toast.makeText(getApplicationContext(), "Reminder already created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 16: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createReminder();
                }
                else {
                    Toast.makeText(this,"Reminder can't be created without permission", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setCountdown(final TextView countdown) {
        hasCountdown = true;
        final DecimalFormat df = new DecimalFormat("00");
        final long millisToLaunch = launch.getCal().getTimeInMillis() - System.currentTimeMillis();
        countDownTimer = new CountDownTimer(millisToLaunch, 1000) {
            public void onTick(long millisUntilFinished) {
                int days = (int) (millisUntilFinished / 1000 / 60 / 60 / 24);
                millisUntilFinished %= 1000 * 60 * 60 * 24;
                int hours = (int) (millisUntilFinished / 1000 / 60 / 60);
                millisUntilFinished %= 1000 * 60 * 60;
                int minutes = (int) (millisUntilFinished / 1000 / 60);
                millisUntilFinished %= 1000 * 60;
                int seconds = (int) (millisUntilFinished / 1000);
                String timeRemaining = "";
                if (days > 0) timeRemaining += df.format(days) + ":";
                timeRemaining += df.format(hours) + ":" + df.format(minutes) + ":" + df.format(seconds);
                countdown.setText("T-" + timeRemaining);
            }

            @Override
            public void onFinish() {
                countdown.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hasCountdown)
            countDownTimer.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detailed_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return false;
    }
}