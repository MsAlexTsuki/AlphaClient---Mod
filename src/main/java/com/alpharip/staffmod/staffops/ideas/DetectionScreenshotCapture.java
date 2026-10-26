package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores references supplied by an external screenshot capture implementation. */
public class DetectionScreenshotCapture {
    private final List<String> references = new ArrayList<String>();

    public boolean addReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            return false;
        }
        references.add(reference.trim());
        return true;
    }

    public List<String> getReferences() {
        return Collections.unmodifiableList(new ArrayList<String>(references));
    }

    public void clear() {
        references.clear();
    }
}
