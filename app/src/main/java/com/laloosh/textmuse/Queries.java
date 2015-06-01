package com.laloosh.textmuse;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

public class Queries {
    public interface ContactsQuery {

        final static int QUERY_ID = 1;
        final static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        final static Uri FILTER_URI = ContactsContract.Contacts.CONTENT_FILTER_URI;

        @SuppressLint("InlinedApi")
        final static String SELECTION =
                (AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME) +
                        "<>''" + " AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1 AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";

        @SuppressLint("InlinedApi")
        final static String SORT_ORDER =
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

                // The contact's row id
                ContactsContract.Contacts._ID,

                // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
                // a "permanent" contact URI.
                ContactsContract.Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
                // some other useful identifier such as an email address. This column isn't
                // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
                // instead.
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
                // you generate the pointer from the contact's ID value and constants defined in
                // android.provider.ContactsContract.Contacts.
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts._ID,

                // The sort order column for the returned Cursor, used by the AlphabetIndexer
                SORT_ORDER,

        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int PHOTO_THUMBNAIL_DATA = 3;
        final static int SORT_KEY = 4;
    }

    public interface PhoneNumQuery {

        final static int QUERY_ID = 2;
        final static Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final static String SELECTION =
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND " + ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP +
                        "=1 AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";

        final static String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.IS_PRIMARY + " DESC ";
//        final static String SORT_ORDER =
//                AndroidUtils.hasJellyBeanMR2() ? ContactsContract.CommonDataKinds.Phone.TIMES_USED : ContactsContract.CommonDataKinds.Phone.IS_PRIMARY;


        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                AndroidUtils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int NUMBER = 3;
        final static int TYPE = 4;
        final static int LABEL = 5;
        final static int IS_PRIMARY = 6;
    }

    public interface PhotoQuery {

        final static int QUERY_ID = 3;
        final static Uri CONTENT_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final static String SORT_ORDER = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DATA_PATH = 1;
        final static int BUCKET_DISPLAY_NAME = 2;
        final static int DATE_TAKEN = 3;
        final static int MIME_TYPE = 4;
    }
}
