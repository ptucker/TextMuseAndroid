package com.laloosh.textmuse.broadcastreceivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.ui.MainCategoryActivity;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.LocalNotification;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Calendar;
import java.util.List;

public class AlarmReceivedBroadcastReceiver extends BroadcastReceiver {

    //This broadcast intent must match the one in the manifest for this broadcast receiver
    public static final String BROADCAST_INTENT_STRING = "com.laloosh.textmuse.alarmbroadcast.triggered";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(Constants.TAG, "Triggered alarm received broadcast receiver.");
        if (shouldCreateNotification(context)) {

            Log.d(Constants.TAG, "Alarm received broadcast receiver -- firing notification");
            updateNotificationData(context);

            String notificationText = getNotificationText(context);
            showReminderNotification(context, notificationText);
        }

        return;
    }

    private boolean shouldCreateNotification(Context context) {

        TextMuseSettings settings = TextMuseSettings.load(context);
        if (settings == null || !settings.notifications) {
            return false;
        }

        //See if we need to post a notification
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        String lastNotifiedString = sharedPreferences.getString(Constants.SHARED_PREF_KEY_LAST_NOTIFIED, null);
        int notificationCount = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_NOTIFICATION_COUNT, 0);

        if (lastNotifiedString == null) {
            //Shouldn't happen normally, unless the user never ran the app. Just set this to now

            setLastNotifiedTime(context);
            return false;
        }

        DateTime lastNotified;

        try {
            lastNotified = fmt.parseDateTime(lastNotifiedString);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not parse our date. Aborting AlarmReceivedBroadcastReceiver");
            setLastNotifiedTime(context);
            return false;
        }

        int dayPassedCount = (notificationCount + 1) * Constants.NOTIFICATION_FREQUENCY;
        DateTime timeGate = lastNotified.plusDays(dayPassedCount);

        if (DateTime.now().isAfter(timeGate)) {
            return true;
        }

        return false;
    }

    private void updateNotificationData(Context context) {
        //Save the data indicating that we are showing a notification so that we don't show too many
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String currentTime = fmt.print(DateTime.now());

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        int notificationCount = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_NOTIFICATION_COUNT, 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREF_KEY_LAST_NOTIFIED, currentTime);
        editor.putInt(Constants.SHARED_PREF_KEY_NOTIFICATION_COUNT, notificationCount + 1);
        editor.commit();
    }

    private void showReminderNotification(Context context, String text) {

        //Build the intent that will start the activity on click
        Intent resultIntent = new Intent(context, MainCategoryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a default sound to play
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("TextMuse")
                .setContentText(text)
                .setSmallIcon(R.drawable.notification_icon)   
                .setContentIntent(pendingIntent)
                .setSound(sound, AudioManager.STREAM_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void setLastNotifiedTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String currentTime = fmt.print(DateTime.now());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREF_KEY_LAST_NOTIFIED, currentTime);
        editor.commit();
    }

    private String getNotificationText(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        int notificationCount = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_NOTIFICATION_COUNT, 0);

        GlobalData globalData = GlobalData.getInstance();
        globalData.loadData(context);

        TextMuseData data = globalData.getData();
        if (data == null) {
            return null;
        }

        List<LocalNotification> notifications = data.localNotifications;
        if (notifications == null || notifications.size() <= 0) {
            return null;
        }

        int notificationIndex = notificationCount % notifications.size();

        return notifications.get(notificationIndex).text;
    }

    //Sets an alarm to go off around 1 PM each day
    public static void setAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(BROADCAST_INTENT_STRING);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Set the alarm to run at around 1 PM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 00);

        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
