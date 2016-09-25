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

    private Context mContext;
    private Note mNote;

    public DownloadImageAsyncTask(Note note, Context context) {
        mNote = note;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(Constants.TAG, "Starting async task to download image for note id " + mNote.noteId);
        ConnectionUtils connUtils = new ConnectionUtils();
        connUtils.downloadNoteImage(mNote, mContext);
        return null;
    }
}
