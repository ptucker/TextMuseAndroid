package com.laloosh.textmuse.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.utils.GuidedTour;
import com.laloosh.textmuse.utils.SimpleBitmapTarget;
import com.laloosh.textmuse.utils.SmsUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MmsSendActivity extends AppCompatActivity implements SimpleBitmapTarget.SimpleBitmapTargetHandler, GuidedTour.Pause {
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

        getSMSPermissions();
    }

    private static final int SEND_SMS_PERMISSIONS_REQUEST = 2;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 4;
    List<String> perms;
    private void getSMSPermissions() {
        perms = new ArrayList<String>();
        perms.add(Manifest.permission.READ_SMS);
        perms.add(Manifest.permission.SEND_SMS);
        for (int i=0; i<perms.size(); i++) {
            if (ContextCompat.checkSelfPermission(this, perms.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                // The permission is already granted.
                perms.remove(i);
                i--;
            }
            else {
                // Check if the user has been asked about this permission already and denied
                // it. If so, we want to give more explanation about why the permission is needed.
                if (shouldShowRequestPermissionRationale(perms.get(i))) {
                    // TODO
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                }

            }
        }

        if (perms.size() > 0) {
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(perms.toArray(new String[perms.size()]), READ_SMS_PERMISSIONS_REQUEST);
        }
        else
            handleNote();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_SMS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == perms.size()) {
                boolean granted = true;
                for (int i=0; i<grantResults.length; i++)
                    granted &= (grantResults[i] == PackageManager.PERMISSION_GRANTED);

                if (granted)
                    handleNote();
                else {
                    //Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
            else {
                //Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void handleNote() {

        mTarget = new SimpleBitmapTarget(this);

        Picasso.get()
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
        if (GlobalData.getInstance().getSettings().firstLaunch) {
            RelativeLayout parent = (RelativeLayout)findViewById(R.id.mms_send_root);
            GlobalData.getInstance().getGuidedTour().addGuidedStepViewForKey(GuidedTour.GuidedTourSteps.DONE, this, parent, this);
        }
        else
            this.onComplete();
    }

    public void onComplete() {
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
