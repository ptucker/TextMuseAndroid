package com.laloosh.textmuse.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

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

    private TextMuseData mData;
    private boolean mFinishedLoading;
    private boolean mFinishedLoadingSkins;
    private FetchNotesAsyncTask.FetchNotesResult mFinishedLoadingResult;
    private FetchSkinsAsyncTask.FetchSkinsResult mFinishedLoadingSkinsResult;
    private Timer mTimer;
    private boolean mFirstLaunch;
    private String mLaunchMessage;
    private boolean mTimerFired;

    private FetchNotesAsyncTask mFetchNotesTask;
    private FetchSkinsAsyncTask mFetchSkinsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        getNewContent();
        cleanImageCacheTask();
        scheduleTimerForFinish();
        startupAzureIntegration();

        Intent intent = getIntent();
        mLaunchMessage = intent.getStringExtra(Constants.LAUNCH_MESSAGE_EXTRA);
    }

    //Sets up the layout.  Must be called after we load the global data
    private void setupLayout() {
        int skinId = TextMuseSkinData.getCurrentlySelectedSkin(this);
        if (skinId <= 0) {
            setContentView(R.layout.activity_splash_screen);
        } else {
            if (mData == null || mData.skinData == null) {
                setContentView(R.layout.activity_splash_screen);
            } else {
                //Use the current skin data to show a different splash screen
                setContentView(R.layout.activity_splash_screen_skin);

                ImageView splashScreenLogo = (ImageView) findViewById(R.id.splashScreenSkinLogo);
                ImageView splashScreenIcon = (ImageView) findViewById(R.id.splashScreenSkinIcon);

                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mData.skinData.getIconImageFilename());
                if (file.exists()) {
                    Picasso.with(this)
                           .load(file)
                           .into(splashScreenIcon);
                } else {
                    Picasso.with(this)
                            .load(mData.skinData.icon)
                            .into(splashScreenIcon);
                }

                //Pick a logo
                //First get the screen size
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screenWidth = displaymetrics.widthPixels;

                //Get the closest in size and just pick one
                ArrayList<TextMuseLaunchIcon> closestSizeIcons = mData.skinData.getClosestSizeLaunchIcons(screenWidth);

                if (closestSizeIcons == null || closestSizeIcons.size() <= 0) {
                    setContentView(R.layout.activity_splash_screen);
                    return;
                }

                Random ran = new Random();
                int randIndex = ran.nextInt(closestSizeIcons.size());

                TextMuseLaunchIcon icon = closestSizeIcons.get(randIndex);
                File logoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mData.skinData.getSplashImageFilename(icon, randIndex));
                if (logoFile.exists()) {
                    Picasso.with(this)
                            .load(logoFile)
                            .fit()
                            .centerInside()
                            .into(splashScreenLogo);
                } else {
                    Picasso.with(this)
                            .load(icon.url)
                            .fit()
                            .centerInside()
                            .into(splashScreenLogo);
                }
            }
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

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerFinished();
            }
        }, SPLASH_SCREEN_MAX_TIME);

    }

    private void timerFinished() {
        if (mTimerFired) {
            return;
        }

        mTimerFired = true;

        if (!mFinishedLoading || (mFinishedLoading && mFinishedLoadingResult == FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED)) {
            if (mData == null) {
               loadRawContent();
            }
        }

        Intent intent;

        if (mFirstLaunch) {
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
        mFinishedLoading = true;
        mFinishedLoadingResult = result;

        if (mFinishedLoadingSkins) {
            mTimer.cancel();
            timerFinished();
        }
    }

    @Override
    public void handleFetchSkinsResult(FetchSkinsAsyncTask.FetchSkinsResult result) {
        mFinishedLoadingSkins = true;
        mFinishedLoadingSkinsResult = result;

        if (mFinishedLoading) {
            mTimer.cancel();
            timerFinished();
        }
    }

    private void startupAzureIntegration() {
        AzureIntegrationSingleton azureIntegrationSingleton = AzureIntegrationSingleton.getInstance();
        azureIntegrationSingleton.startupIntegration(this.getApplicationContext());
    }
}
