package com.laloosh.textmuse.app;

public class Constants {
    public static final String TAG = "TextMuse";

    public static final String DATA_FILE = "TextMuse.Cache";

    public static final String CONTACTS_FILE = "Contacts.Cache";

    public static final String SETTINGS_FILE = "Settings.Cache";

    public static final int[] COLOR_LIST = {0xffe76636, 0xff19ab67, 0xff006dac};

    public static final int DEFAULT_NUMBER_RECENT_CONTACTS = 5;

    //explicitly define a shared prefs file instead of using the getDefaultSharedPreferences
    //since somehow the default file changes based on shortcuts
    public static final String SHARED_PREF_FILE = "com.laloosh.textmuse.sharedprefs";

    public static final String SHARED_PREF_KEY_LAUNCHED_BEFORE = "haslaunchedbefore";

    public static final String SHARED_PREF_KEY_LAST_NOTIFIED = "lastnotified";
    public static final String SHARED_PREF_KEY_NOTIFICATION_COUNT = "notificationcount";

    public static final String SHARED_PREF_KEY_REGISTERED = "registered";
    public static final String SHARED_PREF_KEY_REGISTER_NAME = "registername";
    public static final String SHARED_PREF_KEY_REGISTER_EMAIL= "registeremail";
    public static final String SHARED_PREF_KEY_REGISTER_BMONTH = "registerbmonth";
    public static final String SHARED_PREF_KEY_REGISTER_BYEAR = "registerbyear";

    public static final String SHARED_PREF_KEY_SKIN_COUNT = "skincount";
    public static final String SHARED_PREF_KEY_SKIN_BASE = "skinnum";
    public static final String SHARED_PREF_KEY_SKIN_CURRENT_ID = "currentskinid";

    //We will do a backoff on the notifications based on this frequency.  We'll first
    //notify the user after 3 days of non-use, then 6 days after that, then 9 days after that, etc.
    public static final int NOTIFICATION_FREQUENCY = 3;

    public static final int LOCAL_NOTE_SIZE = 15;

    //64 mb image cache for saved images
    public static final long MAX_IMAGE_CACHE_SIZE = 64 * 1024 * 1024;

    public static final String LAUNCH_MESSAGE_EXTRA = "com.laloosh.textmuse.launch.messageextra";

    public static final String GOOGLE_API_YOUTUBE = "AIzaSyCwp-hP3BadhQKKfnsFSPqoGPV9wmNANM0";

    public enum Builds { University, Humanix}
    public static final Builds BuildType = Builds.Humanix;
    public static final int HumanixSponsorID = 82;

}
