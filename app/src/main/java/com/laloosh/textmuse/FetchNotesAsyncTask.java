package com.laloosh.textmuse;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;


public class FetchNotesAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String UPDATE_URL = "http://www.textmuse.com/admin/notes.php";

    FetchNotesAsyncTaskHandler mHandler;
    int mAppId = -1;

    public FetchNotesAsyncTask(FetchNotesAsyncTaskHandler handler, int appId) {
        mHandler = handler;
        mAppId = appId;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to fetch new data from server");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        if (mAppId > 0) {
            HashMap<String, String> webParams = new HashMap<String, String>();
            webParams.put("app", Integer.toString(mAppId));
            result = connUtils.getUrl(UPDATE_URL, webParams);
        } else {
            result = connUtils.getUrl(UPDATE_URL, null);
        }

        Log.d(Constants.TAG, "Finished async task to fetch new data from server, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler != null) {
            mHandler.handleFetchResult(s);
        }
    }

    public static interface FetchNotesAsyncTaskHandler {
        public void handleFetchResult(String s);
    }
}
