package com.laloosh.textmuse.utils;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by petertucker on 4/25/17.
 */

public class TextMuseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "TextMuseIIDService";

    public void onTokenRefresh() {
        Log.d(TAG, "Refreshing GCM Reg Token");

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
