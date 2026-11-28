package com.alpharip.staffmod.buildsymmetryguides;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure coordinate reflection logic; it never places or changes blocks. */
public final class SymmetryCalculator {
    private SymmetryCalculator() { }

    public static List<SymmetryGuidePoint> calculate(BlockCoordinates source,
            BlockCoordinates origin, SymmetryAxis axis) {
        if (source == null || origin == null || axis == null) {
            return new ArrayList<SymmetryGuidePoint>();
        }
        Map<BlockCoordinates, SymmetryGuidePoint> results =
                new LinkedHashMap<BlockCoordinates, SymmetryGuidePoint>();
        if (axis.reflectsX()) add(results, source, origin, true, false, "X");
        if (axis.reflectsZ()) add(results, source, origin, false, true, "Z");
        if (axis == SymmetryAxis.BOTH) add(results, source, origin, true, true, "X + Z");
        return new ArrayList<SymmetryGuidePoint>(results.values());
    }

    private static void add(Map<BlockCoordinates, SymmetryGuidePoint> results,
            BlockCoordinates source, BlockCoordinates origin,
            boolean reflectX, boolean reflectZ, String label) {
        int x = reflectX ? 2 * origin.getX() - source.getX() : source.getX();
        int z = reflectZ ? 2 * origin.getZ() - source.getZ() : source.getZ();
        BlockCoordinates destination = new BlockCoordinates(x, source.getY(), z);
        if (!destination.equals(source) && !results.containsKey(destination)) {
            results.put(destination, new SymmetryGuidePoint(destination, label));
        }
    }
}
