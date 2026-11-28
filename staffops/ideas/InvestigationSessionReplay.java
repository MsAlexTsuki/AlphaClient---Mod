package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cursor model for reviewing a supplied session event list; it does not render a replay. */
public final class InvestigationSessionReplay {
    private List<InvestigationSessionRecorder.Event> events = Collections.emptyList();
    private int cursor;

    public synchronized void load(List<InvestigationSessionRecorder.Event> source) {
        events = source == null ? Collections.<InvestigationSessionRecorder.Event>emptyList()
            : Collections.unmodifiableList(new ArrayList<InvestigationSessionRecorder.Event>(source));
        cursor = 0;
    }

    public synchronized InvestigationSessionRecorder.Event current() {
        return cursor >= 0 && cursor < events.size() ? events.get(cursor) : null;
    }
    public synchronized InvestigationSessionRecorder.Event next() {
        if (cursor < events.size()) cursor++;
        return current();
    }
    public synchronized InvestigationSessionRecorder.Event previous() {
        if (cursor > 0) cursor--;
        return current();
    }
    public synchronized int getCursor() { return cursor; }
    public synchronized int size() { return events.size(); }
}