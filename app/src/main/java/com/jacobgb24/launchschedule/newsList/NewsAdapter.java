package com.jacobgb24.launchschedule.newsList;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jacobgb24.launchschedule.MainActivity;
import com.jacobgb24.launchschedule.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob_000 on 9/16/2015.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<FeedParser.Entry> list = new ArrayList<>();
    private Activity activity;
    private FirebaseAnalytics firebaseAnalytics= FirebaseAnalytics.getInstance(activity.getApplicationContext());

    public NewsAdapter(List<FeedParser.Entry> list, Activity a) {
        this.list = list;
        this.activity = a;
    }

    public void setList(List<FeedParser.Entry> newsList) {
        this.list.clear();
        this.list.addAll(newsList);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int pos) {
        holder.newsTitle.setText(list.get(pos).getTitle());
        holder.newsDate.setText(list.get(pos).getPublished());
        if (!PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getBoolean("pref_disImg", false))
            Glide.with(activity.getApplicationContext()).load(list.get(pos).getImg()).centerCrop().override(200, 200).into(holder.newsImage);
        else
            holder.newsImage.setVisibility(View.GONE);
        holder.newsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).loadURL(list.get(pos).getLink());

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, list.get(pos).getLink());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "News Activity");
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        });

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_list_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView newsTitle, newsDate;
        public ImageView newsImage;
        public LinearLayout newsLinearLayout;

        public ViewHolder(View v) {
            super(v);
            newsTitle = (TextView) v.findViewById(R.id.news_title);
            newsDate = (TextView) v.findViewById(R.id.news_date);
            newsImage = (ImageView) v.findViewById(R.id.news_image);
            newsLinearLayout = (LinearLayout) v.findViewById(R.id.news_item_layout);
        }
    }
}
