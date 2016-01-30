package com.laloosh.textmuse.datamodel;

public class NoteExtended implements Comparable<NoteExtended>{

    public Note note;

    public int notePos;

    public int categoryIndex;
    public String categoryName;
    public boolean categoryRequiredFlag;
    public boolean categoryNewFlag;
    public boolean categoryVersionFlag;

    public int score;

    public NoteExtended(Note inNote, Category category, int inCategoryIndex, int inNotePos, int inScore) {
        note = inNote;
        notePos = inNotePos;
        categoryIndex = inCategoryIndex;

        categoryName = category.name;
        categoryRequiredFlag = category.requiredFlag;
        categoryNewFlag = category.newFlag;
        categoryVersionFlag = category.versionFlag;

        score = inScore;
    }

    @Override
    public int compareTo(NoteExtended another) {
        return Integer.valueOf(this.score).compareTo(Integer.valueOf(another.score));
    }
}
