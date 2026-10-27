package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds a simple ordered list of supplied evidence notes for a detection review. */
public class DetectionEvidenceBuilder {
    public static final class EvidenceItem {
        public final long timestampMillis;
        public final String category;
        public final String description;

        public EvidenceItem(long timestampMillis, String category, String description) {
            this.timestampMillis = timestampMillis;
            this.category = category == null ? "" : category;
            this.description = description == null ? "" : description;
        }
    }

    private final List<EvidenceItem> evidence = new ArrayList<EvidenceItem>();

    public void add(long timestampMillis, String category, String description) {
        evidence.add(new EvidenceItem(timestampMillis, category, description));
    }

    public List<EvidenceItem> getEvidence() {
        return Collections.unmodifiableList(new ArrayList<EvidenceItem>(evidence));
    }

    public void clear() { evidence.clear(); }
}
