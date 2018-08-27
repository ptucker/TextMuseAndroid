package com.laloosh.textmuse.datamodel;

import android.content.Context;

import com.laloosh.textmuse.utils.GuidedTour;

import java.util.HashSet;
import java.util.Iterator;

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
    private Context mContext;
    private TextMuseStoredContacts mStoredContacts;
    private TextMuseSettings mSettings;
    private GuidedTour mGuidedTour;
    private boolean mLoaded = false;

    public void loadData(Context context) {
        mContext = context;
        mData = TextMuseData.load(context);
        mStoredContacts = TextMuseStoredContacts.load(context);

        if (mStoredContacts == null) {
            mStoredContacts = new TextMuseStoredContacts();
        }

        TextMuseSettings settings = TextMuseSettings.load(context);
        if (settings != null)
            mSettings = settings;
        mLoaded = mData != null;

        mGuidedTour = new GuidedTour();
    }

    public TextMuseData getData() {
        return mData;
    }

    public void updateData(TextMuseData data) {
        Category localTexts = null;
        Category localPhotos = null;
        Category pinnedNotes = null;
        HashSet<Integer> flaggedNotes = null;
        if (mData != null) {
            localTexts = mData.localTexts;
            localPhotos = mData.localPhotos;
            pinnedNotes = mData.pinnedNotes;
            flaggedNotes = mData.flaggedNotesSet;
        }

        mData = data;

        mData.localTexts = localTexts;
        mData.localPhotos = localPhotos;
        mData.pinnedNotes = pinnedNotes;
        mData.flaggedNotesSet = flaggedNotes;
        if (mData.localTexts == null) {
            mData.setupNewLocalNotes();
        }

        mData.filterFlaggedNotes();
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

    public GuidedTour getGuidedTour() { return mGuidedTour; }

    public boolean hasLoadedData() {
        return mLoaded;
    }

}
