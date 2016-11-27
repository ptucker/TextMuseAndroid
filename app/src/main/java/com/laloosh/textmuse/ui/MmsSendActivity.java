package com.laloosh.textmuse.ui;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.utils.SimpleBitmapTarget;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MmsSendActivity extends AppCompatActivity implements SimpleBitmapTarget.SimpleBitmapTargetHandler {
    public static final String NOTE_EXTRA = "note";
    public static final String PHONE_NUMBERS_EXTRA = "phone numbers";

    private static final int MAX_IMAGE_DIMEN = 350;

    @Bind(R.id.mmsLayoutImage) View mLayoutImage;
    @Bind(R.id.mmsText) EditText mEditText;
    @Bind(R.id.mmsImageView) ImageView mImageView;

    private SimpleBitmapTarget mTarget;
    private Bitmap mBitmap;
    private boolean mFinishedLoading;
    private ProgressDialog mProgressDialog;
    private boolean mClosedImage;
    private Note mNote;
    private String[] mPhoneNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mms_send);
        ButterKnife.bind(this);

        mNote = getIntent().getParcelableExtra(NOTE_EXTRA);
        mPhoneNumbers = getIntent().getStringArrayExtra(PHONE_NUMBERS_EXTRA);
        if (mNote == null || mPhoneNumbers == null) {
            finish();
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.show();

        handleNote();
    }

    protected void handleNote() {

        Picasso.with(this)
                .load(mNote.getDisplayMediaUrl(this))
                .error(R.drawable.placeholder_image)
                .resize(MAX_IMAGE_DIMEN, MAX_IMAGE_DIMEN)
                .centerInside()
                .into(mImageView);

        mTarget = new SimpleBitmapTarget(this);

        Picasso.with(this)
                .load(mNote.getDisplayMediaUrl(this))
                .resize(MAX_IMAGE_DIMEN, MAX_IMAGE_DIMEN)
                .centerInside()
                .into(mTarget);

        mEditText.setText(mNote.getText());
    }

    @Override
    public void finishedLoadingBitmap(Bitmap bitmap) {
        mFinishedLoading = true;
        mBitmap = bitmap;
        mProgressDialog.dismiss();
    }

    @OnClick(R.id.mmsCloseImage)
    public void closeImage() {
        mClosedImage = true;
        mLayoutImage.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.mmsSendNow)
    public void sendNow() {
        mProgressDialog.show();

        Settings sendSettings = new Settings();
        sendSettings.setUseSystemSending(true);
        Transaction sendTransaction = new Transaction(this, sendSettings);

        Message mMessage = new Message(mEditText.getText().toString(), mPhoneNumbers);
        if (!mClosedImage && mBitmap != null) {
            mMessage.setImage(mBitmap);
        }

        sendTransaction.sendNewMessage(mMessage, 0L);
    }
}
