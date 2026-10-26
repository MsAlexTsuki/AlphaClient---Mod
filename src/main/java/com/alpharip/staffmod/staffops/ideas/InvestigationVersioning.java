package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tracks local named versions of investigation text snapshots. */
public final class InvestigationVersioning {
    public static final class Version {
        private final int number;
        private final long timestampMillis;
        private final String description;
        private final String content;

        private Version(int number, long timestampMillis, String description, String content) {
            this.number = number;
            this.timestampMillis = timestampMillis;
            this.description = description == null ? "" : description;
            this.content = content == null ? "" : content;
        }
        public int getNumber() { return number; }
        public long getTimestampMillis() { return timestampMillis; }
        public String getDescription() { return description; }
        public String getContent() { return content; }
    }

    private final List<Version> versions = new ArrayList<Version>();
    public synchronized Version add(long timestampMillis, String description, String content) {
        Version version = new Version(versions.size() + 1, timestampMillis, description, content);
        versions.add(version);
        return version;
    }
    public synchronized List<Version> getVersions() {
        return Collections.unmodifiableList(new ArrayList<Version>(versions));
    }
    public synchronized Version getLatest() {
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
    }
}