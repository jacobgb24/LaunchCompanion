package com.jacobgb24.launchschedule.launchList;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

public class Launch implements Parcelable, Serializable {
    static final long serialVersionUID =5550090631781729025L;
    private String mission, vehicle, location, date, time, description, imgUrl, vidUrl, vidTitle;
    private Calendar cal;
    private int year;
    private boolean hasCal;

    public Launch() {
    }

    public void writeToParcel(Parcel pc, int flags) {
        pc.writeString(mission);
        pc.writeString(vehicle);
        pc.writeString(location);
        pc.writeString(date);
        pc.writeString(time);
        pc.writeString(description);
        pc.writeString(imgUrl);
        pc.writeString(vidUrl);
        pc.writeString(vidTitle);
        pc.writeInt(hasCal ? 1 : 0);
        pc.writeLong(cal.getTimeInMillis());

    }

    private Launch(Parcel pc) {
        mission = pc.readString();
        vehicle = pc.readString();
        location = pc.readString();
        date = pc.readString();
        time = pc.readString();
        description = pc.readString();
        imgUrl = pc.readString();
        vidUrl = pc.readString();
        vidTitle = pc.readString();
        hasCal = pc.readInt() != 0;
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTimeInMillis(pc.readLong());
        cal = (Calendar) tempCal.clone();
    }

    public static final Parcelable.Creator<Launch> CREATOR = new Parcelable.Creator<Launch>() {
        public Launch createFromParcel(Parcel pc) {
            return new Launch(pc);
        }

        public Launch[] newArray(int size) {
            return new Launch[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    String getMission() {
        return mission;
    }

    void setMission(String mission) {
        this.mission = mission;
    }

    String getVehicle() {
        return vehicle;
    }

    void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    String getLocation() {
        return location;
    }

    void setLocation(String location) {
        this.location = location;
    }

    String getDate() {
        return date;
    }

    void setDate(String date) {
        this.date = date;
    }

    String getTime() {
        return time;
    }

    void setTime(String time) {
        this.time = time;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    String getImgUrl() {
        return imgUrl;
    }

    String getVidUrl() {
        return vidUrl;
    }

    void setVidUrl(String vidUrl) {
        this.vidUrl = vidUrl;
    }

    int getYear() {
        return year;
    }

    void setYear(int year) {
        this.year = year;
    }

    Calendar getCal() {
        return cal;
    }

    void setCal(Calendar cal) {
        this.cal = cal;
    }

    boolean hasCal() {
        return hasCal;
    }

    void setHasCal(boolean hasCal) {
        this.hasCal = hasCal;
    }

    String getVidTitle() {
        return vidTitle;
    }

    void setVidTitle(String vidTitle) {
        this.vidTitle = vidTitle;
    }

}
