package com.laloosh.textmuse.utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.firebase.iid.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.microsoft.windowsazure.messaging.NotificationHub;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by petertucker on 4/25/17.
 */

public class RegistrationIntentService extends IntentService {
    private static String TAG = "RegIntentService";
    private String FCM_token;
    private NotificationHub hub;
    private int skinid;

    public RegistrationIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        skinid = TextMuseSkinData.getCurrentlySelectedSkin(this);
        String resultString = null;
        String regID = null;
        String storedToken = null;
        ArrayList<String> tags = getTags();

        //Set up notification channels
        AzureTextMuseNotificationChannelUtil channelUtil = new AzureTextMuseNotificationChannelUtil(this.getApplicationContext());

        try{
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    FCM_token = instanceIdResult.getToken();
                    Log.d(TAG, "FCM Registration Token: " + FCM_token);
                }
            });
            int countdown = 5;
            do {
                TimeUnit.SECONDS.sleep(1);
                countdown--;
            } while (FCM_token == null && countdown > 0);
            Log.d(TAG, "FCM token: " + FCM_token);

            if (((regID = sharedPreferences.getString("registrationID", null)) == null)) {
                NotificationHub hub = new NotificationHub(AzureNotificationSettings.HubName,
                        AzureNotificationSettings.HubListenConnectionString, this);
                regID = hub.register(FCM_token, tags.toArray(new String[tags.size()])).getRegistrationId();
                resultString = "New NH Registration Successfully - RegID: " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID).apply();
                sharedPreferences.edit().putString("FCMToken", FCM_token).apply();
            }
            else if ((storedToken = sharedPreferences.getString("FCMToken", "")).compareTo(FCM_token) != 0) {
                NotificationHub hub = new NotificationHub(AzureNotificationSettings.HubName,
                        AzureNotificationSettings.HubListenConnectionString, this);
                regID = hub.register(FCM_token, tags.toArray(new String[tags.size()])).getRegistrationId();
                resultString = "New NH Registration Successfully - RegID: " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID).apply();
                sharedPreferences.edit().putString("FCMToken", FCM_token).apply();
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
