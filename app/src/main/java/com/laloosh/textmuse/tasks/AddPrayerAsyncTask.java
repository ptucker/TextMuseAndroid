package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;
import com.laloosh.textmuse.utils.PointsHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class AddPrayerAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String ADD_EVENT_URL = "https://www.textmuse.com/admin/addprayer.php";

    private WeakReference<AddPrayerHandler> mHandler;
    private String mDescription;
    private String mEventDate;
    private String mEmail;
    private Context mContext;
    private int mAppId;

    public AddPrayerAsyncTask(AddPrayerHandler handler, String description, String eventDate, String email, int appId, Context context) {
        mHandler = new WeakReference<AddPrayerHandler>(handler);
        mDescription = description;
        mEventDate = eventDate;
        mEmail = email;
        mAppId = appId;
        mContext = context;
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

        webParams.put("app", Integer.toString(mAppId));

        result = connUtils.postUrl(ADD_EVENT_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to add an event, length = " + (result == null ? "null" : Integer.toString(result.length())));

        if (result != null && result.toLowerCase().contains("success")) {
            PointsHelper.checkPoints(result, mContext);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        AddPrayerHandler handler = mHandler.get();

        if (handler != null) {
            handler.handlePostResult(s);
        }
    }

    public interface AddPrayerHandler {
        public void handlePostResult(String s);
    }
}
