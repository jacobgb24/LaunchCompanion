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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
    private FirebaseAnalytics firebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false))
            setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
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
        LinearLayout videoItem = (LinearLayout) findViewById(R.id.dcard_vid_item);
        TextView videoTitle = (TextView) findViewById(R.id.dcard_videourl);

        if (!launch.hasCal() || launch.getCal().getTimeInMillis() < System.currentTimeMillis()) {
            countdown.setVisibility(View.GONE);
        } else {
            setCountdown(countdown);
        }

        if(launch.getVidUrl().equals("NONE") || !launch.hasCal()){
            videoItem.setVisibility(View.GONE);
        }
        else{
            videoTitle.setText("Watch " + launch.getVidTitle());
        }

        timeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (launch.hasCal() && !(launch.getCal().getTimeInMillis() < System.currentTimeMillis())) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailedActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, 16);
                    }
                    else {
                        createReminder();
                    }
                }
            }
        });
        locItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, launch.getMission());
                    firebaseAnalytics.logEvent("view_location", bundle);
                    startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + launch.getLocation())));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Could not open map", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        videoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, launch.getMission());
                firebaseAnalytics.logEvent("watch_webcast", bundle);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(launch.getVidUrl()));
                startActivity(browserIntent);
            }
        });

        time.setText(launch.getDate() + " at " + launch.getTime());
        location.setText(launch.getLocation());
        details.setText(launch.getDescription());
        subtitle.setText(launch.getVehicle());

        //Load Image
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_noImages", false)) {
            String r = launch.getVehicle();
            if (r.contains("Falcon") && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_easteregg", false)) {
                    Glide.with(this).load("https://thumbs.gfycat.com/HandyCleverAustralianshelduck-size_restricted.gif").asGif().error(R.drawable.defaultimg).into(imageView);
                    Toast.makeText(getApplicationContext(), "You found the easter egg!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    firebaseAnalytics.logEvent("easteregg_found", bundle);
            }
            else if(!launch.getImgUrl().equals("NONE"))
                Glide.with(this).load(launch.getImgUrl()).placeholder(R.drawable.placeholder).error(R.drawable.defaultimg).into(imageView);
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
                    .putExtra(CalendarContract.Events.ORIGINAL_ID, launch.getMission().hashCode())
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
            Cursor cursor = CalendarContract.Instances.query(getContentResolver(), null, launch.getCal().getTimeInMillis(), launch.getCal().getTimeInMillis(), launch.getMission());
            if (cursor.getCount() < 1) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, launch.getMission());
                firebaseAnalytics.logEvent("create_reminder", bundle);
                startActivity(intent);
            } else
                Toast.makeText(getApplicationContext(), "Reminder already created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log("Error making reminder");
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 16: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createReminder();
                } else {
                    Toast.makeText(this, "Reminder can't be created without permission", Toast.LENGTH_SHORT).show();
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
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}