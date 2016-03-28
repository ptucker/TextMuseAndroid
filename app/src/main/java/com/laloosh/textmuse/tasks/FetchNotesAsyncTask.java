package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.laloosh.textmuse.utils.WebDataParser;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseCurrentSkinData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseLaunchIcon;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.laloosh.textmuse.utils.ConnectionUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public class FetchNotesAsyncTask extends AsyncTask<Void, Void, FetchNotesAsyncTask.FetchNotesResult> {

    public static final String UPDATE_URL = "http://www.textmuse.com/admin/notes.php";
//    http://www.textmuse.com/admin/notes.php?ts=2015-5-17%2021:04:32&app=25681&sponsor=7
    WeakReference<FetchNotesAsyncTaskHandler> mHandler;
    WeakReference<Context> mContext;
    int mAppId = -1;
    int mSkinId = -1;

    //Uses the skin ID that's been chosen already
    public FetchNotesAsyncTask(FetchNotesAsyncTaskHandler handler, Context context, int appId) {
        mHandler = new WeakReference<FetchNotesAsyncTaskHandler>(handler);

        //Use the application context since the download might take longer than the current screen in the worst case
        mContext = new WeakReference<Context>(context.getApplicationContext());
        mAppId = appId;

        mSkinId = TextMuseSkinData.getCurrentlySelectedSkin(context);
    }

    //Different constructor that expects an explicit skin ID
    public FetchNotesAsyncTask(FetchNotesAsyncTaskHandler handler, Context context, int appId, int skinId) {
        mHandler = new WeakReference<FetchNotesAsyncTaskHandler>(handler);

        //Use the application context since the download might take longer than the current screen in the worst case
        mContext = new WeakReference<Context>(context.getApplicationContext());
        mAppId = appId;

        mSkinId = skinId;
    }

    @Override
    protected FetchNotesResult doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to fetch new data from server");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("highlight", "1");
        if (mAppId > 0) {
            webParams.put("app", Integer.toString(mAppId));
            Log.d(Constants.TAG, "Fetching new data for app ID: " + Integer.toString(mAppId));
        }

        if (mSkinId > 0) {
            webParams.put("sponsor", Integer.toString(mSkinId));
            Log.d(Constants.TAG, "Fetching new data for skin ID: " + Integer.toString(mSkinId));
        }

        result = connUtils.getUrl(UPDATE_URL, webParams);

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

            //Still update our photos
            Context context = mContext.get();
            if (context != null) {
                oldData.updatePhotos(context);
            }
            return FetchNotesResult.FETCH_SUCCEEDED_SAME_DATA;
        }

        Log.d(Constants.TAG, "Different data downloaded");
        handleDataUpdate(globalData, parsedData);
        return FetchNotesResult.FETCH_SUCCEEDED_DIFFERENT_DATA;

    }

    private void handleDataUpdate(GlobalData globalData, TextMuseData newData) {

        Log.d(Constants.TAG, "Updating and saving data");

        TextMuseSettings settings = GlobalData.getInstance().getSettings();
        if (settings != null) {
            newData.reorderCategories(settings.getCategoryOrder());
        }

        TextMuseData oldData = globalData.getData();

        globalData.updateData(newData);

        Context context = mContext.get();
        if (context != null) {

            newData.updatePhotos(context);
            newData.updateNoteImageFlags(context);
            newData.save(context);
        }


        //If the skin info changed, make sure that we have the launch screens
        if (oldData == null) {
            if (newData.skinData != null) {
                downloadLaunchImages(newData);
            }
        } else {
            if ((oldData.skinData == null && newData.skinData != null) || (oldData.skinData != null && newData.skinData != null && oldData.skinData.skinId != newData.skinData.skinId)) {
                downloadLaunchImages(newData);
            }
        }
    }

    //This downloads the launch images one by one on this thread.  Since this is down on an asynctask, this is ok
    private void downloadLaunchImages(TextMuseData newData) {
        Context context = mContext.get();
        if (context == null) {
            return;
        }

        HashMap<Integer, Integer> widthToCountMap = new HashMap<Integer, Integer>();
        TextMuseCurrentSkinData skinData = newData.skinData;
        for (TextMuseLaunchIcon icon : skinData.launchIcons) {
            if (!widthToCountMap.containsKey(icon.width)) {
                widthToCountMap.put(icon.width, 0);
            }

            int curCount = widthToCountMap.get(icon.width);
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), skinData.getSplashImageFilename(icon, curCount));
            widthToCountMap.put(icon.width, curCount + 1);

            downloadImageToFile(context, icon.url, file);
        }

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), skinData.getIconImageFilename());
        downloadImageToFile(context, skinData.icon, file);
    }

    //Returns true if succeeded
    private boolean downloadImageToFile(Context context, String url, File file) {
        try {
            Bitmap bitmap = Picasso.with(context)
                    .load(url)
                    .get();

            if (bitmap != null) {
                FileOutputStream fos = null;
                try {
                    Log.d(Constants.TAG, "Attempting to write splash image to file at path: " + file.getAbsolutePath());
                    fos = new FileOutputStream(file, false);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                    fos.close();
                } catch (Exception ioerror) {
                    Log.e(Constants.TAG, "Could not write bitmap for splash image with url " + url, ioerror);
                    return false;
                } finally {
                    IOUtils.closeQuietly(fos);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not download the icon at " + url, e);
            return false;
        }

        Log.d(Constants.TAG, "Succeeded in writing image with url " + url + " to file at path: " + file.getAbsolutePath());

        return true;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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
