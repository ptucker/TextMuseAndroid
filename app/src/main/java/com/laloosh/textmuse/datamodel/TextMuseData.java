package com.laloosh.textmuse.datamodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.laloosh.textmuse.Constants;
import com.laloosh.textmuse.Queries;
import com.laloosh.textmuse.WebDataParser;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TextMuseData {

    public List<Category> categories;
    public List<LocalNotification> localNotifications;

    public Category localTexts;
    public Category localPhotos;

    public DateTime timestamp;
    public int appId;

    public TextMuseData() {
        setupNewLocalNotes();
    }

    //Saves the data to our local preferences
    public void save(Context context) {
        if (!DataPersistenceHelper.save(context, Constants.DATA_FILE, this)) {
            Log.e(Constants.TAG, "Could not save TextMuseData");
        }
    }

    //Loads the cached data from our local preferences
    public static TextMuseData load(Context context) {
        return DataPersistenceHelper.load(context, Constants.DATA_FILE, TextMuseData.class);
    }

    public static TextMuseData loadRawContent(Context context) {
        String rawResult = DataPersistenceHelper.loadRawContent(context);
        WebDataParser parser = new WebDataParser();
        TextMuseData data = parser.parse(rawResult);

        //Manually set the app ID to a value that it should not be normally,
        //to prevent us from screwing up metrics, and so we'll get a normal app id from the server
        //later
        data.appId = -1;
        return data;
    }


    //Compares the categories to see if they are similar enough.  This function is called when
    //we download a new TextMuseData from the server
    public boolean isDataSimilar(TextMuseData data) {

        //if anything is empty, just assume that the data is different
        if (this.categories == null || this.categories.size() <= 0 || data.categories == null || data.categories.size() <= 0) {
            Log.d(Constants.TAG, "Data different 1");
            return false;
        }

        if (this.categories.size() != data.categories.size()) {
            Log.d(Constants.TAG, "Data different 2");
            return false;
        }

        //For some reason, sometimes the new flag is not set correctly.  Just use a hashset to compare
        HashSet<String> notesHashset = new HashSet<String>();

        for (Category category : this.categories) {
            if (category.notes != null && category.notes.size() > 0) {
                for (Note note : category.notes) {
                    notesHashset.add(category.name + Integer.toString(note.noteId));
                }
            }
        }

        for (Category category: data.categories) {
            if (category.notes != null && category.notes.size() > 0) {
                for (Note note : category.notes) {
                    if (!notesHashset.remove(category.name + Integer.toString(note.noteId))) {
                        return false;
                    }
                }
            }
        }

        if (!notesHashset.isEmpty()) {
            return false;
        }

        return true;
    }

    public void setupNewLocalNotes() {
        localTexts = new Category();
        localTexts.name = "My Texts";
        localTexts.requiredFlag = true;
        localTexts.notes = new ArrayList<Note>();

        for (int i = 0; i < Constants.LOCAL_NOTE_SIZE; i++) {
            Note note = new Note(true);
            localTexts.notes.add(note);
        }

        localPhotos = new Category();
        localPhotos.name = "My Photos";
        localPhotos.requiredFlag = true;
        localPhotos.notes = new ArrayList<Note>();
    }

    public void updatePhotos(Context context) {
        if (localPhotos == null) {
            localPhotos = new Category();
            localPhotos.name = "My Photos";
            localPhotos.requiredFlag = true;
            localPhotos.notes = new ArrayList<Note>();
        }
        localPhotos.notes.clear();

        ContentResolver cr = context.getContentResolver();
        Cursor  cur = cr.query(Queries.PhotoQuery.CONTENT_URI,
                Queries.PhotoQuery.PROJECTION,
                null,
                null,
                Queries.PhotoQuery.SORT_ORDER);

        if (cur.getCount() > 0) {

            int curIndex = 0;
            while (cur.moveToNext() && curIndex < Constants.LOCAL_NOTE_SIZE) {
                String photoPath = cur.getString(Queries.PhotoQuery.DATA_PATH);

                try {
                    File file = new File(photoPath);
                    if (file.isFile()) {
                        Note note = new Note(true);
                        note.mediaUrl = Uri.fromFile(file).toString();
                        localPhotos.notes.add(note);
                        curIndex++;
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Photo path: " + photoPath + " was not found");
                    continue;
                }
            }
        }
    }
}
