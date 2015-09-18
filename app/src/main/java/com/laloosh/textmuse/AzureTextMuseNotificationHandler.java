package com.laloosh.textmuse;

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

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.microsoft.windowsazure.mobileservices.notifications.MobileServicePush;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

import java.util.ArrayList;

public class AzureTextMuseNotificationHandler extends NotificationsHandler {

    @Override
    public void onRegistered(final Context context, final String gcmRegistrationId) {
        super.onRegistered(context, gcmRegistrationId);

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                try {
                    AzureIntegrationSingleton client = AzureIntegrationSingleton.getInstance();
                    if (client.getStarted() && client.getClient() != null) {

                        ArrayList<String> tags = null;

                        GlobalData globalData = GlobalData.getInstance();
                        if (globalData.hasLoadedData()) {
                            TextMuseData data = globalData.getData();
                            if (data.appId > 0) {
                                tags = new ArrayList<String>();
                                tags.add(Integer.toString(data.appId));

                                int skinId = TextMuseSkinData.getCurrentlySelectedSkin(context);
                                if (skinId <= 0) {
                                    skinId = 0;
                                }
                                tags.add("skin" + Integer.toString(skinId));
                            }
                        }

                        MobileServicePush pushClient = client.getClient().getPush();

                        if (tags != null && tags.size() > 0) {
                            pushClient.register(gcmRegistrationId, tags.toArray(new String[tags.size()]));
                        } else {
                            pushClient.register(gcmRegistrationId, null);
                        }

                        Log.d(Constants.TAG, "Succeeded in sending off the registration with GCM ID: " + gcmRegistrationId);
                    }
                }
                catch(Exception e) {
                    Log.e(Constants.TAG, "Failed to register for google cloud messaging with the azure client!");
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

        showReminderNotification(context, notificationMessage, url, inAppMessage, messageTitle);
    }

    private void showReminderNotification(Context context, String text, String url, String inAppMessage, String messageTitle) {

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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

}

