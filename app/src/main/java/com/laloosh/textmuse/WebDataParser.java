package com.laloosh.textmuse;

import android.util.Log;

import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.LocalNotification;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;

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

            return parseContent(xpp);          //TODO: need to actually return this value somewhere


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
            } else if (name.equals("c")) {
                parsedData.categories.add(parseCategory(xpp));
            }
        }

        return parsedData;
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
