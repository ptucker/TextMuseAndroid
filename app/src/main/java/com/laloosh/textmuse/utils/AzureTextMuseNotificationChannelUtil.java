package com.laloosh.textmuse.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;

import com.laloosh.textmuse.R;

// from https://code.tutsplus.com/tutorials/android-o-how-to-use-notification-channels--cms-28616

public class AzureTextMuseNotificationChannelUtil extends ContextWrapper {

    private NotificationManager mManager;
    public static final String MESSAGE_CHANNEL_ID = "com.laloosh.textmuse.MESSAGE";
    public static final String MESSAGE_CHANNEL_NAME = "Messages";

    public AzureTextMuseNotificationChannelUtil(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            // create messages channel
            NotificationChannel androidChannel = new NotificationChannel(MESSAGE_CHANNEL_ID,
                    MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            getManager().createNotificationChannel(androidChannel);
        }
    }

    public Notification.Builder getMessagesChannelNotification(String messageTitle, String text) {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        builder = builder.setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(messageTitle)
                        .setStyle(new Notification.BigTextStyle().bigText(text))
                        .setContentText(text)
                        .setDefaults(AudioManager.STREAM_NOTIFICATION)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= 26) {
            builder = builder.setChannelId(AzureTextMuseNotificationChannelUtil.MESSAGE_CHANNEL_ID);
        }
        return builder;
    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}