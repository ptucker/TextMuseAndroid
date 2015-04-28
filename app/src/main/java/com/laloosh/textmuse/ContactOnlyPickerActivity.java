package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.dialogs.PhoneNumberRemovedDialogFragment;

import java.util.ArrayList;


public class ContactOnlyPickerActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String CHOSEN_CONTACTS_EXTRA = "com.laloosh.textmuse.chosencontacts";

    private static final String SAVE_STATE_CONTACT_PICKER = "contactpickerstate";

    private ContactsAdapter mAdapter;
    private ContactPickerState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_only_picker);

        if (savedInstanceState != null) {
            Log.d(Constants.TAG, "Restoring instance state for contact picker only activity");
            mState = savedInstanceState.getParcelable(SAVE_STATE_CONTACT_PICKER);
        } else {
            mState = new ContactPickerState();
        }

        mAdapter = new ContactsAdapter(this, mHandler, mState);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(Queries.ContactsQuery.QUERY_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(Constants.TAG, "Saving instance state for contact picker only activity");
        outState.putParcelable(SAVE_STATE_CONTACT_PICKER, mState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_only_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            gatherAndReturnData();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        gatherAndReturnData();
        super.onBackPressed();
    }

    public void gatherAndReturnData() {
        ArrayList<TextMuseContact> contacts = mState.getSelectedContacts();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(CHOSEN_CONTACTS_EXTRA, contacts);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id != Queries.ContactsQuery.QUERY_ID) {
            Log.e(Constants.TAG, "Unsupported query ID in contacts fragment: " + id);
            return null;
        }

        Uri contentUri;
        contentUri = Queries.ContactsQuery.CONTENT_URI;

        return new CursorLoader(this,
                contentUri,
                Queries.ContactsQuery.PROJECTION,
                Queries.ContactsQuery.SELECTION,
                null,
                Queries.ContactsQuery.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == Queries.ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Queries.ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(null);
        }
    }

    private ContactsAdapter.ContactsAdapterHandler mHandler = new ContactsAdapter.ContactsAdapterHandler() {

        @Override
        public void phoneLookupFailed() {
            PhoneNumberRemovedDialogFragment fragment = PhoneNumberRemovedDialogFragment.newInstance();
            fragment.show(ContactOnlyPickerActivity.this.getSupportFragmentManager(), "phoneLookupFailed");
        }

    };


}
