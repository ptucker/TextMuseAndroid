package com.laloosh.textmuse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;


public class ContactsPickerActivity extends ActionBarActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    ContactGroupListAdapter mAdapter;
    TextMuseStoredContacts mStoredContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_picker);

        mStoredContacts = GlobalData.getInstance().getStoredContacts();
        if (mStoredContacts == null) {
            GlobalData.getInstance().loadData(this);
            mStoredContacts = GlobalData.getInstance().getStoredContacts();
        }

        //Still no loaded contacts?  Just create an empty one
        if (mStoredContacts == null) {
            mStoredContacts = new TextMuseStoredContacts();
        }

        mAdapter = new ContactGroupListAdapter(mStoredContacts, this);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(Queries.ContactsQuery.QUERY_ID, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts_picker, menu);
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
            mAdapter.swapContactCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == Queries.ContactsQuery.QUERY_ID) {
            mAdapter.swapContactCursor(null);
        }
    }

    public static class PhoneNumberRemovedDialogFragment extends DialogFragment {

        public static PhoneNumberRemovedDialogFragment newInstance() {
            return new PhoneNumberRemovedDialogFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("We could not get the phone number for this contact.  Please pick a different contact.")
                    .setTitle("Could not get phone number")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PhoneNumberRemovedDialogFragment.this.dismiss();
                        }
                    });

            return builder.create();
        }
    }

    public static class ContactGroupListAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

        private TextMuseStoredContacts mStoredContacts;
        private ContactsAdapter mContactsAdapter;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        public ContactGroupListAdapter(TextMuseStoredContacts storedContacts, Context context) {
            mStoredContacts = storedContacts;
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);

            //TODO: need to handle callbacks
            mContactsAdapter = new ContactsAdapter(context, null);
        }

        @Override
        public int getCount() {

            int total = 0;
            int sectionTitleCount = 3;

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

        private View getItemListView(int index, ContactItemType itemType, View convertView, ViewGroup parent) {

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

            switch (itemType) {
                case GROUP_ITEM_PLACEHOLDER:
                    viewHolder.displayName.setText("Add a group");
                    viewHolder.selectedCheckbox.setVisibility(View.INVISIBLE);
                    break;
                case GROUP_ITEM_TYPE:
                    viewHolder.displayName.setText(mStoredContacts.groups.get(index).displayName);
                    viewHolder.selectedCheckbox.setVisibility(View.VISIBLE);
                    break;
                case RECENT_ITEM_PLACEHOLDER:
                    viewHolder.displayName.setText("No recent contacts");
                    viewHolder.selectedCheckbox.setVisibility(View.INVISIBLE);
                    break;
                case RECENT_ITEM_TYPE:
                    viewHolder.displayName.setText(mStoredContacts.recentContacts.get(index).displayName);
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

            toggleEnabled(v);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //Get the parent of the checkbox and get its tag
            View parentView = (View) buttonView.getParent();
            GroupContactViewHolder viewHolder = (GroupContactViewHolder) parentView.getTag();

            //TODO: handle this case
            Log.d(Constants.TAG, "Item with displayname " + viewHolder.displayName.getText() + " ischecked: " + isChecked);
        }

        public void toggleEnabled (View view) {
            GroupContactViewHolder holder = (GroupContactViewHolder) view.getTag();
            if (holder != null &&
                holder.contactItemType != ContactItemType.GROUP_ITEM_PLACEHOLDER &&
                holder.contactItemType != ContactItemType.RECENT_ITEM_PLACEHOLDER) {

                holder.selectedCheckbox.setChecked(!holder.selectedCheckbox.isChecked());
            }
        }

        public Cursor swapContactCursor(Cursor newCursor) {
            return mContactsAdapter.swapCursor(newCursor);
        }


        private enum ContactItemType {
            TITLE_TYPE,
            GROUP_ITEM_TYPE,
            RECENT_ITEM_TYPE,
            GROUP_ITEM_PLACEHOLDER,
            RECENT_ITEM_PLACEHOLDER
        }

        private class GroupContactViewHolder extends ContactViewHolder {
            public ContactItemType contactItemType;
            public int itemIndex;
        }

    }


}
