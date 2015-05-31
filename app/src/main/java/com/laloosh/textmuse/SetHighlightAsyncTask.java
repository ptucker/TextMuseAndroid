package com.laloosh.textmuse;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.laloosh.textmuse.datamodel.Note;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class SetHighlightAsyncTask extends AsyncTask<Void, Void, String> {

    public static final String LIKE_URL = "http://www.textmuse.com/admin/notelike.php";

    WeakReference<SetHighlightAsyncTaskHandler> mHandler;
    WeakReference<View> mView;
    Note mNote;
    int mAppId = -1;
    int mNoteId = -1;
    int mLike = -1;

    public SetHighlightAsyncTask(SetHighlightAsyncTaskHandler handler, int appId, boolean liked, Note note, View view) {
        mHandler = new WeakReference<SetHighlightAsyncTaskHandler>(handler);
        mView = new WeakReference<View>(view);
        mAppId = appId;
        mNoteId = note.noteId;
        mLike = liked ? 1 : 0;
        mNote = note;
    }

    @Override
    protected String doInBackground(Void... params) {

        Log.d(Constants.TAG, "Starting async task to fetch new data from server");

        String result = null;
        ConnectionUtils connUtils = new ConnectionUtils();

        HashMap<String, String> webParams = new HashMap<String, String>();
        webParams.put("app", Integer.toString(mAppId));
        webParams.put("id", Integer.toString(mNoteId));
        webParams.put("h", Integer.toString(mLike));

        result = connUtils.postUrl(LIKE_URL, webParams);

        Log.d(Constants.TAG, "Finished async task to fetch new data from server, length = " + (result == null ? "null" : Integer.toString(result.length())));

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if (mHandler == null) {
            return;
        }

        SetHighlightAsyncTaskHandler handler = mHandler.get();

        if (handler != null) {
            handler.handlePostResult(s, mNote, mLike != 0, mView.get());
        }
    }

    public interface SetHighlightAsyncTaskHandler {
        public void handlePostResult(String s, Note note, boolean liked, View view);
    }
}
