package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class RemitBadgeAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String REMIT_BADGE_URL = "https://www.textmuse.com/admin/remitbadge.php";

    private WeakReference<RemitBadgeEventHandler> mHandler;
    private int mAppId;
    private int mMessageId;

    public RemitBadgeAsyncTask(RemitBadgeEventHandler handler, int appId, int messageId) {
        mHandler = new WeakReference<RemitBadgeEventHandler>(handler);
        mAppId = appId;
        mMessageId = messageId;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to remit badge");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("game", Integer.toString(-1 * mMessageId));

        result = connUtils.postUrl(REMIT_BADGE_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to remit a badge, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        RemitBadgeEventHandler handler = mHandler.get();

        if (handler != null) {
            handler.handleRemitPostResult(s);
        }
    }

    public interface RemitBadgeEventHandler {
        public void handleRemitPostResult(String s);
    }
}
