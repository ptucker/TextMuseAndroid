package com.laloosh.textmuse;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.laloosh.textmuse.datamodel.Note;

import java.io.File;
import java.util.Set;

public class SmsUtils {
    public static Intent createSmsIntent(Context context, Note note, Set<String> phoneNumberSet) {

        String smsTo = getSmsTo(phoneNumberSet);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(smsTo));

        intent.putExtra("sms_body", getText(note));

        Uri mediaUri = getMediaFile(context, note);
        if (mediaUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, mediaUri);
        }

        //intent.setType("vnd.android-dir/mms-sms");

        return intent;
    }

    private static String getSmsTo(Set<String> phoneNumberSet) {

        String separator = "; ";
        String smsTo = "smsto:";

        //Samsung separator is different, for some reason...
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            separator = ", ";
        }

        boolean start = true;
        for (String phoneNum : phoneNumberSet) {
            if (!start) {
                smsTo += separator;
            }

            smsTo += PhoneNumberUtils.stripSeparators(phoneNum);
            start = false;
        }

        Log.d(Constants.TAG, "SMS to link is: " + smsTo);

        return smsTo;
    }

    private static String getText(Note note) {
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

        }

        text += "\n\nSent by TextMuse";

        return text;
    }

    private static Uri getMediaFile(Context context, Note note) {
        if (note.mediaUrl != null && note.mediaUrl.length() > 0 && note.savedInternally) {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), note.getInternalFilename());
            if (!file.isFile()) {
                return null;
            }
            return Uri.fromFile(file);
        }

        return null;
    }
}
