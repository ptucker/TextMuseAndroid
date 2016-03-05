package com.laloosh.textmuse.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;

public class BootTimeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "Booted -- registering alarm for notifications");
        AlarmReceivedBroadcastReceiver.setAlarm(context);
        return;
    }
}
