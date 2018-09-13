package com.laloosh.textmuse.utils;

import android.app.Notification;
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

import org.json.JSONObject;

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

    class TextMuseNotification {
        public String message;
        public String url;
        public String extendedMsg;
        public String title;
        public String highight;

        public void parseBundle(Bundle bundle) {
            if (inBundle(bundle))
                return;
            else if (bundleInBundle(bundle))
                return;
            else
                stringInBundle(bundle);
        }

        private boolean inBundle(Bundle bundle) {
            message = bundle.getString("message");
            if (message == null)
                message = bundle.getString("alert");
            if (message != null) {
                url = bundle.getString("messageUrl");
                extendedMsg = bundle.getString("extendedMessage");
                title = bundle.getString("messageTitle");
                highight = bundle.getString("highlight");
                return true;
            }
            else
                return false;
        }

        private boolean bundleInBundle(Bundle bundle) {
            Bundle b = bundle.getBundle("notification");
            return b != null ? (inBundle(b)) : false;
        }

        private boolean stringInBundle(Bundle bundle) {
            //Figure out the key
            String k = "";
            String[] possibleKeys = {"gcm.notification.body", "notification", "data", "message"};
            for (String key: bundle.keySet())
            {
                String v = bundle.getString(key);
                for (String p: possibleKeys) {
                    if (key.equals(p) && v != null && v.length() > 0) {
                        k = key;
                        break;
                    }
                }
                if (k.length() > 0)
                    break;
            }

            //Now that we have a key, let's figure out if it's plain text or JSON
            try {
                JSONObject msg = new JSONObject(bundle.getString(k));
                parseJSON(msg);
            }
            catch (Exception e) {
                //Not JSON. Good luck with what you have!
                message = bundle.getString(k);
            }

            return true;
        }

        private void parseJSON(JSONObject msg) {
            //Let's see if there's a data member first
            try {
                Object m = msg.get("data");
                msg = (JSONObject) m;
            }
            catch (Exception e) { }

            //Now hopefully we have what we're looking for
            try {
                message = msg.getString("message");
                if (msg.has("messageUrl"))
                    url = msg.getString("messageUrl");
                if (msg.has("extendedMessage"))
                    extendedMsg = msg.getString("extendedMessage");
                if (msg.has("messageTitle"))
                    title = msg.getString("messageTitle");
                if (msg.has("highlight"))
                    highight = msg.getString("highlight");
            }
            catch (Exception e) {
                //Nope. Do your best
                message = msg.toString();
            }
        }
    }

    @Override
    public void onReceive(Context context, Bundle bundle) {
        TextMuseNotification notification = new TextMuseNotification();
        notification.parseBundle(bundle);

        //Clear out the notifications
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        showReminderNotification(context, notification.message, notification.url,
                notification.extendedMsg, notification.title, notification.highight);
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

        AzureTextMuseNotificationChannelUtil channelUtil = new AzureTextMuseNotificationChannelUtil(context);
        Notification.Builder mBuilder = channelUtil.getMessagesChannelNotification(messageTitle, text)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }

}

