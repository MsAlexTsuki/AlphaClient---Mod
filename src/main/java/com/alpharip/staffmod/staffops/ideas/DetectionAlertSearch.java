package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Searches stored alert entries by text. */
public class DetectionAlertSearch {
    private final List<String> entries = new ArrayList<String>();
    public void add(String entry) { if (entry != null) { entries.add(entry); } }
    public List<String> search(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.unmodifiableList(new ArrayList<String>(entries));
        }
        List<String> matches = new ArrayList<String>();
        for (String entry : entries) {
            if (entry != null && entry.toLowerCase().contains(query.toLowerCase())) {
                matches.add(entry);
            }
        }
        return Collections.unmodifiableList(matches);
    }
}