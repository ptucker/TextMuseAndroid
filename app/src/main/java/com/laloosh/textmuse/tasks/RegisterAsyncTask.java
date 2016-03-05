package com.laloosh.textmuse.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class RegisterAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String FEEDBACK_URL = "http://www.textmuse.com/admin/adduser.php";

    WeakReference<RegisterAsyncTaskHandler> mHandler;
    String mName;
    String mEmail;
    int mBirthMonth;
    int mBirthYear;

    public RegisterAsyncTask(RegisterAsyncTaskHandler handler, String name, String email, int birthMonth, int birthYear) {
        mHandler = new WeakReference<RegisterAsyncTaskHandler>(handler);
        mName = name;
        mEmail = email;
        mBirthMonth = birthMonth;
        mBirthYear = birthYear;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to register");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        if (mName != null && mName.length() > 0) {
            webParams.put("name", mName);
        }

        if (mEmail != null && mEmail.length() > 0) {
            webParams.put("email", mEmail);
        }

        webParams.put("bmonth", Integer.toString(mBirthMonth));
        webParams.put("byear", Integer.toString(mBirthYear));

        result = connUtils.postUrl(FEEDBACK_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to register, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        RegisterAsyncTaskHandler handler = mHandler.get();

        if (handler != null) {
            handler.handleRegisterResult(s != null);
        }
    }

    public interface RegisterAsyncTaskHandler {
        public void handleRegisterResult(boolean success);
    }
}
