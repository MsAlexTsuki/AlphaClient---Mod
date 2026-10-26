package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores explicitly submitted local investigation session events; does not capture events automatically. */
public final class InvestigationSessionRecorder {
    public static final class Event {
        private final long timestampMillis;
        private final String type;
        private final String detail;

        public Event(long timestampMillis, String type, String detail) {
            this.timestampMillis = timestampMillis;
            this.type = type == null ? "" : type;
            this.detail = detail == null ? "" : detail;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getType() { return type; }
        public String getDetail() { return detail; }
    }

    private final List<Event> events = new ArrayList<Event>();
    private boolean recording;

    public synchronized void start() { recording = true; }
    public synchronized void stop() { recording = false; }
    public synchronized boolean isRecording() { return recording; }

    public synchronized boolean record(long timestampMillis, String type, String detail) {
        if (!recording) return false;
        events.add(new Event(timestampMillis, type, detail));
        return true;
    }

    public synchronized List<Event> getEvents() {
        return Collections.unmodifiableList(new ArrayList<Event>(events));
    }
    public synchronized void clear() { events.clear(); }
}