package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Creates an in-memory snapshot of supplied investigation fields; no disk or remote backup occurs. */
public final class InvestigationBackup {
    public static final class Snapshot {
        private final long createdAtMillis;
        private final Map<String, String> fields;

        private Snapshot(long createdAtMillis, Map<String, String> fields) {
            this.createdAtMillis = createdAtMillis;
            this.fields = Collections.unmodifiableMap(new LinkedHashMap<String, String>(fields));
        }
        public long getCreatedAtMillis() { return createdAtMillis; }
        public Map<String, String> getFields() { return fields; }
    }

    public Snapshot create(long timestampMillis, Map<String, String> fields) {
        return new Snapshot(timestampMillis,
            fields == null ? Collections.<String, String>emptyMap() : fields);
    }
}