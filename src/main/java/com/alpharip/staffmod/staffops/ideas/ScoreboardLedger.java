package com.alpharip.staffmod.staffops.ideas;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Session scoreboard history model; polling and sparkline rendering are pending. */
public final class ScoreboardLedger {
    public static final class Snapshot {
        public final long timestamp;
        public final String key, value;
        public Snapshot(long timestamp, String key, String value) {
            this.timestamp = timestamp; this.key = key == null ? "" : key; this.value = value == null ? "" : value;
        }
    }
    private final List<Snapshot> history = new ArrayList<Snapshot>();
    public void record(Snapshot snapshot) { if (snapshot != null) history.add(snapshot); }
    public List<Snapshot> getHistory() { return Collections.unmodifiableList(new ArrayList<Snapshot>(history)); }
    public void clear() { history.clear(); }
}
