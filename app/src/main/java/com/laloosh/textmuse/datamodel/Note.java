package com.laloosh.textmuse.datamodel;


import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

    public int noteId;
    public boolean newFlag;
    public String text;
    public String mediaUrl;
    public String extraUrl;

    //Non-serialized value that
    public transient boolean savedInternally;
    public transient boolean saveFailed;

    public Note() {}

    public String getInternalFilename() {
        return Integer.toString(noteId) + ".jpg";
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

        savedInternally = (in.readByte() != 0);
        saveFailed = (in.readByte() != 0);
    }



}
