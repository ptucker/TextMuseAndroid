package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.broadcastreceivers.AlarmReceivedBroadcastReceiver;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class MainCategoryFragment extends Fragment {

    private static final int RANDOM_NOTES_PER_CATEGORY = 3;

    private TextMuseData mData;
    private ViewPager mMainViewPager;
    private HeaderViewPagerAdapter mMainPagerAdapter;
    private ListView mListView;
    private MainCategoryListArrayAdapter mCategoryListAdapter;
    private ArrayList<Note> mRandomNotes;

    //private List<ViewPager> mCategoryViewPagers;

    public static MainCategoryFragment newInstance() {
        MainCategoryFragment fragment = new MainCategoryFragment();
        return fragment;
    }

    public MainCategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData instance = GlobalData.getInstance();
        instance.loadData(getActivity());
        mData = instance.getData();

        generateRandomNotes();

        setLastNotified();
        setNotificationAlarm();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_category, container, false);

        mMainViewPager = (ViewPager) view.findViewById(R.id.mainFragmentViewPagerTop);

        mMainPagerAdapter = new HeaderViewPagerAdapter(getActivity(), mRandomNotes);
        mMainViewPager.setAdapter(mMainPagerAdapter);

//        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if (position == mMainPagerAdapter.getCount() - 1) {
//                    Log.d(Constants.TAG, "At end of view pager, moving to first element");
//                    mMainViewPager.setCurrentItem(0, false);
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
////                if (state == ViewPager.SCROLL_STATE_IDLE) {
////                    int index = mMainViewPager.getCurrentItem();
////                    if (index == )
////                }
//            }
//        });

        mListView = (ListView) view.findViewById(R.id.mainFragmentListView);
        mListView.setVisibility(View.GONE);

        generateViewsFromData();

        loadDataFromInternet();

        return view;
    }

    private void generateRandomNotes() {

        if (mData == null || mData.categories == null) {
            mRandomNotes = null;
            return;
        }

        //if we have data, then get some of it
        mRandomNotes = new ArrayList<Note> ();

        for (Category category : mData.categories) {
            if (category.notes != null && category.notes.size() > 0) {
                if (category.notes.size() <= RANDOM_NOTES_PER_CATEGORY) {
                    mRandomNotes.addAll(category.notes);
                } else {
                    int indexGap = category.notes.size() / RANDOM_NOTES_PER_CATEGORY;
                    for (int i = 0; i < RANDOM_NOTES_PER_CATEGORY; i++) {
                        mRandomNotes.add(category.notes.get(i * indexGap));
                    }
                }
            }
        }

        //De-dupe the notes since sometimes there are duplicates in different categories
        HashSet<Integer> noteIds = new HashSet<Integer> ();
        Iterator iterator = mRandomNotes.iterator();
        while (iterator.hasNext()) {
            Note note = (Note) iterator.next();

            if (!noteIds.contains(note.noteId)) {
                noteIds.add(note.noteId);
            } else {
                iterator.remove();
            }
        }

        //Randomize the notes
        Random rand = new Random();
        for (int i = 0; i < mRandomNotes.size() - 1; i++) {
            int index = rand.nextInt(mRandomNotes.size() - i);
            if (index == 0) {
                continue;
            }

            Note swapNote = mRandomNotes.get(i + index);
            mRandomNotes.set(i + index, mRandomNotes.get(i));
            mRandomNotes.set(i, swapNote);
        }
    }

    private void generateViewsFromData() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            if (mCategoryListAdapter != null) {
                mCategoryListAdapter.updateCategories(mData.categories);
            } else {
                mCategoryListAdapter = new MainCategoryListArrayAdapter(getActivity(), mData.categories);
                mListView.setAdapter(mCategoryListAdapter);
            }
            mListView.setVisibility(View.VISIBLE);
        }

    }

    private void loadDataFromInternet() {
        View view = getView();
        if (view != null) {
            TextView textView = (TextView) view.findViewById(R.id.mainFragmentTextViewNoItems);
            textView.setText("Loading...");
        }

        FetchNotesAsyncTask.FetchNotesAsyncTaskHandler handler = new FetchNotesAsyncTask.FetchNotesAsyncTaskHandler() {
            @Override
            public void handleFetchResult(String s) {
                handleNewData(s);
            }
        };

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(handler);
        task.execute();
    }

    private void showFailureMessage() {
        //Don't do anything when we already have data... just keep showing the old data
        if (mData != null) {
            Log.d(Constants.TAG, "Could not get new data, keeping old data in views");
            return;
        }

        View view = getView();
        if (view != null) {
            TextView textView = (TextView) view.findViewById(R.id.mainFragmentTextViewNoItems);

            textView.setText("Could not load data from the internet. Please check your connection and try again.");
        }
    }

    private void setLastNotified() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
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
        AlarmReceivedBroadcastReceiver.setAlarm(getActivity());
    }

    private void handleNewData(String s) {
        Log.d(Constants.TAG, "Fetch data complete, parsing data");
        TextMuseData data = parseData(s);
        Log.d(Constants.TAG, "Parsing data complete");

        if (data == null || data.categories == null || data.categories.size() <= 0) {
            showFailureMessage();
            return;
        }

        boolean differentData = true;
        if (mData != null) {
            if (mData.isDataSimilar(data)) {
                differentData = false;
                Log.d(Constants.TAG, "Download of new data did not show any differences. Keeping old data");
            }
        }

        if (differentData) {
            data.save(getActivity());
            mData = data;
            GlobalData.getInstance().updateData(data);
            generateViewsFromData();
            generateRandomNotes();
            mMainPagerAdapter.updateNotes(mRandomNotes);
        }

    }

    private TextMuseData parseData(String s) {

        if (s == null || s.length() <= 0) {
            return null;
        }

        WebDataParser parser = new WebDataParser();
        return parser.parse(s);
    }


    public static class HeaderViewPagerAdapter extends PagerAdapter {

        private ArrayList<Note> mNotes;
        private LayoutInflater mLayoutInflater;
        private Activity mContext;

        //Needed to download images.  Should be pulled out into a base class
        private HashMap<Integer, ImageDownloadTarget> mDownloadTargets;

        public HeaderViewPagerAdapter(Activity activity, ArrayList<Note> notes) {
            mNotes = notes;
            mLayoutInflater = activity.getLayoutInflater();
            mContext = activity;
            mDownloadTargets = new HashMap<Integer, ImageDownloadTarget>();
        }

        public void updateNotes(ArrayList<Note> notes) {
            mNotes = notes;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            //If we don't have any notes, then just show the textmuse welcome banner. If we do, then
            //the first and last elements need to be the welcome banner for wraparound behavior to work
            if (mNotes == null) {
                return 1;
            } else {
                return mNotes.size() + 2;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = null;

            if (mNotes == null || mNotes.size() <= 0) {
                view = mLayoutInflater.inflate(R.layout.item_category_header, container, false);
            } else {
                if (position == 0 || position == getCount() - 1) {
                    view = mLayoutInflater.inflate(R.layout.item_category_header, container, false);
                } else {
                    final int indexPosition = position - 1;
                    final Note note = mNotes.get(indexPosition);

                    if (note.hasDisplayableMedia()) {
                        view = mLayoutInflater.inflate(R.layout.item_category_main_textimage, container, false);

                        ImageView imageView = (ImageView) view.findViewById(R.id.mainViewImageViewItemBackground);

                        Picasso.with(mContext)
                                .load(note.mediaUrl)
                                .fit()
                                .centerCrop()
                                .into(imageView);

                        if (!mDownloadTargets.containsKey(note.noteId)) {

                            //Do another picasso task to write the image file to external storage.  This will
                            //reuse the same image in the cache so it won't go to network again
                            ImageDownloadTarget downloadTarget = new ImageDownloadTarget(mContext, note);

                            //Use a hashmap to keep track of these for 2 reasons--to prevent them from getting
                            //garbage collected, and also so that we don't download twice
                            mDownloadTargets.put(note.noteId, downloadTarget);
                            Picasso.with(mContext)
                                    .load(note.mediaUrl)
                                    .into(downloadTarget);
                        }

                    } else {
                        view = mLayoutInflater.inflate(R.layout.item_category_main_textonly, container, false);

                        View background = view.findViewById(R.id.mainViewBackgroundView);
                        int color = Constants.COLOR_LIST[position % Constants.COLOR_LIST.length];
                        background.setBackgroundColor(color);

                    }

                    TextView textView = (TextView) view.findViewById(R.id.mainViewTextViewText);
                    if (note.hasDisplayableText()) {
                        textView.setText(note.text);
                    } else {
                        textView.setVisibility(View.GONE);
                    }

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ContactsPickerActivity.class);
                            intent.putExtra(ContactsPickerActivity.NOTE_OBJECT_EXTRA, note);
                            mContext.startActivity(intent);
                        }
                    });
                }

            }

            if (view != null) {
                container.addView(view);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public static class MainCategoryListArrayAdapter extends ArrayAdapter<Category> {

        private Activity mContext;
        private List<Category> mCategories;

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

        public MainCategoryListArrayAdapter(Activity context, List<Category> categories) {
            super(context, R.layout.list_ele_category, categories);
            this.mContext = context;
            this.mCategories = categories;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            Category category = mCategories.get(position);
            if (category.notes == null || category.notes.size() <= 0) {
                return null;
            }

            Note firstNote = category.notes.get(0);

            if (rowView == null) {
                LayoutInflater inflater = mContext.getLayoutInflater();
                ViewHolder viewHolder = new ViewHolder();

                if (firstNote.mediaUrl != null && firstNote.mediaUrl.length() > 0) {
                    //TODO: check if youtube...

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

            int color = Constants.COLOR_LIST[position % Constants.COLOR_LIST.length];

            holder.mCategoryTitle.setText(category.name);
            holder.mCategoryTitle.setTextColor(color);

            int newCount = 0;
            for (Note note : category.notes) {
                if (note.newFlag) {
                    newCount++;
                }
            }

            holder.mNewBadgeText.setText(Integer.toString(newCount) + " NEW");
            holder.mNewBadgeBackground.setColorFilter(color);

            holder.mArrow.setColorFilter(color);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SelectMessageActivity.class);
                    intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, position);
                    intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
                    mContext.startActivity(intent);
                }
            };

            holder.mArrow.setOnClickListener(onClickListener);
            holder.mNewBadge.setOnClickListener(onClickListener);
            holder.mTextLayout.setOnClickListener(onClickListener);

            if (holder.mTextOnly) {
                holder.mBackgroundViewTextOnly.setBackgroundColor(color);
            } else {
                Picasso.with(mContext)
                        .load(firstNote.mediaUrl)
                        .fit()
                        .centerCrop()
                        .into(holder.mBackgroundImageView);
            }

            if (firstNote.text == null || firstNote.text.length() <= 0) {
                holder.mTextView.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG, "Category: " + category.name + " had first note with id: " + firstNote.noteId + " with empty text");
            } else {
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
            Category category = mCategories.get(position);
            Note firstNote = category.notes.get(0);
            if (firstNote.mediaUrl != null && firstNote.mediaUrl.length() > 0) {
                return 0;
            } else {
                return 1;
            }
        }

        public void updateCategories(List<Category> categories) {
            mCategories = categories;
            this.notifyDataSetChanged();
        }
    }


}
