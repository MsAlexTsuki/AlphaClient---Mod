package com.alpharip.staffmod.remembervillagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory index of remembered villager trade snapshots, keyed by entity UUID. */
public final class VillagerTradeRegistry {
    private final Map<String, VillagerTradeRecord> records =
            new LinkedHashMap<String, VillagerTradeRecord>();

    public synchronized boolean remember(VillagerTradeRecord record) {
        if (record == null) return false;
        VillagerTradeRecord previous = records.get(record.getVillagerId());
        if (previous != null && previous.hasSameTrades(record.getTrades())) return false;
        records.put(record.getVillagerId(), record);
        return true;
    }

    public synchronized VillagerTradeRecord get(String villagerId) {
        return records.get(villagerId);
    }

    public synchronized boolean forget(String villagerId) {
        return records.remove(villagerId) != null;
    }

    public synchronized void clear() {
        records.clear();
    }

    public synchronized int size() {
        return records.size();
    }

    public synchronized List<VillagerTradeRecord> getAll() {
        return Collections.unmodifiableList(
                new ArrayList<VillagerTradeRecord>(records.values()));
    }

    synchronized void replaceAll(Collection<VillagerTradeRecord> values) {
        records.clear();
        if (values == null) return;
        for (VillagerTradeRecord value : values) {
            if (value != null) records.put(value.getVillagerId(), value);
        }
    }
}
