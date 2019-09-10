package com.laloosh.textmuse.dialogs;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;


public class ImageSizeDownloadTarget implements Target {

    WeakReference<ImageSizeDownloadTargetHandler> mHandler;

    public ImageSizeDownloadTarget(ImageSizeDownloadTargetHandler handler) {
        mHandler = new WeakReference<ImageSizeDownloadTargetHandler>(handler);
    }

    @Override
    public void onBitmapFailed(Exception ex, Drawable errorDrawable) {
        Log.d(Constants.TAG, "Bitmap load failed: " + ex.getLocalizedMessage());
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        ImageSizeDownloadTargetHandler handler = mHandler.get();
        Log.d(Constants.TAG, "Bitmap load finished! handler null status: " + (handler == null));
        if (handler != null) {
            handler.doneLoadingBitmap(bitmap);
        }
    }

    public interface ImageSizeDownloadTargetHandler {
        public void doneLoadingBitmap(Bitmap bitmap);
    }
}
