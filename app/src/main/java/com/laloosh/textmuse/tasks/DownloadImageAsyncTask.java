package com.laloosh.textmuse.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.utils.ConnectionUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Void> {
    public interface DownloadImageAsyncListener {
        public void onComplete();
    }

    private Context mContext;
    private Note mNote;
    private DownloadImageAsyncListener mListener;

    public DownloadImageAsyncTask(Note note, Context context, DownloadImageAsyncListener listener) {
        mNote = note;
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(Constants.TAG, "Starting async task to download image for note id " + mNote.noteId);
        ConnectionUtils connUtils = new ConnectionUtils();
        connUtils.downloadNoteImage(mNote, mContext);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null)
            mListener.onComplete();
    }
}
