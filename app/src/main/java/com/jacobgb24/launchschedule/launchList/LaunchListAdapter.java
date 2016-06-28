/*
 * Copyright 2013 Mark Injerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jacobgb24.launchschedule.launchList;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jacobgb24.launchschedule.R;

import java.util.ArrayList;
import java.util.List;

public class LaunchListAdapter extends RecyclerView.Adapter<LaunchListAdapter.ViewHolder> {
    private List<Launch> list = new ArrayList<>();
    private Activity activity;

    public LaunchListAdapter(List<Launch> list, Activity a) {
        this.list = list;
        this.activity = a;
    }

    public void setList(List<Launch> launchList) {
        this.list.clear();
        this.list.addAll(launchList);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, final int pos) {
        holder.mission.setText(list.get(pos).getMission());
        holder.mission.setSelected(true);
        String test = list.get(pos).getLocation();
        test=test.substring(test.lastIndexOf(",")+1,test.length()).trim();

        holder.vehicle.setText(list.get(pos).getVehicle()+" • "+test);
        holder.date.setText(list.get(pos).getDate());
        holder.time.setText(Html.fromHtml(list.get(pos).getTime()));

        holder.launchLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DetailedActivity.class);
                intent.putExtra("LAUNCH_OBJ", (Parcelable) list.get(pos));
                activity.startActivity(intent);
            }
        });


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.launch_list_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mission, vehicle, date, time;
        public LinearLayout launchLinearLayout;

        public ViewHolder(View v) {
            super(v);
            launchLinearLayout = (LinearLayout) v.findViewById(R.id.launch_item_layout);
            mission = (TextView) v.findViewById(R.id.mission);
            vehicle = (TextView) v.findViewById(R.id.vehicle);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
        }
    }
}
