package com.alpharip.staffmod.buildergridandruleroverlay;

import java.util.ArrayList;
import java.util.List;

/** Pure coordinate and grid-line calculations, independent of rendering. */
public final class BuilderGridCalculator {
    private BuilderGridCalculator() { }

    /** Preserves the original idea's nearest-multiple snapping rule. */
    public static double snap(double coordinate, int spacing) {
        int safeSpacing = Math.max(1, Math.min(64, spacing));
        return Math.round(coordinate / safeSpacing) * safeSpacing;
    }

    public static int snap(int coordinate, int spacing) {
        return (int) snap((double) coordinate, spacing);
    }

    /** Returns grid coordinates spanning the requested inclusive world range. */
    public static List<Integer> gridLines(int minimum, int maximum, int spacing) {
        int safeSpacing = Math.max(1, Math.min(64, spacing));
        List<Integer> result = new ArrayList<Integer>();
        int first = (int) (Math.floor((double) minimum / safeSpacing) * safeSpacing);
        for (long coordinate = first; coordinate <= maximum; coordinate += safeSpacing) {
            if (coordinate >= Integer.MIN_VALUE && coordinate <= Integer.MAX_VALUE) {
                result.add((int) coordinate);
            }
            if (coordinate > (long) Integer.MAX_VALUE - safeSpacing) break;
        }
        return result;
    }
}
