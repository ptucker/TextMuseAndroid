package com.laloosh.textmuse.datamodel;


import android.content.Context;
import android.util.Log;

import com.laloosh.textmuse.Constants;

import java.util.ArrayList;
import java.util.HashSet;

public class TextMuseSettings {

    public boolean notifications = true;
    public boolean saveRecentContacts = true;
    public int recentContactLimit = Constants.DEFAULT_NUMBER_RECENT_CONTACTS;

    //This arraylist is used for serialization to file, but is not used for lookups. This is not
    //actually kept up to date besides the save/load
    public ArrayList<String> unshownCategories;

    //This is a nicer data structure for holding the list of what categories we should not show, but
    //gson doesn't serialize sets very well, so we'll maintain this on the fly and not serialize it.
    //This is the data structure that we use during normal operations
    private transient HashSet<String> unshownCategoriesSet;

    public TextMuseSettings() {
        saveRecentContacts = true;
        recentContactLimit = Constants.DEFAULT_NUMBER_RECENT_CONTACTS;
        unshownCategories = new ArrayList<String> ();
        unshownCategoriesSet = new HashSet<String> ();
    }

    //Saves the data to our local preferences
    public void save(Context context) {

        //showncategories is how we will store this data, since it is easily serialized by gson
        //this is really a workaround until I can figure out a good way to deserialize sets
        if (unshownCategories == null) {
            unshownCategories = new ArrayList<String>();
        }

        unshownCategories.clear();
        for (String name : unshownCategoriesSet) {
            unshownCategories.add(name);
        }

        if (!DataPersistenceHelper.save(context, Constants.SETTINGS_FILE, this)) {
            Log.e(Constants.TAG, "Could not save settings data");
        }
    }

    //Loads the cached data from our local preferences
    public static TextMuseSettings load(Context context) {
        TextMuseSettings settings = DataPersistenceHelper.load(context, Constants.SETTINGS_FILE, TextMuseSettings.class);

        if (settings != null) {
            if (settings.unshownCategoriesSet == null) {
                settings.unshownCategoriesSet = new HashSet<String>();
            }

            settings.unshownCategoriesSet.clear();
            for (String name : settings.unshownCategories) {
                settings.unshownCategoriesSet.add(name);
            }
        }

        return settings;
    }


    public void setShowCategory(String categoryName, boolean shouldShow) {
        if (!shouldShow) {
            unshownCategoriesSet.add(categoryName);
        } else {
            unshownCategoriesSet.remove(categoryName);
        }
    }

    public boolean shouldShowCategory(String categoryName) {
        return !(unshownCategoriesSet.contains(categoryName));
    }
}
