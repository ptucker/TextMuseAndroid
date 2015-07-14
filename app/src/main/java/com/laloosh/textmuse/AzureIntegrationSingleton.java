package com.laloosh.textmuse;

import android.content.Context;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import java.net.MalformedURLException;


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

    private static final String SENDER_ID = "949243437629";
    private static final String MOBILE_SERVICE_URL = "https://textmuse.azure-mobile.net/";
    private static final String MOBILE_APPKEY = "AkuctIqaVFjQEPTOZDLcaGEutliECb96";
    private MobileServiceClient mClient;
    private boolean mStarted = false;

    //This should be called on app startup in order to start up the Azure client
    public void startupIntegration(Context context) {
        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(MOBILE_SERVICE_URL, MOBILE_APPKEY, context);

            NotificationsManager.handleNotifications(context, SENDER_ID, AzureTextMuseNotificationHandler.class);
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

}
