package com.laloosh.textmuse;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;


public class CalculateFreeSpaceCleanupAsyncTask extends AsyncTask<Void, Void, Void> {

    WeakReference<Context> mContext;

    public CalculateFreeSpaceCleanupAsyncTask(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Context context = mContext.get();
        if (context == null) {
            return null;
        }

        Log.d(Constants.TAG, "Starting async task to cleanup old files");

        try {
            File pictureDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            long folderSize = getFolderSize(pictureDirectory);
            if (folderSize > Constants.MAX_IMAGE_CACHE_SIZE) {
                Log.d(Constants.TAG, "Removing old cache files");
                FileUtils.cleanDirectory(pictureDirectory);
                Log.d(Constants.TAG, "Removal succeeded");
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Exception when attempting to calculate disk space");
            return null;
        }

        Log.d(Constants.TAG, "Finished async task to cleanup old files");

        return null;
    }


    private long getFolderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += getFolderSize(file);
            }
        }
        return length;
    }
}
