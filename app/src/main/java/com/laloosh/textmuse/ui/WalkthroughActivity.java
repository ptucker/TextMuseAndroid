package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.viewpagerindicator.CirclePageIndicator;


public class WalkthroughActivity extends ActionBarActivity {
    public static final String INITIAL_LAUNCH_EXTRA = "com.laloosh.textmuse.initiallaunch";

    private ViewPager mViewPager;
    private CirclePageIndicator mPageIndicator;
    private WalkthroughAdapter mAdapter;
    private boolean mInitialLaunch;

    private static final String[] WALKTHROUGH_DESCRIPTIONS = {"Every day, you'll find great local deals, great events, university news, and other fun stuff.",
                                                              "Choose a text to see more, save it for later, or share it with someone.",
                                                              "Choose a friend or group. If they have more than one number, tap the person's name.",
                                                              "Before you send it, you can make some edits to give it that personal touch.",
                                                              "Use the settings to personalize TextMuse - choose which categories you want to see and send us feedback."
                                                             };

    private static final int[] WALKTHROUGH_IMAGE_RESOURCES = {R.drawable.page1_walkthrough,
                                                              R.drawable.page2_walkthrough,
                                                              R.drawable.page3_walkthrough,
                                                              R.drawable.page4_walkthrough,
                                                              R.drawable.page5_walkthrough
                                                             };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_walkthrough);

        if (savedInstanceState != null) {
            mInitialLaunch = savedInstanceState.getBoolean(INITIAL_LAUNCH_EXTRA, false);
        } else {
            Intent intent = getIntent();
            mInitialLaunch = intent.getBooleanExtra(INITIAL_LAUNCH_EXTRA, false);
        }

        mAdapter = new WalkthroughAdapter(getSupportFragmentManager(), mInitialLaunch);

        mViewPager = (ViewPager) findViewById(R.id.walkthroughViewPager);
        mViewPager.setAdapter(mAdapter);

        mPageIndicator = (CirclePageIndicator) findViewById(R.id.walkthroughPageIndicator);
        mPageIndicator.setViewPager(mViewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(INITIAL_LAUNCH_EXTRA, mInitialLaunch);
        super.onSaveInstanceState(outState);
    }

    public static class WalkthroughAdapter extends FragmentPagerAdapter {
        private boolean mInitialLaunch;

        public WalkthroughAdapter(FragmentManager fm, boolean initialLaunch){
            super(fm);
            mInitialLaunch = initialLaunch;
        }

        @Override
        public Fragment getItem(int position) {
            return WalkthroughFragment.newInstance(position, mInitialLaunch);
        }

        @Override
        public int getCount() {
            return WALKTHROUGH_DESCRIPTIONS.length;
        }
    }

    public static class WalkthroughFragment extends Fragment {
        private int mNum;
        private boolean mInitialLaunch;

        public static WalkthroughFragment newInstance(int num, boolean initialLaunch) {
            WalkthroughFragment fragment = new WalkthroughFragment();

            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putBoolean("initiallaunch", initialLaunch);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
            mInitialLaunch = getArguments() != null ? getArguments().getBoolean("initiallaunch") : false;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.walkthrough_item, container, false);

            ImageView imageView = (ImageView) v.findViewById(R.id.walkthroughImage);
            TextView textView = (TextView) v.findViewById(R.id.walkthroughTextViewDescription);
            ViewGroup doneButton = (ViewGroup) v.findViewById(R.id.walkthroughDoneButton);

            textView.setText(WALKTHROUGH_DESCRIPTIONS[mNum]);
            imageView.setImageResource(WALKTHROUGH_IMAGE_RESOURCES[mNum]);

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        if (mInitialLaunch) {
                            //launched from initial walkthrough
                            Intent intent = new Intent(activity, RegisterActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(RegisterActivity.REGISTER_AFTER_WALKTHROUGH_EXTRA, true);
                            startActivity(intent);
                            activity.finish();
                        } else {
                            //launched from settings activity, just finish this one
                            activity.finish();
                        }
                    }
                }
            });

            return v;
        }


    }

}
