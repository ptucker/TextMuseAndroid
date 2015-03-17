package com.laloosh.textmuse;

import android.os.AsyncTask;
import android.util.Log;


public class FetchNotesAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String UPDATE_URL = "http://www.textmuse.com/admin/notes.php";

    FetchNotesAsyncTaskHandler mHandler;

    public FetchNotesAsyncTask(FetchNotesAsyncTaskHandler handler) {
        mHandler = handler;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to fetch new data from server");

        ConnectionUtils connUtils = new ConnectionUtils();
        String result = connUtils.getUrl(UPDATE_URL, null);

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
