package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class FollowSponsorAsyncTask extends AsyncTask<Void, Void, String> {

//    www.textmuse.com/admin/following.php?app=56201&sponsor=76&follow=1
    public static final String FOLLOW_URL = "http://www.textmuse.com/admin/following.php";

    private WeakReference<FollowResultHandler> mHandler;
    private int mAppId;
    private int mSponsorId;
    private int mFollow;

    public FollowSponsorAsyncTask(FollowResultHandler handler, int appId, int sponsorId, boolean follow) {
        mHandler = new WeakReference<FollowResultHandler>(handler);
        mAppId = appId;
        mSponsorId = sponsorId;
        mFollow = follow ? 1 : 0;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to follow sponsor: sponsor id: " + mSponsorId + " with follow :" + mFollow);

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("sponsor", Integer.toString(mSponsorId));
        webParams.put("follow", Integer.toString(mFollow));

        result = connUtils.postUrl(FOLLOW_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to follow sponsor, length = " + (result == null ? "null" : Integer.toString(result.length())));
        Log.d(Constants.TAG, "Finished async task to follow sponsor, response was " + (result == null ? "null" : result));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        FollowResultHandler handler = mHandler.get();

        if (handler != null) {
            boolean result = (s!= null && s.toLowerCase().contains("success"));
            handler.handleFlagPostResult(result);
        }
    }

    public interface FollowResultHandler {
        public void handleFlagPostResult(boolean success);
    }
}
