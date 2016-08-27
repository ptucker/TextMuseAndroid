package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.PointUpdate;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.utils.ConnectionUtils;
import com.laloosh.textmuse.utils.PointsHelper;
import com.laloosh.textmuse.utils.WebDataParser;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class ViewCategoryAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String REMIT_DEAL_URL = "http://www.textmuse.com/admin/viewcategory.php";

    private WeakReference<DefaultResultHandler> mHandler;
    private int mAppId;
    private int mCategoryId;
    private Context mContext;

    public ViewCategoryAsyncTask(DefaultResultHandler handler, int appId, int categoryId, Context context) {
        mHandler = new WeakReference<DefaultResultHandler>(handler);
        mAppId = appId;
        mCategoryId = categoryId;
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to view category");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("cat", Integer.toString(mCategoryId));

        result = connUtils.postUrl(REMIT_DEAL_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to view a category, length = " + (result == null ? "null" : Integer.toString(result.length())));

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

        DefaultResultHandler handler = mHandler.get();

        if (handler != null) {
            handler.handleResult(s);
        }
    }
}
