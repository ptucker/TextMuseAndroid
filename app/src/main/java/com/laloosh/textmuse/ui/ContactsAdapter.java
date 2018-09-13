package com.laloosh.textmuse.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.Queries;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.dialogs.ChoosePhoneNumberDialogFragment;

import java.util.ArrayList;

public class ContactsAdapter extends CursorAdapter implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ChoosePhoneNumberDialogFragment.ChoosePhoneNumberDialogHandler {
    private LayoutInflater mInflater; // Stores the layout inflater
    private AppCompatActivity mContext;
    private ContactsAdapterHandler mHandler;
    private CompoundButton mCurrentButton;  //Used during dialog bits
    private ContactPickerState mState;

    public ContactsAdapter(AppCompatActivity activity, ContactsAdapterHandler handler, ContactPickerState state) {
        super(activity, null, 0);

        // Stores inflater for use later
        mInflater = LayoutInflater.from(activity);
        mContext = activity;
        mHandler = handler;
        mState = state;
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

        holder.selectedCheckbox.setOnCheckedChangeListener(null);

        //This only does a lookup by lookupKey, so this is ok, even though we haven't
        //filled in the full information yet
        holder.selectedCheckbox.setChecked(mState.isContactChecked(contactData));
        holder.selectedCheckbox.setOnCheckedChangeListener(this);

        view.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        TextMuseContact contactData = (TextMuseContact) buttonView.getTag();

        Log.d(Constants.TAG, "Contact with display name of " + contactData.displayName + " ischecked: " + isChecked);

        if (!isChecked) {
            mState.uncheckedContact(contactData);
        } else {

            //let's get the data for this contact
            ContentResolver cr = mContext.getContentResolver();

            Cursor cur = cr.query(Queries.PhoneNumQuery.CONTENT_URI,
                    Queries.PhoneNumQuery.PROJECTION,
                    Queries.PhoneNumQuery.SELECTION,
                    new String[] {contactData.lookupKey},
                    Queries.PhoneNumQuery.SORT_ORDER);

            if (cur.getCount() > 0) {
                ArrayList<TextMuseContact> contacts = new ArrayList<TextMuseContact>();

                while (cur.moveToNext()) {
                    String lookupKey = cur.getString(Queries.PhoneNumQuery.LOOKUP_KEY);
                    String displayName = cur.getString(Queries.PhoneNumQuery.DISPLAY_NAME);
                    String number = cur.getString(Queries.PhoneNumQuery.NUMBER);
                    int type = cur.getInt(Queries.PhoneNumQuery.TYPE);
                    String label = cur.getString(Queries.PhoneNumQuery.LABEL);

                    Log.d(Constants.TAG, "Got contact with this info:");
                    Log.d(Constants.TAG, "Lookup key:" + lookupKey);
                    Log.d(Constants.TAG, "Display Name:" + displayName);
                    Log.d(Constants.TAG, "Number:" + number);
                    Log.d(Constants.TAG, "Type:" + type);
                    Log.d(Constants.TAG, "Label:" + label);

                    String typeString = ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), type, label).toString();
                    Log.d(Constants.TAG, "Type as string:" + typeString);

                    TextMuseContact foundContactData = new TextMuseContact(lookupKey, displayName, number, typeString);
                    contacts.add(foundContactData);
                }

                if (contacts.size() == 1) {
                    mState.checkedContact(contacts.get(0));
                } else {
                    //More than one phone number! let's launch the dialog!
                    ChoosePhoneNumberDialogFragment fragment = ChoosePhoneNumberDialogFragment.newInstance(contacts);
                    fragment.setHandler(this);
                    mCurrentButton = buttonView;
                    fragment.show(mContext.getSupportFragmentManager(), "phoneNumberChoiceFragment");
                }

            } else {
                Log.e(Constants.TAG, "Error getting phone number from lookup!");

                if (mHandler != null) {
                    mHandler.phoneLookupFailed();
                }

                //Clear the checkbox on failure
                buttonView.setOnCheckedChangeListener(null);
                buttonView.setChecked(false);
                buttonView.setOnCheckedChangeListener(this);
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
        Log.d(Constants.TAG, "Toggle enabled on contacts adapter!");
        toggleEnabled(v);
    }

    //This is called whenever we have multiple phone numbers and the person has selected one of them
    @Override
    public void selectedContact(TextMuseContact contact) {
        mState.checkedContact(contact);
        mCurrentButton = null;
    }

    @Override
    public void canceledContactSelection() {
        if (mCurrentButton != null) {
            mCurrentButton.setOnCheckedChangeListener(null);
            mCurrentButton.setChecked(false);
            mCurrentButton.setOnCheckedChangeListener(this);

            mCurrentButton = null;
        }
    }


    public interface ContactsAdapterHandler {
        public void phoneLookupFailed();
    }

}
