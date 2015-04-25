package com.laloosh.textmuse.datamodel;


import android.os.Parcel;
import android.os.Parcelable;

public class TextMuseContact implements Parcelable {
    public String lookupKey;
    public String displayName;
    public String phoneNumber;
    public String numberType;

    public TextMuseContact() {}

    public TextMuseContact(String lookupKey, String displayName, String phoneNumber, String numberType) {
        this.lookupKey = lookupKey;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.numberType = numberType;
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeString(out, lookupKey);
        ParcelUtils.writeString(out, displayName);
        ParcelUtils.writeString(out, phoneNumber);
        ParcelUtils.writeString(out, numberType);
    }

    public static final Parcelable.Creator<TextMuseContact> CREATOR = new Parcelable.Creator<TextMuseContact>() {
        public TextMuseContact createFromParcel(Parcel in) {
            return new TextMuseContact(in);
        }

        public TextMuseContact[] newArray(int size) {
            return new TextMuseContact[size];
        }
    };

    protected TextMuseContact(Parcel in) {
        lookupKey = ParcelUtils.readString(in);
        displayName = ParcelUtils.readString(in);
        phoneNumber = ParcelUtils.readString(in);
        numberType = ParcelUtils.readString(in);
    }


//    @Override
//    public boolean equals(Object o) {
//        if (o == null) return false;
//        if (!(o instanceof ChosenContact)) return false;
//
//        ChosenContact other = (ChosenContact) o;
//        return other.lookupKey.equals(this.lookupKey);
//    }
//
//    @Override
//    public int hashCode() {
//        return lookupKey.hashCode();
//
//    }
}
