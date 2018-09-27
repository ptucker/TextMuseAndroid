package com.laloosh.textmuse.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.DataPersistenceHelper;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.NoteExtended;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.events.ShowCategoriesChangedEvent;
import com.laloosh.textmuse.tasks.DownloadImageAsyncTask;
import com.laloosh.textmuse.tasks.FetchNotesAsyncTask;
import com.laloosh.textmuse.tasks.SetHighlightAsyncTask;
import com.laloosh.textmuse.utils.ColorHelpers;
import com.laloosh.textmuse.utils.GuidedTour;
import com.laloosh.textmuse.utils.OnBackListener;
import com.laloosh.textmuse.utils.SmsUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class HomeFragment extends Fragment
        implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler, TabSelectListener, OnBackListener {
    private static final String ARG_ALREADY_LOADED_DATA = "arg.alreadyloadeddata";
    private static final String ARG_EVENTS_ONLY = "arg.eventsonly";
    private static final int REQUEST_SELECT_MESSAGE = 1005;

    private boolean mAlreadyLoaded;
    private boolean mEventsOnly;
    private boolean mTabSelected;

    private TextMuseData mData;
    private TextMuseSettings mSettings;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private MainNotesAdapter mAdapter;
    private DrawerListArrayAdapter mDrawerListAdapter;
    private DrawerLayout mDrawerLayout;
    private TextView mTextViewDrawerSettings;
    private TextView mTextViewDrawerSaved;
    private ListView mDrawerList;
    private ImageView mToolbarImage;
    //private ImageView mFilterButton;
    private RecyclerView mFilterView;
    private LinearLayoutManager mFilterLayoutManager;
    private RecyclerView.Adapter mFilterAdapter;
    private TextView mTextView;
    private boolean mShowPhotos;
    private View mCategoryFilter;
    private String mFilter = Constants.CATEGORY_FILTER_ALL;
    private String mHighlighted;

    private View.OnClickListener mDrawerListener;

    private Boolean mDrawerOpen;

    private ArrayList<NoteExtended> mSortedNotes;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(boolean alreadyLoaded, boolean eventsOnly, String highlighted) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ALREADY_LOADED_DATA, alreadyLoaded);
        args.putBoolean(ARG_EVENTS_ONLY, eventsOnly);
        fragment.setArguments(args);
        fragment.mHighlighted = highlighted;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlreadyLoaded = getArguments().getBoolean(ARG_ALREADY_LOADED_DATA);
            mEventsOnly = getArguments().getBoolean(ARG_EVENTS_ONLY);
        }

        if (mEventsOnly) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAdapter = null;
        mDrawerListAdapter = null;
        mCategoryFilter = null;

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
        //mFilterButton = (ImageView)v.findViewById(R.id.filterContentIcon);

        mListView = (ListView) v.findViewById(R.id.mainFragmentListView);
        mListView.setVisibility(View.GONE);

        mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) v.findViewById(R.id.mainFragmentListViewCategories);
        mTextViewDrawerSaved = (TextView) v.findViewById(R.id.mainFragmentDrawerSavedTexts);
        mTextViewDrawerSettings = (TextView) v.findViewById(R.id.mainFragmentDrawerSettings);

        mTextViewDrawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerOpen = false;
                mDrawerLayout.closeDrawers();
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                    activity.startActivityForResult(intent, HomeActivity.REQUEST_CODE_SETTINGS);
                }
            }
        });

        mTextViewDrawerSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerOpen = false;
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(v.getContext(), SavedTextsActivity.class);
                startActivity(intent);
            }
        });

        mDrawerOpen = false;
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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

        final ArrayList<Category> categories = new ArrayList<>(mData.categories);
        Category all = new Category();
        all.id = -1; all.name = Constants.CATEGORY_FILTER_ALL;
        categories.add(0, all);
        mFilterView = (RecyclerView) v.findViewById(R.id.mainFragmentFilter);
        mFilterView.setBackgroundColor(Color.BLACK);
        mFilterLayoutManager = new LinearLayoutManager(this.getContext());
        mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterView.setLayoutManager(mFilterLayoutManager);
        mFilterAdapter = new CategoryArrayAdapter(this.getContext(), categories);
        mFilterView.setAdapter(mFilterAdapter);

        if (mHighlighted != null && mHighlighted.length() > 0) {
            int highlighted = Integer.parseInt(mHighlighted);
            Note note = null;
            int iNote=-1, iCategory=-1, color=-1;
            for (int i=0; i<mSortedNotes.size(); i++) {
                if (mSortedNotes.get(i).note.noteId == highlighted) {
                    note = mSortedNotes.get(i).note;
                    iNote = mSortedNotes.get(i).notePos;
                    iCategory = mSortedNotes.get(i).categoryIndex;
                    int[] colors = GlobalData.getInstance().getData().getColorList();
                    color = colors[iCategory % colors.length];
                }
            }
            if (note != null) {
                ViewGroup root = (ViewGroup) v.findViewById(R.id.mainFragmentRoot);
                View detail = MessageDetailFactory.CreateDetailView(root, note, this.getActivity(), color, iCategory, iNote);
                Animation detailSlide = AnimationUtils.loadAnimation(this.getContext(), R.anim.activityfadein);
                root.addView(detail);
                detail.startAnimation(detailSlide);
            }
        }


        if (instance.getSettings().firstLaunch) {
            RelativeLayout parent = (RelativeLayout)v.findViewById(R.id.mainFragmentRoot);
            GlobalData.getInstance().getGuidedTour().addGuidedStepViewForKey(GuidedTour.GuidedTourSteps.CONTENT, getActivity(), parent);
        }

        return v;
    }

    public boolean onBack() {
        boolean ret = mAdapter.getDetail() != null;
        if (ret) {
            MessageDetailFactory.removeView(mAdapter.getDetail());
            mAdapter.clearDetail();
        }
        return ret;
    }

    class CategoryArrayAdapter extends RecyclerView.Adapter<CategoryArrayAdapter.CategoryViewHolder> {
        private final Context context;
        private final List<Category> values;
        private Button btnSelected = null;

        public CategoryArrayAdapter(Context context, List<Category> values) {
            super();
            this.context = context;
            this.values = values;
        }

        @Override
        public int getItemCount() { return values.size(); }

        @Override
        public void onBindViewHolder(CategoryViewHolder holder, int position) {
            holder.mButton.setText(values.get(position).name);
            if (values.get(position).name == mFilter)
                selectButton(holder.mButton);
            else
                unselectButton(holder.mButton);
        }

        @Override
        public CategoryArrayAdapter.CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_ele_categoryfilter, parent, false);
            CategoryViewHolder vh = new CategoryViewHolder(rowView);
            return vh;
        }

        private void selectButton(Button btn) {
            if (btnSelected != null)
                unselectButton(btnSelected);
            btnSelected = btn;
            btnSelected.setTypeface(null, Typeface.BOLD);
            btnSelected.setTextColor(Color.WHITE);
        }

        private void unselectButton(Button btn) {
            btn.setTypeface(null, Typeface.NORMAL);
            btn.setTextColor(Color.LTGRAY);
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            public Button mButton;

            public CategoryViewHolder(View v) {
                super(v);
                mButton = (Button)v.findViewById(R.id.categoryname);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mFilter = mButton.getText().toString();
                        CategoryArrayAdapter.this.selectButton(mButton);
                        generateViewsFromData();
                    }
                });
            }

        }

    }

    protected void setDrawerListener() {
        mDrawerOpen = false;
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerOpen) {
                    mDrawerLayout.closeDrawers();
                    mDrawerOpen = false;
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    mDrawerOpen = true;
                }
            }
        };
        //mToolbarImage.setOnClickListener(mDrawerListener);
        mToolbarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);
                getActivity().startActivityForResult(intent, HomeActivity.REQUEST_CODE_SETTINGS);
            }
        });
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mEventsOnly) {
            inflater.inflate(R.menu.menu_event, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_event) {
            Intent intent = new Intent(getContext(), AddEventActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "on activity result in fragment");
        if (requestCode == REQUEST_SELECT_MESSAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                refreshCurrentNotes(data.getIntExtra(SelectMessageActivity.RESULT_EXTRA_NOTE_ID, -1));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void refreshCurrentNotes(int noteIdToRemove) {
        if (mSortedNotes == null) {
            return;
        }

        boolean changed = false;
        Iterator<NoteExtended> iterator = mSortedNotes.iterator();
        while (iterator.hasNext()) {
            NoteExtended noteExtended = iterator.next();
            if (noteExtended.note.noteId == noteIdToRemove) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            mAdapter.updateNotes(mSortedNotes);
        }

        mDrawerListAdapter.updateSettings(mSettings);
        mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
    }

    //This is used by eventbus once we get the event
    public void onEvent(ShowCategoriesChangedEvent event) {
        mData = GlobalData.getInstance().getData();

        generateNoteList();
        mAdapter.updateNotes(mSortedNotes);

        mDrawerListAdapter.updateSettings(mSettings);
        mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
    }

    @Override
    public void onTabDeselected() {
        mToolbarImage.setOnClickListener(null);
        mDrawerOpen = false;
        mTabSelected = false;
    }

    @Override
    public void onTabSelected() {
        //mToolbarImage.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.ic_menu_white));
        mToolbarImage.setImageDrawable(ContextCompat.getDrawable(this.getContext(), R.drawable.gear_white));
        mToolbarImage.setAlpha(1.0f);
        setDrawerListener();
        mTabSelected = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(Constants.TAG, "On attach of eventsonly = " + Boolean.toString(mEventsOnly));
    }

    public void onEvent(ShowNoteDetailEvent event) {
        Log.d(Constants.TAG, "On event to show note details");
        if (mTabSelected) {
            Log.d(Constants.TAG, "Actually firing event");
            startActivityForResult(event.getIntent(), REQUEST_SELECT_MESSAGE);
        }
    }

    private void generateViewsFromData() {
        Log.d(Constants.TAG, "Re-generating views from data");
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            //Make sure there are no empty categories
            mData.removeEmptyCategories();

            if (mAdapter != null) {
                generateNoteList();
                mAdapter.updateNotes(mSortedNotes);
                mDrawerListAdapter.updateCategories(mData.categories, mData.localTexts, mData.localPhotos);
            } else {

                generateNoteList();
                mAdapter = new MainNotesAdapter(this.getContext(), mSortedNotes, mData, this.getActivity());
                mListView.setAdapter(mAdapter);

                mDrawerListAdapter = new DrawerListArrayAdapter(this.getContext(), mData.categories, mData.localTexts, mData.localPhotos, mSettings, mShowPhotos);
                mDrawerList.setAdapter(mDrawerListAdapter);
            }

            mListView.setVisibility(View.VISIBLE);
        }

    }

    //Generates the pinned hash set and the sorted notes array.  These depend on your settings
    private Random rnd = new Random();
    private void generateNoteList() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {

            mSortedNotes = new ArrayList<>();
            ArrayList<NoteExtended> tmpNotes = new ArrayList<>();

            int categoryPos = -1;
            for (Category category : mData.categories) {
                categoryPos++;

                if ((mFilter == Constants.CATEGORY_FILTER_ALL && !mSettings.shouldShowCategory(category.name)) ||
                        (mFilter != Constants.CATEGORY_FILTER_ALL && mFilter != category.name)) {
                    continue;
                }

                int inCategoryPos = 0;
                for (Note note : category.notes) {
                    if ((mEventsOnly && note.isEvent()) || !mEventsOnly || (mEventsOnly && category.eventCategory) ||
                            mFilter != Constants.CATEGORY_FILTER_ALL || note.isBadge) {
                        int score = rnd.nextInt(3);
                        score += note.newFlag ? 4 : 0;
                        score += note.liked ? 1 : 0;
                        score += mData.hasPinnedNote(note.noteId) ? 1 : 0;
                        score += category.notes.size() - inCategoryPos / 3;
                        if (note.isBadge)
                            score = 1000;

                        tmpNotes.add(new NoteExtended(note, category, categoryPos, inCategoryPos, score));
                    }

                    inCategoryPos++;
                }

            }

            Collections.sort(tmpNotes, Collections.<NoteExtended>reverseOrder());
            mSortedNotes = new ArrayList<>(tmpNotes);
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

            /*
            We shouldn't have any empty categories
            if (category.notes == null || category.notes.size() <= 0) {
                return null;
            }
            */

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
                        EventBus.getDefault().post(new ShowNoteDetailEvent(intent));
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
        private Activity mActivity;
        private ArrayList<NoteExtended> mNotes;
        private TextMuseData mData;
        private View detail;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;
            //public ImageView mArrow;

            public View mBackgroundViewTextOnly;
            public TextView mTextView;
            public ImageView mBackgroundImageView;

            public ImageView mSendImageView;

            public ViewGroup mLayoutSend;

            public ViewGroup mTextLayout;

            public boolean mTextOnly;
        }

        public MainNotesAdapter(Context context, ArrayList<NoteExtended> notes, TextMuseData data, Activity activity) {
            super(context, R.layout.list_ele_category, notes);

            this.mContext = context;
            this.mData = data;
            this.mNotes = notes;
            this.mActivity = activity;
        }


        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View rowView = convertView;
            final NoteExtended noteExtended = mNotes.get(position);
            final Note note = noteExtended.note;

            if (rowView == null) {

                LayoutInflater inflater = LayoutInflater.from(mContext);
                ViewHolder viewHolder = new ViewHolder();

                if (note.hasDisplayableMedia()) {
                    rowView = inflater.inflate(R.layout.list_ele_category_textimage3, parent, false);
                    viewHolder.mBackgroundImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewItemBackground);
                    DisplayMetrics metrics = new DisplayMetrics();
                    ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                    viewHolder.mBackgroundImageView.setMaxHeight((int)(metrics.heightPixels * 0.67));
                    viewHolder.mTextOnly = false;
                } else {
                    rowView = inflater.inflate(R.layout.list_ele_category_textonly3, parent, false);
                    viewHolder.mBackgroundViewTextOnly = rowView.findViewById(R.id.mainViewBackgroundView);
                    viewHolder.mTextOnly = true;
                }

                if (note.isBadge) {
                    TextView tv = (TextView)rowView.findViewById(R.id.mainViewLayoutSendText);
                    tv.setVisibility(View.INVISIBLE);
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
            final int color = colorList[position % colorList.length];

            if (note.hasSponsor)
                holder.mCategoryTitle.setText(note.sponsorName);
            else
                holder.mCategoryTitle.setText(noteExtended.categoryName);
            holder.mCategoryTitle.setTextColor(ColorHelpers.getTextColorForWhiteBackground(color));

            View.OnClickListener onNoteClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup root = (ViewGroup)mActivity.findViewById(R.id.mainFragmentRoot);
                    detail = MessageDetailFactory.CreateDetailView(root, note, mActivity, color, noteExtended.categoryIndex, noteExtended.notePos);
                    Animation detailSlide = AnimationUtils.loadAnimation(mContext, R.anim.activityfadein);
                    root.addView(detail);
                    detail.startAnimation(detailSlide);

                    TextMuseSettings settings = GlobalData.getInstance().getSettings();
                    if (settings.firstLaunch) {
                        RelativeLayout parent = (RelativeLayout)detail.findViewById(R.id.detail_view_root);
                        GlobalData.getInstance().getGuidedTour().addGuidedStepViewForKey(GuidedTour.GuidedTourSteps.TEXTIT, mActivity, parent);
                    }
                    else if (note.hasSponsor && !settings.notifiedFollow) {
                        RelativeLayout parent = (RelativeLayout)detail.findViewById(R.id.detail_view_root);
                        ArrayList<String> params = new ArrayList<>();
                        params.add(note.sponsorName);
                        params.add(note.sponsorName);
                        GlobalData.getInstance().getGuidedTour().addGuidedStepViewForKey(GuidedTour.GuidedTourSteps.SPONSOR, mActivity, parent, null, params);
                        settings.notifiedFollow = true;
                        settings.save(getContext());
                    }
                    else if (note.minSendCount > 0 && !settings.notifiedBadge) {
                        RelativeLayout parent = (RelativeLayout)detail.findViewById(R.id.detail_view_root);
                        ArrayList<String> params = new ArrayList<>();
                        params.add(note.sponsorName);
                        params.add(String.format("%d", note.minSendCount));
                        params.add(note.sponsorName);
                        GlobalData.getInstance().getGuidedTour().addGuidedStepViewForKey(GuidedTour.GuidedTourSteps.BADGE, mActivity, parent, null, params);
                        settings.notifiedBadge = true;
                        settings.save(getContext());
                    }

                }
            };

            holder.mTextLayout.setOnClickListener(onNoteClickListener);

            if (!note.hasDisplayableText()) {
                holder.mTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.mTextView.setVisibility(View.VISIBLE);
                String t = note.getText();
                holder.mTextView.setText(t);
            }

            if (!note.hasDisplayableMedia()) {
                holder.mBackgroundViewTextOnly.setBackgroundColor(color);
                holder.mTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));
            } else {

                DownloadImageAsyncTask task = new DownloadImageAsyncTask(note, mContext.getApplicationContext());
                task.execute();

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
                    }
                });
            }

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

        public View getDetail() { return detail; }

        public void clearDetail() { detail = null; }

        public void updateNotes(ArrayList<NoteExtended> notes) {
            mNotes = notes;
            this.notifyDataSetChanged();
        }
    }

    public static class ShowNoteDetailEvent {
        private Intent mIntent;

        public ShowNoteDetailEvent(Intent intent) {
            mIntent = intent;
        }

        public Intent getIntent() {
            return mIntent;
        }
    }
}
