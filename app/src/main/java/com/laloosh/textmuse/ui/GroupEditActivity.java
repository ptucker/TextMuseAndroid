package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;
import com.laloosh.textmuse.dialogs.NoUsersDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class GroupEditActivity extends ActionBarActivity implements NoUsersDialogFragment.NoUsersDialogHandler {

    public static final String NEW_GROUP_NAME_EXTRA = "com.laloosh.textmuse.newgroup.name";
    public static final String EXISTING_GROUP = "com.laloosh.textmuse.existinggroup";

    private static final String SAVE_STATE_ADDING_GROUP_KEY = "addinggroup";
    private static final String SAVE_STATE_GROUP_KEY = "currentgroup";

    private static final int CONTACT_PICKER_REQUEST_CODE = 1999;

    private TextView mEmptyTextView;
    private ListView mListView;
    private GroupContactListAdapter mAdapter;
    private TextMuseGroup mGroup;
    private TextMuseStoredContacts mStoredContacts;
    private boolean mAddingGroup;
    private boolean mBackPressed;

    private ActionMode mActionMode = null;
    private int mActionModeIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        GlobalData globalData = GlobalData.getInstance();
        mStoredContacts = globalData.getStoredContacts();
        if (mStoredContacts == null) {
            //attempt to load the data again in case somehow the singleton got destroyed and we lost the data in it
            globalData.loadData(this);
            mStoredContacts = globalData.getStoredContacts();
        }

        if (mStoredContacts == null) {
            mStoredContacts = new TextMuseStoredContacts();
        }

        //We get the data for the group through the intent
        Intent intent = getIntent();
        setupInitialData(intent, savedInstanceState);

        if (mGroup == null) {
            return;
        }

        mBackPressed = false;

        setTitle(mGroup.displayName);

        //We should have an mGroup here
        mAdapter = new GroupContactListAdapter(this, mGroup.contactList);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);
        mListView.setAdapter(mAdapter);

        if (mGroup.contactList.size() <= 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
        }
    }

    private void setupInitialData(Intent intent, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            Log.d(Constants.TAG, "Restoring instance state for group edit activity");

            //Launch from a restore of this activity, load up our state from the bundle
            mAddingGroup = savedInstanceState.getBoolean(SAVE_STATE_ADDING_GROUP_KEY);
            mGroup = savedInstanceState.getParcelable(SAVE_STATE_GROUP_KEY);

        } else {

            //Normal launch
            String newGroupName = intent.getStringExtra(NEW_GROUP_NAME_EXTRA);
            if (newGroupName == null || newGroupName.isEmpty()) {
                //Existing group edit! -- find the existing group

                mAddingGroup = false;
                String existingGroupName = intent.getStringExtra(EXISTING_GROUP);
                if (existingGroupName != null && !existingGroupName.isEmpty()) {

                    for (TextMuseGroup group : mStoredContacts.groups) {
                        if (group.displayName.equals(existingGroupName)) {
                            mGroup = group;
                            break;
                        }
                    }

                    if (mGroup == null) {
                        Log.e(Constants.TAG, "Invalid state when starting group edit activity -- existing group not found!");
                        finish();
                        return;
                    }
                } else {
                    Log.e(Constants.TAG, "Invalid state when starting group edit activity -- not a new or existing group");
                    finish();
                    return;
                }
            } else {
                //New group time!
                mGroup = new TextMuseGroup(newGroupName);
                mAddingGroup = true;
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(Constants.TAG, "Saving instance state for group edit activity");

        outState.putBoolean(SAVE_STATE_ADDING_GROUP_KEY, mAddingGroup);
        outState.putParcelable(SAVE_STATE_GROUP_KEY, mGroup);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_add_contact) {
            clearActionMode();

            Intent intent = new Intent(this, ContactOnlyPickerActivity.class);
            startActivityForResult(intent, CONTACT_PICKER_REQUEST_CODE);

            return true;
        } else if (id == android.R.id.home) {

            persistGroup();
            setResult(Activity.RESULT_OK);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        mBackPressed = true;

        persistGroup();
        setResult(Activity.RESULT_OK);
        super.onBackPressed();

    }

    @Override
    public void noUsersProceedToDelete() {

        persistGroup();
        setResult(Activity.RESULT_OK);

        if (mBackPressed) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    public void noUsersCancel() {
        mBackPressed = false;
        //Don't do anything... the user decided to cancel the back press and add more contacts
    }

    //Persists the group if there are members and deletes it if there are no members
    private void persistGroup() {
        mStoredContacts.removeGroup(mGroup.displayName);
        mStoredContacts.addGroup(mGroup);
        mStoredContacts.save(this);
        GlobalData.getInstance().updateStoredContacts(mStoredContacts);
    }

    private void startActionMode() {
        if (mActionMode == null) {
            mActionMode = this.startSupportActionMode(mActionModeCallback);
        }
    }

    private void clearActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_group_edit_popup, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove_contact:
                    if (mActionModeIndex == -1) {
                        mActionMode.finish();
                    } else {
                        TextMuseContact contact = mGroup.contactList.get(mActionModeIndex);
                        mGroup.removeContact(contact);
                        mStoredContacts.save(GroupEditActivity.this);
                        mActionMode.finish();
                    }

                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mActionModeIndex = -1;
            mAdapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<TextMuseContact> contacts = data.getParcelableArrayListExtra(ContactOnlyPickerActivity.CHOSEN_CONTACTS_EXTRA);
                if (contacts != null) {
                    for (TextMuseContact c : contacts) {
                        mGroup.addContact(c);
                    }
                    mAdapter.notifyDataSetChanged();
                    if (mGroup.contactList.size() <= 0) {
                        mEmptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyTextView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public class GroupContactListAdapter extends ArrayAdapter<TextMuseContact> {

        private LayoutInflater mLayoutInflater;
        private Context mContext;
        private List<TextMuseContact> mContacts;


        private class ViewHolder {
            public TextView nameTextView;
        }

        public GroupContactListAdapter(Context context, List<TextMuseContact> objects) {
            super(context, R.layout.activity_group_edit_list_ele, objects);
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mContacts = objects;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.activity_group_edit_list_ele, parent, false);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.nameTextView = (TextView) rowView.findViewById(R.id.groupEditTextViewName);

                rowView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) rowView.getTag();
            TextMuseContact contact = mContacts.get(position);

            viewHolder.nameTextView.setText(contact.displayName);

            if (position == mActionModeIndex) {
                rowView.setBackgroundColor(0xffcccccc);
            } else {
                rowView.setBackgroundColor(0xffffffff);
            }

            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setBackgroundColor(0xffcccccc);

                    mActionModeIndex = position;
                    startActionMode();
                    return false;
                }
            });

            return rowView;
        }

    }
}
