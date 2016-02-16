package com.laloosh.textmuse;

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
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseRecentContact;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;
import com.laloosh.textmuse.dialogs.CannotSendTextDialogFragment;
import com.laloosh.textmuse.dialogs.EnterGroupDialogFragment;
import com.laloosh.textmuse.dialogs.NoContactsSelectedDialogFragment;
import com.laloosh.textmuse.dialogs.PhoneNumberRemovedDialogFragment;

import java.util.HashSet;


public class ContactsPickerActivity extends ActionBarActivity  implements LoaderManager.LoaderCallbacks<Cursor>, EnterGroupDialogFragment.GroupNameChangeHandler{

    public static final String CATEGORY_POSITION_EXTRA = "com.laloosh.textmuse.contactspicker.categoryextra";
    public static final String NOTE_POSITION_EXTRA = "com.laloosh.textmuse.contactspicker.noteposition";
    public static final String NOTE_ID_EXTRA = "com.laloosh.textmuse.contactspicker.noteid";
    public static final String NOTE_OBJECT_EXTRA = "com.laloosh.textmuse.contactspicker.noteobj";

    private static final int REQUEST_CODE_GROUPEDIT = 1000;
    private static final String SAVE_STATE_PICKER_STATE = "contactandgrouppickerstate";
    private static final String SAVE_STATE_NOTE_STATE = "notestate";

    ContactGroupListAdapter mAdapter;
    TextMuseStoredContacts mStoredContacts;
    TextMuseSettings mSettings;
    ContactAndGroupPickerState mState;
    Note mNote;

    private ActionMode mActionMode = null;
    private int mActionModeIndex = -1;

    protected String mSearchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_picker);

        if (savedInstanceState != null) {
            Log.d(Constants.TAG, "Loading state in contacts picker activity");
            mState = savedInstanceState.getParcelable(SAVE_STATE_PICKER_STATE);
        } else {
            mState = new ContactAndGroupPickerState();
        }

        mStoredContacts = GlobalData.getInstance().getStoredContacts();
        mSettings = GlobalData.getInstance().getSettings();
        if (mStoredContacts == null) {
            GlobalData.getInstance().loadData(this);
            mStoredContacts = GlobalData.getInstance().getStoredContacts();
        }

        Intent intent = getIntent();
        findChosenNote(intent, savedInstanceState);
        if (mNote == null) {
            finish();
            return;
        }

        //Still no loaded contacts?  Just create an empty one
        if (mStoredContacts == null) {
            mStoredContacts = new TextMuseStoredContacts();
        }

        mAdapter = new ContactGroupListAdapter(this, mHandler);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(Queries.ContactsQuery.QUERY_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(Constants.TAG, "Saving state in contacts picker activity");
        outState.putParcelable(SAVE_STATE_PICKER_STATE, mState);
        outState.putParcelable(SAVE_STATE_NOTE_STATE, mNote);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts_picker, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
                getSupportLoaderManager().restartLoader(Queries.ContactsQuery.QUERY_ID, null, ContactsPickerActivity.this);
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
                mAdapter.notifyDataSetChanged();
                getSupportLoaderManager().restartLoader(Queries.ContactsQuery.QUERY_ID, null, ContactsPickerActivity.this);
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

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_add_group) {
            createNewGroup();
            return true;
        } else if (id == R.id.menu_send) {

            HashSet<String> phoneNumberSet = getSelectedPhoneNumbers();
            if (phoneNumberSet.isEmpty()) {
                NoContactsSelectedDialogFragment fragment = NoContactsSelectedDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), "nocontactsfragment");
                return true;
            }

            updateRecentContacts();
            sendNoteIdToServer();

            Intent intent = SmsUtils.createSmsIntent(this, mNote, phoneNumberSet);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Problem launching activity to send SMS/MMS", e);
                CannotSendTextDialogFragment fragment = CannotSendTextDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), "cantsendtextfragment");
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findChosenNote(Intent intent, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mNote = savedInstanceState.getParcelable(SAVE_STATE_NOTE_STATE);
            return;
        }

        //See if a parcelable was passed in
        Note noteObj = intent.getParcelableExtra(NOTE_OBJECT_EXTRA);

        if (noteObj != null) {
            mNote = noteObj;
            return;
        }

        //If no parcelable was passed in, then we must have gotten a set of indexes
        int categoryPosition = intent.getIntExtra(CATEGORY_POSITION_EXTRA, Integer.MIN_VALUE);
        int notePosition = intent.getIntExtra(NOTE_POSITION_EXTRA, Integer.MIN_VALUE);
        int noteId = intent.getIntExtra(NOTE_ID_EXTRA, Integer.MIN_VALUE);

        if (categoryPosition == Integer.MIN_VALUE || notePosition == Integer.MIN_VALUE || noteId == Integer.MIN_VALUE) {
            Log.e(Constants.TAG, "Note information not passed in properly; exiting");
            return;
        }

        TextMuseData data = GlobalData.getInstance().getData();
        if (data != null && data.categories != null) {
            Category category;
            if (categoryPosition == data.categories.size()) {
                category = data.localTexts;
            } else if (categoryPosition == data.categories.size() + 1) {
                category = data.localPhotos;
            } else if (categoryPosition == data.categories.size() + 2) {
                category = data.pinnedNotes;
            } else {
                category = data.categories.get(categoryPosition);
            }
            if (category.notes != null && category.notes.size() > notePosition) {
                Note note = category.notes.get(notePosition);

                if (note.noteId == noteId) {
                    mNote = note;
                } else {
                    Log.e(Constants.TAG, "Note data corrupted");
                }
            }
        } else {
            Log.e(Constants.TAG, "TextMuse data corrupted");
        }
    }

    private void updateRecentContacts() {
        mStoredContacts.addOrUpdateRecentContacts(mState.getSelectedContacts(), mSettings);
        mStoredContacts.save(this);
        mAdapter.notifyDataSetChanged();
    }

    private void sendNoteIdToServer() {
        TextMuseData data = GlobalData.getInstance().getData();
        if (data != null) {
            int appId = data.appId;
            if (appId > 0) {
                NoteSendAsyncTask asyncTask = new NoteSendAsyncTask(appId, mNote.noteId);
                asyncTask.execute();
            }
        }
    }

    private HashSet<String> getSelectedPhoneNumbers() {
        HashSet<String> resultSet = new HashSet<String>();

        for (TextMuseGroup g : mState.getSelectedGroups()) {
            if (g.contactList != null && g.contactList.size() > 0) {
                for (TextMuseContact c : g.contactList) {
                    resultSet.add(c.phoneNumber);
                }
            }
        }

        for (TextMuseRecentContact rc : mState.getSelectedRecentContacts()) {
            resultSet.add(rc.phoneNumber);
        }

        for (TextMuseContact c : mState.getSelectedContacts()) {
            resultSet.add(c.phoneNumber);
        }

        return resultSet;
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
            mAdapter.swapContactCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Queries.ContactsQuery.QUERY_ID) {
            mAdapter.swapContactCursor(null);
        }
    }

    private ContactsAdapter.ContactsAdapterHandler mHandler = new ContactsAdapter.ContactsAdapterHandler() {

        @Override
        public void phoneLookupFailed() {
            PhoneNumberRemovedDialogFragment fragment = PhoneNumberRemovedDialogFragment.newInstance();
            fragment.show(ContactsPickerActivity.this.getSupportFragmentManager(), "phoneLookupFailed");
        }

    };

    @Override
    public void handleNewGroupName(String name) {
        if (name != null && !name.isEmpty()) {
            Intent intent = new Intent(this, GroupEditActivity.class);
            intent.putExtra(GroupEditActivity.NEW_GROUP_NAME_EXTRA, name);
            startActivityForResult(intent, REQUEST_CODE_GROUPEDIT);
        }
    }

    @Override
    public void handleRenameGroupName(String oldname, String newName) {
        TextMuseGroup group = mStoredContacts.getGroup(oldname);
        if (group != null) {
            group.displayName = newName;
        }

        mStoredContacts.save(this);
        mAdapter.notifyDataSetChanged();
        clearActionMode();
    }

    @Override
    public void onGroupNameEditCancel() {
        clearActionMode();
    }

    @Override
    public boolean isUsableGroupName(String name) {
        return !(mStoredContacts.groupNameExists(name));
    }

    //Called when we start adding a new group, either through the menu or through the tile
    public void createNewGroup() {
        Log.d(Constants.TAG, "Launching add a group flow");
        EnterGroupDialogFragment fragment = EnterGroupDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "enterGroupFragment");
    }

    //renames the group with index mActionModeIndex
    public void renameGroup() {
        Log.d(Constants.TAG, "Renaming group");
        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        EnterGroupDialogFragment fragment = EnterGroupDialogFragment.newInstance(group.displayName);
        fragment.show(getSupportFragmentManager(), "enterGroupFragment");
    }

    public void removeGroup() {
        Log.d(Constants.TAG, "Removing group");
        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        mStoredContacts.removeGroup(group);
        mStoredContacts.save(this);
        mAdapter.notifyDataSetChanged();
    }

    public void editGroup() {
        Log.d(Constants.TAG, "Editing group");

        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        Intent intent = new Intent(this, GroupEditActivity.class);
        intent.putExtra(GroupEditActivity.EXISTING_GROUP, group.displayName);

        clearActionMode();

        startActivityForResult(intent, REQUEST_CODE_GROUPEDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GROUPEDIT) {
            if (resultCode == Activity.RESULT_OK) {
                TextMuseStoredContacts contacts = GlobalData.getInstance().getStoredContacts();

                //contacts can be null in the case that the user went to the next screen and decided
                //to quit out of it, so just keep our old data if that's the case
                if (contacts != null) {
                    //update our stored contacts if we got some new data
                    mStoredContacts = contacts;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void startActionMode() {
        if (mActionMode == null) {
            mActionMode = this.startSupportActionMode(mActionModeCallback);
        }
    }

    private void clearActionMode() {
        if (mActionMode != null) {
            mActionModeIndex = -1;
            mActionMode.finish();
        }
    }

    protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_contacts_picker_popup, menu);
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
                case R.id.menu_remove_group:

                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        removeGroup();
                        clearActionMode();
                    }

                    return true;

                case R.id.menu_rename_group:
                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        renameGroup();
                        mActionMode.finish();
                    }

                    return true;

                case R.id.menu_edit_group:
                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        editGroup();
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


    public class ContactGroupListAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

        private ContactsAdapter mContactsAdapter;
        private ActionBarActivity mContext;
        private LayoutInflater mLayoutInflater;

        public ContactGroupListAdapter(ActionBarActivity context, ContactsAdapter.ContactsAdapterHandler handler) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mContactsAdapter = new ContactsAdapter(context, handler, mState);
        }

        @Override
        public int getCount() {

            int total = 0;
            int sectionTitleCount = 3;

            if (mSearchTerm != null) {
                //In a search, only show the searched contacts

                total = mContactsAdapter.getCount();
            } else {
                total += sectionTitleCount;

                if (mStoredContacts.hasGroups()) {
                    total += mStoredContacts.groups.size();
                } else {
                    total += 1;  //One placeholder item telling the user to add a group
                }

                if (mStoredContacts.hasRecentContacts()) {
                    total += mStoredContacts.recentContacts.size();
                } else {
                    total += 1;  //One placeholder item telling the user that there are no recent contacts
                }

                total += mContactsAdapter.getCount();
            }

            return total;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (mSearchTerm != null) {
                //in the case of a search, only display the cursor adapter's results
                return mContactsAdapter.getView(position, convertView, parent);
            }

            int tempPosition = position;
            
            //First position is the groups label
            if (tempPosition == 0) {
                return getTitleView("GROUPS", convertView, parent);
            }

            //subtract the first title segment
            tempPosition -= 1;

            if (mStoredContacts.hasGroups()) {
                if (tempPosition < mStoredContacts.groups.size()) {
                    return getItemListView(tempPosition, ContactItemType.GROUP_ITEM_TYPE, convertView, parent);
                }

                tempPosition -= mStoredContacts.groups.size();
            } else {
                //If there are no groups, we will still show a box with a message
                if (tempPosition == 0) {
                    return getItemListView(tempPosition, ContactItemType.GROUP_ITEM_PLACEHOLDER, convertView, parent);
                }

                tempPosition -= 1;
            }

            //Next up is the section title for Recent Contacts
            if (tempPosition == 0) {
                return getTitleView("RECENT CONTACTS", convertView, parent);
            }

            tempPosition -= 1;

            //Next come the recent contacts
            if (mStoredContacts.hasRecentContacts()) {
                if (tempPosition < mStoredContacts.recentContacts.size()) {
                    return getItemListView(tempPosition, ContactItemType.RECENT_ITEM_TYPE, convertView, parent);
                }

                tempPosition -= mStoredContacts.recentContacts.size();
            } else {
                //If there are no recent, we will still show a box with a message
                if (tempPosition == 0) {
                    return getItemListView(tempPosition, ContactItemType.RECENT_ITEM_PLACEHOLDER, convertView, parent);
                }
                tempPosition -= 1;
            }

            //Section title for normal contacts
            if (tempPosition == 0) {
                return getTitleView("CONTACTS", convertView, parent);
            }
            tempPosition -= 1;

            //Last section is the stuff managed by CursorAdapter
            return mContactsAdapter.getView(tempPosition, convertView, parent);
        }

        private View getTitleView(String title, View convertView, ViewGroup parent) {

            View rowView = convertView;

            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.contact_select_title_item, parent, false);
                GroupContactViewHolder viewHolder = new GroupContactViewHolder();
                viewHolder.contactItemType = ContactItemType.TITLE_TYPE;
                viewHolder.displayName = (TextView) rowView.findViewById(R.id.contactListItemTextViewTitle);
                rowView.setTag(viewHolder);
            }

            GroupContactViewHolder viewHolder = (GroupContactViewHolder) rowView.getTag();

            viewHolder.displayName.setText(title);

            return rowView;
        }

        private View getItemListView(final int index, ContactItemType itemType, View convertView, ViewGroup parent) {

            View rowView = convertView;

            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.contact_list_item, parent, false);
                GroupContactViewHolder viewHolder = new GroupContactViewHolder();
                viewHolder.displayName = (TextView) rowView.findViewById(R.id.contactListItemDisplayName);
                viewHolder.selectedCheckbox = (CheckBox) rowView.findViewById(R.id.contactListItemCheckBox);
                rowView.setTag(viewHolder);
            }

            GroupContactViewHolder viewHolder = (GroupContactViewHolder) rowView.getTag();

            viewHolder.contactItemType = itemType;
            viewHolder.itemIndex = index;

            //Temporarily unset the checked change listener so that we don't trigger anything when setting checked state
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(null);

            rowView.setBackgroundColor(0xffffffff);

            switch (itemType) {
                case GROUP_ITEM_PLACEHOLDER:
                    viewHolder.displayName.setText("Add a group");
                    viewHolder.selectedCheckbox.setVisibility(View.INVISIBLE);
                    break;
                case GROUP_ITEM_TYPE:
                    viewHolder.displayName.setText(mStoredContacts.groups.get(index).displayName);
                    viewHolder.selectedCheckbox.setChecked(mState.isGroupChecked(mStoredContacts.groups.get(index)));
                    viewHolder.selectedCheckbox.setVisibility(View.VISIBLE);

                    if (index == mActionModeIndex) {
                        rowView.setBackgroundColor(0xffcccccc);
                    }

                    rowView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            v.setBackgroundColor(0xffcccccc);
                            mActionModeIndex = index;
                            startActionMode();
                            return true;
                        }
                    });


                    break;
                case RECENT_ITEM_PLACEHOLDER:
                    viewHolder.displayName.setText("No recent contacts");
                    viewHolder.selectedCheckbox.setVisibility(View.INVISIBLE);
                    break;
                case RECENT_ITEM_TYPE:
                    viewHolder.displayName.setText(mStoredContacts.recentContacts.get(index).displayName);
                    viewHolder.selectedCheckbox.setChecked(mState.isRecentContactChecked(mStoredContacts.recentContacts.get(index)));
                    viewHolder.selectedCheckbox.setVisibility(View.VISIBLE);
                    break;
                default:
            }

            rowView.setOnClickListener(this);
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(this);

            return rowView;
        }


        //There are 3 different view types, one for titles, one for layout entries, and one
        //for the views that are handled by the contactsadapter
        @Override
        public int getItemViewType(int position) {

            if (mSearchTerm != null) {
                //In the case of a search, there is only one type of element--the type managed by the CursorAdapter
                return 2;
            }

            //First position is the groups label
            if (position == 0) {
                return 0;
            }

            //subtract the first title segment
            position -= 1;

            //
            if (mStoredContacts.hasGroups()) {
                if (position < mStoredContacts.groups.size()) {
                    return 1;
                }

                position -= mStoredContacts.groups.size();
            } else {
                //If there are no groups, we will still show a box with a message
                if (position == 0) {
                    return 1;
                }

                position -= 1;
            }

            //Next up is the section title for Recent Contacts
            if (position == 0) {
                return 0;
            }

            position -= 1;

            //Next come the recent contacts
            if (mStoredContacts.hasRecentContacts()) {
                if (position < mStoredContacts.recentContacts.size()) {
                    return 1;
                }

                position -= mStoredContacts.recentContacts.size();
            } else {
                //If there are no recent, we will still show a box with a message
                if (position == 0) {
                    return 1;
                }
                position -= 1;
            }

            //Section title for normal contacts
            if (position == 0) {
                return 0;
            }

            //Last section is the stuff managed by CursorAdapter
            return 2;
        }

        @Override
        public int getViewTypeCount() {
            //3 different types of views here, since we assume that the CursorAdapter views are slightly
            //different.  They may not be, but the recycling mechanism may be

            return 3;
        }

        @Override
        public void onClick(View v) {
            GroupContactViewHolder holder = (GroupContactViewHolder) v.getTag();
            Log.d(Constants.TAG, "View with display name " + holder.displayName.getText() + " clicked!!");

            //Check to see if this is a placeholder, and if so, launch the Add a group flow
            if (holder.contactItemType == ContactItemType.GROUP_ITEM_PLACEHOLDER) {

                try {
                    ContactsPickerActivity contactsPickerActivity = (ContactsPickerActivity) mContext;
                    contactsPickerActivity.createNewGroup();
                } catch (ClassCastException e) {
                    Log.e(Constants.TAG, "Contact and group adapter not attached to contact picker activity!");
                }
            } else if (holder.contactItemType != ContactItemType.RECENT_ITEM_PLACEHOLDER) {
                holder.selectedCheckbox.setChecked(!holder.selectedCheckbox.isChecked());
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //Get the parent of the checkbox and get its tag
            View parentView = (View) buttonView.getParent();
            GroupContactViewHolder viewHolder = (GroupContactViewHolder) parentView.getTag();

            Log.d(Constants.TAG, "Item with displayname " + viewHolder.displayName.getText() + " ischecked: " + isChecked);
            switch (viewHolder.contactItemType) {
                case GROUP_ITEM_TYPE:
                    TextMuseGroup group = mStoredContacts.groups.get(viewHolder.itemIndex);
                    if (isChecked) {
                        mState.checkedGroup(group);
                    } else {
                        mState.uncheckedGroup(group);
                    }
                    break;
                case RECENT_ITEM_TYPE:
                    TextMuseRecentContact recentcontact = mStoredContacts.recentContacts.get(viewHolder.itemIndex);
                        if (isChecked) {
                            mState.checkedRecentContact(recentcontact);
                        } else {
                            mState.uncheckedRecentContact(recentcontact);
                        }
                    break;
                default:
                    Log.d(Constants.TAG, "Received oncheckedchanged for category that we don't care about");
            }

        }

        public Cursor swapContactCursor(Cursor newCursor) {
            Cursor cursor = mContactsAdapter.swapCursor(newCursor);
            notifyDataSetChanged();
            return cursor;
        }

        private class GroupContactViewHolder extends ContactViewHolder {
            public ContactItemType contactItemType;
            public int itemIndex;
        }
    }

    protected enum ContactItemType {
        TITLE_TYPE,
        GROUP_ITEM_TYPE,
        RECENT_ITEM_TYPE,
        GROUP_ITEM_PLACEHOLDER,
        RECENT_ITEM_PLACEHOLDER
    }
}
