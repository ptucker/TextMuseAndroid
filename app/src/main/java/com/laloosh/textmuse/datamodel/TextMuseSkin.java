package com.laloosh.textmuse.datamodel;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.gson.GsonConverter;

public class TextMuseSkin {
    public int skinId;
    public int color;
    public String iconUrl;
    public String name;

    public String getJson() {
        try {
            Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();
            String json = gson.toJson(this);

            return json;
        } catch (Exception e) {
            Log.d(Constants.TAG, "Could not get gson for skin", e);
            return null;
        }
    }

    public static TextMuseSkin fromJson(String json) {
        try {
            Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();
            TextMuseSkin skin = gson.fromJson(json, TextMuseSkin.class);

            return skin;
        } catch (Exception e) {
            Log.d(Constants.TAG, "Could not convert json to skin: " + json, e);
            return null;
        }

    }
}
