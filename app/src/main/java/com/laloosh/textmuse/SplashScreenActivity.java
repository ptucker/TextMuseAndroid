package com.laloosh.textmuse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;

import java.util.Timer;
import java.util.TimerTask;


public class SplashScreenActivity extends ActionBarActivity implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler{

    //4 seconds is the max amount of time the splash screen is going to be up
    private static final long SPLASH_SCREEN_MAX_TIME = 4000;

    private TextMuseData mData;
    private boolean mFinishedLoading;
    private FetchNotesAsyncTask.FetchNotesResult mFinishedLoadingResult;
    private Timer mTimer;
    private boolean mFirstLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        GlobalData instance = GlobalData.getInstance();
        instance.loadData(this);
        mData = instance.getData();

        //TODO: If no data, use a cached version that we bundle with the app....

        mFirstLaunch = isFirstLaunch();
        setLaunchedBefore();

        getNewContent();
        scheduleTimerForFinish();
    }

    private void getNewContent() {
        FetchNotesAsyncTask task = new FetchNotesAsyncTask(this, getApplicationContext(), mData == null ? -1 : mData.appId);
        task.execute();
    }

    private void scheduleTimerForFinish() {

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerFinished();
            }
        }, SPLASH_SCREEN_MAX_TIME);

    }

    private void timerFinished() {
        Intent intent;

        if (mFirstLaunch) {
            //Go to the walkthrough screen

            intent = new Intent(SplashScreenActivity.this, WalkthroughActivity.class);
            intent.putExtra(WalkthroughActivity.INITIAL_LAUNCH_EXTRA, true);
        } else {
            intent = new Intent(SplashScreenActivity.this, MainCategoryActivity.class);

            if (mFinishedLoading && mFinishedLoadingResult != FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED) {
                intent.putExtra(MainCategoryActivity.ALREADY_LOADED_DATA_EXTRA, true);
            }
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashScreenActivity.this.finish();

    }

    private boolean isFirstLaunch() {

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        boolean hasLaunchedBefore = sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_LAUNCHED_BEFORE, false);

        if (!hasLaunchedBefore) {
            return true;
        }

        return false;
    }

    private void setLaunchedBefore() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHARED_PREF_KEY_LAUNCHED_BEFORE, true);
        editor.commit();
    }

    @Override
    public void handleFetchResult(FetchNotesAsyncTask.FetchNotesResult result) {
        mFinishedLoading = true;
        mFinishedLoadingResult = result;

        mTimer.cancel();
        timerFinished();
    }
}
