package com.laloosh.textmuse.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TextMuseGroup implements Parcelable {

    public String displayName;
    public List<TextMuseContact> contactList;

    public TextMuseGroup() {
        displayName = "";
        contactList = new ArrayList<TextMuseContact>();
    }

    public TextMuseGroup(String name) {
        displayName = name;
        contactList = new ArrayList<TextMuseContact>();
    }

    public TextMuseGroup(String name, List<TextMuseContact> contactList) {
        displayName = name;
        this.contactList = contactList;
    }

    public boolean removeContact(TextMuseContact contact) {
        if (contactList == null) {
            return false;
        } else {
            return contactList.remove(contact);
        }
    }

    public void addContact(TextMuseContact contact) {
        if (contactList == null) {
            contactList = new ArrayList<TextMuseContact>();
        }

        //update if we already have an entry.  This could happen if some information changes
        //for a lookup key
        for (int i = 0; i < contactList.size(); i++) {
            TextMuseContact c = contactList.get(i);
            if (c.lookupKey.equals(contact.lookupKey)) {
                contactList.set(i, contact);
                return;
            }
        }

        contactList.add(contact);
    }

    //Parcelable stuff
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeString(out, displayName);
        out.writeTypedList(contactList);
    }

    public static final Parcelable.Creator<TextMuseGroup> CREATOR = new Parcelable.Creator<TextMuseGroup>() {
        public TextMuseGroup createFromParcel(Parcel in) {
            return new TextMuseGroup(in);
        }

        public TextMuseGroup[] newArray(int size) {
            return new TextMuseGroup[size];
        }
    };

    protected TextMuseGroup(Parcel in) {
        displayName = ParcelUtils.readString(in);
        contactList = in.createTypedArrayList(TextMuseContact.CREATOR);
    }

}
