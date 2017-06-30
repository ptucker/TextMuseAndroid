package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.content.Intent;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.dialogs.ExpandedImageDialogFragment;
import com.laloosh.textmuse.tasks.FollowSponsorAsyncTask;
import com.laloosh.textmuse.tasks.NoteSeeItAsyncTask;
import com.laloosh.textmuse.tasks.SetHighlightAsyncTask;
import com.laloosh.textmuse.utils.AndroidUtils;
import com.laloosh.textmuse.utils.AzureIntegrationSingleton;
import com.laloosh.textmuse.utils.ImageDownloadTarget;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by petertucker on 6/23/17.
 */

public class MessageDetailFactory {
    private static TextMuseData mData;
    private static Activity mActivity;
    private static LayoutInflater mLayoutInflater;
    private static HashMap<Integer, ImageDownloadTarget> mDownloadTargets;
    private static int mColor, mCategoryPosition;

    private static final int YOUTUBE_RECOVERY_DIALOG_REQUEST_CODE = 1222;
    private static final int YOUTUBE_FRAMELAYOUT_BASE_ID = 10000000;
    private static final String YOUTUBE_FRAGMENT_NAME = "youtubefragment";


    public static View CreateDetailView(ViewGroup container, final Note note, Activity activity, int color, int categoryPosition, final int position) {
        View view = null;
        boolean useImageLayout = note.hasDisplayableMedia() || note.isMediaYoutube();
        mData = GlobalData.getInstance().getData();
        mActivity = activity;
        mLayoutInflater = activity.getLayoutInflater();
        mDownloadTargets = new HashMap<Integer, ImageDownloadTarget>();
        mColor = color;
        mCategoryPosition = categoryPosition;

        if (note.isLocalNote() && !note.hasDisplayableMedia()) {
            view = CreateDetailTextEntry(container, note);
        } else if (note.hasDisplayableMedia()) {
            view = CreateDetailTextImage(container, note);
        } else if (note.isMediaYoutube()) {
            view = CreateDetailTextYoutube(container, note);
        } else {
            view = CreateDetailTextOnly(container, note);
        }

        view.setBackgroundColor(Color.WHITE);
        if (!note.isLocalNote() || (note.isLocalNote() && note.hasDisplayableMedia())) {
            TextView textView = (TextView) view.findViewById(R.id.detailViewTextViewText);
            if (useImageLayout && (!note.hasDisplayableText())) {
                ViewGroup textLayout = (ViewGroup) view.findViewById(R.id.detailViewLayoutText);
                textLayout.setVisibility(View.GONE);
            } else {
                if (AndroidUtils.hasTextSizeBug()) {
                    //Due to a bug in android between some specific versions, text resizing doesn't
                    //work properly unless you add a double byte space around it
                    final String DOUBLE_BYTE_SPACE = "\u3000";
                    textView.setText(DOUBLE_BYTE_SPACE + note.getText() + DOUBLE_BYTE_SPACE);
                } else {
                    textView.setText(note.getText());
                }
            }
        }

        SetupTouch(view);
        SetupSeeIt(view, note);
        SetupFollows(view, note);
        SetupQuotes(view, note, position);
        SetupBadgeDetail(view, note);
        SetupClose(view);

        return view;
    }

    private static View CreateDetailTextEntry(ViewGroup container, final Note note) {
        //Local texts
        View view = mLayoutInflater.inflate(R.layout.detail_view_text_entry, container, false);

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

        return view;
    }

    private static View CreateDetailTextImage(ViewGroup container, final Note note) {
        View view = mLayoutInflater.inflate(R.layout.detail_view_textimage, container, false);
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
                DialogFragment fragment = ExpandedImageDialogFragment.newInstance(note);
                fragment.show(mActivity.getFragmentManager(), "expandedimagefragment");
            }
        });

        return view;
    }

    private static View CreateDetailTextYoutube(ViewGroup container, final Note note) {
        View view = mLayoutInflater.inflate(R.layout.detail_view_textvideo, container, false);

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
                YouTubePlayerFragment fragment = (YouTubePlayerFragment) mActivity.getFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_NAME);
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

                    FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
                    transaction.remove(fragment);
                    transaction.commit();
                    mActivity.getFragmentManager().executePendingTransactions();
                } else {
                    //Create it if this is the first time
                    fragment = YouTubePlayerFragment.newInstance();
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

                FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
                transaction.add(framelayoutId, fragment, YOUTUBE_FRAGMENT_NAME);
                transaction.commit();
                fragment.initialize(Constants.GOOGLE_API_YOUTUBE, listener);
            }
        });

        return view;
    }

    private static View CreateDetailTextOnly(ViewGroup container, final Note note) {
        View view = mLayoutInflater.inflate(R.layout.detail_view_textonly, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.detailViewImageViewBackgroundBubble);
        imageView.setColorFilter(mColor);

        return view;
    }

    private static void SetupSeeIt(View view, final Note note) {
        //Link if necessary
        ViewGroup linkLayout = (ViewGroup) view.findViewById(R.id.detailViewLayoutLinkParent);
        if (note.hasExternalLink()) {
            linkLayout.setVisibility(View.VISIBLE);

            ViewGroup internalLinkLayout = (ViewGroup) view.findViewById(R.id.detailViewLayoutLink);
            internalLinkLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(mActivity, WebviewActivity.class);
                        intent.putExtra(WebviewActivity.WEBVIEW_ACTIVITY_EXTRA, note.getExternalLinkUri(Integer.toString(mData.appId)).toString());
                        mActivity.startActivity(intent);

                        NoteSeeItAsyncTask task = new NoteSeeItAsyncTask(null, mData.appId, note.noteId, v.getContext());
                        task.execute();
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "Could not open link!");
                        //TODO: error popup?
                    }
                }
            });
        } else {
            linkLayout.setVisibility(View.INVISIBLE);
        }
    }

    private static void SetupQuotes(View view, final Note note, final int position) {
        //Quote boxes
        ImageView quote = (ImageView) view.findViewById(R.id.detailViewImageViewLeftQuote);
        if (quote != null)
            quote.setColorFilter(0xff000000);
        quote = (ImageView) view.findViewById(R.id.detailViewImageViewRightQuote);
        if (quote != null)
            quote.setColorFilter(0xff000000);

        ViewGroup selectButton = (ViewGroup) view.findViewById(R.id.detailViewLayoutSelect);
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
    }

    private static void SetupFollows(View view, final Note note) {
        final ViewGroup followButton= (ViewGroup) view.findViewById(R.id.detailViewLayoutFollow);
        if (note.hasSponsor) {
            final TextView follow = (TextView)view.findViewById(R.id.detailViewFollowButton);
            followButton.setVisibility(View.VISIBLE);

            if (note.follow) {
                follow.setText("unfollow");
            } else {
                follow.setText("follow");
            }

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (note.follow) {
                        //unfollow, since we are already following
                        mData.unfollowSponsor(note.sponsorId);
                    } else {
                        mData.followSponsor(note.sponsorId);
                    }

                    FollowSponsorAsyncTask task = new FollowSponsorAsyncTask(null, mData.appId, note.sponsorId, !note.follow);
                    task.execute();

                    AzureIntegrationSingleton azureIntegrationSingleton = AzureIntegrationSingleton.getInstance();
                    azureIntegrationSingleton.registerForGcm(mActivity);

                    note.follow = !note.follow;
                    mData.save(mActivity);

                    Log.d(Constants.TAG, "note follow flag set to : " + note.follow);

                    if (note.follow) {
                        Log.d(Constants.TAG, "note setting text to unfollow");
                        follow.setText("unfollow");
                    } else {
                        Log.d(Constants.TAG, "note setting text to follow");
                        follow.setText("follow");
                    }
                }
            });

        } else {
            followButton.setVisibility(View.INVISIBLE);
        }
    }

    private static void SetupBadgeDetail(View view, final Note note) {
        LinearLayout badges = (LinearLayout)view.findViewById(R.id.detailViewBadgeImageLayout);
        if (note.badgeUrl != null && note.badgeUrl.length() > 0) {
            badges.setVisibility(View.VISIBLE);

            ImageView[] badgeIcons = {
                    (ImageView)view.findViewById(R.id.detailViewImageViewBadge1),
                    (ImageView)view.findViewById(R.id.detailViewImageViewBadge2),
                    (ImageView)view.findViewById(R.id.detailViewImageViewBadge3)
            };

            for (ImageView i:badgeIcons) {
                Picasso.with(mActivity)
                        .load(note.badgeUrl)
                        .error(R.drawable.placeholder_image)
                        .fit()
                        .centerCrop()
                        .into(i);
            }
        }
        else {
            badges.setVisibility(View.GONE);
        }

        LinearLayout rewards = (LinearLayout)view.findViewById(R.id.detailViewRewardLayout);
        if (note.minSendCount > 0) {
            rewards.setVisibility(View.VISIBLE);
            TextView sendreward = (TextView)view.findViewById(R.id.detailViewTextViewSendReward);
            sendreward.setText(String.format("Text to %d: %s", note.minSendCount, note.winnerText));
            TextView visitreward = (TextView)view.findViewById(R.id.detailViewTextViewVisitReward);
            visitreward.setText(String.format("Come with %d badges for an even better deal", note.minVisitCount));
        }
        else {
            rewards.setVisibility(View.GONE);
        }
    }

    private static void SetupTouch(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float yDown;

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                int action = MotionEventCompat.getActionMasked(motionEvent);
                if (action == MotionEvent.ACTION_DOWN) {
                    yDown = motionEvent.getY();
                }
                if (action == MotionEvent.ACTION_UP) {
                    if (motionEvent.getY() < yDown - 50)
                        removeView(view);
                }
                return true;
            }
        });
    }

    private static void SetupClose(final View view) {
        Button close = (Button)view.findViewById(R.id.detailViewCloseButton);
        close.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         removeView(view);
                                     }
                                 }
            );
    }

    private static void removeView(View view) {
        ViewGroup root = (ViewGroup)view.getParent();
        Animation detailSlide = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.activityslideup);
        view.startAnimation(detailSlide);
        root.removeView(view);
    }
}
