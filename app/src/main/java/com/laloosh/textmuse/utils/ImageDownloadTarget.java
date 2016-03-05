package com.laloosh.textmuse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Note;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class ImageDownloadTarget implements Target {

    private WeakReference<Context> mContext;
    private Note mNote;

    public ImageDownloadTarget(Context context, Note textMuseNote) {
        mContext = new WeakReference<Context>(context);
        mNote = textMuseNote;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.d(Constants.TAG, "Bitmap loaded in image download target");

        Context context = mContext.get();

        if (!isExternalStorageWritable() || context == null) {
            mNote.saveFailed = true;
            mNote.savedInternally = false;
            return;
        }

        FileOutputStream fos = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), mNote.getInternalFilename());
            Log.d(Constants.TAG, "Attempting to write image to file at path: " + file.getAbsolutePath());
            fos = new FileOutputStream(file, false);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not write note with ID " + mNote.noteId + " to file.", e);
            mNote.saveFailed = true;
            mNote.savedInternally = false;
            return;
        } finally {
            IOUtils.closeQuietly(fos);
        }

        Log.d(Constants.TAG, "File write successful");
        mNote.saveFailed = false;
        mNote.savedInternally = true;
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d(Constants.TAG, "Bitmap load failed in image download target");
        mNote.savedInternally = false;
        mNote.saveFailed = true;
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        mNote.saveFailed = false;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
