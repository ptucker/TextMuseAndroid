package com.laloosh.textmuse.ui;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.dialogs.GeneralDialogAndFinishFragment;
import com.laloosh.textmuse.dialogs.SetHighlightProblemDialogFragment;
import com.laloosh.textmuse.tasks.FlagContentAsyncTask;
import com.laloosh.textmuse.tasks.RemitBadgeAsyncTask;
import com.laloosh.textmuse.tasks.RemitDealAsyncTask;
import com.laloosh.textmuse.tasks.ViewCategoryAsyncTask;
import com.laloosh.textmuse.utils.ColorHelpers;
import com.laloosh.textmuse.utils.ImageDownloadTarget;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.HashMap;
import java.util.List;

public class SelectMessageActivity extends AppCompatActivity implements FlagContentAsyncTask.FlagContentResultHandler, RemitBadgeAsyncTask.RemitBadgeEventHandler {

    public static final String CATEGORY_EXTRA = "com.laloosh.textmuse.category.extra";
    public static final String COLOR_OFFSET_EXTRA = "com.laloosh.textmuse.category.coloroffset.extra";
    public static final String NOTE_INDEX_EXTRA = "com.lalaoosh.textmuse.noteindex.extra";

    public static final String CATEGORY_EXTRA_NAME = "com.laloosh.textmuse.categoryname.extra";
    public static final String NOTE_ID_EXTRA = "com.laloosh.textmuse.note.id.extra";

    public static final String RESULT_EXTRA_NOTE_ID = "noteId";

    private static final String SAVE_STATE_POSITION = "savestateposition";
    private static final int YOUTUBE_RECOVERY_DIALOG_REQUEST_CODE = 1222;
    private static final int YOUTUBE_FRAMELAYOUT_BASE_ID = 10000000;
    private static final String YOUTUBE_FRAGMENT_NAME = "youtubefragment";

    private ViewPager mViewPager;
    private CirclePageIndicator mPageIndicator;
    private NoteViewPagerAdapter mPagerAdapter;

    private int mCategoryIndex;
    private int mColorOffset;

    private Category mCategory;
    private Note mActiveNote;

    private TextMuseData mData;

    private boolean mRequireSave;

    private boolean mIsBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_message);

        mData = GlobalData.getInstance().getData();
        if (mData == null || mData.categories == null) {
            //quit the activity and go to the previous screen if somehow there's no data
            finish();
            return;
        }

        int currentItem = -1;
        if (savedInstanceState != null) {
            mCategoryIndex = savedInstanceState.getInt(CATEGORY_EXTRA);
            mColorOffset = savedInstanceState.getInt(COLOR_OFFSET_EXTRA);
            currentItem = savedInstanceState.getInt(SAVE_STATE_POSITION);
        } else {
            Intent intent = getIntent();

            mCategoryIndex = intent.getIntExtra(CATEGORY_EXTRA, 0);
            mColorOffset = intent.getIntExtra(COLOR_OFFSET_EXTRA, 0);

            int noteIndex = intent.getIntExtra(NOTE_INDEX_EXTRA, -1);
            if (noteIndex > 0) {
                currentItem = noteIndex;
            }

            String categoryName = intent.getStringExtra(CATEGORY_EXTRA_NAME);
            int noteId = intent.getIntExtra(NOTE_ID_EXTRA, -1);

            if (categoryName != null) {
                int i = 0;
                for (Category category : mData.categories) {
                    if (category.name.equals(categoryName)) {
                        //Override the category index if a name was passed in
                        mCategoryIndex = i;
                        break;
                    }
                    i++;
                }
            }

            if (noteId >= 0 && mCategoryIndex < mData.categories.size()) {
                Category category = mData.categories.get(mCategoryIndex);
                int i = 0;
                for (Note note : category.notes) {
                    if (note.noteId == noteId) {
                        //Set the current item by ID if possible
                        currentItem = i;
                        break;
                    }
                    i++;
                }
            }
        }


        if (mCategoryIndex == mData.categories.size()) {
            mCategory = mData.localTexts;
            mRequireSave = true;
        } else if (mCategoryIndex == mData.categories.size() + 1) {
            mCategory = mData.localPhotos;
            mRequireSave = false;
        } else if (mCategoryIndex == mData.categories.size() + 2) {
            mCategory = mData.pinnedNotes;
            mRequireSave = false;
        } else {
            mCategory = mData.categories.get(mCategoryIndex);
            mRequireSave = false;
        }

        if (mCategory.name.toLowerCase().contains("badge") || mCategory.name.toLowerCase().contains("deal")) {
            mIsBadge = true;
            invalidateOptionsMenu();
        }

        if (mCategory.id > 0) {
            ViewCategoryAsyncTask task = new ViewCategoryAsyncTask(null, mData.appId, mCategory.id, this);
            task.execute();
        }

        mViewPager = (ViewPager) findViewById(R.id.selectMessageViewPager);

        int[] colorList = mData.getColorList();
        int color = colorList[mColorOffset % colorList.length];

        mPagerAdapter = new NoteViewPagerAdapter(mCategory.notes, this, color, mCategoryIndex, mData);
        mViewPager.setAdapter(mPagerAdapter);

        mPageIndicator = (CirclePageIndicator) findViewById(R.id.selectMessagePageIndicator);
        mPageIndicator.setViewPager(mViewPager);

        if (mRequireSave) {
            mPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    mData.save(SelectMessageActivity.this);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        //Restore our page
        if (currentItem > 0 && mPagerAdapter.getCount() > currentItem) {
            mViewPager.setCurrentItem(currentItem);
        }

        TextView categoryTextView = (TextView) findViewById(R.id.selectMessageTextViewCategory);
        categoryTextView.setText(mCategory.name);
        categoryTextView.setBackgroundColor(color);
        categoryTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));

        overridePendingTransition(R.anim.activitydropdown, R.anim.activityslideup);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CATEGORY_EXTRA, mCategoryIndex);
        outState.putInt(COLOR_OFFSET_EXTRA, mColorOffset);
        outState.putInt(SAVE_STATE_POSITION, mViewPager.getCurrentItem());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        if (mRequireSave) {
            mData.save(this);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mIsBadge) {
            getMenuInflater().inflate(R.menu.menu_select_message, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_select_message_flag, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_remit) {
            remitClicked();
            return true;
        } else if (id == R.id.menu_flag) {
            flagClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!mPagerAdapter.onBack())
            super.onBackPressed();
    }

    protected void remitClicked() {
        int index = mViewPager.getCurrentItem();
        Note note = mCategory.notes.get(index);
        mActiveNote = note;

        if (mCategory.name.toLowerCase().contains("deal")) {
            RemitDealAsyncTask task = new RemitDealAsyncTask(null, mData.appId, note.noteId, this);
            task.execute();
        } else {
            RemitBadgeAsyncTask task = new RemitBadgeAsyncTask(this, mData.appId, note.noteId);
            task.execute();
        }
    }

    //Flags the current message
    protected void flagClicked() {
        int index = mViewPager.getCurrentItem();
        Note note = mCategory.notes.get(index);
        mActiveNote = note;

        FlagContentAsyncTask task = new FlagContentAsyncTask(this, note.noteId);
        task.execute();
    }

    @Override
    public void handleFlagPostResult(String s) {
        removeActiveNote();

        GeneralDialogAndFinishFragment fragment = GeneralDialogAndFinishFragment.newInstance("Flagged Content", "You have flagged this content as inappropriate.");
        fragment.show(getSupportFragmentManager(), "flag_dialog");
    }

    @Override
    public void handleRemitPostResult(String s) {
        removeActiveNote();

        GeneralDialogAndFinishFragment fragment = GeneralDialogAndFinishFragment.newInstance("Claimed Deal", "You have claimed this deal!");
        fragment.show(getSupportFragmentManager(), "claimed_dialog");
    }

    protected void removeActiveNote() {
        if (mActiveNote != null) {
            mData.flagNote(mActiveNote.noteId);
            mData.removeEmptyCategories();
            mData.save(this);

            Intent intent = new Intent();
            intent.putExtra(RESULT_EXTRA_NOTE_ID, mActiveNote.noteId);
            setResult(RESULT_OK, intent);
        }
    }

    public void showHighlightFailedDialog() {
        SetHighlightProblemDialogFragment fragment = SetHighlightProblemDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "highlightfailedfragment");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == YOUTUBE_RECOVERY_DIALOG_REQUEST_CODE) {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public static class NoteViewPagerAdapter extends PagerAdapter {

        private List<Note> mNotes;
        private LayoutInflater mLayoutInflater;
        private AppCompatActivity mActivity;
        private int mColor;
        private HashMap<Integer, ImageDownloadTarget> mDownloadTargets;
        private int mCategoryPosition;
        private TextMuseData mData;
        private View detail;

        public NoteViewPagerAdapter(List<Note> notes, AppCompatActivity activity, int color, int categoryPosition, TextMuseData data) {
            mNotes = notes;
            mActivity = activity;
            mLayoutInflater = activity.getLayoutInflater();
            mColor = color;
            mDownloadTargets = new HashMap<Integer, ImageDownloadTarget>();
            mCategoryPosition = categoryPosition;
            mData = data;
        }

        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            Log.d(Constants.TAG, "Instantiating view at position " + position);

            final Note note = mNotes.get(position);

            detail = MessageDetailFactory.CreateDetailView(container, note, mActivity, mColor, mCategoryPosition, position);

            container.addView(detail);

            return detail;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(Constants.TAG, "Destroying view at position " + position);

            View view = (View) object;
            container.removeView(view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public boolean onBack() {
            boolean ret = detail != null;
            if (ret) {
                MessageDetailFactory.removeView(detail);
                detail = null;
            }
            return ret;
        }
    }
}