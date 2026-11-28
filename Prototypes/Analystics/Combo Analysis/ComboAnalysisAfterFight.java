package com.alpharip.staffmod.analystics.comboanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Analyzes ordered hit timestamps supplied by combat-event hooks. */
public class ComboAnalysisAfterFight {
    private final List<Long> hits = new ArrayList<Long>();

    public void recordHit(long time) {
        if (time < 0 || (!hits.isEmpty() && time < hits.get(hits.size() - 1))) {
            throw new IllegalArgumentException("Hit times must be non-negative and ordered.");
        }
        hits.add(time);
    }

    public int getHitCount() {
        return hits.size();
    }

    public List<Long> getHitTimes() {
        return Collections.unmodifiableList(new ArrayList<Long>(hits));
    }

    public int longestCombo(long maxGap) {
        if (maxGap < 0) {
            throw new IllegalArgumentException();
        }

        int best = hits.isEmpty() ? 0 : 1;
        int current = best;

        for (int i = 1; i < hits.size(); i++) {
            if (hits.get(i) - hits.get(i - 1) <= maxGap) {
                current++;
            } else {
                current = 1;
            }
            best = Math.max(best, current);
        }

        return best;
    }

    public void clear() {
        hits.clear();
    }
}