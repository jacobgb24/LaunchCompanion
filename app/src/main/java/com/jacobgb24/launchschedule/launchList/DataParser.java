package com.jacobgb24.launchschedule.launchList;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jacobgb24.launchschedule.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jacob_000 on 7/25/2015.
 */
class DataParser {

    static List<Launch> parseData(String data, Context context) {
        boolean hr24 = DateFormat.is24HourFormat(context);

        List<Launch> list = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        // Remove data before and after launch fragment_launchlist, remove unwanted tags, consolidate launch time identifier
        data = data.substring(data.indexOf("<div class=\"datename"), data.indexOf("</div>", data.lastIndexOf("missdescrip")) + 6)
                .replaceAll("</?span( [a-z]+=\"(?!launchdate|mission)[^\"]+\")?>|</?[BU]>|</?[aA][^>]*?>", "")
                .replaceAll("&#8217;", "'").replaceAll("&#038;", "+").replaceAll("&amp;", "+").replaceAll("&#8221;", "\"").replaceAll("&#8220;", "\"")
                .replaceAll("</p>", "</div>")
                .replaceAll("\\p{So}+", "•") //replaces �
                .replaceAll("Launch (times?|window|period):", "Launch time:");


        int prevLength = Integer.MAX_VALUE;
        while (data.contains("\"datename\"") && data.length() < prevLength) {
            prevLength = data.length();
            try {
                Launch launch = new Launch();

                // Isolate launch from the rest of the HTML
                String eventData = data.substring(data.indexOf("<div class=\"datename"), data.indexOf("</div>", data.indexOf("missdescrip")) + 6);
                data = data.substring(data.indexOf("</", data.indexOf("missdescrip")) + 6, data.length());

                // Date
                int tmpIndex = eventData.indexOf("launchdate");
                String date = eventData.substring(tmpIndex + 12, eventData.indexOf("<", tmpIndex)).replace("Sept.", "Sep.").trim();
                if(date.equals(""))
                    date="TBD";
                launch.setDate(date);
                launch.setYear(year);

                // Vehicle
                tmpIndex = eventData.indexOf("mission");
                launch.setVehicle(eventData.substring(tmpIndex + 9, eventData.indexOf(" •", tmpIndex)));
                // Mission / Payload
                launch.setMission(eventData.substring(eventData.indexOf("• ", tmpIndex) + 2, eventData.indexOf("<", tmpIndex)));

                // Time / Window
                tmpIndex = eventData.indexOf("missiondata");
                if (eventData.indexOf("time:", tmpIndex) != -1) {
                    tmpIndex = eventData.indexOf("time:", tmpIndex) + 5;
                    String time = eventData.substring(tmpIndex, eventData.toLowerCase().indexOf("<br", tmpIndex)).replaceAll("\\.m\\.", "m").trim();
                    time = time.replaceAll("Approx. (:[0-9]{2})?| on [0-9]{1,2}(st|nd|rd|th)| \\([^)]*\\)", "");
                    time = time.replaceAll("(:[0-9]{2})", "");
                    if(time.trim().equals(""))
                        time="TBD";
                    String newDate = "";
                    // both date and time present
                    if (!time.contains("TBD") && !launch.getDate().contains("TBD")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d HHmm z yyyy", Locale.ENGLISH);
                        if (time.contains("-")) {
                            try {
                                Date d1 = sdf.parse(launch.getDate().replaceAll("\\?|[0-9]{1,2}/|NET|\\.", "").trim() + " " + time.substring(0, time.indexOf("-")) + " GMT"+" "+ launch.getYear());
                                Date d2 = sdf.parse(launch.getDate().replaceAll("\\?|[0-9]{1,2}/|NET|\\.", "").trim() + " " + time.substring(time.indexOf("-") + 1, time.length())+" "+ launch.getYear());
                                if (hr24) sdf.applyPattern("H:mm");
                                else sdf.applyPattern("h:mm a");
                                time = sdf.format(d1);
                                if (hr24) sdf.applyPattern("H:mm z");
                                else sdf.applyPattern("h:mm a z");
                                time += "-" + sdf.format(d2);
                                sdf.applyPattern("MMM. d");
                                newDate = sdf.format(d1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (time.contains("GMT"))
                            try {
                                Date d1 = sdf.parse(launch.getDate().replaceAll("\\?|[0-9]{1,2}/|NET|\\.", "").trim() + " " + time+" "+ launch.getYear());
                                if (hr24) sdf.applyPattern("H:mm z");
                                else sdf.applyPattern("h:mm a z");
                                time = sdf.format(d1);
                                sdf.applyPattern("MMM. d");
                                newDate = sdf.format(d1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        launch.setHasCal(true);
                        launch.setDate(newDate);
                    }
                    //missing date but time set
                    else if (!time.contains("TBD")){
                        SimpleDateFormat sdf = new SimpleDateFormat("HHmm z yyyy", Locale.ENGLISH);
                        if (time.contains("-")) {
                            try {
                                Date d1 = sdf.parse(time.substring(0, time.indexOf("-")) + " GMT" + " " + launch.getYear());
                                Date d2 = sdf.parse(time.substring(time.indexOf("-") + 1, time.length()) + " " + launch.getYear());
                                if (hr24) sdf.applyPattern("H:mm");
                                else sdf.applyPattern("h:mm a");
                                time = sdf.format(d1);
                                if (hr24) sdf.applyPattern("H:mm z");
                                else sdf.applyPattern("h:mm a z");
                                time += "-" + sdf.format(d2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                Date d1 = sdf.parse(time + " " + launch.getYear());
                                if (hr24) sdf.applyPattern("H:mm z");
                                else sdf.applyPattern("h:mm a z");
                                time = sdf.format(d1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        launch.setHasCal(false);
                    }
                    else
                        launch.setHasCal(false);
                    launch.setTime(time);

                }

                // Location
                tmpIndex = eventData.indexOf("missiondata");
                launch.setLocation(eventData.substring(eventData.indexOf("site:", tmpIndex) + 6, eventData.indexOf("</div", tmpIndex)).trim());

                // Description
                tmpIndex = eventData.indexOf("missdescrip");
                launch.setDescription(eventData.substring(eventData.indexOf("p>", tmpIndex) + 2, eventData.indexOf("</div", tmpIndex)));

                // Calendar
                Calendar cal = CalendarCreator(launch, hr24);
                launch.setCal(cal);
                if (cal.get(Calendar.YEAR) > year) {
                    launch.setYear(year = cal.get(Calendar.YEAR));
                }

                //Image URL
                launch.setImgUrl(ImgUrl(launch.getVehicle()));

                //Video Stream URL
                if(launch.getVehicle().contains("Falcon")) {
                    launch.setVidUrl("https://www.spacex.com/webcast/");
                    launch.setVidTitle("SpaceX webcast");
                }
                else if(launch.getVehicle().contains("Delta") || launch.getVehicle().contains("Atlas") || launch.getVehicle().contains("Vulcan")) {
                    launch.setVidUrl("https://www.ulalaunch.com/webcast.aspx");
                    launch.setVidTitle("ULA webcast");
                }
                else if(launch.getLocation().contains("Florida") || launch.getMission().contains("ISS") || launch.getMission().contains("Progress")) {
                    launch.setVidUrl("https://www.nasa.gov/multimedia/nasatv/#public");
                    launch.setVidTitle("NASA TV");
                }
                else if(launch.getLocation().contains("Kazakhstan")) {
                    launch.setVidUrl("https://online.roscosmos.ru/");
                    launch.setVidTitle("Roscosmos");
                }
                else
                    launch.setVidUrl("NONE");


                list.add(launch);
            } catch (Exception e) {
                e.printStackTrace();
                String lastParsed = list.size() > 0 ? list.get(list.size() - 1).getMission() : "N/A";
                FirebaseCrashlytics.getInstance().log("Error parsing launch after "+ lastParsed);
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        return list;
    }

    private static String ImgUrl(String vehicle){
        String imgFalcon9 = "https://i.imgur.com/xYXarSa.jpg",
                imgSoyuz = "https://i.imgur.com/dPKLDG1.jpg",
                imgRockot = "https://i.imgur.com/DFfnUeH.jpg",
                imgH2B = "https://i.imgur.com/bar7eAS.jpg",
                imgLongMarch = "https://i.imgur.com/ouTHTvc.jpg",
                imgDelta2 = "https://i.imgur.com/hjPGe44.jpg",
                imgDelta4 = "https://i.imgur.com/OkWUZvb.jpg",
                imgDelta4H = "https://i.imgur.com/KZ3sms3.jpg",
                imgPSLV = "https://i.imgur.com/tJQguXE.jpg",
                imgProton = "https://i.imgur.com/LVZh3te.jpg",
                imgGSLV = "https://i.imgur.com/wN5kC9U.jpg",
                imgAtlas5 = "https://i.imgur.com/GJt4xBO.jpg",
                imgAriane5 = "https://i.imgur.com/sWDV4kh.jpg",
                imgVega = "https://i.imgur.com/TSPzXUi.jpg",
                imgMinotaur = "https://i.imgur.com/bdQWcx1.jpg",
                imgAntares = "https://i.imgur.com/6k0cDUv.jpg";
        if (vehicle.contains("Falcon"))
            return imgFalcon9;
        else if (vehicle.contains("Soyuz"))
            return imgSoyuz;
        else if (vehicle.contains("Rockot"))
            return imgRockot;
        else if (vehicle.contains("H-2"))
            return imgH2B;
        else if (vehicle.contains("Long March"))
            return imgLongMarch;
        else if (vehicle.contains("Delta 2") || vehicle.contains("Delta II"))
            return imgDelta2;
        else if ((vehicle.contains("Delta 4") || vehicle.contains("Delta IV")) && vehicle.contains("Heavy"))
            return imgDelta4H;
        else if (vehicle.contains("Delta 4") || vehicle.contains("Delta IV"))
            return imgDelta4;
        else if (vehicle.contains("PSLV"))
            return imgPSLV;
        else if (vehicle.contains("Proton"))
            return imgProton;
        else if (vehicle.contains("GSLV"))
            return imgGSLV;
        else if (vehicle.contains("Atlas 5"))
            return imgAtlas5;
        else if (vehicle.contains("Ariane 5"))
            return imgAriane5;
        else if (vehicle.contains("Vega"))
            return imgVega;
        else if (vehicle.contains("Minotaur"))
            return imgMinotaur;
        else if (vehicle.contains("Antares"))
            return imgAntares;
        else
            return "NONE";
    }

    /**
     * Creates a {@link Calendar} from string-based times in an {@link Launch} object.
     *
     * @param launch The launch from which to create the calendar. It does not get modified.
     * @return A {@link } containing the calendar and the accuracy value for the calendar.
     */
    private static Calendar CalendarCreator(Launch launch, boolean hr24) {
        final SimpleDateFormat sdf = new SimpleDateFormat("", Locale.ENGLISH);
        if (hr24) sdf.applyPattern("H:mm zzz MMM d yyyy");
        else sdf.applyPattern("h:mm a zzz MMM d yyyy");
        sdf.setTimeZone(TimeZone.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.clear();
        try {
            boolean hasCal = launch.hasCal();
            int year = launch.getYear();
            String date = launch.getDate().replaceAll("\\?|[0-9]{1,2}/|NET|\\.", "").trim();
            String time = launch.getTime();
            if (hasCal) {
                if (time.contains("-"))
                    time = time.substring(0, time.indexOf("-")) + " " + time.substring(time.lastIndexOf(" ")+1, time.length());

                cal.setTime(sdf.parse(time + " " + date + " " + year));
            }
            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.MONTH, -1);
            if (cal.before(cal2)) {
                cal.add(Calendar.YEAR, 1);
            }
        } catch (ParseException e) {
            cal.clear();
            e.printStackTrace();
        }
        return cal;
    }
}
