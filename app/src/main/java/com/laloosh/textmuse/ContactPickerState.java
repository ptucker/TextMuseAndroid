package com.laloosh.textmuse;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.laloosh.textmuse.datamodel.ParcelUtils;
import com.laloosh.textmuse.datamodel.TextMuseContact;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactPickerState implements Parcelable{
    protected HashMap<String, TextMuseContact> mSelectedContacts;

    public ContactPickerState() {
        mSelectedContacts = new HashMap<String, TextMuseContact>();
    }

    public void checkedContact(TextMuseContact contact) {
        mSelectedContacts.put(contact.lookupKey, contact);
    }

    public void uncheckedContact(TextMuseContact contact) {
        mSelectedContacts.remove(contact.lookupKey);
    }

    public boolean isContactChecked(TextMuseContact contact) {
        return mSelectedContacts.containsKey(contact.lookupKey);
    }

    //Return this as a list since we will need to serialize this data structure
    public ArrayList<TextMuseContact> getSelectedContacts() {
        return new ArrayList<TextMuseContact>(mSelectedContacts.values());
    }


    //Stuff for parcelable
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        ArrayList<TextMuseContact> contacts = getSelectedContacts();

//        Log.d(Constants.TAG, "Contact picker state, writing out selected contacts, count is " + contacts.size());
        ParcelUtils.writeArrayList(out, contacts);
    }

    public static final Parcelable.Creator<ContactPickerState> CREATOR = new Parcelable.Creator<ContactPickerState>() {
        public ContactPickerState createFromParcel(Parcel in) {
            return new ContactPickerState(in);
        }

        public ContactPickerState[] newArray(int size) {
            return new ContactPickerState[size];
        }
    };

    protected ContactPickerState(Parcel in) {
        this();

        ArrayList<TextMuseContact> contacts = ParcelUtils.readTypedList(in, TextMuseContact.CREATOR);

//        Log.d(Constants.TAG, "Contact picker state, reading in contacts " + contacts.size());

        if (contacts != null && contacts.size() > 0) {
            for (TextMuseContact c : contacts) {
                mSelectedContacts.put(c.lookupKey, c);
            }
        }
    }



}
