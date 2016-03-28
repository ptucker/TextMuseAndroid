package com.laloosh.textmuse.datamodel;


import java.util.List;

public class Category {

    public String name;
    public boolean requiredFlag;
    public boolean newFlag;
    public boolean versionFlag;
    public boolean eventCategory;

    public List<Note> notes;
}
