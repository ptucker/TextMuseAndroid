package com.laloosh.textmuse.utils;

//https://docs.microsoft.com/en-us/azure/notification-hubs/notification-hubs-android-push-notification-google-fcm-get-started

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.notifications.MobileServicePush;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.google.firebase.FirebaseApp;

import java.net.MalformedURLException;
import java.util.ArrayList;


public class AzureIntegrationSingleton {

    //Singleton details
    private static class SingletonHolder {
        private static final AzureIntegrationSingleton instance = new AzureIntegrationSingleton();
    }

    public static AzureIntegrationSingleton getInstance() {
        return SingletonHolder.instance;
    }

    private AzureIntegrationSingleton() {}
    //End singleton details

    //private static final String SENDER_ID = "949243437629";
    private static final String SENDER_ID = "765970005379";
    private static final String MOBILE_SERVICE_URL = "https://textmuse.azure-mobile.net/";
    //private static final String MOBILE_APPKEY = "AkuctIqaVFjQEPTOZDLcaGEutliECb96";
    private static final String MOBILE_APPKEY = "AIzaSyC58iQZc8PJMF3tZhgAJY8zu1SCFmbyCHA";
    private MobileServiceClient mClient;
    private boolean mStarted = false;
    private String mGcmRegistrationId = null;

    //This should be called on app startup in order to start up the Azure client
    public void startupIntegration(Context context) {
        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(MOBILE_SERVICE_URL, MOBILE_APPKEY, context);

            FirebaseApp app = FirebaseApp.initializeApp(context);
            NotificationsManager.handleNotifications(context, AzureNotificationSettings.SenderID,
                    AzureTextMuseNotificationHandler.class);
            mStarted = true;
            Log.d(Constants.TAG, "Started up azure integration for push notifications");
        } catch (MalformedURLException e) {
            Log.e(Constants.TAG, "Could not start up azure integration!");
        }
    }

    public MobileServiceClient getClient() {
        return mClient;
    }

    public boolean getStarted() {
        return mStarted;
    }

    public String getGcmRegistrationId() {
        return mGcmRegistrationId;
    }

    public void setGcmRegistrationId(String gcmRegistrationId) {
        mGcmRegistrationId = gcmRegistrationId;
    }

    public void registerWithNotificationHubs(Context context) {
        int skin = TextMuseSkinData.getCurrentlySelectedSkin(context);
        Intent intent = new Intent(context, RegistrationIntentService.class);
        intent.putExtra(Constants.SKIN_ID_EXTRA, skin);
        try {
            context.startService(intent);
        }
        catch (Exception ex) {
            ;   //possible illegal state exception
        }

        /*
        deprecated with GCM. New code in RegistrationIntentService (see getTags()).
        try {
            if (getStarted() && getClient() != null && !TextUtils.isEmpty(mGcmRegistrationId)) {

                ArrayList<String> tags = null;
                tags = new ArrayList<String>();

                GlobalData globalData = GlobalData.getInstance();
                if (globalData.hasLoadedData()) {
                    TextMuseData data = globalData.getData();
                    if (data.appId > 0) {
                        tags.add(Integer.toString(data.appId));

                        int skinId = TextMuseSkinData.getCurrentlySelectedSkin(context);
                        if (skinId <= 0) {
                            skinId = 0;
                        }
                        tags.add("skin" + Integer.toString(skinId));
                    }
                    if (data.followedSponsorsSet != null && data.followedSponsorsSet.size() > 0) {
                        for (Integer id : data.followedSponsorsSet) {
                            tags.add("spon" + Integer.toString(id));
                        }
                    }
                }

                MobileServicePush pushClient = getClient().getPush();

                if (tags.size() > 0) {
                    pushClient.register(mGcmRegistrationId, tags.toArray(new String[tags.size()]));
                } else {
                    pushClient.register(mGcmRegistrationId, null);
                }

                Log.d(Constants.TAG, "Succeeded in sending off the registration with GCM ID: " + mGcmRegistrationId);
            }
        }
        catch(Exception e) {
            Log.e(Constants.TAG, "Failed to register for google cloud messaging with the azure client!");
        }
         */
    }
}
