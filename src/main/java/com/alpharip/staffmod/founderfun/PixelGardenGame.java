// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Core Pixel Garden economy, crop lifecycle, inventory, upgrades and offline rules. */
public final class PixelGardenGame {
    public static final long GROW_TIME_MILLIS = 20L * 60L * 1000L;
    public static final long MAX_OFFLINE_PROGRESS_MILLIS = 2L * 60L * 60L * 1000L;
    public static final int STARTING_COINS = 100;
    public static final int STARTING_PLOTS = 1;
    public static final int MAX_PLOTS = 28;
    public static final int AUTO_HARVEST_COST = 2000;
    public static final int GROWTH_BOOST_COST = 1250;
    public static final int EXPANSION_COST = 50;
    public static final int[] EXPANSION_PRICES = {50, 100, 150, 250, 300, 400, 450, 500, 600, 700, 800, 900, 1000, 1150, 1300, 1500, 2000, 2500, 3000, 4000, 5000, 5500, 5600, 5700, 5800, 5900, 6000};
    private static final PixelGardenGame INSTANCE = new PixelGardenGame();

    public enum PlotState { LOCKED, EMPTY, GROWING, READY }
    public enum Crop {
        WHEAT("Wheat", 5, 8, 10L * 60L * 1000L),
        POTATO("Potato", 10, 12, 20L * 60L * 1000L),
        CARROT("Carrot", 8, 16, 30L * 60L * 1000L),
        PUMPKIN("Pumpkin", 20, 30, 40L * 60L * 1000L);

        public final String displayName;
        public final int seedPrice;
        public final int sellPrice;
        public final long growDurationMillis;
        Crop(String displayName, int seedPrice, int sellPrice, long growDurationMillis) {
            this.displayName = displayName;
            this.seedPrice = seedPrice;
            this.sellPrice = sellPrice;
            this.growDurationMillis = growDurationMillis;
        }
    }

    public static final class Plot {
        private boolean unlocked;
        private Crop crop;
        private long plantedAt;
        private long growDurationMillis = GROW_TIME_MILLIS;
        private Plot(boolean unlocked) { this.unlocked = unlocked; }
        public boolean isUnlocked() { return unlocked; }
        public Crop getCrop() { return crop; }
        public PlotState getState(long now) {
            if (!unlocked) return PlotState.LOCKED;
            if (crop == null || plantedAt <= 0L) return PlotState.EMPTY;
            return now - plantedAt >= growDurationMillis ? PlotState.READY : PlotState.GROWING;
        }
        public long getRemainingMillis(long now) {
            return getState(now) == PlotState.GROWING
                ? Math.max(0L, growDurationMillis - (now - plantedAt)) : 0L;
        }
        private void plant(Crop crop, long now, int growthLevels) {
            this.crop = crop;
            this.plantedAt = now;
            this.growDurationMillis = Math.max(5L * 60L * 1000L,
                    crop.growDurationMillis * (100L - 15L * growthLevels) / 100L);
        }
        private void clear() { crop = null; plantedAt = 0L; growDurationMillis = GROW_TIME_MILLIS; }
    }

    private final List<Plot> plots = new ArrayList<Plot>();
    private final int[] seeds = new int[Crop.values().length];
    private final int[] inventory = new int[Crop.values().length];
    private int pixelGardenUpgradeCoins = STARTING_COINS;
    private boolean autoHarvest;
    private int growthLevels;
    private long lastProcessedAt = System.currentTimeMillis();

    private PixelGardenGame() {
        for (int i = 0; i < STARTING_PLOTS; i++) plots.add(new Plot(true));
        for (int i = STARTING_PLOTS; i < MAX_PLOTS; i++) plots.add(new Plot(false));
        seeds[Crop.WHEAT.ordinal()] = 3;
    }
    public synchronized void resetToDefaults() {
        pixelGardenUpgradeCoins = STARTING_COINS;
        autoHarvest = false;
        growthLevels = 0;
        lastProcessedAt = System.currentTimeMillis();
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = 0;
            inventory[i] = 0;
        }
        seeds[Crop.WHEAT.ordinal()] = 3;
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            plot.unlocked = i < STARTING_PLOTS;
            plot.clear();
        }
    }
    public static PixelGardenGame getInstance() { return INSTANCE; }
    public synchronized int getCoins() { return pixelGardenUpgradeCoins; }
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
        if (cost > pixelGardenUpgradeCoins || cost > Integer.MAX_VALUE) return false;
        pixelGardenUpgradeCoins -= (int) cost;
        seeds[crop.ordinal()] += amount;
        return true;
    }
    public synchronized boolean plant(int plotIndex, Crop crop, long now) {
        if (crop == null || plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.EMPTY || seeds[crop.ordinal()] <= 0) return false;
        seeds[crop.ordinal()]--;
        plot.plant(crop, now, growthLevels);
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
        if (earned > Integer.MAX_VALUE - pixelGardenUpgradeCoins) return false;
        inventory[crop.ordinal()] -= amount;
        pixelGardenUpgradeCoins += (int) earned;
        return true;
    }
    public synchronized boolean sellAll(Crop crop) {
        return crop != null && sell(crop, inventory[crop.ordinal()]);
    }
    public synchronized boolean buyAutoHarvest() {
        if (autoHarvest || pixelGardenUpgradeCoins < AUTO_HARVEST_COST) return false;
        pixelGardenUpgradeCoins -= AUTO_HARVEST_COST;
        autoHarvest = true;
        return true;
    }
    public synchronized boolean buyGrowthBoost() {
        if (growthLevels >= 3) return false;
        int cost = GROWTH_BOOST_COST * (growthLevels + 1);
        if (pixelGardenUpgradeCoins < cost) return false;
        pixelGardenUpgradeCoins -= cost;
        growthLevels++;
        return true;
    }
    public synchronized boolean expandGarden() {
        int expansionCost = getUpgradeCost("expansion");
        if (expansionCost < 0 || pixelGardenUpgradeCoins < expansionCost) return false;
        int unlockCount = Math.min(1, MAX_PLOTS - getUnlockedPlots());
        pixelGardenUpgradeCoins -= expansionCost;
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
        if ("expansion".equals(upgrade)) {
            int bought = getUnlockedPlots() - STARTING_PLOTS;
            return bought >= EXPANSION_PRICES.length ? -1 : EXPANSION_PRICES[bought];
        }
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
    /** Creates a portable account-save payload; never includes auth/session secrets. */
    public synchronized JsonObject toSaveData() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        root.addProperty("pixelGardenUpgradeCoins", pixelGardenUpgradeCoins);
        root.addProperty("auto_harvest", autoHarvest);
        root.addProperty("growth_levels", growthLevels);
        root.addProperty("unlocked_plots", getUnlockedPlots());
        root.addProperty("last_processed_at", lastProcessedAt);
        JsonArray seedArray = new JsonArray();
        JsonArray inventoryArray = new JsonArray();
        for (Crop crop : Crop.values()) {
            seedArray.add(new JsonPrimitive(seeds[crop.ordinal()]));
            inventoryArray.add(new JsonPrimitive(inventory[crop.ordinal()]));
        }
        root.add("seeds", seedArray);
        root.add("inventory", inventoryArray);
        JsonArray plotArray = new JsonArray();
        for (Plot plot : plots) {
            JsonObject savedPlot = new JsonObject();
            savedPlot.addProperty("unlocked", plot.unlocked);
            if (plot.crop != null) savedPlot.addProperty("crop", plot.crop.name());
            savedPlot.addProperty("planted_at", plot.plantedAt);
            savedPlot.addProperty("grow_duration", plot.growDurationMillis);
            plotArray.add(savedPlot);
        }
        root.add("plots", plotArray);
        return root;
    }

    /** Restores a save after validating every value and then applies capped offline progress. */
    public synchronized void loadSaveData(JsonObject root) {
        if (root == null) return;
        int savedCoins = root.has("pixelGardenUpgradeCoins") ? root.get("pixelGardenUpgradeCoins").getAsInt() : STARTING_COINS;
        int savedGrowth = root.has("growth_levels") ? root.get("growth_levels").getAsInt() : 0;
        boolean savedAuto = root.has("auto_harvest") && root.get("auto_harvest").getAsBoolean();
        int unlocked = root.has("unlocked_plots") ? root.get("unlocked_plots").getAsInt() : STARTING_PLOTS;
        long savedAt = root.has("last_processed_at") ? root.get("last_processed_at").getAsLong() : System.currentTimeMillis();
        int[] savedSeeds = readIntArray(root.get("seeds"), Crop.values().length);
        int[] savedInventory = readIntArray(root.get("inventory"), Crop.values().length);
        JsonArray savedPlots = root.has("plots") && root.get("plots").isJsonArray()
                ? root.getAsJsonArray("plots") : new JsonArray();
        Crop[] crops = new Crop[plots.size()];
        long[] planted = new long[plots.size()];
        long[] durations = new long[plots.size()];
        int restoredUnlocked = 0;
        for (int i = 0; i < Math.min(savedPlots.size(), plots.size()); i++) {
            JsonElement element = savedPlots.get(i);
            if (!element.isJsonObject()) continue;
            JsonObject savedPlot = element.getAsJsonObject();
            if (savedPlot.has("unlocked") && savedPlot.get("unlocked").getAsBoolean()) restoredUnlocked++;
            if (savedPlot.has("crop")) {
                try { crops[i] = Crop.valueOf(savedPlot.get("crop").getAsString()); }
                catch (IllegalArgumentException ignored) { crops[i] = null; }
            }
            if (savedPlot.has("planted_at")) planted[i] = Math.max(0L, savedPlot.get("planted_at").getAsLong());
            if (savedPlot.has("grow_duration")) durations[i] = Math.max(5L * 60L * 1000L, savedPlot.get("grow_duration").getAsLong());
        }
        restore(savedCoins, savedSeeds, savedInventory, savedAuto, savedGrowth,
                Math.max(unlocked, restoredUnlocked), crops, planted, durations, savedAt);
    }

    private static int[] readIntArray(JsonElement element, int length) {
        int[] result = new int[length];
        if (element == null || !element.isJsonArray()) return result;
        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < Math.min(length, array.size()); i++) {
            try { result[i] = Math.max(0, array.get(i).getAsInt()); }
            catch (RuntimeException ignored) { result[i] = 0; }
        }
        return result;
    }


    /** Restore validated state from the account save service. */
    public synchronized void restore(int coinsValue, int[] seedValues, int[] inventoryValues,
            boolean autoHarvestValue, int growthLevelValue, int unlockedCount, Crop[] cropValues,
            long[] plantedAtValues, long[] growDurationValues, long savedAt) {
        pixelGardenUpgradeCoins = Math.max(0, coinsValue);
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
                plot.plant(cropValues[i], plantedAtValues[i], growthLevels);
                if (growDurationValues != null && i < growDurationValues.length && growDurationValues[i] > 0L) {
                    plot.growDurationMillis = Math.max(5L * 60L * 1000L, growDurationValues[i]);
                }
            }
        }
        lastProcessedAt = savedAt > 0L ? savedAt : System.currentTimeMillis();
        processTime(System.currentTimeMillis());
    }
}
