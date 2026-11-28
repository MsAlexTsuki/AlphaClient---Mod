package com.alpharip.staffmod.predictions.armor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable scan result containing the armor pieces below the warning threshold. */
public final class ArmorDurabilityWarningResult {
    private final List<ArmorDurabilityWarningPiece> lowDurabilityPieces;

    ArmorDurabilityWarningResult(List<ArmorDurabilityWarningPiece> pieces) {
        this.lowDurabilityPieces = Collections.unmodifiableList(
                new ArrayList<ArmorDurabilityWarningPiece>(pieces));
    }

    public List<ArmorDurabilityWarningPiece> getLowDurabilityPieces() {
        return lowDurabilityPieces;
    }

    public int getLowDurabilityCount() {
        return lowDurabilityPieces.size();
    }

    public boolean hasWarnings() {
        return !lowDurabilityPieces.isEmpty();
    }
}
