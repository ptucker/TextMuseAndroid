package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v7.widget.SearchView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Queries;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.dialogs.PhoneNumberRemovedDialogFragment;

import java.util.ArrayList;


public class ContactOnlyPickerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String CHOSEN_CONTACTS_EXTRA = "com.laloosh.textmuse.chosencontacts";

    private static final String SAVE_STATE_CONTACT_PICKER = "contactpickerstate";

    private ContactsAdapter mAdapter;
    private ContactPickerState mState;
    private String mSearchTerm;

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

        // Locate the search item
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        // Retrieves the system search manager service
        final SearchManager searchManager =
                (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

        // Retrieves the SearchView from the search menu item
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Assign searchable info to SearchView.
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


//        final int textViewID = searchView.getContext().getResources().getIdentifier("android:id/search_src_text",null, null);
//        final AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(textViewID);
//        try {
//            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
//            mCursorDrawableRes.setAccessible(true);
//            mCursorDrawableRes.set(searchTextView, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
//        } catch (Exception e) {}



        // Set listeners for SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                // Nothing needs to happen when the user submits the search string
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }

                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }

                mSearchTerm = newFilter;

                // Restarts the loader. This triggers onCreateLoader(), which builds the
                // necessary content Uri from mSearchTerm.
                getSupportLoaderManager().restartLoader(Queries.ContactsQuery.QUERY_ID, null, ContactOnlyPickerActivity.this);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                mSearchTerm = null;
                getSupportLoaderManager().restartLoader(Queries.ContactsQuery.QUERY_ID, null, ContactOnlyPickerActivity.this);
                return true;

            }
        });

        if (mSearchTerm != null) {
            // If search term is already set here then this fragment is
            // being restored from a saved state and the search menu item
            // needs to be expanded and populated again.

            // Stores the search term (as it will be wiped out by
            // onQueryTextChange() when the menu item is expanded).
            final String savedSearchTerm = mSearchTerm;

            // Expands the search menu item
            MenuItemCompat.expandActionView(searchItem);

            // Sets the SearchView to the previous search string
            searchView.setQuery(savedSearchTerm, false);
        }

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

        if (mSearchTerm == null) {
            contentUri = Queries.ContactsQuery.CONTENT_URI;
        } else {
            contentUri = Uri.withAppendedPath(Queries.ContactsQuery.FILTER_URI, Uri.encode(mSearchTerm));
        }

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
