package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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

import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MainCategoryFragment extends Fragment {


    private TextMuseData mData;

    private ViewPager mMainViewPager;

    private ListView mListView;

    private MainCategoryListArrayAdapter mCategoryListAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_category, container, false);

        ArrayList<String> titleList = new ArrayList<String>();
        titleList.add("Send a text to Taurus Johnson");
        titleList.add("Wish John a happy birthday");
        titleList.add("Send well wishes to Amy");
        titleList.add("Say hello to Boris");

        mMainViewPager = (ViewPager) view.findViewById(R.id.mainFragmentViewPagerTop);

        HeaderViewPagerAdapter pagerAdapter = new HeaderViewPagerAdapter(titleList, getActivity());
        mMainViewPager.setAdapter(pagerAdapter);

        mListView = (ListView) view.findViewById(R.id.mainFragmentListView);
        mListView.setVisibility(View.GONE);

        generateViewsFromData();

        loadDataFromInternet();

        return view;
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

        private List<String> mHeaderTexts;
        private LayoutInflater mLayoutInflater;

        public HeaderViewPagerAdapter(List<String> headerTexts, Activity activity) {
            mHeaderTexts = headerTexts;
            mLayoutInflater = activity.getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mHeaderTexts.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mLayoutInflater.inflate(R.layout.item_category_header, container, false);

            TextView textView = (TextView) view.findViewById(R.id.mainViewTextView);
            textView.setText(mHeaderTexts.get(position).toUpperCase());

            container.addView(view);

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
