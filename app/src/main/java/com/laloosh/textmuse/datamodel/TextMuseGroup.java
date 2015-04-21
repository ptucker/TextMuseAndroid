package com.laloosh.textmuse.datamodel;

import java.util.ArrayList;
import java.util.List;

public class TextMuseGroup {

    public String displayName;
    public List<TextMuseContact> contactList;

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

}
