package com.alpharip.staffmod.trainers.aimtrainer;

/**
 * Immutable snapshot of a completed Aim Trainer session.
 */
public final class AimTrainerResult {
    private final int hits;
    private final int misses;
    private final double accuracy;
    private final long elapsedMillis;
    private final long heatmapSamples;
    private final long[][] heatmap;

    AimTrainerResult(int hits, int misses, double accuracy,
                     long elapsedMillis, long heatmapSamples, long[][] heatmap) {
        this.hits = hits;
        this.misses = misses;
        this.accuracy = accuracy;
        this.elapsedMillis = elapsedMillis;
        this.heatmapSamples = heatmapSamples;
        this.heatmap = copyGrid(heatmap);
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public int getAttempts() {
        return hits + misses;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public long getHeatmapSamples() {
        return heatmapSamples;
    }

    public int getHeatmapGridSize() {
        return heatmap.length;
    }

    public long getHeatmapCell(int x, int y) {
        if (y < 0 || y >= heatmap.length || x < 0 || x >= heatmap.length) {
            throw new IndexOutOfBoundsException("Heatmap cell is outside the grid.");
        }
        return heatmap[y][x];
    }

    public long[][] getHeatmapSnapshot() {
        return copyGrid(heatmap);
    }

    private static long[][] copyGrid(long[][] source) {
        long[][] copy = new long[source.length][];
        for (int y = 0; y < source.length; y++) {
            copy[y] = source[y].clone();
        }
        return copy;
    }
}
