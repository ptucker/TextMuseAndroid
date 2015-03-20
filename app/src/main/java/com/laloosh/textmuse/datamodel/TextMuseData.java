package com.laloosh.textmuse.datamodel;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.laloosh.textmuse.Constants;
import com.laloosh.textmuse.datamodel.gson.GsonConverter;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class TextMuseData {

    public List<Category> categories;
    public List<LocalNotification> localNotifications;

    public DateTime timestamp;
    public int appId;

    //Saves the data to our local preferences
    public void save(Context context) {

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = context.openFileOutput(Constants.DATA_FILE, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(fos);

            String xml = getJson();
            if (xml != null && xml.length() > 0) {
                bos.write(xml.getBytes());
            } else {
                Log.e(Constants.TAG, "Could not get the XML representation of the textmuse data");
            }
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Problem opening file for saving textmuse data", e);
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when writing to file for saving textmuse data", e);
        } finally {
            if (bos != null) {
                IOUtils.closeQuietly(bos);
            }
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    private String getJson() {
        Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();
        String json = gson.toJson(this);
        return json;
    }

    //Loads the cached data from our local preferences
    public static TextMuseData load(Context context) {

        TextMuseData textMuseData = null;
        StringBuilder data = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader buffreader = null;

        try {
            FileInputStream fis = context.openFileInput(Constants.DATA_FILE);
            isr = new InputStreamReader(fis);
            buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                data.append(readString);
                readString = buffreader.readLine();
            }

        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Could not open cached data file", e);
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException reading cache file", e);
        } finally {
            if (buffreader != null) {
                IOUtils.closeQuietly(buffreader);
            }
            if (isr != null) {
                IOUtils.closeQuietly(isr);
            }
        }

        String jsonData = data.toString();

        Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();

        try {
            textMuseData = gson.fromJson(jsonData, TextMuseData.class);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not read in text muse cache data ", e);
        }

        return textMuseData;
    }

}
