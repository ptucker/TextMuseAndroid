package com.laloosh.textmuse.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.ui.SplashScreenActivity;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

public class AzureTextMuseNotificationHandler extends NotificationsHandler {

    @Override
    public void onRegistered(final Context context, final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                AzureIntegrationSingleton client = AzureIntegrationSingleton.getInstance();
                client.setGcmRegistrationId(gcmRegistrationId);
                if (client.getStarted() && client.getClient() != null) {
                    client.registerForGcm(context);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        String notificationMessage = bundle.getString("message");
        String alertMessage = bundle.getString("alert");
        if (notificationMessage == null || notificationMessage.length() <= 0) {
            notificationMessage = alertMessage;
        }

        String url = bundle.getString("messageUrl");
        String inAppMessage = bundle.getString("extendedMessage");
        String messageTitle = bundle.getString("messageTitle");
        String highlighted = bundle.getString("highlight");
        if (inAppMessage == null || inAppMessage.length() <= 0)
            inAppMessage = notificationMessage;

        //Clear out the notifications
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        showReminderNotification(context, notificationMessage, url, inAppMessage, messageTitle, highlighted);
    }

    private void showReminderNotification(Context context, String text, String url, String inAppMessage, String messageTitle, String highlighted) {

        //Build the intent that will start the activity on click
        Intent resultIntent;
        PendingIntent pendingIntent;
        if (url != null) {
            try {
                resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            } catch (Exception e) {
                resultIntent = new Intent(context, SplashScreenActivity.class);
                Log.e(Constants.TAG, "Could not create intent to start web browser in notification");
            }
        } else {
            resultIntent = new Intent(context, SplashScreenActivity.class);

            if (inAppMessage != null) {
                resultIntent.putExtra(Constants.LAUNCH_MESSAGE_EXTRA, inAppMessage);
            }
        }
        if (highlighted != null) {
            resultIntent.putExtra(Constants.HIGHLIGHTED_MESSAGE_EXTRA, highlighted);
        }

        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a default sound to play
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (messageTitle == null || messageTitle.length() <= 0) {
            messageTitle = "TextMuse";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(messageTitle)
                .setContentText(text)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setSound(sound, AudioManager.STREAM_NOTIFICATION);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(text);
        builder.setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}

