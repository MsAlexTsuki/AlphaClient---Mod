package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local metadata catalogue for evidence references.
 * This does not capture screenshots, verify authenticity, or upload evidence.
 */
public final class EvidenceManager {
    private final List<EvidenceReference> references = new ArrayList<EvidenceReference>();
    private long nextId = 1L;

    public EvidenceReference addReference(String label, String localReference,
                                          String notes, long capturedAtMillis) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Evidence label must not be blank");
        }
        EvidenceReference item = new EvidenceReference(nextId++, label.trim(),
                localReference, notes, capturedAtMillis);
        references.add(item);
        return item;
    }

    public List<EvidenceReference> getReferences() {
        return Collections.unmodifiableList(new ArrayList<EvidenceReference>(references));
    }

    public static final class EvidenceReference {
        private final long id;
        private final String label;
        private final String localReference;
        private final String notes;
        private final long capturedAtMillis;

        private EvidenceReference(long id, String label, String localReference,
                                  String notes, long capturedAtMillis) {
            this.id = id;
            this.label = label;
            this.localReference = localReference == null ? "" : localReference;
            this.notes = notes == null ? "" : notes;
            this.capturedAtMillis = capturedAtMillis;
        }

        public long getId() { return id; }
        public String getLabel() { return label; }
        public String getLocalReference() { return localReference; }
        public String getNotes() { return notes; }
        public long getCapturedAtMillis() { return capturedAtMillis; }
    }

    // TODO(Supabase): define secure evidence storage, access control, and retention before upload.
}