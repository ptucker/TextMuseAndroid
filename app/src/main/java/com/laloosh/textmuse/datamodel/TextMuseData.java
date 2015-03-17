package com.laloosh.textmuse.datamodel;

import org.joda.time.DateTime;

import java.util.List;

public class TextMuseData {

    public List<Category> categories;
    public List<LocalNotification> localNotifications;

    public DateTime timestamp;
    public int appId;
}
