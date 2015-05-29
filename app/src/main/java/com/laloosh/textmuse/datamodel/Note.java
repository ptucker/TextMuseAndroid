package com.laloosh.textmuse.datamodel;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

    public int noteId;
    public boolean newFlag;
    public String text;
    public String mediaUrl;
    public String extraUrl;
    public boolean liked;

    //Non-serialized value that are used temporarily
    public transient boolean savedInternally;
    public transient boolean saveFailed;

    public Note() {}

    public String getInternalFilename() {
        return Integer.toString(noteId) + ".jpg";
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

    public Uri getExternalLinkUri() {
        if (!hasExternalLink()) {
            return null;
        }

        String url = extraUrl;
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
    }



}
