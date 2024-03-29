package com.laloosh.textmuse.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.dialogs.GeneralDialogFragment;
import com.laloosh.textmuse.tasks.AddEventAsyncTask;
import com.laloosh.textmuse.tasks.AddPrayerAsyncTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddEventActivity extends AppCompatActivity
        implements AddEventAsyncTask.AddEventHandler, AddPrayerAsyncTask.AddPrayerHandler {

    @Bind(R.id.addEventTextViewButtonSubmit) TextView mButtonSubmit;
    @Bind(R.id.addEventEditTextDate) EditText mEditTextDate;
    @Bind(R.id.addEventEditTextDescription) EditText mEditTextDescription;
    @Bind(R.id.addEventEditTextEmail) EditText mEditTextEmail;
    @Bind(R.id.addEventEditTextLocation) EditText mEditTextLocation;

    boolean isPrayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = super.getIntent();
        isPrayer = i.getStringExtra ("Filter").toLowerCase().contains("prayer");
        setContentView(R.layout.activity_add_event);
        ButterKnife.bind(this);
        if (isPrayer) {
            mEditTextLocation.setVisibility(View.INVISIBLE);
            TextView label = this.findViewById(R.id.addEventTextViewDescription);
            label.setText("Submit a prayer request so others around you can pray for you.");
            mButtonSubmit.setText("Add prayer");
        }
    }


    @OnClick (R.id.addEventTextViewButtonSubmit)
    public void onSubmit() {

        TextMuseData data = GlobalData.getInstance().getData();
        if (data.skinData == null) {
            finish();
            return;
        }

        int skinId = data.skinData.skinId;

        String date = mEditTextDate.getText().toString();
        String description = mEditTextDescription.getText().toString();
        String email = mEditTextEmail.getText().toString();
        String location = mEditTextLocation.getText().toString();

        if (TextUtils.isEmpty(description)) {
            GeneralDialogFragment fragment = GeneralDialogFragment.newInstance("Error", "Please enter a description.");
            fragment.show(getSupportFragmentManager(), "error dialog");

            return;
        }

        if (TextUtils.isEmpty(date)) {
            GeneralDialogFragment fragment = GeneralDialogFragment.newInstance("Error", "Please enter a date.");
            fragment.show(getSupportFragmentManager(), "error dialog");

            return;
        }


        if (TextUtils.isEmpty(email)) {
            GeneralDialogFragment fragment = GeneralDialogFragment.newInstance("Error", "Please enter an email.");
            fragment.show(getSupportFragmentManager(), "error dialog");

            return;
        }

        if (isPrayer) {
            AddPrayerAsyncTask task = new AddPrayerAsyncTask(this, description, date, email, data.appId, AddEventActivity.this);
            task.execute();
        }
        else {
            AddEventAsyncTask task = new AddEventAsyncTask(this, description, date, email, location, skinId, data.appId, AddEventActivity.this);
            task.execute();
        }
    }


    @Override
    public void handlePostResult(String s) {
        if (s != null) {
            finish();
        } else {
            GeneralDialogFragment fragment = GeneralDialogFragment.newInstance("Unable to post event", "Your event was not able to be posted.");
            fragment.show(getSupportFragmentManager(), "error dialog");
        }
    }
}
