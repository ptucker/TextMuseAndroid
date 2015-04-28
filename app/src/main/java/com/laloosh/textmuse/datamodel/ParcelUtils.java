package com.laloosh.textmuse.datamodel;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.laloosh.textmuse.Constants;

import java.util.ArrayList;

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

    public static void writeArrayList(Parcel dest, ArrayList<? extends Parcelable> arrayList) {
        boolean shouldWrite = true;
        if (arrayList == null || arrayList.size() <= 0) {
            shouldWrite = false;
        }
        dest.writeByte((byte) ((shouldWrite) ? 1 : 0));
        if (shouldWrite) {
            dest.writeTypedList(arrayList);
        }
    }

    //This function returns an empty list if it was empty or null before
    public static <T> ArrayList<T> readTypedList(Parcel source, Parcelable.Creator<T> creator) {
        if (source.readByte() == (byte) 1) {
            return source.createTypedArrayList(creator);
        }

        return new ArrayList<T>();
    }
}
