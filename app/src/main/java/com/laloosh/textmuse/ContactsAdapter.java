package com.laloosh.textmuse;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.TextMuseContact;

public class ContactsAdapter extends CursorAdapter implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private LayoutInflater mInflater; // Stores the layout inflater
    private Context mContext;
    private ContactsAdapterHandler mHandler;

    public ContactsAdapter(Context context, ContactsAdapterHandler handler) {
        super(context, null, 0);

        // Stores inflater for use later
        mInflater = LayoutInflater.from(context);

        mContext = context;
        mHandler = handler;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View itemLayout =
                mInflater.inflate(R.layout.contact_list_item, viewGroup, false);

        ContactViewHolder holder = new ContactViewHolder();
        holder.displayName = (TextView) itemLayout.findViewById(R.id.contactListItemDisplayName);
        holder.selectedCheckbox = (CheckBox) itemLayout.findViewById(R.id.contactListItemCheckBox);

        TextMuseContact contactData = new TextMuseContact();
        holder.selectedCheckbox.setTag(contactData);
        holder.selectedCheckbox.setOnCheckedChangeListener(this);

        itemLayout.setTag(holder);

        return itemLayout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ContactViewHolder holder = (ContactViewHolder) view.getTag();
        String displayName = cursor.getString(Queries.ContactsQuery.DISPLAY_NAME);
        holder.displayName.setText(displayName);

        TextMuseContact contactData = (TextMuseContact) holder.selectedCheckbox.getTag();
        contactData.lookupKey = cursor.getString(Queries.ContactsQuery.LOOKUP_KEY);
        contactData.displayName = displayName;

        view.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        TextMuseContact contactData = (TextMuseContact) buttonView.getTag();

        Log.d(Constants.TAG, "Contact with display name of " + contactData.displayName + " ischecked: " + isChecked);

        if (!isChecked) {
            if (mHandler != null) {
                mHandler.uncheckedContact(contactData);
            }
        } else {

            //let's get the data for this contact
            ContentResolver cr = mContext.getContentResolver();

            Cursor cur = cr.query(Queries.PhoneNumQuery.CONTENT_URI,
                    Queries.PhoneNumQuery.PROJECTION,
                    Queries.PhoneNumQuery.SELECTION,
                    new String[] {contactData.lookupKey},
                    Queries.PhoneNumQuery.SORT_ORDER);

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(Queries.PhoneNumQuery.ID);
                    String lookupKey = cur.getString(Queries.PhoneNumQuery.LOOKUP_KEY);
                    String displayName = cur.getString(Queries.PhoneNumQuery.DISPLAY_NAME);
                    String number = cur.getString(Queries.PhoneNumQuery.NUMBER);
                    int type = cur.getInt(Queries.PhoneNumQuery.TYPE);
                    String label = cur.getString(Queries.PhoneNumQuery.LABEL);
                    int isPrimary = cur.getInt(Queries.PhoneNumQuery.IS_PRIMARY);

                    Log.d(Constants.TAG, "Got contact with this info:");
                    Log.d(Constants.TAG, "ID:" + id);
                    Log.d(Constants.TAG, "Lookup key:" + lookupKey);
                    Log.d(Constants.TAG, "Display Name:" + displayName);
                    Log.d(Constants.TAG, "Number:" + number);
                    Log.d(Constants.TAG, "Type:" + type);
                    Log.d(Constants.TAG, "Label:" + label);
                    Log.d(Constants.TAG, "Is primary:" + isPrimary);

                    String typeString = ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), type, label).toString();
                    Log.d(Constants.TAG, "Type as string:" + typeString);

                    contactData.lookupKey = lookupKey;
                    contactData.displayName = displayName;
                    contactData.phoneNumber = number;
                    contactData.numberType = typeString;

                    if (mHandler != null) {
                        mHandler.checkedContact(contactData);
                    }

                    //TODO: only get the first one for now....
                    break;
                }
            } else {
                Log.e(Constants.TAG, "Error getting phone number from lookup!");

                if (mHandler != null) {
                    mHandler.phoneLookupFailed();
                }
            }

            cur.close();

        }
    }

    public void toggleEnabled (View view) {
        ContactViewHolder holder = (ContactViewHolder) view.getTag();
        holder.selectedCheckbox.setChecked(!holder.selectedCheckbox.isChecked());
    }

    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }
        return super.getCount();
    }

    @Override
    public void onClick(View v) {
        toggleEnabled(v);
    }


    public interface ContactsAdapterHandler {
        public void phoneLookupFailed();
        public void checkedContact(TextMuseContact contact);
        public void uncheckedContact(TextMuseContact contact);
    }


}
