package com.laloosh.textmuse.utils;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.firebase.iid.*;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.microsoft.windowsazure.messaging.NotificationHub;

import java.util.ArrayList;

/**
 * Created by petertucker on 4/25/17.
 */

public class RegistrationIntentService extends IntentService {
    private static String TAG = "RegIntentService";
    private NotificationHub hub;
    private int skinid;

    public RegistrationIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        skinid = intent.getIntExtra(Constants.SKIN_ID_EXTRA, 0);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString = null;
        String regID = null;
        String storedToken = null;
        ArrayList<String> tags = getTags();

        try{
            String fcm = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "FCM token: " + fcm);

            if (((regID = sharedPreferences.getString("registrationID", null)) == null)) {
                NotificationHub hub = new NotificationHub(AzureNotificationSettings.HubName,
                        AzureNotificationSettings.HubListenConnectionString, this);
                regID = hub.register(fcm, tags.toArray(new String[tags.size()])).getRegistrationId();
                resultString = "New NH Registration Successfully - RegID: " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID).apply();
                sharedPreferences.edit().putString("FCMToken", fcm).apply();
            }
            else if ((storedToken = sharedPreferences.getString("FCMToken", "")) != fcm) {
                NotificationHub hub = new NotificationHub(AzureNotificationSettings.HubName,
                        AzureNotificationSettings.HubListenConnectionString, this);
                regID = hub.register(fcm, tags.toArray(new String[tags.size()])).getRegistrationId();
                resultString = "New NH Registration Successfully - RegID: " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID).apply();
                sharedPreferences.edit().putString("FCMToken", fcm).apply();
            }
        }
        catch (Exception ex) {
            Log.e(TAG, resultString = "Failed to register for notifications", ex);
        }
    }

    private ArrayList<String> getTags() {
        ArrayList<String> tags = null;
        tags = new ArrayList<String>();

        GlobalData globalData = GlobalData.getInstance();
        if (globalData.hasLoadedData()) {
            TextMuseData data = globalData.getData();
            if (data.appId > 0) {
                tags.add(Integer.toString(data.appId));

                tags.add("skin" + Integer.toString(skinid));
            }
            if (data.followedSponsorsSet != null && data.followedSponsorsSet.size() > 0) {
                for (Integer id : data.followedSponsorsSet) {
                    tags.add("spon" + Integer.toString(id));
                }
            }
        }

        return tags;
    }
}
