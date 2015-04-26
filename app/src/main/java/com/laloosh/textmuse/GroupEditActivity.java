package com.laloosh.textmuse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;
import com.laloosh.textmuse.dialogs.NoUsersDialogFragment;

import java.util.List;


public class GroupEditActivity extends ActionBarActivity implements NoUsersDialogFragment.NoUsersDialogHandler {

    public static final String NEW_GROUP_NAME_EXTRA = "com.laloosh.textmuse.newgroup.name";
    public static final String EXISTING_GROUP = "com.laloosh.textmuse.existinggroup";

    private TextView mEmptyTextView;
    private ListView mListView;
    private GroupContactListAdapter mAdapter;
    private TextMuseGroup mGroup;
    private TextMuseStoredContacts mStoredContacts;
    private boolean mAddingGroup;
    private boolean mBackPressed;

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
        setupInitialData(intent);

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

    private void setupInitialData(Intent intent) {

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

    //TODO: options menu, long press to remove, add a name
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        mBackPressed = true;

        if (mGroup.contactList == null || mGroup.contactList.size() <= 0) {
            Log.e(Constants.TAG, "No users added to this group");

            NoUsersDialogFragment fragment = NoUsersDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "noUsersDialogFragment");
        } else {
            persistGroup();
            setResult(Activity.RESULT_OK);
            super.onBackPressed();
        }

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
        if (mAddingGroup) {
            if (mGroup.contactList != null && mGroup.contactList.size() > 0) {
                mStoredContacts.addGroup(mGroup);
                mStoredContacts.save(this);
                GlobalData.getInstance().updateStoredContacts(mStoredContacts);
            }
        } else {
            mStoredContacts.removeGroup(mGroup.displayName);

            if (mGroup.contactList != null && mGroup.contactList.size() > 0) {
                mStoredContacts.addGroup(mGroup);
            }

            mStoredContacts.save(this);
            GlobalData.getInstance().updateStoredContacts(mStoredContacts);
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
            super(context, R.layout.dialog_choose_phonenum_list_ele, objects);
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

            return rowView;
        }

    }
}
