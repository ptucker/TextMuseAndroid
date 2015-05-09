package com.laloosh.textmuse;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.laloosh.textmuse.datamodel.Note;

import java.io.File;
import java.util.List;
import java.util.Set;

public class SmsUtils {

    //If an intent produces too many providers, then it's probably the wrong one...
    private static final int TOO_MANY_PROVIDERS = 6;

    public static Intent createSmsIntent(Context context, Note note, Set<String> phoneNumberSet) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
            return getPostKitKatIntent(context, note, phoneNumberSet);

        } else {

            return getPreKitKatIntent(context, note, phoneNumberSet);
        }
    }

    private static String getToList(Set<String> phoneNumberSet) {
        String separator = "; ";
        String toList = "";

        //Samsung separator is different, for some reason...
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            separator = ", ";
        }

        boolean start = true;
        for (String phoneNum : phoneNumberSet) {
            if (!start) {
                toList += separator;
            }

            toList += PhoneNumberUtils.stripSeparators(phoneNum);
            start = false;
        }

        Log.d(Constants.TAG, "To list for sms is: " + toList);

        return toList;
    }

    private static String getSmsTo(Set<String> phoneNumberSet) {

        return "smsto:" + getToList(phoneNumberSet);

    }

    private static String getText(Note note, boolean imageAsUrl) {
        String text = "";
        if (note.text != null) {
            text += note.text;
        }

        if (note.extraUrl != null && note.extraUrl.length() > 0) {
            if (text.length() <= 0) {
                //in the empty case, don't add parens
                text += note.extraUrl;
            } else {
                text += "\n(" + note.extraUrl + ")";
            }

        } else {
            //only add the image url if we pass in the flag and also don't have an extra url
            if (imageAsUrl && note.mediaUrl != null && note.mediaUrl.length() > 0) {
                if (text.length() <= 0) {
                    //in the empty case, don't add parens
                    text += note.mediaUrl;
                } else {
                    text += "\n(" + note.mediaUrl + ")";
                }

            }
        }

        text += "\n\nSent by TextMuse";

        return text;
    }

    private static Uri getMediaFile(Context context, Note note) {
        if (note.hasDisplayableMedia() && note.savedInternally) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), note.getInternalFilename());
            if (!file.isFile()) {
                return null;
            }
            return Uri.fromFile(file);
        }

        return null;
    }

    private static Intent getTextOnlyIntent(Note note, Set<String> phoneNumberSet) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getSmsTo(phoneNumberSet)));
        intent.putExtra("sms_body", getText(note, true));

        return intent;
    }

    //Only call this function post Kit-Kat, or else this will crash
    private static Intent getPostKitKatIntent(Context context, Note note, Set<String> phoneNumberSet) {

        Intent intent;
        List<ResolveInfo> resolveInfoList;
        PackageManager packageManager = context.getPackageManager();
        Uri mediaUri = getMediaFile(context, note);

        if (mediaUri == null) {
            Log.d(Constants.TAG, "No images to attach, using simpler intent");
            return getTextOnlyIntent(note, phoneNumberSet);
        }

        String[] knownGoodSmsApps = {"com.android.mms"};

        //Try some known good sms apps.  These apps allow you to attach pictures and text, and also
        //allow you to specify people to send stuff to. They also have good back button behavior.
        //Try to use these apps if they are present, even if they aren't the default app
        for (int i = 0; i < knownGoodSmsApps.length; i++) {
            intent = new Intent(Intent.ACTION_SEND);

            intent.setPackage(knownGoodSmsApps[i]);
            intent.putExtra("address", getToList(phoneNumberSet));
            intent.putExtra("sms_body", getText(note, false));

            if (mediaUri != null) {
                intent.putExtra(Intent.EXTRA_STREAM, mediaUri);
                intent.setType("image/png");
            }

            resolveInfoList = packageManager.queryIntentActivities(intent, 0);
            if (resolveInfoList.size() > 0) {
                return intent;
            }
        }

        //On HTC devices, also try manually using the HTC activity to see if it works
        if (Build.MANUFACTURER.equalsIgnoreCase("htc")) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setClassName("com.htc.sense.mms", "com.htc.sense.mms.ui.ComposeMessageActivity");
            intent.putExtra("address", getToList(phoneNumberSet));
            intent.putExtra("sms_body", getText(note, false));

            if (mediaUri != null) {
                intent.putExtra(Intent.EXTRA_STREAM, mediaUri);
                intent.setType("image/png");
            }

            resolveInfoList = packageManager.queryIntentActivities(intent, 0);
            if (resolveInfoList.size() > 0) {
                return intent;
            }
        }

        Log.d(Constants.TAG, "Could not use known good messaging app, just creating a sendto intent without an image");

        return getTextOnlyIntent(note, phoneNumberSet);
    }

    private static Intent getPreKitKatIntent(Context context, Note note, Set<String> phoneNumberSet) {

        List<ResolveInfo> resolveInfoList;
        PackageManager packageManager = context.getPackageManager();
        Uri mediaUri = getMediaFile(context, note);

        if (mediaUri == null) {
            Log.d(Constants.TAG, "No images to attach, using simpler intent");
            return getTextOnlyIntent(note, phoneNumberSet);
        }

        //Try the default SMS/MMS package that comes with Android first. This is the best case scenario,
        //since we can attach an image and text at the same time
        Intent intent = new Intent(Intent.ACTION_SEND);

//        intent.setPackage("com.android.mms");
        intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        intent.putExtra("address", getToList(phoneNumberSet));
        intent.putExtra("sms_body", getText(note, false));

        if (mediaUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, mediaUri);
            intent.setType("image/png");
        }

        resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfoList.size() > 0) {
            return intent;
        }

        Log.d(Constants.TAG, "Could not use default messaging app, attempting second method to get intent");

        //At this point, the normal SMS app was not found on pre-kitkat.  Attempt to use a different intent...
        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("address", getToList(phoneNumberSet));
        intent.putExtra("sms_body", getText(note, false));

        if (mediaUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, mediaUri);
            intent.setType("image/png");
        }

        resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfoList.size() > 0 && resolveInfoList.size() < TOO_MANY_PROVIDERS) {
            return intent;
        } else {
            Log.d(Constants.TAG, "Could not use vnd.android-dir/mms-sms method, number of activities matched: " + resolveInfoList.size());
        }

        //Finally, if we get here, then our attempts to attach the image have failed.  Either too many
        //activities matched (meaning that we probably matched to email and other share providers),
        //or nothing matched.  Just attempt to do a sendto intent without the image.
        Log.d(Constants.TAG, "Defaulting to sendto method with no images");
        return getTextOnlyIntent(note, phoneNumberSet);
    }
}
