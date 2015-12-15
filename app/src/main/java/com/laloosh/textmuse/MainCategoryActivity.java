package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.broadcastreceivers.AlarmReceivedBroadcastReceiver;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.dialogs.LaunchMessageDialogFragment;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;


public class MainCategoryActivity extends ActionBarActivity implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler{

    public static final String ALREADY_LOADED_DATA_EXTRA = "com.laloosh.textmuse.alreadyloadeddata";

    private static final int REQUEST_CODE_SETTINGS = 2333;

    private TextMuseData mData;
    private TextMuseSettings mSettings;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private MainCategoryListArrayAdapter mCategoryListAdapter;
    private boolean mShowPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_category);

        GlobalData instance = GlobalData.getInstance();
        if (!instance.hasLoadedData()) {
            instance.loadData(this);
        }
        mData = instance.getData();
        mSettings = instance.getSettings();

        setSkinTitle();

        setLastNotified();
        setNotificationAlarm();

        setShowPhotos();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainFragmentSwipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromInternet();
            }
        });

        mListView = (ListView) findViewById(R.id.mainFragmentListView);
        mListView.setVisibility(View.GONE);

        generateViewsFromData();

        Intent intent = getIntent();
        boolean alreadyLoadedData = intent.getBooleanExtra(ALREADY_LOADED_DATA_EXTRA, false);
        if (!alreadyLoadedData) {
            Log.d(Constants.TAG, "Splash screen load data did not succeed. Retrying");
            loadDataFromInternet();
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        } else {
            Log.d(Constants.TAG, "Already successfully loaded data from internet via splash screen. Skipping reload");
        }

        final String startMessage = intent.getStringExtra(Constants.LAUNCH_MESSAGE_EXTRA);
        if (startMessage != null) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    LaunchMessageDialogFragment fragment = LaunchMessageDialogFragment.newInstance(startMessage);
                    fragment.show(getSupportFragmentManager(), "launchmessagefragment");
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setSkinTitle() {
        if (mData != null && mData.skinData != null) {
            setTitle(mData.skinData.name + " TextMuse");

        } else {
            setTitle("TextMuse");
        }
    }


    private void generateViewsFromData() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            if (mCategoryListAdapter != null) {
                mCategoryListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
            } else {
                mCategoryListAdapter = new MainCategoryListArrayAdapter(this, mData.categories, mData.localTexts, mData.localPhotos, mSettings, mShowPhotos);
                mListView.setAdapter(mCategoryListAdapter);
            }
            mListView.setVisibility(View.VISIBLE);
        }

    }

    private void loadDataFromInternet() {
        TextView textView = (TextView) findViewById(R.id.mainFragmentTextViewNoItems);
        textView.setText("Loading...");

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(this, this, mData == null ? -1 : mData.appId);
        task.execute();
    }

    private void showFailureMessage() {
        //Don't do anything when we already have data... just keep showing the old data
        if (mData != null) {
            Log.d(Constants.TAG, "Could not get new data, keeping old data in views");
            return;
        }

        TextView textView = (TextView) findViewById(R.id.mainFragmentTextViewNoItems);
        textView.setText("Could not load data from the internet. Please check your connection and try again.");
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

    private void setShowPhotos() {
        //Only show the My Photos category if this phone supports it and if we have some photos to show
        if (SmsUtils.testIfPhotosSupported(this) &&
                mData != null &&
                mData.localPhotos != null &&
                mData.localPhotos.notes != null &&
                mData.localPhotos.notes.size() > 0) {
            mShowPhotos = true;
        } else {
            mShowPhotos = false;
        }
    }

    @Override
    public void handleFetchResult(FetchNotesAsyncTask.FetchNotesResult result) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if (result == FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED) {
            showFailureMessage();
            return;
        } else if (result == FetchNotesAsyncTask.FetchNotesResult.FETCH_SUCCEEDED_DIFFERENT_DATA) {

            TextMuseData data = GlobalData.getInstance().getData();
            if (data != null) {
                mData = data;

                generateViewsFromData();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                mSettings = GlobalData.getInstance().getSettings();

                if (data != null && data.getBooleanExtra(SettingsActivity.SHOWN_CATEGORIES_CHANGED_EXTRA, false)) {
                    //Refresh our data if the shown categories changed

                    mData = GlobalData.getInstance().getData();
                    setSkinTitle();

                    mCategoryListAdapter.updateSettings(mSettings);

                    mCategoryListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
                }
            }
        }
    }


    public static class MainCategoryListArrayAdapter extends ArrayAdapter<Category> {

        private Activity mContext;
        private List<Category> mOriginalCategories;
        private List<Category> mShownCategories;
        private Category mLocalTexts;
        private Category mLocalPhotos;
        private TextMuseSettings mSettings;
        private boolean mShowPhotos;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;
            public ViewGroup mNewBadge;
            public TextView mNewBadgeText;
            public ImageView mNewBadgeBackground;
            public ImageView mArrow;

            public View mBackgroundViewTextOnly;
            public TextView mLatest;
            public TextView mTextView;
            public ImageView mBackgroundImageView;

            public ViewGroup mTextLayout;

            public boolean mTextOnly;
        }

        public MainCategoryListArrayAdapter(Activity context, List<Category> categories, Category localTexts, Category localPhotos, TextMuseSettings settings, boolean showPhotos) {
            super(context, R.layout.list_ele_category, categories);
            this.mContext = context;
            this.mOriginalCategories = categories;
            this.mSettings = settings;
            this.mShownCategories = new ArrayList<Category>();
            this.mLocalTexts = localTexts;
            this.mLocalPhotos = localPhotos;
            this.mShowPhotos = showPhotos;
            generateShownCategories();
        }

        @Override
        public int getCount() {
            if (mShowPhotos) {
                //+2 for the local texts, photos category
                return mShownCategories.size() + 2;
            } else {
                //+1 for the local texts
                return mShownCategories.size() + 1;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            final Category category;
            if (position == mShownCategories.size()) {
                category = mLocalTexts;
            } else if (position > mShownCategories.size()) {
                category = mLocalPhotos;
            } else {
                category = mShownCategories.get(position);
            }

            if (category.notes == null || category.notes.size() <= 0) {
                return null;
            }

            Note firstNote = category.notes.get(0);

            if (rowView == null) {
                LayoutInflater inflater = mContext.getLayoutInflater();
                ViewHolder viewHolder = new ViewHolder();

                if (firstNote.hasDisplayableMedia()) {
                    rowView = inflater.inflate(R.layout.list_ele_category_textimage, parent, false);
                    viewHolder.mBackgroundImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewItemBackground);
                    viewHolder.mTextOnly = false;
                } else {
                    rowView = inflater.inflate(R.layout.list_ele_category_textonly, parent, false);
                    viewHolder.mBackgroundViewTextOnly = rowView.findViewById(R.id.mainViewBackgroundView);
                    viewHolder.mTextOnly = true;
                }

                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.mainViewTextViewText);
                viewHolder.mLatest = (TextView) rowView.findViewById(R.id.mainViewTextViewLatest);
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewTitle);
                viewHolder.mArrow = (ImageView) rowView.findViewById(R.id.mainFragmentListItemImageArrow);
                viewHolder.mNewBadgeText = (TextView) rowView.findViewById(R.id.mainViewFragmentListItemTextViewNewBadge);
                viewHolder.mNewBadgeBackground = (ImageView) rowView.findViewById(R.id.mainViewFragmentImageViewNewBadge);
                viewHolder.mNewBadge = (ViewGroup) rowView.findViewById(R.id.mainViewFragmentListItemLayoutNewBadge);
                viewHolder.mTextLayout = (ViewGroup) rowView.findViewById(R.id.mainViewRelativeLayoutTextItem);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();

            TextMuseData data = GlobalData.getInstance().getData();
            int[] colorList = data.getColorList();
            int color = colorList[position % colorList.length];

            holder.mCategoryTitle.setText(category.name);
            holder.mCategoryTitle.setTextColor(ColorHelpers.getTextColorForWhiteBackground(color));

            int newCount = 0;
            for (Note note : category.notes) {
                if (note.newFlag) {
                    newCount++;
                }
            }

            if (newCount > 0) {
                holder.mNewBadgeText.setText(Integer.toString(newCount) + " NEW");
            } else {
                holder.mNewBadgeText.setText("NEW");
            }

            holder.mNewBadgeText.setTextColor(ColorHelpers.getTextColorForBackground(color));

            holder.mNewBadgeBackground.setColorFilter(color);

            if (category == mLocalPhotos || category == mLocalTexts) {
                holder.mNewBadge.setVisibility(View.GONE);
                holder.mNewBadgeBackground.setVisibility(View.GONE);
            } else {
                holder.mNewBadge.setVisibility(View.VISIBLE);
                holder.mNewBadgeBackground.setVisibility(View.VISIBLE);
            }

            holder.mArrow.setColorFilter(color);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int originalIndex = getOriginalIndex(category);
                    if (originalIndex >= 0) {
                        Intent intent = new Intent(mContext, SelectMessageActivity.class);
                        intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, originalIndex);
                        intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
                        mContext.startActivity(intent);
                    }
                }
            };

            holder.mArrow.setOnClickListener(onClickListener);
            holder.mNewBadge.setOnClickListener(onClickListener);
            holder.mTextLayout.setOnClickListener(onClickListener);

            //Default the text color to white unless we change it
            holder.mTextView.setTextColor(0xFFFFFFFF);

            if (!firstNote.hasDisplayableMedia()) {
                holder.mBackgroundViewTextOnly.setBackgroundColor(color);
                holder.mTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));
            } else {
                if (firstNote.shouldCenterInside()) {
                    Picasso.with(mContext)
                            .load(firstNote.getDisplayMediaUrl(mContext))
                            .error(R.drawable.placeholder_image)
                            .fit()
                            .centerInside()
                            .into(holder.mBackgroundImageView);
                    holder.mBackgroundImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } else {
                    Picasso.with(mContext)
                            .load(firstNote.getDisplayMediaUrl(mContext))
                            .error(R.drawable.placeholder_image)
                            .fit()
                            .centerCrop()
                            .into(holder.mBackgroundImageView);
                    holder.mBackgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }

            if (firstNote.text == null || firstNote.text.length() <= 0) {
                if (category == mLocalTexts) {
                    //The local texts not having any text means that the user has yet to add any
                    holder.mTextView.setVisibility(View.VISIBLE);
                    holder.mTextView.setText("Add your own texts here!");
                    Log.d(Constants.TAG, "Category: " + category.name + " had empty first note, so putting in filler text");
                } else {
                    holder.mTextView.setVisibility(View.INVISIBLE);
                    Log.d(Constants.TAG, "Category: " + category.name + " had first note with id: " + firstNote.noteId + " with empty text");
                }
            } else {
                holder.mTextView.setVisibility(View.VISIBLE);
                holder.mTextView.setText(firstNote.text);
                Log.d(Constants.TAG, "Category: " + category.name + " had first note with id: " + firstNote.noteId + " with text: " + firstNote.text);
            }

            return rowView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            //Type 0 is an image one, type 1 is the text one
            if (position == mShownCategories.size()) {
                return 1;    //My texts
            } else if (position > mShownCategories.size()) {
                return 0;    //My photos
            } else {
                Category category = mShownCategories.get(position);
                Note firstNote = category.notes.get(0);
                if (firstNote.hasDisplayableMedia()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }

        public void updateSettings(TextMuseSettings settings) {
            mSettings = settings;
        }

        public void updateCategories(List<Category> categories, Category localNotes, Category localPhotos) {
            mOriginalCategories = categories;
            mLocalTexts = localNotes;
            mLocalPhotos = localPhotos;
            generateShownCategories();
            this.notifyDataSetChanged();
        }

        private void generateShownCategories() {
            mShownCategories.clear();
            for (Category c : mOriginalCategories) {
                if (mSettings.shouldShowCategory(c.name)) {
                    mShownCategories.add(c);
                }
            }
        }

        private int getOriginalIndex(Category category) {
            if (category == mLocalTexts) {
                return mOriginalCategories.size();
            } else if (category == mLocalPhotos) {
                return mOriginalCategories.size() + 1;
            } else {
                for (int i = 0; i < mOriginalCategories.size(); i++) {
                    Category c = mOriginalCategories.get(i);
                    if (c.name.equals(category.name)) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }

    private class CategoryAndNoteIndex {
        public int mCategoryIndex;
        public int mNoteIndex;
    }

}
