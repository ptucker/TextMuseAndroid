package com.laloosh.textmuse;


import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseRecentContact;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactAndGroupPickerState extends ContactPickerState {

    protected HashMap<String, TextMuseRecentContact> mSelectedRecentContacts;
    protected HashMap<String, TextMuseGroup> mSelectedGroups;

    public ContactAndGroupPickerState() {
        super();
        mSelectedGroups = new HashMap<String, TextMuseGroup>();
        mSelectedRecentContacts = new HashMap<String, TextMuseRecentContact>();
    }

    //These are broken out separately because it may be possible in odd corner cases to
    //check or uncheck twice in a row
    public void checkedRecentContact(TextMuseRecentContact contact) {
        mSelectedRecentContacts.put(contact.lookupKey, contact);
    }

    public void uncheckedRecentContact(TextMuseRecentContact contact) {
        mSelectedRecentContacts.remove(contact.lookupKey);
    }

    public void checkedGroup(TextMuseGroup group) {
        mSelectedGroups.put(group.displayName, group);
    }

    public void uncheckedGroup(TextMuseGroup group) {
        mSelectedGroups.remove(group.displayName);
    }

    public boolean isRecentContactChecked(TextMuseRecentContact contact) {
        return mSelectedRecentContacts.containsKey(contact.lookupKey);
    }

    public boolean isGroupChecked(TextMuseGroup group) {
        return mSelectedGroups.containsKey(group.displayName);
    }

    public ArrayList<TextMuseRecentContact> getSelectedRecentContacts() {
        return new ArrayList<TextMuseRecentContact>(mSelectedRecentContacts.values());
    }

    public ArrayList<TextMuseGroup> getSelectedGroups() {
        return new ArrayList<TextMuseGroup>(mSelectedGroups.values());
    }

}