package com.laloosh.textmuse;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.laloosh.textmuse.datamodel.TextMuseData;


public class MainActivity extends ActionBarActivity {

    private EditText mTextData;
    private EditText mUrlText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load data from the web and put it in the textview
                loadDataFromInternet();
            }
        });

        mTextData = (EditText) findViewById(R.id.editText);
        mUrlText = (EditText) findViewById(R.id.editTextUrl);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private void loadDataFromInternet() {

        FetchNotesAsyncTask.FetchNotesAsyncTaskHandler handler = new FetchNotesAsyncTask.FetchNotesAsyncTaskHandler() {
            @Override
            public void handleFetchResult(String s) {
                mTextData.setText(s == null ? "null" : s);
                parseData(s);
            }
        };

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(handler);
        task.execute();
    }

    private void parseData(String s) {
        WebDataParser parser = new WebDataParser();
        TextMuseData data = parser.parse(s);

        Log.d(Constants.TAG, "App ID: " + data.appId);

        data.save(this);
    }
}
