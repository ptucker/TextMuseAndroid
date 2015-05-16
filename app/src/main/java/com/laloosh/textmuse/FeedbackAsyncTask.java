package com.laloosh.textmuse;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class FeedbackAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String FEEDBACK_URL = "http://www.textmuse.com/admin/postfeedback.php";

    WeakReference<FeedbackAsyncTaskHandler> mHandler;
    String mName;
    String mEmail;
    String mFeedback;

    public FeedbackAsyncTask(FeedbackAsyncTaskHandler handler, String name, String email, String feedback) {
        mHandler = new WeakReference<FeedbackAsyncTaskHandler>(handler);
        mName = name;
        mEmail = email;
        mFeedback = feedback;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to post feedback");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        if (mName != null && mName.length() > 0) {
            webParams.put("name", mName);
        }

        if (mEmail != null && mEmail.length() > 0) {
            webParams.put("email", mEmail);
        }

        if (mFeedback != null && mFeedback.length() > 0) {
            webParams.put("feedback", mFeedback);
        }

        result = connUtils.postUrl(FEEDBACK_URL, null, webParams);

        Log.d(Constants.TAG, "Finished async task to post feedback, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        FeedbackAsyncTaskHandler handler = mHandler.get();

        if (handler != null) {
            handler.handleFeedbackResult(s != null);
        }
    }

    public interface FeedbackAsyncTaskHandler {
        public void handleFeedbackResult(boolean success);
    }
}
