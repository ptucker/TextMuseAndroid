package com.laloosh.textmuse.datamodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.utils.WebDataParser;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TextMuseData {

    public List<Category> categories;
    public List<LocalNotification> localNotifications;
    public String preamble;
    public String inquiry;

    public Category localTexts;
    public Category localPhotos;

    public Category pinnedNotes;

    public DateTime timestamp;
    public int appId;

    public TextMuseCurrentSkinData skinData;

    public HashSet<Integer> flaggedNotesSet;
    public HashSet<Integer> followedSponsorsSet;

    public int explorerPoints;
    public int sharerPoints;
    public int musePoints;
    public boolean gotMasterBadge;

    private transient HashSet<Integer> mPinnedNotesSet;

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

    //Every note has a flag saying whether the image is saved locally or if we should reload it from the network
    //These flags get reset on reload and when we load new data from the network.  We should set these back
    //again
    public void updateNoteImageFlags(Context context) {

        if (context == null) {
            return;
        }

        //We can ignore the local texts and local photos categories
        if (categories != null && categories.size() > 0) {
            for (Category category : categories) {
                if (category.notes != null && category.notes.size() > 0) {
                    for (Note note : category.notes) {
                        if (note.hasDisplayableMedia()) {
                            note.updateSavedInternally(context);
                        }
                    }
                }
            }
        }
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

        if ((this.skinData == null && data.skinData != null) || (this.skinData != null && data.skinData == null)) {
            Log.d(Constants.TAG, "Skin data presence different");
            return false;
        }

        if ((this.skinData != null && data.skinData != null) && (this.skinData.skinId != data.skinData.skinId)) {
            Log.d(Constants.TAG, "Skin data ID different");
            return false;
        }

        //For some reason, sometimes the new flag is not set correctly.  Just use a hashset to compare
        HashSet<String> notesHashset = new HashSet<String>();

        for (Category category : this.categories) {
            if (category.notes != null && category.notes.size() > 0) {
                for (Note note : category.notes) {
                    notesHashset.add(category.name + note.text + note.mediaUrl + note.winnerText);
                }
            }
        }

        for (Category category: data.categories) {
            if (category.notes != null && category.notes.size() > 0) {
                for (Note note : category.notes) {
                    if (!notesHashset.remove(category.name + note.text + note.mediaUrl + note.winnerText)) {
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

        pinnedNotes = new Category();
        pinnedNotes.name = "Pinned Notes";
        pinnedNotes.requiredFlag = true;
        pinnedNotes.notes = new ArrayList<Note>();

        flaggedNotesSet = new HashSet<>();
        followedSponsorsSet = new HashSet<>();
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
                String bucketName = cur.getString(Queries.PhotoQuery.BUCKET_DISPLAY_NAME);

                //Skip screenshots
                if (bucketName.toLowerCase().contains("screenshot")) {
                    continue;
                }

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

    public void reorderCategories(ArrayList<String> orderedCategories) {

        //Don't need to do anything if we don't have anything in our ordered categories
        if (orderedCategories == null || orderedCategories.size() <= 0) {
            return;
        }

        //Reorder our categories according to the list, with some adjustments.  We'll put things that
        //aren't in the string list first, then go down the string list order

        HashSet<String> categoryNames = new HashSet<String> ();
        ArrayList<String> tempOrderedCategories = new ArrayList<String> ();
        ArrayList<Category> reorderedCategories = new ArrayList<Category> ();

        //Make a hash set with our category names just for easy lookup later
        for (Category c : categories) {
            categoryNames.add(c.name);
        }

        //Copy the string list, taking out trending and highlighted
        for (String c : orderedCategories) {
            if (c.equalsIgnoreCase("Trending") || c.equalsIgnoreCase("Highlighted")) {
                continue;
            } else {
                //Don't copy over category names that aren't in our category list
                if (categoryNames.contains(c)) {
                    tempOrderedCategories.add(c);
                }
            }
        }

        //Add categories that aren't in the ordered list first
        for (Category c : categories) {
            if (!tempOrderedCategories.contains(c.name)) {
                reorderedCategories.add(c);
            }
        }

        //Then add the categories in the ordered list in order
        for (String categoryName : tempOrderedCategories) {
            for (Category c : categories) {
                if (c.name.equalsIgnoreCase(categoryName)) {
                    reorderedCategories.add(c);
                    break;
                }
            }
        }

        categories = reorderedCategories;
    }

    public int[] getColorList() {
        if (Constants.BuildType == Constants.Builds.Humanix) {
            //007db1, white, bb6b1e
            int c1 = 0xff000000 + 0x008dc73f;
            int c2 = 0xff000000 + 0X00ffffff;
            int c3 = 0xff000000 + 0x00231f20;

            return new int[]{c1, c2, c3};
        }
        else {
            if (skinData == null) {
                return Constants.COLOR_LIST;
            } else {
                int c1 = 0xff000000 + skinData.c1;
                int c2 = 0xff000000 + skinData.c2;
                int c3 = 0xff000000 + skinData.c3;

                return new int[]{c1, c2, c3};
            }
        }
    }

    public boolean hasPinnedNote(int noteId) {
        if (pinnedNotes == null || pinnedNotes.notes == null) {
            return false;
        }

        if (mPinnedNotesSet == null) {
            mPinnedNotesSet = new HashSet<>();
            for (Note note : pinnedNotes.notes) {
                mPinnedNotesSet.add(note.noteId);
            }
        }

        return mPinnedNotesSet.contains(noteId);
    }

    public void pinNote(Note noteToPin) {

        if (hasPinnedNote(noteToPin.noteId)) {
            return;
        }

        //If not already in, then just add it in
        pinnedNotes.notes.add(noteToPin);

        mPinnedNotesSet.add(noteToPin.noteId);
    }

    public void unPinNote(Note noteToUnpin) {

        if (!hasPinnedNote(noteToUnpin.noteId)) {
            return;
        }

        Iterator<Note> iter = pinnedNotes.notes.iterator();
        while (iter.hasNext()) {
            Note note = iter.next();
            if (note.noteId == noteToUnpin.noteId) {
                iter.remove();
                mPinnedNotesSet.remove(note.noteId);
                return;
            }
        }
    }

    public void followSponsor(int id) {
        if (followedSponsorsSet == null) {
            followedSponsorsSet = new HashSet<>();
        }

        followedSponsorsSet.add(id);
    }

    public void unfollowSponsor(int id) {
        if (followedSponsorsSet == null) {
            followedSponsorsSet = new HashSet<>();
        } else {
            followedSponsorsSet.remove(id);
        }
    }

    public void flagNote(int id) {
        if (flaggedNotesSet == null) {
            flaggedNotesSet = new HashSet<>();
        }

        flaggedNotesSet.add(id);

        filterFlaggedNotes();
    }

    public void filterFlaggedNotes() {
        //Filter out any flagged notes
        if (flaggedNotesSet != null && flaggedNotesSet.size() > 0) {
            for (Category category : categories) {
                Iterator<Note> iterator = category.notes.iterator();
                while (iterator.hasNext()) {
                    Note note = iterator.next();
                    if (flaggedNotesSet.contains(note.noteId)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void removeEmptyCategories() {
        Iterator<Category> iterator = categories.iterator();
        while (iterator.hasNext()) {
            Category category = iterator.next();
            if (category.notes.size() <= 0) {
                iterator.remove();
            }
        }
    }
}
