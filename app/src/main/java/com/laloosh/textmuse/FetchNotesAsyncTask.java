package com.laloosh.textmuse;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class FetchNotesAsyncTask extends AsyncTask<Void, Void, FetchNotesAsyncTask.FetchNotesResult> {

    public static final String UPDATE_URL = "http://www.textmuse.com/admin/notes.php";

    WeakReference<FetchNotesAsyncTaskHandler> mHandler;
    WeakReference<Context> mContext;
    int mAppId = -1;

    public FetchNotesAsyncTask(FetchNotesAsyncTaskHandler handler, Context context, int appId) {
        mHandler = new WeakReference<FetchNotesAsyncTaskHandler>(handler);
        mContext = new WeakReference<Context>(context);
        mAppId = appId;
    }

    @Override
    protected FetchNotesResult doInBackground(Void... params) {

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

        if (result == null || result.length() <= 0) {
            return FetchNotesResult.FETCH_FAILED;
        }

        WebDataParser parser = new WebDataParser();
        TextMuseData parsedData = parser.parse(result);

        if (parsedData == null) {
            Log.d(Constants.TAG, "Could not parse the notes data from the server");
            return FetchNotesResult.FETCH_FAILED;
        }

        GlobalData globalData = GlobalData.getInstance();
        TextMuseData oldData = globalData.getData();

        if (oldData == null) {
            Log.d(Constants.TAG, "First time getting notes data from server");
            handleDataUpdate(globalData, parsedData);
            return FetchNotesResult.FETCH_SUCCEEDED_DIFFERENT_DATA;
        }

        if (parsedData.isDataSimilar(oldData)) {
            //Don't update anything or save anything if this is the same...
            Log.d(Constants.TAG, "Data from server is the same as the current data--discarding new data");
            return FetchNotesResult.FETCH_SUCCEEDED_SAME_DATA;
        }

        Log.d(Constants.TAG, "Different data downloaded");
        handleDataUpdate(globalData, parsedData);
        return FetchNotesResult.FETCH_SUCCEEDED_DIFFERENT_DATA;

    }

    private void handleDataUpdate(GlobalData globalData, TextMuseData newData) {

        Log.d(Constants.TAG, "Updating and saving data");

        globalData.updateData(newData);

        Context context = mContext.get();
        if (context != null) {
            newData.save(context);
        }
    }

    @Override
    protected void onPostExecute(FetchNotesResult result) {

        FetchNotesAsyncTaskHandler handler = mHandler.get();
        if (handler != null) {
            handler.handleFetchResult(result);
        }

    }

    public interface FetchNotesAsyncTaskHandler {
        public void handleFetchResult(FetchNotesResult result);
    }

    public enum FetchNotesResult {
        FETCH_FAILED,
        FETCH_SUCCEEDED_SAME_DATA,
        FETCH_SUCCEEDED_DIFFERENT_DATA
    }
}
