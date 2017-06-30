package com.laloosh.textmuse.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.BuildConfig;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseLaunchIcon;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.laloosh.textmuse.tasks.CalculateFreeSpaceCleanupAsyncTask;
import com.laloosh.textmuse.tasks.FetchNotesAsyncTask;
import com.laloosh.textmuse.tasks.FetchSkinsAsyncTask;
import com.laloosh.textmuse.utils.AzureIntegrationSingleton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class SplashScreenActivity extends ActionBarActivity implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler, FetchSkinsAsyncTask.FetchSkinsAsyncTaskHandler{

    //10 seconds is the max amount of time the splash screen is going to be up
    private static final long SPLASH_SCREEN_MAX_TIME = 10000;

    //30 seconds for first run max splash screen time
    private static final long SPLASH_SCREEN_MAX_TIME_FIRST_RUN = 30000;

    private TextMuseData mData;
    private boolean mFinishedLoading;
    private boolean mFinishedLoadingSkins;
    private FetchNotesAsyncTask.FetchNotesResult mFinishedLoadingResult;
    private FetchSkinsAsyncTask.FetchSkinsResult mFinishedLoadingSkinsResult;
    private Handler mHandler;
    private boolean mFirstLaunch;
    private String mLaunchMessage;
    private String mHighlighted;
    private boolean mTimerFired;
    private boolean mSkinRetry = true;

    private FetchNotesAsyncTask mFetchNotesTask;
    private FetchSkinsAsyncTask mFetchSkinsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            //def applicationid = "com.laloosh.textmuse"
            //def applicationid = "com.oodles.oodles"
            //def applicationid = "com.laloosh.clearwatertextmuse"
            def applicationid = "com.laloosh.humanixtextmuse"
         */
        Log.d("AppID", com.laloosh.textmuse.BuildConfig.applicationid);
        switch (com.laloosh.textmuse.BuildConfig.applicationid) {
            case "com.laloosh.textmuse":
                Constants.BuildType = Constants.Builds.University;
                break;
            case "com.laloosh.humanixtextmuse":
                Constants.BuildType = Constants.Builds.Humanix;
                break;
            case "com.laloosh.clearwatertextmuse":
                Constants.BuildType = Constants.Builds.Clearwater;
                break;
            case "com.oodles.oodles":
                Constants.BuildType = Constants.Builds.Oodles;
                break;
        }

        //getSupportActionBar().hide();

        GlobalData instance = GlobalData.getInstance();
        instance.loadData(this);
        mData = instance.getData();

        setupLayout();

        if (mData != null) {
            mData.updateNoteImageFlags(this);
        }

        mFirstLaunch = isFirstLaunch();
        setLaunchedBefore();

        mHandler = new Handler();

        getNewContent();
        cleanImageCacheTask();
        scheduleTimerForFinish();
        startupAzureIntegration();

        Intent intent = getIntent();
        mLaunchMessage = intent.getStringExtra(Constants.LAUNCH_MESSAGE_EXTRA);
        mHighlighted = intent.getStringExtra(Constants.HIGHLIGHTED_MESSAGE_EXTRA);
    }

    //Sets up the layout.  Must be called after we load the global data
    private void setupLayout() {
        int skinId = TextMuseSkinData.getCurrentlySelectedSkin(this);
        if (Constants.BuildType == Constants.Builds.Humanix) {
            setContentView(R.layout.activity_splash_screen_humanix);
        } else {
            setContentView(R.layout.activity_splash_screen);
        }
    }

    //If we've failed in loading new data, and we don't have any, then we'll get here.  Load the
    //cached copy of our content and just go with it for now
    private void loadRawContent() {
        Log.d(Constants.TAG, "Falling back to the original content bundled with the app");
        mData = TextMuseData.loadRawContent(this);
        mData.updatePhotos(this);
        GlobalData.getInstance().updateData(mData);
    }

    private void getNewContent() {
        mFetchNotesTask = new FetchNotesAsyncTask(this, getApplicationContext(), mData == null ? -1 : mData.appId);
        mFetchNotesTask.execute();

        mFetchSkinsTask = new FetchSkinsAsyncTask(this, getApplicationContext());
        mFetchSkinsTask.execute();
    }

    private void cleanImageCacheTask() {
        CalculateFreeSpaceCleanupAsyncTask task = new CalculateFreeSpaceCleanupAsyncTask(getApplicationContext());
        task.execute();
    }

    private void scheduleTimerForFinish() {
        long timeDelay = mFirstLaunch ?  SPLASH_SCREEN_MAX_TIME_FIRST_RUN : SPLASH_SCREEN_MAX_TIME;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timerFinished();
            }
        }, timeDelay);
    }

    private void timerFinished() {
        if (mTimerFired) {
            return;
        }

        if (mFirstLaunch && Constants.BuildType == Constants.Builds.University && mSkinRetry && !mFinishedLoadingSkins)
        {
            //Give the request for skins one more try
            mSkinRetry = false;
            long timeDelay = mFirstLaunch ?  SPLASH_SCREEN_MAX_TIME_FIRST_RUN : SPLASH_SCREEN_MAX_TIME;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timerFinished();
                }
            }, timeDelay);
            return;
        }

        mTimerFired = true;

        if (!mFinishedLoading || (mFinishedLoading && mFinishedLoadingResult == FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED)) {
            if (mData == null) {
               loadRawContent();
            }
        }

        Intent intent;

        if (mFirstLaunch && Constants.BuildType == Constants.Builds.University) {
            //Go to the skin selection screen
            if (mFinishedLoadingSkins && mFinishedLoadingSkinsResult == FetchSkinsAsyncTask.FetchSkinsResult.FETCH_SUCCEEDED) {
                intent = new Intent(SplashScreenActivity.this, SkinSelectActivity.class);
                intent.putExtra(SkinSelectActivity.EXTRA_LAUNCH_FROM_SPLASH, true);

            } else {
                //Go to the walkthrough screen if there are no skins or no response

                intent = new Intent(SplashScreenActivity.this, WalkthroughActivity.class);
                intent.putExtra(WalkthroughActivity.INITIAL_LAUNCH_EXTRA, true);
            }

        } else {
            intent = new Intent(SplashScreenActivity.this, HomeActivity.class);

            if (mFinishedLoading && mFinishedLoadingResult != FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED) {
                intent.putExtra(HomeActivity.ALREADY_LOADED_DATA_EXTRA, true);
            }

            if (mLaunchMessage != null && mLaunchMessage.length() > 0) {
                intent.putExtra(Constants.LAUNCH_MESSAGE_EXTRA, mLaunchMessage);
            }
            if (mHighlighted != null && mHighlighted.length() > 0) {
                intent.putExtra(Constants.HIGHLIGHTED_MESSAGE_EXTRA, mHighlighted);
            }
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        SplashScreenActivity.this.finish();

        overridePendingTransition(R.anim.activityfadein, R.anim.activityfadeout);
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
        Log.d(Constants.TAG, "Finished loading main data");
        mFinishedLoading = true;
        mFinishedLoadingResult = result;

        if (mFinishedLoadingSkins) {
            timerFinished();
        }
    }

    @Override
    public void handleFetchSkinsResult(FetchSkinsAsyncTask.FetchSkinsResult result) {
        Log.d(Constants.TAG, "Finished loading skins");
        mFinishedLoadingSkins = true;
        mFinishedLoadingSkinsResult = result;

        if (mFinishedLoading) {
            timerFinished();
        }
    }

    private void startupAzureIntegration() {
        AzureIntegrationSingleton azureIntegrationSingleton = AzureIntegrationSingleton.getInstance();
        azureIntegrationSingleton.startupIntegration(this.getApplicationContext());
    }
}
