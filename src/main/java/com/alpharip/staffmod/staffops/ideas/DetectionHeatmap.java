package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Aggregates supplied event counts by a caller-defined cell key. */
public class DetectionHeatmap {
    private final Map<String, Integer> cellCounts = new LinkedHashMap<String, Integer>();

    public void record(String cellKey) {
        if (cellKey == null || cellKey.trim().isEmpty()) return;
        Integer count = cellCounts.get(cellKey);
        cellCounts.put(cellKey, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
    }

    public Map<String, Integer> getCellCounts() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, Integer>(cellCounts));
    }

    public void clear() { cellCounts.clear(); }
}
