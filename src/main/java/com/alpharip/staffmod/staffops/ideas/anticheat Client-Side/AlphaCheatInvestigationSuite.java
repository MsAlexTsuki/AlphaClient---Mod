package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Holds references to investigation cases for future workflow integration. */
public class AlphaCheatInvestigationSuite {
    private final List<String> caseReferences = new ArrayList<String>();

    public boolean addCase(String reference) {
        if (reference == null || reference.trim().isEmpty()) { return false; }
        caseReferences.add(reference.trim());
        return true;
    }
    public List<String> getCases() {
        return Collections.unmodifiableList(new ArrayList<String>(caseReferences));
    }
    public void clear() { caseReferences.clear(); }
}
