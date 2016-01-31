package com.laloosh.textmuse.datamodel;


import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.laloosh.textmuse.Constants;

import java.io.File;

public class Note implements Parcelable {

    public int noteId;
    public boolean newFlag;
    public String text;
    public String mediaUrl;
    public String extraUrl;
    public boolean liked;
    public int likeCount;

    //Non-serialized value that are used temporarily
    //savedInternally is a transient field since the external drive where we save these
    //is possibly user accessible and can be deleted without the app knowing
    public transient boolean savedInternally;
    public transient boolean saveFailed;

    public Note() {}

    public Note(boolean localNote) {
        if (localNote) {
            noteId = -1;
            text = "";
        }
    }

    public String getInternalFilename() {
        return Integer.toString(noteId) + ".jpg";
    }

    //Local notes are initialized to have an ID of -1
    public boolean isLocalNote() {
        return (noteId < 0);
    }

    public boolean hasDisplayableText() {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean hasDisplayableMedia() {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return false;
        }

        //TODO: for now, just make youtube non-displayable
        if (isMediaYoutube()) {
            return false;
        }

        return true;
    }

    //Used to update our saved internally flag upon reload of the data set
    public void updateSavedInternally(Context context) {
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), getInternalFilename());
            if (file.exists()) {
                savedInternally = true;
            } else {
                savedInternally = false;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not check state of image for note id: " + noteId);
        }
    }

    public String getDisplayMediaUrl(Context context) {
        if (savedInternally) {
            try {
                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), getInternalFilename());
                return Uri.fromFile(file).toString();

            } catch (Exception e) {
                Log.e(Constants.TAG, "Could not get display media url for note id: " + noteId);
            }
        }

        return mediaUrl;
    }

    //On certain images, we should center fit instead of crop.  Only do this for quotehd images
    //for now
    public boolean shouldCenterInside() {
        if (mediaUrl != null && mediaUrl.toLowerCase().contains("quotehd.com")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasExternalLink() {
        if (extraUrl == null || extraUrl.isEmpty()) {
            return false;
        }
        return true;
    }

    public Uri getExternalLinkUri(String appId) {
        if (!hasExternalLink()) {
            return null;
        }

        String url = extraUrl;

        if (appId != null &&
            extraUrl != null &&
            extraUrl.toLowerCase().contains("textmuse") &&
            extraUrl.toLowerCase().contains("%appid%")) {

            extraUrl.replace("%appid%", appId);
        }

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();

        if (scheme == null || scheme.isEmpty()) {
            url = "http://" + extraUrl;
            uri = Uri.parse(url);
        }

        return uri;
    }

    public boolean isMediaYoutube() {
        String[] youtubeBaseUrls = {"http://youtu.be", "http://www.youtube.com", "https://youtu.be", "https://www.youtube.com"};

        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return false;
        }

        String convertedMediaUrl = mediaUrl.toLowerCase().trim();
        for (int i = 0; i < youtubeBaseUrls.length; i++) {
            if (convertedMediaUrl.startsWith(youtubeBaseUrls[i])) {
                return true;
            }
        }

        return false;
    }

    public String getYoutubeVideoTag() {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return null;
        }

        String convertedMediaUrl = mediaUrl.trim();
        Uri uri = Uri.parse(convertedMediaUrl);
        return uri.getLastPathSegment();
    }

    public String getYoutubeImgUrl() {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return null;
        }

        String videoTag = getYoutubeVideoTag();
        return "http://img.youtube.com/vi/" + videoTag + "/hqdefault.jpg";
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(noteId);
        out.writeByte(newFlag ? (byte) 1 : (byte) 0);
        ParcelUtils.writeString(out, text);
        ParcelUtils.writeString(out, mediaUrl);
        ParcelUtils.writeString(out, extraUrl);
        out.writeByte(liked ? (byte) 1 : (byte) 0);

        out.writeByte(savedInternally ? (byte) 1 : (byte) 0);
        out.writeByte(saveFailed ? (byte) 1 : (byte) 0);

        out.writeInt(likeCount);
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    protected Note(Parcel in) {
        noteId = in.readInt();
        newFlag = (in.readByte() != 0);
        text = ParcelUtils.readString(in);
        mediaUrl = ParcelUtils.readString(in);
        extraUrl = ParcelUtils.readString(in);
        liked = (in.readByte() != 0);

        savedInternally = (in.readByte() != 0);
        saveFailed = (in.readByte() != 0);

        likeCount = in.readInt();
    }

}
