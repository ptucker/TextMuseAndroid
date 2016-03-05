package com.laloosh.textmuse.datamodel;

import android.content.Context;
import android.util.Log;

import com.laloosh.textmuse.app.Constants;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

//Base class that holds all of the groups and recent contacts
public class TextMuseStoredContacts {

    public List<TextMuseGroup> groups;
    public List<TextMuseRecentContact> recentContacts;
    public int maxRecentContacts;

    public TextMuseStoredContacts() {
        groups = new ArrayList<TextMuseGroup>();
        addDefaultGroups();
        recentContacts = new ArrayList<TextMuseRecentContact>();
        maxRecentContacts = Constants.DEFAULT_NUMBER_RECENT_CONTACTS;
    }

    //Saves the data to our local preferences
    public void save(Context context) {
        if (!DataPersistenceHelper.save(context, Constants.CONTACTS_FILE, this)) {
            Log.e(Constants.TAG, "Could not save contact data");
        }
    }

    //Loads the cached data from our local preferences
    public static TextMuseStoredContacts load(Context context) {
        return DataPersistenceHelper.load(context, Constants.CONTACTS_FILE, TextMuseStoredContacts.class);
    }

    public boolean hasGroups() {
        return (groups != null && groups.size() > 0);
    }

    public boolean hasRecentContacts() {
        return (recentContacts != null && recentContacts.size() > 0);
    }

    public boolean groupNameExists(String displayName) {
        if (groups == null) {
            return false;
        }

        for (TextMuseGroup g : groups) {
            if (g.displayName.equalsIgnoreCase(displayName)) {
                return true;
            }
        }
        return false;
    }

    public boolean contactExists(String lookupKey) {
        if (recentContacts == null) {
            return false;
        }

        for (TextMuseRecentContact c : recentContacts) {
            if (c.lookupKey.equals(lookupKey)) {
                return true;
            }
        }

        return false;
    }

    //returns True if the adding is success
    public boolean addGroup(TextMuseGroup group) {
        if (groups == null) {
            groups = new ArrayList<TextMuseGroup> ();
        }

        if (groupNameExists(group.displayName)) {
            return false;
        }

        groups.add(group);

        return true;
    }

    public boolean removeGroup(TextMuseGroup group) {
        if (groups == null) {
            return false;
        }
        return groups.remove(group);
    }

    public boolean removeGroup(String displayName) {
        if (groups == null) {
            return false;
        }

        for (TextMuseGroup g : groups) {
            if (g.displayName.equals(displayName)) {
                groups.remove(g);
                return true;
            }
        }

        return false;
    }

    public TextMuseGroup getGroup(String displayName) {
        if (groups == null) {
            return null;
        }

        for (TextMuseGroup g : groups) {
            if (g.displayName.equals(displayName)) {
                return g;
            }
        }

        return null;
    }

//    public boolean updateGroup(String displayName, TextMuseGroup group) {
//        if (groups == null) {
//            return false;
//        }
//
//        for (int i = 0; i < groups.size(); i++) {
//            TextMuseGroup g = groups.get(i);
//            if (g.displayName.equals(displayName)) {
//                groups.set(i, group);
//                return true;
//            }
//        }
//
//        return false;
//    }

    public void updateRecentContactsFromSettings (TextMuseSettings settings) {
        if (settings == null) {
            return;
        }

        if (!settings.saveRecentContacts) {
            maxRecentContacts = 0;
            recentContacts.clear();
            return;
        }

        maxRecentContacts = settings.recentContactLimit;

        if (recentContacts.size() > maxRecentContacts) {
            int numToDelete = recentContacts.size() - maxRecentContacts;
            Iterator iterator = recentContacts.iterator();
            while (iterator.hasNext() && numToDelete > 0) {
                iterator.next();
                iterator.remove();
                numToDelete--;
            }
        }
    }

    public void addOrUpdateRecentContacts(List<TextMuseContact> contactList, TextMuseSettings settings) {
        if (recentContacts == null) {
            recentContacts = new ArrayList<TextMuseRecentContact>();
        }

        updateRecentContactsFromSettings(settings);

        if (!settings.saveRecentContacts) {
            return;
        }

        //If the number of contacts to add to our recent list is as big or bigger than the number
        //that we store, then just clear out our list to add all of the ones we can
        if (contactList.size() >= maxRecentContacts) {
            recentContacts.clear();
            int currentCount = 0;
            for (TextMuseContact c : contactList) {
                TextMuseRecentContact recentContact = new TextMuseRecentContact(c);
                recentContacts.add(recentContact);
                currentCount++;

                if (currentCount >= maxRecentContacts) {
                    break;
                }
            }

            return;
        }

        //Copy the list we got passed in since we'll be removing items from it
        ArrayList<TextMuseContact> contactListTemp = new ArrayList<TextMuseContact>(contactList);

        //Try to do as many updates as we can first
        //Not very efficient, but should be ok given the small size of the recent contacts
        for (TextMuseRecentContact recentContact : recentContacts) {
            Iterator<TextMuseContact> iterator = contactListTemp.iterator();
            while (iterator.hasNext()) {
                TextMuseContact contact = iterator.next();

                if (recentContact.lookupKey.equals(contact.lookupKey)) {
                    recentContact.lastUsed = DateTime.now();

                    //remove this contact so we know that it's been processed already
                    iterator.remove();
                    break;
                }
            }
        }

        //Check to see if there is any more space to add items and add them
        int addCount = maxRecentContacts - recentContacts.size();
        Iterator<TextMuseContact> iterator = contactListTemp.iterator();
        while (iterator.hasNext() && addCount > 0) {
            TextMuseContact contact = iterator.next();

            TextMuseRecentContact recentContact = new TextMuseRecentContact(contact);
            recentContacts.add(recentContact);

            iterator.remove();
            addCount--;
        }


        if (contactListTemp.size() <= 0) {
            return;
        }

        //contactListTemp contains the remaining contacts to add.  Let's sort our recentContacts
        //list based on lastUsed time and replace those entries (since we've already reached max size)
        Comparator<TextMuseRecentContact> recentContactComparator = new Comparator<TextMuseRecentContact>() {
            @Override
            public int compare(TextMuseRecentContact lhs, TextMuseRecentContact rhs) {
                if (lhs.lastUsed.isBefore(rhs.lastUsed)) {
                    return -1;
                } else if (lhs.lastUsed.isAfter(rhs.lastUsed)){
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        Collections.sort(recentContacts, recentContactComparator);

        int i = 0;
        for (TextMuseContact contact : contactListTemp) {
            TextMuseRecentContact recentContact = new TextMuseRecentContact(contact);
            recentContacts.set(i++, recentContact);
            if (i >= maxRecentContacts) {
                break;
            }
        }
    }

    private void addDefaultGroups() {
        TextMuseGroup group = new TextMuseGroup("BFFs");
        addGroup(group);

        group = new TextMuseGroup(("Friends"));
        addGroup(group);

        group = new TextMuseGroup(("Family"));
        addGroup(group);
    }

}
