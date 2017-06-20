package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.utils.WebDataParser;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;

public class FetchSkinsAsyncTask extends AsyncTask<Void, Void, FetchSkinsAsyncTask.FetchSkinsResult> {

    public static final String UPDATE_URL = "http://www.textmuse.com/admin/getskins.php";

    WeakReference<FetchSkinsAsyncTaskHandler> mHandler;
    WeakReference<Context> mContext;

    public FetchSkinsAsyncTask(FetchSkinsAsyncTaskHandler handler, Context context) {
        mHandler = new WeakReference<FetchSkinsAsyncTaskHandler>(handler);
        mContext = new WeakReference<Context>(context);
    }

    @Override
    protected FetchSkinsResult doInBackground(Void... params) {
        Log.d(Constants.TAG, "Starting async task to fetch new data from server");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        result = connUtils.getUrl(UPDATE_URL, null);

        Log.d(Constants.TAG, "Finished async task to get skin data from server, length = " + (result == null ? "null" : Integer.toString(result.length())));

        if (result == null || result.length() <= 0) {
            return FetchSkinsResult.FETCH_FAILED;
        }

        WebDataParser parser = new WebDataParser();
        TextMuseSkinData skinData = parser.parseSkinData(result);

        if (skinData == null) {
            Log.d(Constants.TAG, "Could not parse the skin data from the server");
            return FetchSkinsResult.FETCH_FAILED;
        }

        Log.d(Constants.TAG, "Got skin data from the server");

        Context context = mContext.get();
        if (context != null) {
            skinData.save(context);
        }

        return FetchSkinsResult.FETCH_SUCCEEDED;
    }


    @Override
    protected void onPostExecute(FetchSkinsResult result) {
        FetchSkinsAsyncTaskHandler handler = mHandler.get();
        if (handler != null) {
            handler.handleFetchSkinsResult(result);
        }
    }

    public interface FetchSkinsAsyncTaskHandler {
        public void handleFetchSkinsResult(FetchSkinsResult result);
    }

    public enum FetchSkinsResult {
        FETCH_FAILED,
        FETCH_SUCCEEDED
    }
}
