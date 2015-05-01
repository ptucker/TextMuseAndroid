package com.laloosh.textmuse;


public class Constants {
    public static final String TAG = "TextMuse";

    public static final String DATA_FILE = "TextMuse.Cache";

    public static final String CONTACTS_FILE = "Contacts.Cache";

    static final int[] COLOR_LIST = {0xffe76636, 0xff19ab67, 0xff006dac};

    public static final int DEFAULT_NUMBER_RECENT_CONTACTS = 5;

    //explicitly define a shared prefs file instead of using the getDefaultSharedPreferences
    //since somehow the default file changes based on shortcuts
    public static final String SHARED_PREF_FILE = "com.laloosh.textmuse.sharedprefs";

    public static final String SHARED_PREF_KEY_LAST_NOTIFIED = "lastnotified";
    public static final String SHARED_PREF_KEY_NOTIFICATION_COUNT = "notificationcount";

    //We will do a backoff on the notifications based on this frequency.  We'll first
    //notify the user after 3 days of non-use, then 6 days after that, then 9 days after that, etc.
    public static final int NOTIFICATION_FREQUENCY = 3;

}
