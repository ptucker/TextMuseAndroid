package com.laloosh.textmuse.datamodel;


public class ChosenContact {
    public String lookupKey;
    public String displayName;
    public String phoneNumber;
    public String numberType;

    public ChosenContact(String lookupKey, String displayName, String phoneNumber, String numberType) {
        this.lookupKey = lookupKey;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.numberType = numberType;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (o == null) return false;
//        if (!(o instanceof ChosenContact)) return false;
//
//        ChosenContact other = (ChosenContact) o;
//        return other.lookupKey.equals(this.lookupKey);
//    }
//
//    @Override
//    public int hashCode() {
//        return lookupKey.hashCode();
//
//    }
}
