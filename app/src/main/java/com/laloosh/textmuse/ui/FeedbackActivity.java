package com.laloosh.textmuse.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.dialogs.NoFeedbackDialogFragment;
import com.laloosh.textmuse.tasks.FeedbackAsyncTask;


public class FeedbackActivity extends AppCompatActivity implements FeedbackAsyncTask.FeedbackAsyncTaskHandler {

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mFeedbackEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mNameEditText = (EditText) findViewById(R.id.feedbackActivityEditTextName);
        mEmailEditText = (EditText) findViewById(R.id.feedbackActivityEditTextEmail);
        mFeedbackEditText = (EditText) findViewById(R.id.feedbackActivityEditTextFeedback);

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_REGISTERED, false)) {
            String name = sharedPreferences.getString(Constants.SHARED_PREF_KEY_REGISTER_NAME, "");
            String email = sharedPreferences.getString(Constants.SHARED_PREF_KEY_REGISTER_EMAIL, "");

            mNameEditText.setText(name);
            mEmailEditText.setText(email);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_send) {

            String name = mNameEditText.getText().toString();
            String email = mEmailEditText.getText().toString();
            String feedback = mFeedbackEditText.getText().toString();

            if (feedback == null || feedback.length() <= 0) {
                NoFeedbackDialogFragment fragment = NoFeedbackDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), "nofeedbackfragment");
            } else {
                FeedbackAsyncTask asyncTask = new FeedbackAsyncTask(this, name, email, feedback);
                asyncTask.execute();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void handleFeedbackResult(boolean success) {
        if (success) {
            Toast toast = Toast.makeText(this, "Feedback sent.", Toast.LENGTH_SHORT);
            toast.show();
            this.finish();
        } else {
            Toast toast = Toast.makeText(this, "Feedback could not be sent. Please try again later.", Toast.LENGTH_LONG);
            Log.d(Constants.TAG, "Could not send feedback!");
            toast.show();
        }

    }
}
