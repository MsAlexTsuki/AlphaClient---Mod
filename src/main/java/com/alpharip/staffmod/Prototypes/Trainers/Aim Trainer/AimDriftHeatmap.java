package com.alpharip.staffmod.trainers.aimtrainer;

import java.util.Arrays;

/**
 * Aggregates normalized aim offsets into grid cells.
 * Actual mouse/player sampling and rendering are separate.
 */
public final class AimDriftHeatmap {
    public static final String NAME = "Aim Drift Heatmap";
    public static final String CATEGORY = "Practice";
    public static final String STATUS = "NEW";
    public static final String REQUIRED_ACCESS = "Unconfirmed";

    private final int gridSize;
    private final long[][] cells;
    private long samples;

    public AimDriftHeatmap() {
        this(9);
    }

    public AimDriftHeatmap(int gridSize) {
        if (gridSize < 3 || gridSize % 2 == 0) {
            throw new IllegalArgumentException("grid size must be odd and >= 3");
        }

        this.gridSize = gridSize;
        this.cells = new long[gridSize][gridSize];
    }

    public synchronized void record(double horizontal, double vertical) {
        if (Double.isNaN(horizontal) || Double.isNaN(vertical)
                || Double.isInfinite(horizontal) || Double.isInfinite(vertical)) {
            throw new IllegalArgumentException("offset must be finite");
        }

        int x = cell(horizontal);
        int y = cell(vertical);
        cells[y][x]++;
        samples++;
    }

    private int cell(double value) {
        double normalized = Math.max(-1, Math.min(1, value));
        return Math.min(gridSize - 1,
                (int) Math.round((normalized + 1) * (gridSize - 1) / 2.0));
    }

    public synchronized long getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= gridSize || y >= gridSize) {
            throw new IndexOutOfBoundsException();
        }
        return cells[y][x];
    }

    public synchronized long getSamples() {
        return samples;
    }

    public synchronized long[][] snapshot() {
        long[][] copy = new long[gridSize][gridSize];
        for (int y = 0; y < gridSize; y++) {
            copy[y] = cells[y].clone();
        }
        return copy;
    }

    public synchronized void clear() {
        for (long[] row : cells) {
            Arrays.fill(row, 0);
        }
        samples = 0;
    }
}
