package com.laloosh.textmuse;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.ChosenContact;

import java.util.HashMap;


//public class ContactFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
public class ContactFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    ContactsAdapter mAdapter;
    HashMap<String, ChosenContact> mChosenContacts;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Let this fragment contribute menu items
        setHasOptionsMenu(true);

        // Create the main contacts adapter
        mAdapter = new ContactsAdapter(getActivity());
        mChosenContacts = new HashMap<String, ChosenContact> ();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        return inflater.inflate(R.layout.fragment_contacts_picker, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id != ContactsQuery.QUERY_ID) {
            Log.e(Constants.TAG, "Unsupported query ID in contacts fragment: " + id);
            return null;
        }

        Uri contentUri;
        contentUri = ContactsQuery.CONTENT_URI;

        return new CursorLoader(getActivity(),
                contentUri,
                ContactsQuery.PROJECTION,
                ContactsQuery.SELECTION,
                null,
                ContactsQuery.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ContactsQuery.QUERY_ID) {
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d(Constants.TAG, "onItemClick: Item at position " + position + " clicked");
        mAdapter.toggleEnabled(view);
    }


    private class ContactsAdapter extends CursorAdapter implements SectionIndexer, CompoundButton.OnCheckedChangeListener {
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance

        //Holds the references to the UI elements as well as metadata that we'll need later
        private class ViewHolder {
            TextView displayName;
            CheckBox selectedCheckbox;
        }

        private class ContactData {
            String displayName;
            String contactId;
            String contactLookup;
        }

        public ContactsAdapter(Context context) {
            super(context, null, 0);

            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);

            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            final View itemLayout =
                    mInflater.inflate(R.layout.contact_list_item, viewGroup, false);

            ViewHolder holder = new ViewHolder();
            holder.displayName = (TextView) itemLayout.findViewById(R.id.contactListItemDisplayName);
            holder.selectedCheckbox = (CheckBox) itemLayout.findViewById(R.id.contactListItemCheckBox);

            ContactData contactData = new ContactData();
            holder.selectedCheckbox.setTag(contactData);
            holder.selectedCheckbox.setOnCheckedChangeListener(this);

            itemLayout.setTag(holder);

            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            String displayName = cursor.getString(ContactsQuery.DISPLAY_NAME);
            holder.displayName.setText(displayName);

            ContactData contactData = (ContactData) holder.selectedCheckbox.getTag();
            contactData.contactId = cursor.getString(ContactsQuery.ID);
            contactData.contactLookup = cursor.getString(ContactsQuery.LOOKUP_KEY);
            contactData.displayName = displayName;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            ContactData contactData = (ContactData) buttonView.getTag();

            Log.d(Constants.TAG, "Contact with display name of " + contactData.displayName + " ischecked: " + isChecked);

            if (!isChecked) {
                mChosenContacts.remove(contactData.contactLookup);
            } else {
                //let's get the data for this contact
                ContentResolver cr = getActivity().getContentResolver();

                Cursor cur = cr.query(PhoneNumQuery.CONTENT_URI,
                        PhoneNumQuery.PROJECTION,
                        PhoneNumQuery.SELECTION,
                        new String[] {contactData.contactLookup},
                        PhoneNumQuery.SORT_ORDER);

                if (cur.getCount() > 0) {
                    while (cur.moveToNext()) {

                        String id = cur.getString(PhoneNumQuery.ID);
                        String lookupKey = cur.getString(PhoneNumQuery.LOOKUP_KEY);
                        String displayName = cur.getString(PhoneNumQuery.DISPLAY_NAME);
                        String number = cur.getString(PhoneNumQuery.NUMBER);
                        int type = cur.getInt(PhoneNumQuery.TYPE);
                        String label = cur.getString(PhoneNumQuery.LABEL);
                        int isPrimary = cur.getInt(PhoneNumQuery.IS_PRIMARY);

                        Log.d(Constants.TAG, "Got contact with this info:");
                        Log.d(Constants.TAG, "ID:" + id);
                        Log.d(Constants.TAG, "Lookup key:" + lookupKey);
                        Log.d(Constants.TAG, "Display Name:" + displayName);
                        Log.d(Constants.TAG, "Number:" + number);
                        Log.d(Constants.TAG, "Type:" + type);
                        Log.d(Constants.TAG, "Label:" + label);
                        Log.d(Constants.TAG, "Is primary:" + isPrimary);

                        String typeString = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, label).toString();
                        Log.d(Constants.TAG, "Type as string:" + typeString);

                        ChosenContact chosenContact = new ChosenContact(lookupKey, displayName, number, typeString);
                        mChosenContacts.put(contactData.contactLookup, chosenContact);

                        //only get the first one for now....
                        break;
                    }
                } else {
                    Log.e(Constants.TAG, "Error getting phone number from lookup!");

                    PhoneNumberRemovedDialogFragment fragment = PhoneNumberRemovedDialogFragment.newInstance();
                    fragment.show(ContactFragment.this.getFragmentManager(), "phonelookupfailed");
                }

                cur.close();

            }
        }

        public void toggleEnabled (View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.selectedCheckbox.setChecked(!holder.selectedCheckbox.isChecked());
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }
    }

    public interface ContactsQuery {

        final static int QUERY_ID = 1;
        final static Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        final static Uri FILTER_URI = ContactsContract.Contacts.CONTENT_FILTER_URI;

        @SuppressLint("InlinedApi")
        final static String SELECTION =
                (AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME) +
                        "<>''" + " AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1 AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";

        @SuppressLint("InlinedApi")
        final static String SORT_ORDER =
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

                // The contact's row id
                ContactsContract.Contacts._ID,

                // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
                // a "permanent" contact URI.
                ContactsContract.Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
                // some other useful identifier such as an email address. This column isn't
                // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
                // instead.
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
                // you generate the pointer from the contact's ID value and constants defined in
                // android.provider.ContactsContract.Contacts.
                AndroidUtils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts._ID,

                // The sort order column for the returned Cursor, used by the AlphabetIndexer
                SORT_ORDER,

        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int PHOTO_THUMBNAIL_DATA = 3;
        final static int SORT_KEY = 4;
    }

    public interface PhoneNumQuery {

        final static int QUERY_ID = 2;
        final static Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        final static String SELECTION =
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND " + ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP +
                "=1 AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";

        final static String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.IS_PRIMARY + " DESC ";
//        final static String SORT_ORDER =
//                AndroidUtils.hasJellyBeanMR2() ? ContactsContract.CommonDataKinds.Phone.TIMES_USED : ContactsContract.CommonDataKinds.Phone.IS_PRIMARY;


        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                AndroidUtils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int LOOKUP_KEY = 1;
        final static int DISPLAY_NAME = 2;
        final static int NUMBER = 3;
        final static int TYPE = 4;
        final static int LABEL = 5;
        final static int IS_PRIMARY = 6;
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

}
