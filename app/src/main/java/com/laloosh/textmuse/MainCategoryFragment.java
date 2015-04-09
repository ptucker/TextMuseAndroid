package com.laloosh.textmuse;

import android.app.Activity;
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
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;

import java.util.ArrayList;
import java.util.List;


public class MainCategoryFragment extends Fragment {

    //temporary stuff

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

        loadDataFromInternet();

        return view;
    }

    private void generateViewsFromData() {
        if (mData != null && mData.categories != null && mData.categories.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mCategoryListAdapter = new MainCategoryListArrayAdapter(getActivity(), mData.categories);
            mListView.setAdapter(mCategoryListAdapter);
        }

    }

    private void loadDataFromInternet() {

        FetchNotesAsyncTask.FetchNotesAsyncTaskHandler handler = new FetchNotesAsyncTask.FetchNotesAsyncTaskHandler() {
            @Override
            public void handleFetchResult(String s) {
                parseData(s);
                generateViewsFromData();
            }
        };

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(handler);
        task.execute();
    }

    private void parseData(String s) {

        if (s == null || s.length() <= 0) {
            return;
        }

        WebDataParser parser = new WebDataParser();
        mData = parser.parse(s);

        mData.save(getActivity());
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
            public TextView mNewBadge;
            public ImageView mArrow;
            public ViewPager mViewPager;
        }

        public MainCategoryListArrayAdapter(Activity context, List<Category> categories) {
            super(context, R.layout.list_ele_category, categories);
            this.mContext = context;
            this.mCategories = categories;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = mContext.getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_ele_category, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewTitle);
                viewHolder.mArrow = (ImageView) rowView.findViewById(R.id.mainFragmentListItemImageArrow);
                viewHolder.mNewBadge = (TextView) rowView.findViewById(R.id.mainViewFragmentListItemTextViewNewBadge);
                viewHolder.mViewPager = (ViewPager) rowView.findViewById(R.id.mainFragmentListItemPager);

                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            Category category = mCategories.get(position);
            holder.mCategoryTitle.setText(category.name);

            //TODO: Temp color here
            holder.mCategoryTitle.setTextColor(0xff880000);

            int newCount = 0;
            for (Note note : category.notes) {
                if (note.newFlag) {
                    newCount++;
                }
            }

            holder.mNewBadge.setText(Integer.toString(newCount) + " NEW");
            //TODO: Figure out a way to change background of new badge

            holder.mArrow.setColorFilter(0xff880000);

            CategoryViewPagerAdapter viewPagerAdapter = new CategoryViewPagerAdapter(category.notes, mContext);
            holder.mViewPager.setAdapter(viewPagerAdapter);

            return rowView;
        }
    }


    public static class CategoryViewPagerAdapter extends PagerAdapter {

        private List<Note> mNotes;
        private LayoutInflater mLayoutInflater;

        public CategoryViewPagerAdapter(List<Note> notes, Activity activity) {
            mNotes = notes;
            mLayoutInflater = activity.getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view;
            Note note = mNotes.get(position);

            if (note.mediaUrl != null && note.mediaUrl.length() > 0) {
                view = mLayoutInflater.inflate(R.layout.item_text_with_image, container, false);
            } else {
                view = mLayoutInflater.inflate(R.layout.item_text_panel, container, false);
            }

            TextView textView = (TextView) view.findViewById(R.id.mainViewTextViewText);
            textView.setText(note.text);

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


}
