package com.laloosh.textmuse.ui;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.broadcastreceivers.AlarmReceivedBroadcastReceiver;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.NoteExtended;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.events.ShowCategoriesChangedEvent;
import com.laloosh.textmuse.dialogs.LaunchMessageDialogFragment;
import com.laloosh.textmuse.tasks.FetchNotesAsyncTask;
import com.laloosh.textmuse.utils.ColorHelpers;
import com.laloosh.textmuse.utils.SmsUtils;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;


public class MainCategoryActivity extends AppCompatActivity implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler {

    public static final String ALREADY_LOADED_DATA_EXTRA = "com.laloosh.textmuse.alreadyloadeddata";

    private static final int REQUEST_CODE_SETTINGS = 2333;

    private TextMuseData mData;
    private TextMuseSettings mSettings;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private MainNotesAdapter mAdapter;
    private DrawerListArrayAdapter mDrawerListAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private boolean mShowPhotos;

    private ImageView mToolbarImage;
    private Boolean mDrawerOpen;

    private ArrayList<NoteExtended> mSortedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitleTextColor(0xffffffff);
        mToolbarImage = (ImageView) findViewById(R.id.mainToolbarButton);

        setSupportActionBar(toolbar);

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.mainFragmentListViewCategories);

        mDrawerOpen = false;

        mToolbarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerOpen) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

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
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    //This is used by eventbus once we get the event
    public void onEvent(ShowCategoriesChangedEvent event) {
        generateNoteList();
        mAdapter.updateNotes(mSortedNotes);
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
        } else if (id == android.R.id.home) {
            Log.d(Constants.TAG, "on options item selected -- home pushed!");
        } else if (id == R.id.home) {
            Log.d(Constants.TAG, "on options item selected2 -- home pushed!");

        }


        return super.onOptionsItemSelected(item);
    }

    private void setSkinTitle() {
        setTitle("");
        TextView textView = (TextView) findViewById(R.id.mainToolbarTitle);
        mToolbarImage = (ImageView) findViewById(R.id.mainToolbarButton);

        textView.setText("TextMuse");
        mToolbarImage.setImageResource(R.drawable.launcher_icon);

    }


    private void generateViewsFromData() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            if (mAdapter != null) {
                generateNoteList();
                mAdapter.updateNotes(mSortedNotes);
                mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
            } else {

                generateNoteList();
                mAdapter = new MainNotesAdapter(this, mSortedNotes, mData);
                mListView.setAdapter(mAdapter);

                mDrawerListAdapter = new DrawerListArrayAdapter(this, mData.categories, mData.localTexts, mData.localPhotos, mSettings, mShowPhotos);
                mDrawerList.setAdapter(mDrawerListAdapter);
            }

            mListView.setVisibility(View.VISIBLE);
        }

    }

    //Generates the pinned hash set and the sorted notes array.  These depend on your settings
    private void generateNoteList() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            mSortedNotes = new ArrayList<NoteExtended>();
            ArrayList<NoteExtended> noImagesNotes = new ArrayList<>();
            ArrayList<NoteExtended> imageNotes = new ArrayList<>();
            Random rnd = new Random();

            int categoryPos = -1;
            for (Category category : mData.categories) {
                categoryPos++;

                if (!mSettings.shouldShowCategory(category.name)) {
                    continue;
                }

                int inCategoryPos = 0;
                for (Note note : category.notes) {
                    int score = rnd.nextInt(3);
                    score += note.newFlag ? 4 : 0;
                    score += note.liked ? 1 : 0;
                    score += mData.hasPinnedNote(note.noteId) ? 1 : 0;
                    score += inCategoryPos / 3;

                    if (note.hasDisplayableMedia()) {
                        imageNotes.add(new NoteExtended(note, category, categoryPos, inCategoryPos, score));
                    } else {
                        noImagesNotes.add(new NoteExtended(note, category, categoryPos, inCategoryPos, score));
                    }

                    inCategoryPos++;
                }

            }

            int i = 0;
            for (NoteExtended note : imageNotes) {
                if (i < 3) {
                    note.score = -1;
                }

                mSortedNotes.add(note);
                i++;
            }

            for (NoteExtended note : noImagesNotes) {
                mSortedNotes.add(note);
            }

            Collections.sort(mSortedNotes);
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

                    generateNoteList();
                    mAdapter.updateNotes(mSortedNotes);

                    mDrawerListAdapter.updateSettings(mSettings);
                    mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
                }
            }
        }
    }

    public static class DrawerListArrayAdapter extends ArrayAdapter<Category> {

        private Activity mContext;
        private List<Category> mOriginalCategories;
        private Category mLocalTexts;
        private Category mLocalPhotos;
        private TextMuseSettings mSettings;
        private boolean mShowPhotos;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;
            public CheckBox mCheckbox;
        }

        public DrawerListArrayAdapter(Activity context, List<Category> categories, Category localTexts, Category localPhotos, TextMuseSettings settings, boolean showPhotos) {
            super(context, R.layout.item_main_category_list, categories);

            this.mContext = context;
            this.mOriginalCategories = categories;
            this.mSettings = settings;
            this.mLocalTexts = localTexts;
            this.mLocalPhotos = localPhotos;
            this.mShowPhotos = showPhotos;
        }


        @Override
        public int getCount() {
            if (mShowPhotos) {
                //+2 for the local texts, photos category
                return mOriginalCategories.size() + 2;
            } else {
                //+1 for the local texts
                return mOriginalCategories.size() + 1;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            final Category category;
            boolean shouldShowCheckbox = true;
            if (position == mOriginalCategories.size()) {
                category = mLocalTexts;
                shouldShowCheckbox = false;
            } else if (position > mOriginalCategories.size()) {
                category = mLocalPhotos;
                shouldShowCheckbox = false;
            } else {
                category = mOriginalCategories.get(position);
            }

            if (category.notes == null || category.notes.size() <= 0) {
                return null;
            }

            if (rowView == null) {
                LayoutInflater inflater = mContext.getLayoutInflater();
                ViewHolder viewHolder = new ViewHolder();

                rowView = inflater.inflate(R.layout.item_main_category_list, parent, false);
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewCategory);
                viewHolder.mCheckbox = (CheckBox) rowView.findViewById(R.id.mainFragmentListItemCheckBox);

                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();

            if (shouldShowCheckbox) {
                holder.mCheckbox.setVisibility(View.VISIBLE);

                if (mSettings.shouldShowCategory(category.name)) {
                    holder.mCheckbox.setChecked(true);
                } else {
                    holder.mCheckbox.setChecked(false);
                }

                holder.mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSettings.setShowCategory(category.name, isChecked);
                        EventBus.getDefault().post(new ShowCategoriesChangedEvent());
                    }
                });

            } else {
                holder.mCheckbox.setVisibility(View.INVISIBLE);
            }

            holder.mCategoryTitle.setText(category.name);

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

            rowView.setOnClickListener(onClickListener);

            return rowView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        public void updateSettings(TextMuseSettings settings) {
            mSettings = settings;
        }

        public void updateCategories(List<Category> categories, Category localNotes, Category localPhotos) {
            mOriginalCategories = categories;
            mLocalTexts = localNotes;
            mLocalPhotos = localPhotos;
            this.notifyDataSetChanged();
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


    public static class MainNotesAdapter extends ArrayAdapter<NoteExtended> {

        private Activity mContext;
        private ArrayList<NoteExtended> mNotes;
        private TextMuseData mData;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;

            public View mBackgroundViewTextOnly;
            public TextView mTextView;
            public ImageView mBackgroundImageView;

            public ImageView mSendImageView;

            public ViewGroup mLayoutSend;

            public ViewGroup mTextLayout;

            public boolean mTextOnly;
        }

        public MainNotesAdapter(Activity context, ArrayList<NoteExtended> notes, TextMuseData data) {
            super(context, R.layout.list_ele_category, notes);

            this.mContext = context;
            this.mData = data;
            this.mNotes = notes;
        }


        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            final NoteExtended noteExtended = mNotes.get(position);
            final Note note = noteExtended.note;

            if (rowView == null) {

                LayoutInflater inflater = mContext.getLayoutInflater();
                ViewHolder viewHolder = new ViewHolder();

                if (note.hasDisplayableMedia()) {
                    rowView = inflater.inflate(R.layout.list_ele_category_textimage3, parent, false);
                    viewHolder.mBackgroundImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewItemBackground);
                    viewHolder.mTextOnly = false;
                } else {
                    rowView = inflater.inflate(R.layout.list_ele_category_textonly3, parent, false);
                    viewHolder.mBackgroundViewTextOnly = rowView.findViewById(R.id.mainViewBackgroundView);
                    viewHolder.mTextOnly = true;
                }

                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.mainViewTextViewText);
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewTitle);
                viewHolder.mTextLayout = (ViewGroup) rowView.findViewById(R.id.mainViewRelativeLayoutTextItem);
                viewHolder.mSendImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewSend);

                viewHolder.mLayoutSend = (ViewGroup) rowView.findViewById(R.id.mainViewLayoutSend);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            TextMuseData data = GlobalData.getInstance().getData();
            int[] colorList = data.getColorList();
            int color = colorList[position % colorList.length];

            holder.mCategoryTitle.setText(noteExtended.categoryName);
            holder.mCategoryTitle.setTextColor(ColorHelpers.getTextColorForWhiteBackground(color));

            View.OnClickListener onCategoryClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SelectMessageActivity.class);
                    intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, noteExtended.categoryIndex);
                    intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
//                    intent.putExtra(SelectMessageActivity.NOTE_INDEX_EXTRA, noteExtended.notePos);
                    mContext.startActivity(intent);
                }
            };

            View.OnClickListener onNoteClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SelectMessageActivity.class);
                    intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, noteExtended.categoryIndex);
                    intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
                    intent.putExtra(SelectMessageActivity.NOTE_INDEX_EXTRA, noteExtended.notePos);
                    mContext.startActivity(intent);
                }
            };

            holder.mCategoryTitle.setOnClickListener(onCategoryClickListener);
            holder.mTextLayout.setOnClickListener(onNoteClickListener);

            //Default the text color to white unless we change it
            holder.mTextView.setTextColor(0xFFFFFFFF);

            if (note.text == null || note.text.length() <= 0) {
                holder.mTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.mTextView.setVisibility(View.VISIBLE);
                holder.mTextView.setText(note.text);
            }

            if (!note.hasDisplayableMedia()) {
                holder.mBackgroundViewTextOnly.setBackgroundColor(color);
                holder.mTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));
            } else {

                holder.mBackgroundImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.get()
                                .load(note.getDisplayMediaUrl(mContext))
                                .error(R.drawable.placeholder_image)
                                .resize(holder.mBackgroundImageView.getWidth(), 0)
                                .into(holder.mBackgroundImageView);
                    }
                });
            }

            holder.mSendImageView.setColorFilter(0xff1a1a1a);

            holder.mLayoutSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ContactsPickerActivity.class);
                    intent.putExtra(ContactsPickerActivity.CATEGORY_POSITION_EXTRA, noteExtended.categoryIndex);
                    intent.putExtra(ContactsPickerActivity.NOTE_POSITION_EXTRA, noteExtended.notePos);
                    intent.putExtra(ContactsPickerActivity.NOTE_ID_EXTRA, note.noteId);
                    mContext.startActivity(intent);
                }
            });

            return rowView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            //Type 0 is an image one, type 1 is the text one
            final NoteExtended note = mNotes.get(position);
            if (note.note.hasDisplayableMedia()) {
                return 0;
            } else {
                return 1;
            }
        }

        public void updateNotes(ArrayList<NoteExtended> notes) {
            mNotes = notes;
            this.notifyDataSetChanged();
        }
    }
}
