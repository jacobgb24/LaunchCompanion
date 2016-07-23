package com.jacobgb24.launchschedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by jacob_000 on 7/29/2015.
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_darkTheme", false))
            setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        firebaseAnalytics.logEvent("about_opened", new Bundle());
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AboutFragment())
                .commit();

    }

    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_about);

            Preference version = findPreference("about_version");
            version.setTitle("Version " + getResources().getString(R.string.app_version));
            version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                int count = 0;
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    count++;
                    if (count == 8) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_easteregg", false)) {
                            editor.putBoolean("pref_easteregg", false);
                            Toast.makeText(getActivity().getApplicationContext(), "Easter egg disabled", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            editor.putBoolean("pref_easteregg", true);
                            Toast.makeText(getActivity().getApplicationContext(), "Easter egg enabled", Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity().getApplicationContext(), "To disable, tap on the version 8 times", Toast.LENGTH_SHORT).show();
                        }
                        editor.commit();
                        count = 0;
                    }
                    return false;
                }
            });
            Preference email = findPreference("about_email");
            email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Intent send = new Intent(Intent.ACTION_SENDTO);
                        String uriText = "mailto:" + Uri.encode("jacobldavis98@gmail.com") +
                                "?subject=" + Uri.encode("Launch Companion");
                        Uri uri = Uri.parse(uriText);

                        send.setData(uri);
                        startActivity(Intent.createChooser(send, "Send mail..."));

                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Couldn't send email", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            Preference privacypolicy = findPreference("about_privacyPolicy");
            privacypolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        WebView view = new WebView(getActivity());
                        view.loadUrl("https://jacobgb24.github.io/Launch-Companion-Privacy-Policy/");
                        view.getSettings().setJavaScriptEnabled(false);
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                                .setView(view)
                                .setPositiveButton("ok", null)
                                .create();
                        alertDialog.show();
                    }
                    catch (Exception e){
                        Toast.makeText(getActivity().getApplicationContext(), "Could not open the privacy policy", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            Preference images = findPreference("about_imgs");
            images.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent imgur = new Intent(Intent.ACTION_VIEW, Uri.parse("http://imgur.com/a/OJGIP"));
                    startActivity(imgur);
                    return false;
                }
            });
            Preference sfn = findPreference("about_sfn");
            sfn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent sfn = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spaceflightnow.com"));
                    startActivity(sfn);
                    return false;
                }
            });
            Preference sn = findPreference("about_sn");
            sn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent sn = new Intent(Intent.ACTION_VIEW, Uri.parse("http://spacenews.com"));
                    startActivity(sn);
                    return false;
                }
            });
            Preference glide = findPreference("about_glide");
            glide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    WebView view = new WebView(getActivity());
                    view.loadUrl("file:///android_asset/licenseGlide");
                    AlertDialog alertDialog= new AlertDialog.Builder(getActivity())
                            .setTitle("Glide")
                            .setView(view)
                            .setPositiveButton("ok", null)
                            .create();
                    alertDialog.show();
                    return false;
                }
            });
            Preference lclock = findPreference("about_lclock");
            lclock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    WebView view = new WebView(getActivity());
                    view.loadUrl("file:///android_asset/licenseLclock");
                    AlertDialog alertDialog= new AlertDialog.Builder(getActivity())
                            .setTitle("L-Clock")
                            .setView(view)
                            .setPositiveButton("ok", null)
                            .create();
                    alertDialog.show();
                    return false;
                }
            });
        }
    }
}
