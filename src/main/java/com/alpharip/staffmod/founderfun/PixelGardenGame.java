// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Core Pixel Garden economy, crop lifecycle, inventory, upgrades and offline rules. */
public final class PixelGardenGame {
    public static final long GROW_TIME_MILLIS = 20L * 60L * 1000L;
    public static final long MAX_OFFLINE_PROGRESS_MILLIS = 2L * 60L * 60L * 1000L;
    public static final int STARTING_COINS = 100;
    public static final int STARTING_PLOTS = 9;
    public static final int MAX_PLOTS = 18;
    public static final int AUTO_HARVEST_COST = 250;
    public static final int GROWTH_BOOST_COST = 150;
    public static final int EXPANSION_COST = 200;
    private static final PixelGardenGame INSTANCE = new PixelGardenGame();

    public enum PlotState { LOCKED, EMPTY, GROWING, READY }
    public enum Crop {
        WHEAT("Wheat", 5, 10),
        CARROT("Carrot", 8, 16),
        POTATO("Potato", 10, 20),
        PUMPKIN("Pumpkin", 20, 45);

        public final String displayName;
        public final int seedPrice;
        public final int sellPrice;
        Crop(String displayName, int seedPrice, int sellPrice) {
            this.displayName = displayName;
            this.seedPrice = seedPrice;
            this.sellPrice = sellPrice;
        }
    }

    public static final class Plot {
        private boolean unlocked;
        private Crop crop;
        private long plantedAt;
        private Plot(boolean unlocked) { this.unlocked = unlocked; }
        public boolean isUnlocked() { return unlocked; }
        public Crop getCrop() { return crop; }
        public PlotState getState(long now) {
            if (!unlocked) return PlotState.LOCKED;
            if (crop == null || plantedAt <= 0L) return PlotState.EMPTY;
            return now - plantedAt >= GROW_TIME_MILLIS ? PlotState.READY : PlotState.GROWING;
        }
        public long getRemainingMillis(long now) {
            return getState(now) == PlotState.GROWING
                ? Math.max(0L, GROW_TIME_MILLIS - (now - plantedAt)) : 0L;
        }
        private void plant(Crop crop, long now) { this.crop = crop; this.plantedAt = now; }
        private void clear() { crop = null; plantedAt = 0L; }
    }

    private final List<Plot> plots = new ArrayList<Plot>();
    private final int[] seeds = new int[Crop.values().length];
    private final int[] inventory = new int[Crop.values().length];
    private int coins = STARTING_COINS;
    private boolean autoHarvest;
    private int growthLevels;
    private long lastProcessedAt = System.currentTimeMillis();

    private PixelGardenGame() {
        for (int i = 0; i < STARTING_PLOTS; i++) plots.add(new Plot(true));
        for (int i = STARTING_PLOTS; i < MAX_PLOTS; i++) plots.add(new Plot(false));
        seeds[Crop.WHEAT.ordinal()] = 3;
    }
    public static PixelGardenGame getInstance() { return INSTANCE; }
    public synchronized int getCoins() { return coins; }
    public synchronized int getSeeds(Crop crop) { return seeds[crop.ordinal()]; }
    public synchronized int getInventory(Crop crop) { return inventory[crop.ordinal()]; }
    public synchronized boolean hasAutoHarvest() { return autoHarvest; }
    public synchronized int getGrowthLevels() { return growthLevels; }
    public synchronized int getUnlockedPlots() {
        int count = 0;
        for (Plot plot : plots) if (plot.unlocked) count++;
        return count;
    }
    public synchronized List<Plot> getPlots() {
        return Collections.unmodifiableList(new ArrayList<Plot>(plots));
    }
    public synchronized boolean buySeeds(Crop crop, int amount) {
        if (crop == null || amount <= 0) return false;
        long cost = (long) crop.seedPrice * amount;
        if (cost > coins || cost > Integer.MAX_VALUE) return false;
        coins -= (int) cost;
        seeds[crop.ordinal()] += amount;
        return true;
    }
    public synchronized boolean plant(int plotIndex, Crop crop, long now) {
        if (crop == null || plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.EMPTY || seeds[crop.ordinal()] <= 0) return false;
        seeds[crop.ordinal()]--;
        plot.plant(crop, now);
        return true;
    }
    public synchronized boolean harvest(int plotIndex, long now) {
        if (plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.READY || plot.crop == null) return false;
        inventory[plot.crop.ordinal()]++;
        plot.clear();
        return true;
    }
    public synchronized boolean sell(Crop crop, int amount) {
        if (crop == null || amount <= 0 || amount > inventory[crop.ordinal()]) return false;
        long earned = (long) crop.sellPrice * amount;
        if (earned > Integer.MAX_VALUE - coins) return false;
        inventory[crop.ordinal()] -= amount;
        coins += (int) earned;
        return true;
    }
    public synchronized boolean sellAll(Crop crop) {
        return crop != null && sell(crop, inventory[crop.ordinal()]);
    }
    public synchronized boolean buyAutoHarvest() {
        if (autoHarvest || coins < AUTO_HARVEST_COST) return false;
        coins -= AUTO_HARVEST_COST;
        autoHarvest = true;
        return true;
    }
    public synchronized boolean buyGrowthBoost() {
        if (growthLevels >= 3) return false;
        int cost = GROWTH_BOOST_COST * (growthLevels + 1);
        if (coins < cost) return false;
        coins -= cost;
        growthLevels++;
        return true;
    }
    public synchronized boolean expandGarden() {
        if (getUnlockedPlots() >= MAX_PLOTS || coins < EXPANSION_COST) return false;
        int unlockCount = Math.min(3, MAX_PLOTS - getUnlockedPlots());
        coins -= EXPANSION_COST;
        int unlocked = 0;
        for (Plot plot : plots) {
            if (!plot.unlocked && unlocked < unlockCount) {
                plot.unlocked = true;
                unlocked++;
            }
        }
        return unlocked > 0;
    }
    public synchronized int getUpgradeCost(String upgrade) {
        if ("auto_harvest".equals(upgrade)) return autoHarvest ? -1 : AUTO_HARVEST_COST;
        if ("growth_boost".equals(upgrade)) return growthLevels >= 3 ? -1 : GROWTH_BOOST_COST * (growthLevels + 1);
        if ("expansion".equals(upgrade)) return getUnlockedPlots() >= MAX_PLOTS ? -1 : EXPANSION_COST;
        return -1;
    }

    /**
     * Advances crops through a capped offline window. Auto Harvest converts ready crops
     * to inventory during that window; without it, crops simply remain ready.
     */
    public synchronized void processTime(long now) {
        if (now <= lastProcessedAt) return;
        long elapsed = Math.min(now - lastProcessedAt, MAX_OFFLINE_PROGRESS_MILLIS);
        long effectiveNow = lastProcessedAt + elapsed;
        if (autoHarvest) {
            for (Plot plot : plots) {
                if (plot.getState(effectiveNow) == PlotState.READY && plot.crop != null) {
                    inventory[plot.crop.ordinal()]++;
                    plot.clear();
                }
            }
        }
        lastProcessedAt = now;
    }
    public synchronized long getLastProcessedAt() { return lastProcessedAt; }

    /** Restore validated state from the account save service. */
    public synchronized void restore(int coinsValue, int[] seedValues, int[] inventoryValues,
            boolean autoHarvestValue, int growthLevelValue, int unlockedCount, Crop[] cropValues,
            long[] plantedAtValues, long savedAt) {
        coins = Math.max(0, coinsValue);
        for (Crop crop : Crop.values()) {
            int i = crop.ordinal();
            seeds[i] = seedValues != null && i < seedValues.length ? Math.max(0, seedValues[i]) : 0;
            inventory[i] = inventoryValues != null && i < inventoryValues.length ? Math.max(0, inventoryValues[i]) : 0;
        }
        autoHarvest = autoHarvestValue;
        growthLevels = Math.max(0, Math.min(3, growthLevelValue));
        int count = Math.max(STARTING_PLOTS, Math.min(MAX_PLOTS, unlockedCount));
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            plot.unlocked = i < count;
            plot.clear();
            if (plot.unlocked && cropValues != null && plantedAtValues != null
                    && i < cropValues.length && i < plantedAtValues.length
                    && cropValues[i] != null && plantedAtValues[i] > 0L) {
                plot.plant(cropValues[i], plantedAtValues[i]);
            }
        }
        lastProcessedAt = savedAt > 0L ? savedAt : System.currentTimeMillis();
        processTime(System.currentTimeMillis());
    }
}
