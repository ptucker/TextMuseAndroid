package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class PrayForAsyncTask extends AsyncTask<Void, Void, String> {

//    www.textmuse.com/admin/following.php?app=56201&sponsor=76&follow=1
    public static final String FOLLOW_URL = "https://www.textmuse.com/admin/praying.php";

    private int mAppId;
    private int mNoteId;

    public PrayForAsyncTask(int appId, int noteId) {
        mAppId = appId;
        mNoteId = noteId;
    }

    @Override
    protected String doInBackground(Void... params) {

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("prayer", Integer.toString(mNoteId));

        result = connUtils.postUrl(FOLLOW_URL, webParams);

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
    }
}
