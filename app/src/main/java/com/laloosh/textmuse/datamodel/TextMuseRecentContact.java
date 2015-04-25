package com.laloosh.textmuse.datamodel;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonPrimitive;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class TextMuseRecentContact extends TextMuseContact implements Parcelable{
    public DateTime lastUsed;

    public TextMuseRecentContact(String lookupKey, String displayName, String phoneNumber, String numberType) {
        super (lookupKey, displayName, phoneNumber, numberType);
        this.lastUsed = DateTime.now();
    }

    public TextMuseRecentContact(TextMuseContact contact) {
        super (contact.lookupKey, contact.displayName, contact.phoneNumber, contact.numberType);
        this.lastUsed = DateTime.now();
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String lastUsedString = fmt.print(lastUsed);
        ParcelUtils.writeString(out, lastUsedString);
    }

    public static final Parcelable.Creator<TextMuseRecentContact> CREATOR = new Parcelable.Creator<TextMuseRecentContact>() {
        public TextMuseRecentContact createFromParcel(Parcel in) {
            return new TextMuseRecentContact(in);
        }

        public TextMuseRecentContact[] newArray(int size) {
            return new TextMuseRecentContact[size];
        }
    };

    private TextMuseRecentContact(Parcel in) {
        super(in);

        String lastUsedString = ParcelUtils.readString(in);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        lastUsed = fmt.parseDateTime(lastUsedString);
    }

}
