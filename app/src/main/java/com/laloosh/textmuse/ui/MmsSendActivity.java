package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.utils.SimpleBitmapTarget;
import com.laloosh.textmuse.utils.SmsUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MmsSendActivity extends AppCompatActivity implements SimpleBitmapTarget.SimpleBitmapTargetHandler {
    public static final String NOTE_EXTRA = "note";
    public static final String PHONE_NUMBERS_EXTRA = "phone numbers";

    private static final int MAX_IMAGE_DIMEN = 500;

    @Bind(R.id.mmsLayoutImage) View mLayoutImage;
    @Bind(R.id.mmsText) EditText mEditText;
    @Bind(R.id.mmsImageView) ImageView mImageView;

    private SimpleBitmapTarget mTarget;
    private Bitmap mBitmap;
    private ProgressDialog mProgressDialog;
    private boolean mClosedImage;
    private Note mNote;
    private String[] mPhoneNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mms_send);
        ButterKnife.bind(this);

        setResult(Activity.RESULT_CANCELED);

        mNote = getIntent().getParcelableExtra(NOTE_EXTRA);
        mPhoneNumbers = getIntent().getStringArrayExtra(PHONE_NUMBERS_EXTRA);
        if (mPhoneNumbers == null)
            mPhoneNumbers = new String[]{""};
        if (mNote == null) {
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

        mTarget = new SimpleBitmapTarget(this);

        Picasso.with(this)
                .load(mNote.getDisplayMediaUrl(this))
                .resize(MAX_IMAGE_DIMEN, MAX_IMAGE_DIMEN)
                .centerInside()
                .into(mTarget);

        String text = mNote.getText();
        if (mNote.extraUrl != null && mNote.extraUrl.length() > 0)
            text = text + " (" + mNote.extraUrl + ")";
        mEditText.setText(text);
    }

    @Override
    public void finishedLoadingBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
        mProgressDialog.dismiss();
    }

    @OnClick(R.id.mmsCloseImage)
    public void closeImage() {
        mClosedImage = true;
        mLayoutImage.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.mmsSendNow)
    public void sendNow() {

        Settings sendSettings = new Settings();
        sendSettings.setUseSystemSending(true);
        Transaction sendTransaction = new Transaction(this, sendSettings);

        Message mMessage = new Message(mEditText.getText().toString(), SmsUtils.cleanPhoneNumbers(mPhoneNumbers));
        if (!mClosedImage && mBitmap != null) {
            mMessage.setImage(mBitmap);
        }

        Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT).show();

        sendTransaction.sendNewMessage(mMessage, 0L);

        setResult(Activity.RESULT_OK);
        finish();
    }
}
