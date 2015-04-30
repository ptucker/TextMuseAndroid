package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.HashMap;
import java.util.List;


public class SelectMessageActivity extends ActionBarActivity {

    public static final String CATEGORY_EXTRA = "com.laloosh.textmuse.category.extra";
    public static final String COLOR_OFFSET_EXTRA = "com.laloosh.textmuse.category.coloroffset.extra";

    private ViewPager mViewPager;
    private CirclePageIndicator mPageIndicator;
    private NoteViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_message);

        Intent intent = getIntent();
        int categoryPosition = intent.getIntExtra(CATEGORY_EXTRA, 0);
        int colorOffset = intent.getIntExtra(COLOR_OFFSET_EXTRA, 0);

        TextMuseData data = GlobalData.getInstance().getData();
        if (data == null || data.categories == null || categoryPosition >= data.categories.size()) {
            //quit the activity and go to the previous screen if somehow there's no data
            finish();
            return;
        }

        Category category = data.categories.get(categoryPosition);

        mViewPager = (ViewPager) findViewById(R.id.selectMessageViewPager);

        int color = Constants.COLOR_LIST[colorOffset % Constants.COLOR_LIST.length];

        mPagerAdapter = new NoteViewPagerAdapter(category.notes, this, color, categoryPosition);
        mViewPager.setAdapter(mPagerAdapter);

        mPageIndicator = (CirclePageIndicator) findViewById(R.id.selectMessagePageIndicator);
        mPageIndicator.setViewPager(mViewPager);

        TextView categoryTextView = (TextView) findViewById(R.id.selectMessageTextViewCategory);
        categoryTextView.setText(category.name);
        categoryTextView.setBackgroundColor(color);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class NoteViewPagerAdapter extends PagerAdapter {

        private List<Note> mNotes;
        private LayoutInflater mLayoutInflater;
        private Activity mActivity;
        private int mColor;
        private HashMap<Integer, ImageDownloadTarget> mDownloadTargets;
        private int mCategoryPosition;

        public NoteViewPagerAdapter(List<Note> notes, Activity activity, int color, int categoryPosition) {
            mNotes = notes;
            mActivity = activity;
            mLayoutInflater = activity.getLayoutInflater();
            mColor = color;
            mDownloadTargets = new HashMap<Integer, ImageDownloadTarget>();
            mCategoryPosition = categoryPosition;
        }

        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            Log.d(Constants.TAG, "Instantiating view at position " + position);

            View view;
            boolean useImageLayout = false;
            final Note note = mNotes.get(position);

            //TODO: check if this is youtube...
            if (note.mediaUrl != null && note.mediaUrl.length() > 0) {
                view = mLayoutInflater.inflate(R.layout.detail_view_textimage, container, false);
                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewImage);

                //Try out picasso library to see how it performs
                Picasso.with(mActivity)
                        .load(note.mediaUrl)
                        .fit()
                        .centerCrop()
                        .into(imageView);

                if (!mDownloadTargets.containsKey(note.noteId)) {

                    //Do another picasso task to write the image file to external storage.  This will
                    //reuse the same image in the cache so it won't go to network again
                    ImageDownloadTarget downloadTarget = new ImageDownloadTarget(mActivity, note);

                    //Use a hashmap to keep track of these for 2 reasons--to prevent them from getting
                    //garbage collected, and also so that we don't download twice
                    mDownloadTargets.put(note.noteId, downloadTarget);
                    Picasso.with(mActivity)
                            .load(note.mediaUrl)
                            .into(downloadTarget);
                }

                useImageLayout = true;

            } else {
                view = mLayoutInflater.inflate(R.layout.detail_view_textonly, container, false);

                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewBackgroundBubble);
                imageView.setColorFilter(mColor);
            }

            TextView textView = (TextView) view.findViewById(R.id.detailViewTextViewText);
            if (useImageLayout && (note.text == null || note.text.length() <= 0)) {
                ViewGroup textLayout = (ViewGroup) view.findViewById(R.id.detailViewLayoutText);
                textLayout.setVisibility(View.GONE);
            } else {
                if (AndroidUtils.hasTextSizeBug()) {
                    //Due to a bug in android between some specific versions, text resizing doesn't
                    //work properly unless you add a double byte space around it
                    final String DOUBLE_BYTE_SPACE = "\u3000";
                    textView.setText(DOUBLE_BYTE_SPACE + note.text + DOUBLE_BYTE_SPACE);
                } else {
                    textView.setText(note.text);
                }

            }

            //Select box color
            ImageView selectBackground = (ImageView) view.findViewById(R.id.detailViewImageViewSelect);
            selectBackground.setColorFilter(mColor);

            //Quote boxes
            ImageView quote = (ImageView) view.findViewById(R.id.detailViewImageViewLeftQuote);
            quote.setColorFilter(0xff000000);
            quote = (ImageView) view.findViewById(R.id.detailViewImageViewRightQuote);
            quote.setColorFilter(0xff000000);


            ViewGroup selectButton = (ViewGroup) view.findViewById(R.id.detailViewButtonSelectButton);
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, ContactsPickerActivity.class);
                    intent.putExtra(ContactsPickerActivity.CATEGORY_POSITION_EXTRA, mCategoryPosition);
                    intent.putExtra(ContactsPickerActivity.NOTE_POSITION_EXTRA, position);
                    intent.putExtra(ContactsPickerActivity.NOTE_ID_EXTRA, note.noteId);
                    mActivity.startActivity(intent);
                }
            });

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(Constants.TAG, "Destroying view at position " + position);

            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}