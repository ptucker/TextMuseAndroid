package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class AddEventAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String ADD_EVENT_URL = "http://www.textmuse.com/admin/addEvent.php";

    private WeakReference<AddEventHandler> mHandler;
    private String mDescription;
    private String mEventDate;
    private String mEmail;
    private String mLocation;
    private int mSkinId;


    public AddEventAsyncTask(AddEventHandler handler, String description, String eventDate, String email, String location, int skinId) {
        mHandler = new WeakReference<AddEventHandler>(handler);
        mDescription = description;
        mEventDate = eventDate;
        mEmail = email;
        mLocation = location;
        mSkinId = skinId;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to add an event");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        if (!TextUtils.isEmpty(mDescription)) {
            webParams.put("desc", mDescription);
        }

        if (!TextUtils.isEmpty(mEventDate)) {
            webParams.put("edate", mEventDate);
        }

        if (!TextUtils.isEmpty(mEmail)) {
            webParams.put("email", mEmail);
        }

        if (!TextUtils.isEmpty(mLocation)) {
            webParams.put("loc", mLocation);
        }

        webParams.put("spon", Integer.toString(mSkinId));

        result = connUtils.postUrl(ADD_EVENT_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to add an event, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        AddEventHandler handler = mHandler.get();

        if (handler != null) {
            handler.handlePostResult(s);
        }
    }

    public interface AddEventHandler {
        public void handlePostResult(String s);
    }
}
