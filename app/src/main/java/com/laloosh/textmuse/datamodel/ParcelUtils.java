package com.laloosh.textmuse.datamodel;


import android.os.Parcel;

public class ParcelUtils {
    public static void writeString(Parcel dest, String string) {
        dest.writeByte((byte) (string == null ? 0 : 1));
        if (string != null) {
            dest.writeString(string);
        }
    }

    public static String readString(Parcel source) {
        if (source.readByte() == (byte) 1) {
            return source.readString();
        }
        return null;
    }
}
