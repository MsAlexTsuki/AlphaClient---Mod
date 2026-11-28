package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Equipment/held-item change log; inventory observation is pending. */
public final class SwapLog {
    public static final class Entry {
        public final String player, slot, item;
        public final long timestamp;
        public Entry(String player, String slot, String item, long timestamp) {
            this.player = player == null ? "" : player; this.slot = slot == null ? "" : slot;
            this.item = item == null ? "" : item; this.timestamp = timestamp;
        }
    }
    private final List<Entry> entries = new ArrayList<Entry>();
    public void record(Entry entry) { if (entry != null) entries.add(entry); }
    public List<Entry> getEntries() { return Collections.unmodifiableList(new ArrayList<Entry>(entries)); }
    public void clear() { entries.clear(); }
}
