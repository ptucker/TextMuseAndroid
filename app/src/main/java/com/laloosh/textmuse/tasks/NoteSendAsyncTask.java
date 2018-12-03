package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.PointUpdate;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.utils.ConnectionUtils;
import com.laloosh.textmuse.utils.PointsHelper;
import com.laloosh.textmuse.utils.WebDataParser;

import java.util.HashMap;

public class NoteSendAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String SEND_URL = "https://www.textmuse.com/admin/notesend.php";
    public static final String FIRST_USER_URL = "https://www.textmuse.com/admin/firsttimesender.php";

    int mAppId = -1;
    int mNoteId = -1;
    int mCount = 0;
    int mVersion;
    Context mContext = null;

    public NoteSendAsyncTask(int appId, int noteId, int count, int version, Context context) {
        mAppId = appId;
        mNoteId = noteId;
        mCount = count;
        mVersion = version;
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to send note id to server");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("id", Integer.toString(mNoteId));
        webParams.put("cnt", Integer.toString(mCount));
        webParams.put("version", Integer.toString(mVersion));

        result = connUtils.postUrl(SEND_URL, webParams);

        if (GlobalData.getInstance().getSettings().firstLaunch)
            result = connUtils.postUrl(FIRST_USER_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to send note id to server, length = " + (result == null ? "null" : Integer.toString(result.length())));

        if (result != null && result.toLowerCase().contains("success")) {
            PointsHelper.checkPoints(result, mContext);
        }

        return result;
    }

}
