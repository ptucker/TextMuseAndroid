package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class FlagContentAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String FLAG_CONTENT_URL = "http://www.textmuse.com/admin/flagmessage.php";

    private WeakReference<FlagContentResultHandler> mHandler;
    private int mMessageId;

    public FlagContentAsyncTask(FlagContentResultHandler handler, int messageId) {
        mHandler = new WeakReference<FlagContentResultHandler>(handler);
        mMessageId = messageId;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to flag content");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("id", Integer.toString(mMessageId));

        result = connUtils.postUrl(FLAG_CONTENT_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to flag content, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        FlagContentResultHandler handler = mHandler.get();

        if (handler != null) {
            handler.handleFlagPostResult(s);
        }
    }

    public interface FlagContentResultHandler {
        public void handleFlagPostResult(String s);
    }
}
