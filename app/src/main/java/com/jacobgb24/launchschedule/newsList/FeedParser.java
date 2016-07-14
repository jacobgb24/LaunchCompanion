/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jacobgb24.launchschedule.newsList;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


class FeedParser {

    private static final String ns = null;

    public static List<Entry> parse(InputStream in)
            throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private static List<Entry> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        List<Entry> entries = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();
            if (name.equals("item"))
                    entries.add(readEntry(parser));
            else
                skip(parser);
        }
        return entries;
    }

    private static Entry readEntry(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String img = null;
        String title = null;
        String link = null;
        String publishedOn = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("description")) {
                img= readImgTag(parser, "description");
            } else if (name.equals("title")) {
                title = readBasicTag(parser, "title");
            } else if (name.equals("link")) {
                String tempLink = readBasicTag(parser, "link");
                if (tempLink != null) {
                    link = tempLink;
                }
            } else if (name.equals("pubDate")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("", Locale.ENGLISH);
                    sdf.applyPattern("E, d MMM yyyy");
                    Date date = sdf.parse(readBasicTag(parser, "pubDate"));
                    sdf.applyPattern("d MMM, yyyy");
                    publishedOn = sdf.format(date);
            } else {
                skip(parser);
            }
        }
            return new Entry(img, title, link, publishedOn);
    }

    private static String readBasicTag(XmlPullParser parser, String tag)
        throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, tag);
            String result = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, tag);
            return result;
    }

    private static String readImgTag(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        String link;
        parser.require(XmlPullParser.START_TAG, ns, tag);
        link = readText(parser);
        try {
            link = link.substring(link.indexOf("src") + 4, link.indexOf("class")).replaceAll("\"", "");
        } catch (Exception e){ e.printStackTrace(); }
        while (true) {
            if (parser.nextTag() == XmlPullParser.END_TAG) break;
        }
        return link;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static class Entry {
        public final String img;
        public final String title;
        public final String link;
        public final String published;

        Entry(String img, String title, String link, String published) {
            this.img = img;
            this.title = title;
            this.link = link;
            this.published = published;
        }
        public String getTitle() {
            return title;
        }

        public String getImg() {
            return img;
        }

        public String getLink() {
            return link;
        }

        public String getPublished() {
            return published;
        }
    }
}
