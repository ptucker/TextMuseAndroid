package com.laloosh.textmuse.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.laloosh.textmuse.R;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.buttonTest)
    public void pushedTest() {

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "apple.png");
        Bitmap bMap = BitmapFactory.decodeFile(file.getAbsolutePath());

        Settings sendSettings = new Settings();
        sendSettings.setUseSystemSending(true);
        Transaction sendTransaction = new Transaction(this, sendSettings);

        Message mMessage = new Message("this is a test wooo", "6512788573");
        mMessage.setImage(bMap);   // not necessary for voice or sms messages

        sendTransaction.sendNewMessage(mMessage, 0L);
    }

    @OnClick(R.id.buttonSetAsDefault)
    public void pushedDefault() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                getPackageName());
        startActivity(intent);
    }

}
