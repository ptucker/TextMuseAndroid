package com.laloosh.textmuse.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.ui.SplashScreenActivity;

import java.util.Map;

public class TextMuseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "TextMuseFirebaseService";
    public static String FirebaseToken;
    static Context context;

    class TextMuseNotification {
        public String message;
        public String url;
        public String extendedMsg;
        public String title;
        public String highlight;
        public String cathighlight;

        TextMuseNotification(RemoteMessage rmessage) {
            parseMessage(rmessage);
        }

        public void parseMessage(RemoteMessage rmessage) {
            if (rmessage.getNotification() != null)
                message = rmessage.getNotification().getBody();
            else {
                Map<String, String> parts = rmessage.getData();
                message = parts.get("message");
                url = parts.get("messageUrl");
                extendedMsg = parts.get("extendedMessage");
                title = parts.get("messageTitle");
                highlight = parts.get("highlight");
                cathighlight = parts.get("cathighlight");
            }
        }

    }

    public static void setContext(Context ctx) {
        context = ctx;
    }

    public void onNewToken(String token) {
        Log.d(TAG, "Refreshing GCM Reg Token");
        FirebaseToken = token;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        TextMuseNotification notification = new TextMuseNotification(remoteMessage);

        showReminderNotification(notification);
    }

    private void showReminderNotification(TextMuseNotification notification) {

        //Build the intent that will start the activity on click
        Intent resultIntent;
        PendingIntent pendingIntent;
        if (notification.url != null) {
            try {
                resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(notification.url));
            } catch (Exception e) {
                resultIntent = new Intent(context, SplashScreenActivity.class);
                Log.e(Constants.TAG, "Could not create intent to start web browser in notification");
            }
        } else {
            resultIntent = new Intent(context, SplashScreenActivity.class);

            if (notification.extendedMsg != null) {
                resultIntent.putExtra(Constants.LAUNCH_MESSAGE_EXTRA, notification.extendedMsg);
            }
        }
        if (notification.highlight != null) {
            resultIntent.putExtra(Constants.HIGHLIGHTED_MESSAGE_EXTRA, notification.highlight);
        }
        else if (notification.cathighlight != null) {
            resultIntent.putExtra(Constants.CATHIGHLIGHTED_MESSAGE_EXTRA, notification.cathighlight);
        }

        pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Get a default sound to play
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (notification.title == null || notification.title.length() <= 0) {
            notification.title = "TextMuse";
        }

        AzureTextMuseNotificationChannelUtil channelUtil = new AzureTextMuseNotificationChannelUtil(context);
        Notification.Builder mBuilder = channelUtil.getMessagesChannelNotification(notification.title, notification.message)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }
}
