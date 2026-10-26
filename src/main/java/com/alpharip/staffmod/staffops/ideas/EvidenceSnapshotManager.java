package com.alpharip.staffmod.staffops.ideas;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local metadata registry for snapshots; it does not capture or store image bytes. */
public final class EvidenceSnapshotManager {
    private final List<Snapshot> snapshots = new ArrayList<Snapshot>();
    private long nextId = 1L;

    public Snapshot register(String label, String reference, long createdAtMillis) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Snapshot label must not be blank");
        }
        Snapshot snapshot = new Snapshot(nextId++, label.trim(), reference, createdAtMillis);
        snapshots.add(snapshot);
        return snapshot;
    }

    public List<Snapshot> getSnapshots() {
        return Collections.unmodifiableList(new ArrayList<Snapshot>(snapshots));
    }

    public static final class Snapshot {
        private final long id;
        private final String label;
        private final String reference;
        private final long createdAtMillis;

        private Snapshot(long id, String label, String reference, long createdAtMillis) {
            this.id = id;
            this.label = label;
            this.reference = reference == null ? "" : reference;
            this.createdAtMillis = createdAtMillis;
        }
        public long getId() { return id; }
        public String getLabel() { return label; }
        public String getReference() { return reference; }
        public long getCreatedAtMillis() { return createdAtMillis; }
    }

    // TODO(Supabase): define storage, authorization, and retention before remote persistence.
}