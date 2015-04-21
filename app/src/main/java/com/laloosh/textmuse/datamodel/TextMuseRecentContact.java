package com.laloosh.textmuse.datamodel;


import org.joda.time.DateTime;

public class TextMuseRecentContact extends TextMuseContact {
    public DateTime lastUsed;

    public TextMuseRecentContact(String lookupKey, String displayName, String phoneNumber, String numberType) {
        super (lookupKey, displayName, phoneNumber, numberType);
        this.lastUsed = DateTime.now();
    }

    public TextMuseRecentContact(TextMuseContact contact) {
        super (contact.lookupKey, contact.displayName, contact.phoneNumber, contact.numberType);
        this.lastUsed = DateTime.now();
    }
}
