package com.laloosh.textmuse.utils;

import android.text.TextUtils;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.LocalNotification;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.PointUpdate;
import com.laloosh.textmuse.datamodel.TextMuseCurrentSkinData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseLaunchIcon;
import com.laloosh.textmuse.datamodel.TextMuseSkin;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;


public class WebDataParser {

    public TextMuseData parse(String data) {

        if (data == null) {
            return null;
        }

        StringReader reader = new StringReader(data);

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(reader);
            xpp.nextTag();

            return parseContent(xpp);

        } catch (XmlPullParserException e) {
            Log.e(Constants.TAG, "Error parsing XML data", e);
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when parsing XML data", e);
        }

        if (reader != null) {
            reader.close();
        }

        return null;
    }

    //used to parse /admin/getskins.php
    public TextMuseSkinData parseSkinData(String data) {
        if (data == null) {
            return null;
        }

        StringReader reader = new StringReader(data);

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(reader);
            xpp.nextTag();

            return parseSkinContent(xpp);

        } catch (XmlPullParserException e) {
            Log.e(Constants.TAG, "Error parsing XML data", e);
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when parsing XML data", e);
        }

        if (reader != null) {
            reader.close();
        }

        return null;

    }

    //Used to parse generic output
    public PointUpdate parsePointUpdate(String data) {
        if (data == null) {
            return null;
        }

        StringReader reader = new StringReader(data);

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(reader);
            xpp.nextTag();

            PointUpdate result = new PointUpdate();

            xpp.require(XmlPullParser.START_TAG, null, "results");
            xpp.nextTag();
            xpp.require(XmlPullParser.START_TAG, null, "success");

            int attributeCount = xpp.getAttributeCount();
            for (int i = 0; i < attributeCount; i++) {
                String attributeName = xpp.getAttributeName(i);
                String attributeValue = xpp.getAttributeValue(i);

                if (attributeName.equalsIgnoreCase("ep")) {
                    result.ep = Integer.parseInt(attributeValue);
                } else if (attributeName.equalsIgnoreCase("mp")) {
                    result.mp = Integer.parseInt(attributeValue);
                } else if (attributeName.equalsIgnoreCase("sp")) {
                    result.sp = Integer.parseInt(attributeValue);
                }
            }

            return result;
        } catch (XmlPullParserException e) {
            Log.e(Constants.TAG, "Error parsing XML data", e);
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when parsing XML data", e);
        }

        if (reader != null) {
            reader.close();
        }

        return null;
    }

    protected TextMuseSkinData parseSkinContent(XmlPullParser xpp) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "ss");

        TextMuseSkinData parsedData = new TextMuseSkinData();
        parsedData.skins = new ArrayList<TextMuseSkin>();

        while (xpp.next() != XmlPullParser.END_DOCUMENT) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (xpp.getName().equalsIgnoreCase("s")) {
                parsedData.skins.add(parseSkin(xpp));
            }
        }

        return parsedData;
    }

    protected TextMuseSkin parseSkin(XmlPullParser xpp) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "s");

        TextMuseSkin skin = new TextMuseSkin();

        int attributeCount = xpp.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xpp.getAttributeName(i);
            String attributeValue = xpp.getAttributeValue(i);

            if (attributeName.equalsIgnoreCase("id")) {
                skin.skinId = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("color")) {
                skin.color = Integer.parseInt(attributeValue, 16);
            } else if (attributeName.equalsIgnoreCase("icon")) {
                skin.iconUrl = attributeValue;
            }
        }

        skin.name = readText(xpp);

        xpp.require(XmlPullParser.END_TAG, null, "s");

        return skin;
    }

    protected TextMuseData parseContent(XmlPullParser xpp) throws XmlPullParserException, IOException {

        xpp.require(XmlPullParser.START_TAG, null, "notes");

        TextMuseData parsedData = new TextMuseData();

        parsedData.categories = new ArrayList<Category>();
        parsedData.localNotifications = new ArrayList<LocalNotification>();

        //parse top level attributes
        int attributeCount = xpp.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xpp.getAttributeName(i);
            String attributeValue = xpp.getAttributeValue(i);
            if (attributeName.equalsIgnoreCase("ts")) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                parsedData.timestamp = formatter.parseDateTime(attributeValue);
            } else if (attributeName.equalsIgnoreCase("app")) {
                parsedData.appId = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("ep")) {
                parsedData.explorerPoints = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("mp")) {
                parsedData.musePoints = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("sp")) {
                parsedData.sharerPoints = Integer.parseInt(attributeValue);
            }
        }

        //parse everything else
        while (xpp.next() != XmlPullParser.END_DOCUMENT) {

            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = xpp.getName();
            // Starts by looking for the entry tag
            if (name.equals("ns")) {
                parseLocalNotifications(xpp, parsedData);
            } else if (name.equals("ens")) {
                parseNoteExtensions(xpp, parsedData);
            } else if (name.equals("c")) {
                parsedData.categories.add(parseCategory(xpp));
            } else if (name.equals("skin")) {
                parseCurrentSkinData(xpp, parsedData);
            }
        }

        return parsedData;
    }

    protected void parseCurrentSkinData(XmlPullParser xpp, TextMuseData parsedData) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "skin");

        TextMuseCurrentSkinData skinData = new TextMuseCurrentSkinData();
        skinData.launchIcons = new ArrayList<TextMuseLaunchIcon>();

        int attributeCount = xpp.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xpp.getAttributeName(i);
            String attributeValue = xpp.getAttributeValue(i);
            if (attributeName.equalsIgnoreCase("id")) {
                skinData.skinId = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("name")) {
                skinData.name = attributeValue;
            } else if (attributeName.equalsIgnoreCase("c1")) {
                skinData.c1 = Integer.parseInt(attributeValue, 16);
            } else if (attributeName.equalsIgnoreCase("c2")) {
                skinData.c2 = Integer.parseInt(attributeValue, 16);
            } else if (attributeName.equalsIgnoreCase("c3")) {
                skinData.c3 = Integer.parseInt(attributeValue, 16);
            } else if (attributeName.equalsIgnoreCase("home")) {
                skinData.home = attributeValue;
            } else if (attributeName.equalsIgnoreCase("title")) {
                skinData.title = attributeValue;
            } else if (attributeName.equalsIgnoreCase("icon")) {
                skinData.icon = attributeValue;
            } else if (attributeName.equalsIgnoreCase("master")) {
                skinData.masterName = attributeValue;
            } else if (attributeName.equalsIgnoreCase("masterurl")) {
                skinData.masterIconUrl = attributeValue;
            }
        }

        while (xpp.next() != XmlPullParser.END_TAG) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = xpp.getName();
            if (name.equalsIgnoreCase("launch")) {
                TextMuseLaunchIcon icon = new TextMuseLaunchIcon();

                int launchAttributeCount = xpp.getAttributeCount();
                for (int i = 0; i < launchAttributeCount; i++) {
                    String attributeName = xpp.getAttributeName(i);
                    String attributeValue = xpp.getAttributeValue(i);

                    if (attributeName.equalsIgnoreCase("width")) {
                        icon.width = Integer.parseInt(attributeValue);
                    } else if (attributeName.equalsIgnoreCase("url")) {
                        icon.url = attributeValue;
                    }
                }

                skinData.launchIcons.add(icon);

                if (xpp.isEmptyElementTag()) {
                    xpp.next();
                }

                xpp.require(XmlPullParser.END_TAG, null, "launch");
            }

        }

        xpp.require(XmlPullParser.END_TAG, null, "skin");

        parsedData.skinData = skinData;
    }

    protected void parseLocalNotifications(XmlPullParser xpp, TextMuseData parsedData) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "ns");

        while (xpp.next() != XmlPullParser.END_TAG) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = xpp.getName();
            if (name.equalsIgnoreCase("t")) {
                String notification = readText(xpp);

                if (notification != null) {
                    parsedData.localNotifications.add(new LocalNotification(notification));
                }
                xpp.require(XmlPullParser.END_TAG, null, "t");
            }

        }

        xpp.require(XmlPullParser.END_TAG, null, "ns");
    }

    protected void parseNoteExtensions(XmlPullParser xpp, TextMuseData parsedData) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "ens");

        while (xpp.next() != XmlPullParser.END_TAG) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = xpp.getName();
            if (name.equalsIgnoreCase("p")) {
                String p = readText(xpp);

                if (p!= null) {
                    parsedData.preamble = p;
                }
                xpp.require(XmlPullParser.END_TAG, null, "p");
            } else if (name.equalsIgnoreCase("i")) {
                String i = readText(xpp);

                if (i!= null) {
                    parsedData.inquiry = i;
                }
                xpp.require(XmlPullParser.END_TAG, null, "i");
            }

        }

        xpp.require(XmlPullParser.END_TAG, null, "ens");
    }

    protected Category parseCategory(XmlPullParser xpp) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "c");

        Category category = new Category();
        category.notes = new ArrayList<Note>();

        int attributeCount = xpp.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xpp.getAttributeName(i);
            String attributeValue = xpp.getAttributeValue(i);

            if (attributeName.equalsIgnoreCase("name")) {
                category.name = attributeValue;
            } else if (attributeName.equalsIgnoreCase("required")) {
                category.requiredFlag = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("new")) {
                category.newFlag = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("version")) {
                category.versionFlag = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("event")) {
                category.eventCategory = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("id")) {
                category.id = Integer.parseInt(attributeValue);
            }
        }

        while (xpp.next() != XmlPullParser.END_TAG) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = xpp.getName();
            if (name.equalsIgnoreCase("n")) {

                Note note = parseNote(xpp);
                if (note != null) {
                    category.notes.add(note);
                }
            }
        }

        xpp.require(XmlPullParser.END_TAG, null, "c");

        return category;
    }

    protected Note parseNote(XmlPullParser xpp) throws XmlPullParserException, IOException {
        xpp.require(XmlPullParser.START_TAG, null, "n");

        Note note = new Note();

        int attributeCount = xpp.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String attributeName = xpp.getAttributeName(i);
            String attributeValue = xpp.getAttributeValue(i);

            if (attributeName.equalsIgnoreCase("id")) {
                note.noteId = Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("new")) {
                note.newFlag = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("liked")) {
                note.liked = (Integer.parseInt(attributeValue) > 0);
            } else if (attributeName.equalsIgnoreCase("likecount")) {
                note.likeCount =  Integer.parseInt(attributeValue);
            } else if (attributeName.equalsIgnoreCase("edate")) {
                note.eventDate = attributeValue;
            } else if (attributeName.equalsIgnoreCase("loc")) {
                note.location = attributeValue;
            } else if (attributeName.equalsIgnoreCase("notesponsor")) {
                if (!TextUtils.isEmpty(attributeValue)) {
                    note.sponsorId = Integer.parseInt(attributeValue);
                    note.hasSponsor = true;
                }
            } else if (attributeName.equalsIgnoreCase("follow")) {
                if (!TextUtils.isEmpty(attributeValue)) {
                    note.follow = (Integer.parseInt(attributeValue) > 0);
                }
            }
        }

        while (xpp.next() != XmlPullParser.END_TAG) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            if (xpp.isEmptyElementTag()) {
                continue;
            }

            String name = xpp.getName();

            if (name.equalsIgnoreCase("text")) {
                note.text = readText(xpp);
            } else if (name.equalsIgnoreCase("media")) {
                note.mediaUrl = readText(xpp);
            } else if (name.equalsIgnoreCase("url")) {
                note.extraUrl = readText(xpp);
            } else if (name.equalsIgnoreCase("sp_name")) {
                note.sponsorName = readText(xpp);
            } else if (name.equalsIgnoreCase("sp_logo")) {
                note.sponsorLogoUrl = readText(xpp);
            } else {
                xpp.nextTag();
            }
        }

        xpp.require(XmlPullParser.END_TAG, null, "n");

        return note;
    }

    protected String readText(XmlPullParser xpp) throws IOException, XmlPullParserException {
        String result = null;
        if (xpp.next() == XmlPullParser.TEXT) {
            result = xpp.getText();
            xpp.nextTag();
        }
        return result;
    }

}
