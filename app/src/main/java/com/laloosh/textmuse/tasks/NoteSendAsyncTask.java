package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.util.HashMap;

public class NoteSendAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String SEND_URL = "http://www.textmuse.com/admin/notesend.php";

    int mAppId = -1;
    int mNoteId = -1;
    int mCount = 0;

    public NoteSendAsyncTask(int appId, int noteId, int count) {
        mAppId = appId;
        mNoteId = noteId;
        mCount = count;
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

        result = connUtils.postUrl(SEND_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to send note id to server, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

}
