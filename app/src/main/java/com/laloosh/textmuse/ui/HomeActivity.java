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
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
                EventBus.getDefault().post(new TabSelectedEvent(tab.getPosition()));
                super.onTabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                EventBus.getDefault().post(new TabDeselectedEvent(tab.getPosition()));
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

        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new TabSelectedEvent(0));
            }
        });
    }

    private void setSkinTitle() {
        setTitle("");
        TextView textView = (TextView) findViewById(R.id.mainToolbarTitle);
        mToolbarImage = (ImageView) findViewById(R.id.mainToolbarButton);

        if (mData != null && mData.skinData != null) {
            textView.setText(mData.skinData.name + " TextMuse");

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
            textView.setText("TextMuse");
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

        public MainAdapter(FragmentManager fm, boolean alreadyLoadedData) {
            super(fm);
            mAlreadyLoadedData = alreadyLoadedData;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return HomeFragment.newInstance(mAlreadyLoadedData, false, 0);
            } else if (position == 1) {
                return HomeFragment.newInstance(mAlreadyLoadedData, true, 1);
            } else {
                return new GroupsFragment();
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
    }
}
