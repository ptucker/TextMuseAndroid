package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.broadcastreceivers.AlarmReceivedBroadcastReceiver;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.events.ShowCategoriesChangedEvent;
import com.laloosh.textmuse.datamodel.events.TabDeselectedEvent;
import com.laloosh.textmuse.datamodel.events.TabSelectedEvent;
import com.laloosh.textmuse.dialogs.LaunchMessageDialogFragment;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

public class HomeActivity extends AppCompatActivity {

    public static final String ALREADY_LOADED_DATA_EXTRA = "com.laloosh.textmuse.alreadyloadeddata";
    public static final int REQUEST_CODE_SETTINGS = 2333;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MainAdapter mAdapter;
    private ImageView mToolbarImage;

    private boolean mAlreadyLoadedData = false;
    private TextMuseData mData;

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
        mToolbar.setTitleTextColor(0xffffffff);
        mToolbarImage = (ImageView) findViewById(R.id.mainToolbarButton);

        setSupportActionBar(mToolbar);
        setSkinTitle();

        setLastNotified();
        setNotificationAlarm();

        Intent intent = getIntent();
        mAlreadyLoadedData = intent.getBooleanExtra(ALREADY_LOADED_DATA_EXTRA, false);

        mViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mAdapter = new MainAdapter(getSupportFragmentManager(), mAlreadyLoadedData);
        mViewPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                notifyTabSelected(tab.getPosition());
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                notifyTabDeselected(tab.getPosition());
                super.onTabUnselected(tab);
            }
        });


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
        mToolbarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_menu_white));

        if (mData != null && mData.skinData != null) {
            textView.setText(mData.skinData.name + " TextMuse");
        } else {
            textView.setText("TextMuse");
        }
    }

    public void setSkinImage() {
        if (mData != null && mData.skinData != null) {
            File logoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mData.skinData.getIconImageFilename());
            if (logoFile.exists()) {
                Picasso.with(this)
                        .load(logoFile)
                        .fit()
                                //.placeholder(R.drawable.launcher_icon)
                        .error(R.drawable.launcher_icon)
                        .centerInside()
                        .into(mToolbarImage);
            } else {
                mToolbarImage.setImageResource(R.drawable.launcher_icon);
            }
        } else {
            mToolbarImage.setImageResource(R.drawable.launcher_icon);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main_category, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
//
//            return true;
//        } else if (id == android.R.id.home) {
//            Log.d(Constants.TAG, "on options item selected -- home pushed!");
//        } else if (id == R.id.home) {
//            Log.d(Constants.TAG, "on options item selected2 -- home pushed!");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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

        public MainAdapter(FragmentManager fm, boolean alreadyLoadedData) {
            super(fm);
            mAlreadyLoadedData = alreadyLoadedData;
            mTabMap = new HashMap<>(3);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return HomeFragment.newInstance(mAlreadyLoadedData, false);
            } else if (position == 1) {
                return HomeFragment.newInstance(mAlreadyLoadedData, true);
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
                return "Events";
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
    }
}
