package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.dialogs.ExpandedImageDialogFragment;
import com.laloosh.textmuse.dialogs.SetHighlightProblemDialogFragment;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.HashMap;
import java.util.List;


public class SelectMessageActivity extends ActionBarActivity {

    public static final String CATEGORY_EXTRA = "com.laloosh.textmuse.category.extra";
    public static final String COLOR_OFFSET_EXTRA = "com.laloosh.textmuse.category.coloroffset.extra";
    public static final String NOTE_INDEX_EXTRA = "com.lalaoosh.textmuse.noteid.extra";

    private static final String SAVE_STATE_POSITION = "savestateposition";
    private static final int YOUTUBE_RECOVERY_DIALOG_REQUEST_CODE = 1222;
    private static final int YOUTUBE_FRAMELAYOUT_BASE_ID = 10000000;
    private static final String YOUTUBE_FRAGMENT_NAME = "youtubefragment";

    private ViewPager mViewPager;
    private CirclePageIndicator mPageIndicator;
    private NoteViewPagerAdapter mPagerAdapter;

    private int mCategoryIndex;
    private int mColorOffset;

    private TextMuseData mData;

    private boolean mRequireSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_message);

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
        }

        mData = GlobalData.getInstance().getData();
        if (mData == null || mData.categories == null) {
            //quit the activity and go to the previous screen if somehow there's no data
            finish();
            return;
        }

        Category category;
        if (mCategoryIndex == mData.categories.size()) {
            category = mData.localTexts;
            mRequireSave = true;
        } else if (mCategoryIndex > mData.categories.size()) {
            category = mData.localPhotos;
            mRequireSave = false;
        } else {
            category = mData.categories.get(mCategoryIndex);
            mRequireSave = false;
        }

        mViewPager = (ViewPager) findViewById(R.id.selectMessageViewPager);

        int[] colorList = mData.getColorList();
        int color = colorList[mColorOffset % colorList.length];

        mPagerAdapter = new NoteViewPagerAdapter(category.notes, this, color, mCategoryIndex, mData);
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
        categoryTextView.setText(category.name);
        categoryTextView.setBackgroundColor(color);
        categoryTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));
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
        private ActionBarActivity mActivity;
        private int mColor;
        private HashMap<Integer, ImageDownloadTarget> mDownloadTargets;
        private int mCategoryPosition;
        private TextMuseData mData;

        public NoteViewPagerAdapter(List<Note> notes, ActionBarActivity activity, int color, int categoryPosition, TextMuseData data) {
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

            View view;
            boolean useImageLayout = false;
            final Note note = mNotes.get(position);

            if (note.isLocalNote() && !note.hasDisplayableMedia()) {
                //Local texts
                view = mLayoutInflater.inflate(R.layout.detail_view_text_entry, container, false);

                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewBackgroundBubble);
                imageView.setColorFilter(mColor);

                EditText editText = (EditText) view.findViewById(R.id.detailViewEditText);
                editText.setText(note.text);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        note.text = s.toString();
                    }
                });

            } else if (note.hasDisplayableMedia()) {
                view = mLayoutInflater.inflate(R.layout.detail_view_textimage, container, false);
                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewImage);

                //Try out picasso library to see how it performs
                Picasso.with(mActivity)
                        .load(note.getDisplayMediaUrl(mActivity))
                        .error(R.drawable.placeholder_image)
                        .fit()
                        .centerCrop()
                        .into(imageView);

                Log.d(Constants.TAG, "Seeing if this contains key: " + note.noteId);
                if (!note.isLocalNote() && !mDownloadTargets.containsKey(note.noteId)) {

                    Log.d(Constants.TAG, "Preparing to download image: " + note.mediaUrl + " with note ID: " + note.noteId);
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

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Load the expanded image on a click
                        ExpandedImageDialogFragment fragment = ExpandedImageDialogFragment.newInstance(note);
                        fragment.show(mActivity.getSupportFragmentManager(), "expandedimagefragment");
                    }
                });

                useImageLayout = true;

            } else if (note.isMediaYoutube()) {
                view = mLayoutInflater.inflate(R.layout.detail_view_textvideo, container, false);

                final int framelayoutId = YOUTUBE_FRAMELAYOUT_BASE_ID + note.noteId;
                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.detailViewYoutubeFragmentPlaceholder);
                frameLayout.setId(framelayoutId);

                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewYoutubeThumbnailImage);
                final RelativeLayout previewLayout = (RelativeLayout) view.findViewById(R.id.detailViewYoutubeThumbnailLayout);

                //Try out picasso library to see how it performs
                Picasso.with(mActivity)
                        .load(note.getYoutubeImgUrl())
                        .error(R.drawable.placeholder_image)
                        .fit()
                        .centerCrop()
                        .into(imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //On click of the image, let's load the youtube fragment
                        previewLayout.setVisibility(View.GONE);

                        //Search for the old fragment
                        YouTubePlayerSupportFragment fragment = (YouTubePlayerSupportFragment) mActivity.getSupportFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_NAME);
                        if (fragment != null) {
                            //If it exists, then remove it first
                            try {
                                View parentView = (View) fragment.getView().getParent();
                                View thumbnailViewLayout = parentView.findViewById(R.id.detailViewYoutubeThumbnailLayout);
                                thumbnailViewLayout.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                //log and ignore this since the view parent could be out of scope
                                Log.d(Constants.TAG, "Could not get parent of youtube fragment");
                            }

                            FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
                            transaction.remove(fragment);
                            transaction.commit();
                            mActivity.getSupportFragmentManager().executePendingTransactions();
                        } else {
                            //Create it if this is the first time
                            fragment = YouTubePlayerSupportFragment.newInstance();
                        }

                        YouTubePlayer.OnInitializedListener listener = new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                                if (!wasRestored) {
                                    youTubePlayer.loadVideo(note.getYoutubeVideoTag());
                                }
                            }

                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                                if (youTubeInitializationResult.isUserRecoverableError()) {
                                    youTubeInitializationResult.getErrorDialog(mActivity, YOUTUBE_RECOVERY_DIALOG_REQUEST_CODE).show();
                                } else {
                                    String errorMessage = "Could not initialize video player for the youtube video.";
                                    Toast.makeText(mActivity, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        };

                        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
                        transaction.add(framelayoutId, fragment, YOUTUBE_FRAGMENT_NAME);
                        transaction.commit();
                        fragment.initialize(Constants.GOOGLE_API_YOUTUBE, listener);
                    }
                });


                useImageLayout = true;

            } else {
                view = mLayoutInflater.inflate(R.layout.detail_view_textonly, container, false);

                ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewBackgroundBubble);
                imageView.setColorFilter(mColor);
            }

            if (!note.isLocalNote() || (note.isLocalNote() && note.hasDisplayableMedia())) {
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
            }

            //Link if necessary
            ViewGroup linkLayout = (ViewGroup) view.findViewById(R.id.detailViewLayoutLink);
            if (note.hasExternalLink()) {
                linkLayout.setVisibility(View.VISIBLE);
                linkLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(mActivity, WebviewActivity.class);
                            intent.putExtra(WebviewActivity.WEBVIEW_ACTIVITY_EXTRA, note.getExternalLinkUri(Integer.toString(mData.appId)).toString());
                            mActivity.startActivity(intent);
//                            Intent intent = new Intent(Intent.ACTION_VIEW, note.getExternalLinkUri(Integer.toString(mData.appId)));
//                            mActivity.startActivity(intent);
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "Could not open link!");
                            //TODO: error popup?
                        }
                    }
                });
            } else {
                linkLayout.setVisibility(View.GONE);
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

            TextView selectText = (TextView) view.findViewById(R.id.detailViewTextViewSelect);
            selectText.setTextColor(ColorHelpers.getTextColorForBackground(mColor));

            container.addView(view);

            return view;
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

    }
}