package com.laloosh.textmuse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.laloosh.textmuse.dialogs.AlreadyRegisteredDialogFragment;
import com.laloosh.textmuse.dialogs.BadRegisterDataDialogFragment;
import com.laloosh.textmuse.dialogs.NoRegisterDataDialogFragment;
import com.laloosh.textmuse.dialogs.RegistrationTooYoungDialogFragment;

import org.joda.time.DateTime;


public class RegisterActivity extends ActionBarActivity implements RegisterAsyncTask.RegisterAsyncTaskHandler{
    public static final String REGISTER_THROUGH_WALKTHROUGH_EXTRA = "com.laloosh.textmuse.walkthrough";
    public static final String REGISTER_AFTER_WALKTHROUGH_EXTRA = "com.laloosh.textmuse.afterwalkthrough";

    private EditText mEditTextName;
    private EditText mEditTextEmail;
    private EditText mEditTextBirthMonth;
    private EditText mEditTextBirthYear;

    private boolean mRegistered;
    private boolean mAfterWalkthrough;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent intent = getIntent();
        boolean fromWalkthrough = intent.getBooleanExtra(REGISTER_THROUGH_WALKTHROUGH_EXTRA, false);
        mAfterWalkthrough = intent.getBooleanExtra(REGISTER_AFTER_WALKTHROUGH_EXTRA, false);
        if (fromWalkthrough || mAfterWalkthrough) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        mEditTextName = (EditText) findViewById(R.id.registerEditTextName);
        mEditTextEmail = (EditText) findViewById(R.id.registerEditTextEmail);
        mEditTextBirthMonth = (EditText) findViewById(R.id.registerEditTextBirthMonth);
        mEditTextBirthYear = (EditText) findViewById(R.id.registerEditTextBirthYear);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_REGISTERED, false)) {
            String name = sharedPreferences.getString(Constants.SHARED_PREF_KEY_REGISTER_NAME, "");
            String email = sharedPreferences.getString(Constants.SHARED_PREF_KEY_REGISTER_EMAIL, "");
            int bmonth = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_REGISTER_BMONTH, 1);
            int byear = sharedPreferences.getInt(Constants.SHARED_PREF_KEY_REGISTER_BYEAR, 2000);

            mEditTextName.setText(name);
            mEditTextEmail.setText(email);
            mEditTextBirthMonth.setText(Integer.toString(bmonth));
            mEditTextBirthYear.setText(Integer.toString(byear));
            mRegistered = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mAfterWalkthrough) {
            getMenuInflater().inflate(R.menu.menu_register_after_walkthrough, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_register, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_send) {
            boolean result = handleRegistrationSubmit();
            if (mAfterWalkthrough && result) {
                goToMainActivity();
            }
            return true;
        } else if (id == R.id.menu_skip) {
            goToMainActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainCategoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //Returns true on success or false on failure
    private boolean handleRegistrationSubmit() {

        if (mRegistered) {
            AlreadyRegisteredDialogFragment fragment = AlreadyRegisteredDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "alreadyregisteredfragment");
            return false;
        }

        String name = mEditTextName.getText().toString();
        String email = mEditTextEmail.getText().toString();
        String birthMonthStr = mEditTextBirthMonth.getText().toString();
        String birthYearStr = mEditTextBirthYear.getText().toString();
        int birthMonth, birthYear;

        if (name.length() <= 0 || email.length() <= 0 || birthMonthStr.length() <= 0 || birthYearStr.length() <= 0) {
            Log.e(Constants.TAG, "Incomplete name or email");
            showIncompleteInfoDialog();
            return false;

        }

        try {
            birthMonth = Integer.parseInt(birthMonthStr);
            birthYear = Integer.parseInt(birthYearStr);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Incorrent format of birth months");
            showBadInputDialog();
            return false;
        }

        DateTime now = DateTime.now();

        if (birthMonth > 12 || birthMonth < 1 || birthYear < 1900 || birthYear > now.getYear()) {
            Log.e(Constants.TAG, "Incorrent format of birth months");
            showBadInputDialog();
            return false;
        }

        if (now.getYear() - birthYear < 13 || (now.getYear() - birthYear == 13 && now.getMonthOfYear() < birthMonth)) {
            Log.e(Constants.TAG, "Too young for registration: month: " + Integer.toString(now.getMonthOfYear()) + " and year: " + Integer.toString(now.getYear()));
            showTooYoungDialog();
            return false;
        }

        //The info appears ok, let's submit it

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(Constants.SHARED_PREF_KEY_REGISTERED, true);
        editor.putString(Constants.SHARED_PREF_KEY_REGISTER_NAME, name);
        editor.putString(Constants.SHARED_PREF_KEY_REGISTER_EMAIL, email);
        editor.putInt(Constants.SHARED_PREF_KEY_REGISTER_BMONTH, birthMonth);
        editor.putInt(Constants.SHARED_PREF_KEY_REGISTER_BYEAR, birthYear);

        editor.commit();

        mRegistered = true;

        RegisterAsyncTask asyncTask = new RegisterAsyncTask(this, name, email, birthMonth, birthYear);
        asyncTask.execute();

        return true;
   }

    private void showIncompleteInfoDialog(){
        NoRegisterDataDialogFragment fragment = NoRegisterDataDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "noregisterdatafragment");
    }

    private void showBadInputDialog() {
        BadRegisterDataDialogFragment fragment = BadRegisterDataDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "badregisterfragment");
    }

    private void showTooYoungDialog() {
        RegistrationTooYoungDialogFragment fragment = RegistrationTooYoungDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "tooyoungfragment");
    }

    @Override
    public void handleRegisterResult(boolean success) {
        if (success) {
            Toast toast = Toast.makeText(this, "Registration submitted.", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "Registration could not be submitted. Please try again later.", Toast.LENGTH_LONG);
            Log.d(Constants.TAG, "Could not send registration!");
            toast.show();

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.SHARED_PREF_KEY_REGISTERED, false);
            editor.commit();
        }
    }
}
