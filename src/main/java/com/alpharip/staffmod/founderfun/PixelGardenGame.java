// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * First playable Pixel Garden foundation.
 *
 * This model intentionally keeps gameplay rules separate from the GUI so
 * account-backed persistence and additional crops can be added independently.
 */
public final class PixelGardenGame {
    public static final long GROW_TIME_MILLIS = 20L * 60L * 1000L;
    public static final int STARTING_COINS = 100;
    public static final int STARTING_PLOTS = 9;
    public static final int SEED_PRICE = 5;
    public static final int WHEAT_SELL_PRICE = 10;
    private static final PixelGardenGame INSTANCE = new PixelGardenGame();

    public enum PlotState {
        LOCKED, EMPTY, GROWING, READY
    }

    public static final class Plot {
        private boolean unlocked;
        private long plantedAt;

        private Plot(boolean unlocked) {
            this.unlocked = unlocked;
            this.plantedAt = 0L;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public PlotState getState(long now) {
            if (!unlocked) return PlotState.LOCKED;
            if (plantedAt <= 0L) return PlotState.EMPTY;
            return now - plantedAt >= GROW_TIME_MILLIS ? PlotState.READY : PlotState.GROWING;
        }

        public long getRemainingMillis(long now) {
            if (getState(now) != PlotState.GROWING) return 0L;
            return Math.max(0L, GROW_TIME_MILLIS - (now - plantedAt));
        }

        private void plant(long now) {
            plantedAt = now;
        }

        private void clear() {
            plantedAt = 0L;
        }
    }

    private final List<Plot> plots = new ArrayList<Plot>();
    private int coins = STARTING_COINS;
    private int wheatSeeds = 3;
    private int wheatInventory = 0;

    private PixelGardenGame() {
        for (int i = 0; i < STARTING_PLOTS; i++) {
            plots.add(new Plot(true));
        }
    }

    public static PixelGardenGame getInstance() {
        return INSTANCE;
    }

    public synchronized int getCoins() {
        return coins;
    }

    public synchronized int getWheatSeeds() {
        return wheatSeeds;
    }

    public synchronized int getWheatInventory() {
        return wheatInventory;
    }

    public synchronized List<Plot> getPlots() {
        return Collections.unmodifiableList(new ArrayList<Plot>(plots));
    }

    public synchronized boolean buyWheatSeeds(int amount) {
        if (amount <= 0) return false;
        long totalCost = (long) SEED_PRICE * amount;
        if (totalCost > coins) return false;
        coins -= (int) totalCost;
        wheatSeeds += amount;
        return true;
    }

    public synchronized boolean plant(int plotIndex, long now) {
        if (plotIndex < 0 || plotIndex >= plots.size() || wheatSeeds <= 0) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.EMPTY) return false;
        wheatSeeds--;
        plot.plant(now);
        return true;
    }

    public synchronized boolean harvest(int plotIndex, long now) {
        if (plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.READY) return false;
        plot.clear();
        wheatInventory++;
        return true;
    }

    public synchronized boolean sellWheat(int amount) {
        if (amount <= 0 || amount > wheatInventory) return false;
        wheatInventory -= amount;
        coins += amount * WHEAT_SELL_PRICE;
        return true;
    }

}
