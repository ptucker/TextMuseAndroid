package com.laloosh.textmuse.datamodel;


import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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

    public static void writeDateTime(Parcel dest, DateTime dateTime) {
        dest.writeByte((byte) (dateTime == null ? 0 : 1));
        if (dateTime != null) {
            final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            String dateString =  fmt.print(dateTime);
            dest.writeString(dateString);
        }
    }

    public static DateTime readDateTime(Parcel source) {
        if (source.readByte() == (byte) 1) {
            final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
            DateTime result = fmt.parseDateTime(source.readString());
            return result;
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
