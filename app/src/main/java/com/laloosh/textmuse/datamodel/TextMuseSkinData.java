package com.laloosh.textmuse.datamodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;

import java.util.ArrayList;

public class TextMuseSkinData {
    public ArrayList<TextMuseSkin> skins;

    //Skins aren't huge and are thus stored and loaded in the shared preferences.
    public static TextMuseSkinData load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);

        int skinCount = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_SKIN_COUNT, -1);
        if (skinCount <= 0) {
            return null;
        }

        TextMuseSkinData data = new TextMuseSkinData();
        data.skins = new ArrayList<TextMuseSkin>(skinCount);
        for (int i = 0; i < skinCount; i++) {
            String key = Constants.SHARED_PREF_KEY_SKIN_BASE + Integer.toString(i);
            String value = sharedPreferences.getString(key, null);
            if (value != null) {
                TextMuseSkin skin = TextMuseSkin.fromJson(value);
                if (skin != null) {
                    data.skins.add(skin);
                }
            }
        }

        return data;
    }

    public void save(Context context) {
        if (skins == null || skins.size() <= 0) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SHARED_PREF_KEY_SKIN_COUNT, skins.size());
        int curIndex = 0;
        for (TextMuseSkin skin : skins) {
            String key = Constants.SHARED_PREF_KEY_SKIN_BASE + Integer.toString(curIndex++);
            String value = skin.getJson();

            editor.putString(key, value);
        }

        editor.commit();
    }

    //Gets the currently selected skin, or -1 if no skin is selected
    public static int getCurrentlySelectedSkin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SHARED_PREF_KEY_SKIN_CURRENT_ID, -1);
    }

    public static void setCurrentlySelectedSkin(Context context, int id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (id <= 0) {
            editor.remove(Constants.SHARED_PREF_KEY_SKIN_CURRENT_ID);
        } else {
            editor.putInt(Constants.SHARED_PREF_KEY_SKIN_CURRENT_ID, id);
        }
        editor.commit();

        Log.d(Constants.TAG, "Saved selected skin as: " + Integer.toString(id));
    }
}
