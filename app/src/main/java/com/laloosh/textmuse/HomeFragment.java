package com.laloosh.textmuse;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.NoteExtended;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.events.ShowCategoriesChangedEvent;
import com.laloosh.textmuse.datamodel.events.TabDeselectedEvent;
import com.laloosh.textmuse.datamodel.events.TabSelectedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class HomeFragment extends Fragment implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler {
    public static final int TAB_NUMBER = 0;

    private static final String ARG_ALREADY_LOADED_DATA = "arg.alreadyloadeddata";

    private boolean mAlreadyLoaded;


    private TextMuseData mData;
    private TextMuseSettings mSettings;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private MainNotesAdapter mAdapter;
    private DrawerListArrayAdapter mDrawerListAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ImageView mToolbarImage;
    private TextView mTextView;
    private boolean mShowPhotos;

    private View.OnClickListener mDrawerListener;

    private Boolean mDrawerOpen;

    private ArrayList<NoteExtended> mSortedNotes;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(boolean alreadyLoaded) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ALREADY_LOADED_DATA, alreadyLoaded);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlreadyLoaded = getArguments().getBoolean(ARG_ALREADY_LOADED_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAdapter = null;
        mDrawerListAdapter = null;

        mToolbarImage = (ImageView) getActivity().findViewById(R.id.mainToolbarButton);

        GlobalData instance = GlobalData.getInstance();
        if (!instance.hasLoadedData()) {
            instance.loadData(this.getContext());
        }
        mData = instance.getData();
        mSettings = instance.getSettings();

        setShowPhotos();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.mainFragmentSwipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataFromInternet();
            }
        });

        mTextView = (TextView) v.findViewById(R.id.mainFragmentTextViewNoItems);

        mListView = (ListView) v.findViewById(R.id.mainFragmentListView);
        mListView.setVisibility(View.GONE);

        mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) v.findViewById(R.id.mainFragmentListViewCategories);

        mDrawerOpen = false;

        mDrawerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerOpen) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    mDrawerOpen = true;
                }
            }
        };
        mToolbarImage.setOnClickListener(mDrawerListener);

        generateViewsFromData();

        if (!mAlreadyLoaded) {
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

        return v;
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    //This is used by eventbus once we get the event
    public void onEvent(ShowCategoriesChangedEvent event) {

        mData = GlobalData.getInstance().getData();

        generateNoteList();
        mAdapter.updateNotes(mSortedNotes);

        mDrawerListAdapter.updateSettings(mSettings);
        mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
    }

    public void onEvent(TabSelectedEvent event) {
        if (event.tabNumber == TAB_NUMBER) {
            mToolbarImage.setOnClickListener(mDrawerListener);
            mDrawerLayout.closeDrawers();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerOpen = false;
        }
    }

    public void onEvent(TabDeselectedEvent event) {
        if (event.tabNumber == TAB_NUMBER) {
            mToolbarImage.setOnClickListener(null);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerOpen = false;
        }
    }

    private void generateViewsFromData() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            if (mAdapter != null) {
                generateNoteList();
                mAdapter.updateNotes(mSortedNotes);
                mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
            } else {

                generateNoteList();
                mAdapter = new MainNotesAdapter(this.getContext(), mSortedNotes, mData);
                mListView.setAdapter(mAdapter);

                mDrawerListAdapter = new DrawerListArrayAdapter(this.getContext(), mData.categories, mData.localTexts, mData.localPhotos, mSettings, mShowPhotos);
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
        mTextView.setText("Loading...");

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(this, this.getContext(), mData == null ? -1 : mData.appId);
        task.execute();
    }

    private void showFailureMessage() {
        //Don't do anything when we already have data... just keep showing the old data
        if (mData != null) {
            Log.d(Constants.TAG, "Could not get new data, keeping old data in views");
            return;
        }

        mTextView.setText("Could not load data from the internet. Please check your connection and try again.");
    }

    private void setShowPhotos() {
        //Only show the My Photos category if this phone supports it and if we have some photos to show
        if (SmsUtils.testIfPhotosSupported(this.getContext()) &&
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

    public static class DrawerListArrayAdapter extends ArrayAdapter<Category> {

        private Context mContext;
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

        public DrawerListArrayAdapter(Context context, List<Category> categories, Category localTexts, Category localPhotos, TextMuseSettings settings, boolean showPhotos) {
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
                LayoutInflater inflater = LayoutInflater.from(mContext);
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

        private Context mContext;
        private ArrayList<NoteExtended> mNotes;
        private TextMuseData mData;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;
            public ImageView mArrow;

            public View mBackgroundViewTextOnly;
            public TextView mTextView;
            public ImageView mBackgroundImageView;

            public TextView mTextViewLikeCount;

            public ImageView mLikeImageView;
            public ImageView mPinImageView;
            public ImageView mSendImageView;

            public ViewGroup mLayoutLike;
            public ViewGroup mLayoutPin;
            public ViewGroup mLayoutSend;

            public ViewGroup mTextLayout;

            public boolean mTextOnly;
        }

        public MainNotesAdapter(Context context, ArrayList<NoteExtended> notes, TextMuseData data) {
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

                LayoutInflater inflater = LayoutInflater.from(mContext);
                ViewHolder viewHolder = new ViewHolder();

                if (note.hasDisplayableMedia()) {
                    rowView = inflater.inflate(R.layout.list_ele_category_textimage2, parent, false);
                    viewHolder.mBackgroundImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewItemBackground);
                    viewHolder.mTextOnly = false;
                } else {
                    rowView = inflater.inflate(R.layout.list_ele_category_textonly2, parent, false);
                    viewHolder.mBackgroundViewTextOnly = rowView.findViewById(R.id.mainViewBackgroundView);
                    viewHolder.mTextOnly = true;
                }

                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.mainViewTextViewText);
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewTitle);
                viewHolder.mArrow = (ImageView) rowView.findViewById(R.id.mainFragmentListItemImageArrow);
                viewHolder.mTextLayout = (ViewGroup) rowView.findViewById(R.id.mainViewRelativeLayoutTextItem);
                viewHolder.mLikeImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewHeart);
                viewHolder.mPinImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewPin);
                viewHolder.mSendImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewSend);
                viewHolder.mTextViewLikeCount = (TextView) rowView.findViewById(R.id.mainViewTextViewHeartCount);

                viewHolder.mLayoutLike = (ViewGroup) rowView.findViewById(R.id.mainViewLayoutHeart);
                viewHolder.mLayoutPin = (ViewGroup) rowView.findViewById(R.id.mainViewLayoutPin);
                viewHolder.mLayoutSend = (ViewGroup) rowView.findViewById(R.id.mainViewLayoutSend);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            TextMuseData data = GlobalData.getInstance().getData();
            int[] colorList = data.getColorList();
            int color = colorList[position % colorList.length];

            holder.mCategoryTitle.setText(noteExtended.categoryName);
            holder.mCategoryTitle.setTextColor(ColorHelpers.getTextColorForWhiteBackground(color));

            holder.mArrow.setColorFilter(color);

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

            holder.mArrow.setOnClickListener(onCategoryClickListener);
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
                        Glide.with(mContext)
                            .load(note.getDisplayMediaUrl(mContext))
                            .error(R.drawable.placeholder_image)
                            .fitCenter()
                            .override(holder.mBackgroundImageView.getWidth(), Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.mBackgroundImageView);

//                        Picasso.with(mContext)
//                                .load(note.getDisplayMediaUrl(mContext))
//                                .error(R.drawable.placeholder_image)
//                                .resize(holder.mBackgroundImageView.getWidth(), 0)
//                                .into(holder.mBackgroundImageView);
                    }
                });
            }

            if (note.liked) {
                holder.mLikeImageView.setColorFilter(0xffef1111);
            } else {
                holder.mLikeImageView.setColorFilter(0xffdedede);
            }

            holder.mTextViewLikeCount.setText(Integer.toString(note.likeCount));

            if (mData.hasPinnedNote(note.noteId)) {
                holder.mPinImageView.setColorFilter(0xffef1111);
            } else {
                holder.mPinImageView.setColorFilter(0xffdedede);
            }

            holder.mLayoutLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    note.liked = !note.liked;
                    if (note.liked) {
                        holder.mLikeImageView.setColorFilter(0xffef1111);
                        note.likeCount++;
                    } else {
                        holder.mLikeImageView.setColorFilter(0xffdedede);
                        note.likeCount--;
                    }
                    holder.mTextViewLikeCount.setText(Integer.toString(note.likeCount));

                    SetHighlightAsyncTask.SetHighlightAsyncTaskHandler handler = new SetHighlightAsyncTask.SetHighlightAsyncTaskHandler() {
                        @Override
                        public void handlePostResult(String s, Note note, boolean liked, View view) {

                            if (s == null) {
                                //In the case of failure, let's reverse what we did
                                note.liked = !liked;

                                if (note.liked) {
                                    holder.mLikeImageView.setColorFilter(0xffef1111);
                                    note.likeCount++;
                                } else {
                                    holder.mLikeImageView.setColorFilter(0xffdedede);
                                    note.likeCount--;
                                }
                                holder.mTextViewLikeCount.setText(Integer.toString(note.likeCount));
                            } else {
                                note.liked = liked;
                            }

                            mData.save(mContext);
                        }
                    };

                    SetHighlightAsyncTask task = new SetHighlightAsyncTask(handler, mData.appId, note.liked, note, holder.mLayoutLike);
                    task.execute();
                }
            });

            holder.mLayoutPin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.hasPinnedNote(note.noteId)) {
                        mData.unPinNote(note);
                        holder.mPinImageView.setColorFilter(0xffdedede);
                        mData.save(mContext);
                    } else {
                        mData.pinNote(note);
                        holder.mPinImageView.setColorFilter(0xffef1111);
                        mData.save(mContext);
                    }
                }
            });

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
