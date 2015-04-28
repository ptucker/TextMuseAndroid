package com.laloosh.textmuse;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.laloosh.textmuse.datamodel.ParcelUtils;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseRecentContact;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactAndGroupPickerState extends ContactPickerState implements Parcelable{

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



    //Stuff for parcelable
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);

        ArrayList<TextMuseRecentContact> recentContacts = getSelectedRecentContacts();

//        Log.d(Constants.TAG, "Contact and group picker state, writing recent contacts " + recentContacts.size());
        ParcelUtils.writeArrayList(out, recentContacts);

        ArrayList<TextMuseGroup> selectedGroups = getSelectedGroups();

//        Log.d(Constants.TAG, "Contact and group picker state, writing groups " + selectedGroups.size());
        ParcelUtils.writeArrayList(out, selectedGroups);
    }

    public static final Parcelable.Creator<ContactAndGroupPickerState> CREATOR = new Parcelable.Creator<ContactAndGroupPickerState>() {
        public ContactAndGroupPickerState createFromParcel(Parcel in) {
            return new ContactAndGroupPickerState(in);
        }

        public ContactAndGroupPickerState[] newArray(int size) {
            return new ContactAndGroupPickerState[size];
        }
    };

    protected ContactAndGroupPickerState(Parcel in) {
        super(in);
        mSelectedGroups = new HashMap<String, TextMuseGroup>();
        mSelectedRecentContacts = new HashMap<String, TextMuseRecentContact>();

        ArrayList<TextMuseRecentContact> recentContacts = ParcelUtils.readTypedList(in, TextMuseRecentContact.CREATOR);
//        Log.d(Constants.TAG, "Contact and group picker state, reading recent contacts " + recentContacts.size());
        if (recentContacts != null && recentContacts.size() > 0) {
            for (TextMuseRecentContact c : recentContacts) {
                mSelectedRecentContacts.put(c.lookupKey, c);
            }
        }

        ArrayList<TextMuseGroup> groups = ParcelUtils.readTypedList(in, TextMuseGroup.CREATOR);
//        Log.d(Constants.TAG, "Contact and group picker state, reading groups " + groups.size());
        if (groups != null && groups.size() > 0) {
            for (TextMuseGroup g : groups) {
                mSelectedGroups.put(g.displayName, g);
            }
        }
    }

}