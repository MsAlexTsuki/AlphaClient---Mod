package com.alpharip.staffmod.staffops.ideas;
import java.util.LinkedHashMap;
import java.util.Map;
/** Local opponent win/loss model; persistence and Practice nametag display are pending. */
public final class HeadToHeadRecord {
    public static final class Record {
        private int wins, losses;
        public void addWin() { wins++; }
        public void addLoss() { losses++; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
    }
    private final Map<String, Record> records = new LinkedHashMap<String, Record>();
    public Record getOrCreate(String opponent) {
        if (opponent == null || opponent.trim().isEmpty()) throw new IllegalArgumentException("opponent");
        Record record = records.get(opponent);
        if (record == null) { record = new Record(); records.put(opponent, record); }
        return record;
    }
    public Map<String, Record> getRecords() { return new LinkedHashMap<String, Record>(records); }
}
