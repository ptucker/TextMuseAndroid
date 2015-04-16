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


    public void loadData(Context context) {
        mData = TextMuseData.load(context);
    }

    public TextMuseData getData() {
        return mData;
    }

    public void updateData(TextMuseData data) {
        mData = data;
    }

}
