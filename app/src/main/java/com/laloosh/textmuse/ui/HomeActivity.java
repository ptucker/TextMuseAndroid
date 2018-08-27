package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.broadcastreceivers.AlarmReceivedBroadcastReceiver;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.events.ShowCategoriesChangedEvent;
import com.laloosh.textmuse.dialogs.LaunchMessageDialogFragment;
import com.laloosh.textmuse.utils.GuidedTour;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

public class HomeActivity extends AppCompatActivity {

    public static final String ALREADY_LOADED_DATA_EXTRA = "com.laloosh.textmuse.alreadyloadeddata";
    public static final int REQUEST_CODE_SETTINGS = 2333;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private MainAdapter mAdapter;
    private ImageView mToolbarImage;

    private boolean mAlreadyLoadedData = false;
    private TextMuseData mData;
    private String mHighlighted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GlobalData instance = GlobalData.getInstance();
        if (!instance.hasLoadedData()) {
            instance.loadData(this);
        }
        mData = instance.getData();


        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbarImage = (ImageView) findViewById(R.id.mainToolbarButton);

        setSupportActionBar(mToolbar);
        setSkinTitle();

        setLastNotified();
        setNotificationAlarm();

        Intent intent = getIntent();
        mAlreadyLoadedData = intent.getBooleanExtra(ALREADY_LOADED_DATA_EXTRA, false);

        mViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mHighlighted = intent.getStringExtra(Constants.HIGHLIGHTED_MESSAGE_EXTRA);
        mAdapter = new MainAdapter(getSupportFragmentManager(), mAlreadyLoadedData, mHighlighted);
        mViewPager.setAdapter(mAdapter);

        final String startMessage = intent.getStringExtra(Constants.LAUNCH_MESSAGE_EXTRA);
        if (startMessage != null) {
            mViewPager.post(new Runnable() {
                @Override
                public void run() {
                    LaunchMessageDialogFragment fragment = LaunchMessageDialogFragment.newInstance(startMessage);
                    fragment.show(getSupportFragmentManager(), "launchmessagefragment");
                }
            });
        }

        //Post an extra selection event for 0, since we don't get that callback
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                notifyTabSelected(0);
            }
        });
    }

    public void onBackPressed() {
        if (!mAdapter.onBack())
            super.onBackPressed();
    }

    protected void notifyTabSelected(int position) {
        String tag = mAdapter.getTabFragmentTag(position);
        if (tag != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof TabSelectListener) {
                Log.d(Constants.TAG, String.format("Tab %d was selected, firing listener", position));
                TabSelectListener listener = (TabSelectListener) fragment;
                listener.onTabSelected();
            }
        }
    }

    protected void notifyTabDeselected(int position) {
        String tag = mAdapter.getTabFragmentTag(position);
        if (tag != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof TabSelectListener) {
                Log.d(Constants.TAG, String.format("Tab %d was de-selected, firing listener", position));
                TabSelectListener listener = (TabSelectListener) fragment;
                listener.onTabDeselected();
            }
        }
    }

    public void setSkinTitle() {
        setTitle("");
        TextView textView = (TextView) findViewById(R.id.mainToolbarTitle);
        //mToolbarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_menu_white));
        mToolbarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.gear_white));

        if (Constants.BuildType == Constants.Builds.Humanix) {
            textView.setText("Hire Me Northwest");
        }
        else if (Constants.BuildType == Constants.Builds.YouthREACH) {
            textView.setText("YouthREACH");
        }
        else {
            textView.setText("TextMuse");
        }
    }

    public void disableTitleImageLook() {
        mToolbarImage.setAlpha(0.4f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {

                if (data != null && data.getBooleanExtra(SettingsActivity.SHOWN_CATEGORIES_CHANGED_EXTRA, false)) {
                    //Refresh our data if the shown categories changed

                    EventBus.getDefault().post(new ShowCategoriesChangedEvent());
                    return;
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void setLastNotified() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        String currentTime = fmt.print(DateTime.now());

        SharedPreferences.Editor editor = sharedPreferences.edit();

        //A launch of the app is like a notification.  Reset our notification count to 0 and also
        //Put the current time in there so that we can check against it later
        editor.putString(Constants.SHARED_PREF_KEY_LAST_NOTIFIED, currentTime);
        editor.putInt(Constants.SHARED_PREF_KEY_NOTIFICATION_COUNT, 0);
        editor.commit();
    }

    private void setNotificationAlarm() {
        AlarmReceivedBroadcastReceiver.setAlarm(this);
    }

    public static class MainAdapter extends FragmentPagerAdapter {
        private boolean mAlreadyLoadedData = false;
        private HashMap<Integer, String> mTabMap;
        private HomeFragment mFragment;
        private String mHighlighted;

        public MainAdapter(FragmentManager fm, boolean alreadyLoadedData, String highlighted) {
            super(fm);
            mAlreadyLoadedData = alreadyLoadedData;
            mHighlighted = highlighted;
            mTabMap = new HashMap<>(3);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                boolean events = (Constants.BuildType != Constants.Builds.Humanix &&
                        Constants.BuildType != Constants.Builds.YouthREACH);
                mFragment = HomeFragment.newInstance(mAlreadyLoadedData, events, mHighlighted);

                return mFragment;
            } else if (position == 1) {
                return BadgeFragment.newInstance();
            } else {
                return GroupsFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Home";
            } else if (position == 1) {
                return "Badges";
            } else {
                return "Groups";
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            mTabMap.put(position, createdFragment.getTag());
            return createdFragment;
        }

        public String getTabFragmentTag(int position) {
            return mTabMap.get(position);
        }

        public boolean onBack() {
            return mFragment.onBack();
        }
    }
}
