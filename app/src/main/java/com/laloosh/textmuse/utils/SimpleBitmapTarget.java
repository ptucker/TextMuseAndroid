package com.laloosh.textmuse.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class SimpleBitmapTarget implements Target {

    private SimpleBitmapTargetHandler mHandler;

    public SimpleBitmapTarget(SimpleBitmapTargetHandler handler) {
        mHandler = handler;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (mHandler != null) {
            mHandler.finishedLoadingBitmap(bitmap);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d(Constants.TAG, "Bitmap load failed in image target");
        if (mHandler != null) {
            mHandler.finishedLoadingBitmap(null);
        }
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public interface SimpleBitmapTargetHandler {
        public void finishedLoadingBitmap(Bitmap bitmap);
    }

}
