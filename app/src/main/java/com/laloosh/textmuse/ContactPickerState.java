package com.laloosh.textmuse;


import com.laloosh.textmuse.datamodel.TextMuseContact;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactPickerState {
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

}
