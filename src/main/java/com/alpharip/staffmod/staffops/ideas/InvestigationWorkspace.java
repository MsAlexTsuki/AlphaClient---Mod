package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local workspace model for investigation notes and checklist items.
 * It does not open server cases or share data with other staff members.
 */
public final class InvestigationWorkspace {
    private String title = "";
    private String summary = "";
    private final List<String> notes = new ArrayList<String>();
    private final List<ChecklistItem> checklist = new ArrayList<ChecklistItem>();

    public void setDetails(String title, String summary) {
        this.title = title == null ? "" : title;
        this.summary = summary == null ? "" : summary;
    }

    public void addNote(String note) {
        if (note != null && !note.trim().isEmpty()) {
            notes.add(note.trim());
        }
    }

    public void addChecklistItem(String label) {
        if (label != null && !label.trim().isEmpty()) {
            checklist.add(new ChecklistItem(label.trim()));
        }
    }

    public void setChecklistItemComplete(int index, boolean complete) {
        checklist.get(index).setComplete(complete);
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public List<String> getNotes() {
        return Collections.unmodifiableList(new ArrayList<String>(notes));
    }
    public List<ChecklistItem> getChecklist() {
        return Collections.unmodifiableList(new ArrayList<ChecklistItem>(checklist));
    }

    public static final class ChecklistItem {
        private final String label;
        private boolean complete;

        public ChecklistItem(String label) { this.label = label; }
        public String getLabel() { return label; }
        public boolean isComplete() { return complete; }
        public void setComplete(boolean complete) { this.complete = complete; }
    }

    // TODO(Supabase): define case ownership and access policies before shared persistence.
}