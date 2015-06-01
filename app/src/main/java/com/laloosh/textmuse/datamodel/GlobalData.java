package com.laloosh.textmuse.datamodel;

import android.content.Context;

public class GlobalData {

    //Singleton details
    private static class SingletonHolder {
        private static final GlobalData instance = new GlobalData();
    }

    public static GlobalData getInstance() {
        return SingletonHolder.instance;
    }

    private GlobalData() {}
    //End singleton details


    private TextMuseData mData;
    private TextMuseStoredContacts mStoredContacts;
    private TextMuseSettings mSettings;
    private boolean mLoaded = false;

    public void loadData(Context context) {
        mData = TextMuseData.load(context);
        mStoredContacts = TextMuseStoredContacts.load(context);
        mSettings = TextMuseSettings.load(context);
        mLoaded = true;
    }

    public TextMuseData getData() {
        return mData;
    }

    public void updateData(TextMuseData data) {
        Category localTexts = null;
        Category localPhotos = null;
        if (mData != null) {
            localTexts = mData.localTexts;
            localPhotos = mData.localPhotos;
        }

        mData = data;

        mData.localTexts = localTexts;
        mData.localPhotos = localPhotos;
        if (mData.localTexts == null) {
            mData.setupNewLocalNotes();
        }
    }

    public TextMuseStoredContacts getStoredContacts() {
        return mStoredContacts;
    }

    public void updateStoredContacts(TextMuseStoredContacts storedContacts) {
        mStoredContacts = storedContacts;
    }

    public TextMuseSettings getSettings() {
        if (mSettings == null) {
            mSettings = new TextMuseSettings();
        }
        return mSettings;
    }

    public void updateTextMuseSettings(TextMuseSettings settings) {
        mSettings = settings;
    }

    public boolean hasLoadedData() {
        return mLoaded;
    }

}
