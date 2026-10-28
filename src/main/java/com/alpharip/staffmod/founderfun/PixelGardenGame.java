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
    public static final int[] MORE_COINS_PRICES = {500, 700, 1000, 1500, 2000};
    public static final double[] MORE_COINS_MULTIPLIERS = {1.1D, 1.3D, 1.5D, 1.8D, 2.0D};
    public static final int[] MORE_CROPS_PRICES = {750, 1000, 1250, 1500, 1750, 2000};
    public static final int PRESTIGE_COST = 20000;
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
        private long normalPlantedAt;
        private long normalGrowDurationMillis;
        private boolean harvestTimeOverridden;
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
            clearHarvestTimeOverride();
        }
        private void clearHarvestTimeOverride() {
            normalPlantedAt = 0L;
            normalGrowDurationMillis = 0L;
            harvestTimeOverridden = false;
        }
        private void clear() {
            crop = null;
            plantedAt = 0L;
            growDurationMillis = GROW_TIME_MILLIS;
            clearHarvestTimeOverride();
        }
    }

    private final List<Plot> plots = new ArrayList<Plot>();
    private final int[] seeds = new int[Crop.values().length];
    private final int[] inventory = new int[Crop.values().length];
    private int pixelGardenUpgradeCoins = STARTING_COINS;
    private boolean autoHarvest;
    private int growthLevels;
    private int moreCoinsLevel;
    private int moreCropsLevel;
    private int resetTokens;
    private int purchasedUpgradeSteps;
    private long lastProcessedAt = System.currentTimeMillis();
    private final PixelGardenProgressionData progressionData = new PixelGardenProgressionData();

    private PixelGardenGame() {
        for (int i = 0; i < STARTING_PLOTS; i++) plots.add(new Plot(true));
        for (int i = STARTING_PLOTS; i < MAX_PLOTS; i++) plots.add(new Plot(false));
        seeds[Crop.WHEAT.ordinal()] = 3;
    }
    public synchronized void resetToDefaults() {
        pixelGardenUpgradeCoins = STARTING_COINS;
        autoHarvest = false;
        growthLevels = 0;
        moreCoinsLevel = 0;
        moreCropsLevel = 0;
        purchasedUpgradeSteps = 0;
        progressionData.reset();
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
    public synchronized PixelGardenProgressionData getProgressionData() { return progressionData; }
    public synchronized int getCoins() { return pixelGardenUpgradeCoins; }
    public synchronized int getResetTokens() { return resetTokens; }
    public synchronized int getSeeds(Crop crop) { return seeds[crop.ordinal()]; }
    public synchronized int getInventory(Crop crop) { return inventory[crop.ordinal()]; }
    public synchronized boolean hasAutoHarvest() { return autoHarvest; }
    public synchronized int getGrowthLevels() { return growthLevels; }
    public synchronized int getMoreCoinsLevel() { return moreCoinsLevel; }
    public synchronized int getMoreCropsLevel() { return moreCropsLevel; }
    public synchronized int getPurchasedUpgradeSteps() { return purchasedUpgradeSteps; }
    public synchronized double getCoinMultiplier() { return moreCoinsLevel <= 0 ? 1.0D : MORE_COINS_MULTIPLIERS[Math.min(moreCoinsLevel, MORE_COINS_MULTIPLIERS.length) - 1]; }
    public synchronized int getCropYield() { return moreCropsLevel + 1; }
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
        String cropId = crop.name().toLowerCase(java.util.Locale.ROOT);
        progressionData.recordActionProgress("BUY_SEEDS", cropId, amount);
        progressionData.recordTutorialAction("BUY_SEEDS", cropId);
        return true;
    }
    public synchronized boolean plant(int plotIndex, Crop crop, long now) {
        if (crop == null || plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.EMPTY || seeds[crop.ordinal()] <= 0) return false;
        seeds[crop.ordinal()]--;
        plot.plant(crop, now, growthLevels);
        String cropId = crop.name().toLowerCase(java.util.Locale.ROOT);
        progressionData.recordActionProgress("PLANT_CROP", cropId, 1);
        progressionData.recordTutorialAction("PLANT_CROP", cropId);
        return true;
    }
    public synchronized boolean harvest(int plotIndex, long now) {
        if (plotIndex < 0 || plotIndex >= plots.size()) return false;
        Plot plot = plots.get(plotIndex);
        if (plot.getState(now) != PlotState.READY || plot.crop == null) return false;
        Crop harvestedCrop = plot.crop;
        int harvestedAmount = getCropYield();
        inventory[harvestedCrop.ordinal()] += harvestedAmount;
        plot.clear();
        String cropId = harvestedCrop.name().toLowerCase(java.util.Locale.ROOT);
        progressionData.recordActionProgress("HARVEST_CROP", cropId, harvestedAmount);
        progressionData.recordTutorialAction("HARVEST_CROP", cropId);
        return true;
    }

    /** Accelerates growing crops while retaining their original timers for a later normal reset. */
    public synchronized int accelerateHarvestTimes(double multiplier, boolean finish, long now) {
        int changed = 0;
        for (Plot plot : plots) {
            if (plot.getState(now) != PlotState.GROWING) continue;
            if (!plot.harvestTimeOverridden) {
                plot.normalPlantedAt = plot.plantedAt;
                plot.normalGrowDurationMillis = plot.growDurationMillis;
                plot.harvestTimeOverridden = true;
            }
            long remaining = plot.getRemainingMillis(now);
            long newRemaining = finish ? 0L : Math.max(1L, (long) Math.ceil(remaining / multiplier));
            plot.plantedAt = now - Math.max(0L, plot.growDurationMillis - newRemaining);
            changed++;
        }
        return changed;
    }

    /** Restores the saved pre-test timer for every crop whose harvest time was overridden. */
    public synchronized int restoreNormalHarvestTimes(long now) {
        int restored = 0;
        for (Plot plot : plots) {
            if (!plot.harvestTimeOverridden || plot.crop == null || plot.normalPlantedAt <= 0L
                    || plot.normalGrowDurationMillis <= 0L) continue;
            plot.plantedAt = plot.normalPlantedAt;
            plot.growDurationMillis = plot.normalGrowDurationMillis;
            plot.clearHarvestTimeOverride();
            restored++;
        }
        return restored;
    }
    public synchronized boolean sell(Crop crop, int amount) {
        if (crop == null || amount <= 0 || amount > inventory[crop.ordinal()]) return false;
        long earned = (long) Math.floor((double) crop.sellPrice * amount * getCoinMultiplier());
        if (earned > Integer.MAX_VALUE - pixelGardenUpgradeCoins) return false;
        inventory[crop.ordinal()] -= amount;
        pixelGardenUpgradeCoins += (int) earned;
        String cropId = crop.name().toLowerCase(java.util.Locale.ROOT);
        progressionData.recordActionProgress("SELL_CROP", cropId, amount);
        progressionData.recordTutorialAction("SELL_CROP", cropId);
        return true;
    }
    /** Removes harvested crops for an explicit quest submission, never for merely owning them. */
    public synchronized boolean consumeHarvestedCrop(Crop crop, int amount) {
        if (crop == null || amount <= 0 || amount > inventory[crop.ordinal()]) return false;
        inventory[crop.ordinal()] -= amount;
        return true;
    }

    /** Grants supported quest rewards atomically after validating all reward keys and bounds. */
    public synchronized boolean grantQuestRewards(java.util.Map<String, Integer> rewards) {
        if (rewards == null || rewards.isEmpty()) return false;
        long newCoins = pixelGardenUpgradeCoins;
        int[] newSeeds = seeds.clone();
        int[] newInventory = inventory.clone();
        for (java.util.Map.Entry<String, Integer> entry : rewards.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase(java.util.Locale.ROOT);
            Integer boxed = entry.getValue();
            if (boxed == null || boxed.intValue() <= 0) return false;
            int amount = boxed.intValue();
            if ("coins".equals(key)) {
                newCoins += amount;
                if (newCoins > Integer.MAX_VALUE) return false;
            } else if (key.startsWith("seeds_") || key.startsWith("crop_")) {
                boolean seedReward = key.startsWith("seeds_");
                String cropName = key.substring(seedReward ? 6 : 5);
                Crop rewardCrop;
                try { rewardCrop = Crop.valueOf(cropName.toUpperCase(java.util.Locale.ROOT)); }
                catch (IllegalArgumentException exception) { return false; }
                int[] target = seedReward ? newSeeds : newInventory;
                if (amount > Integer.MAX_VALUE - target[rewardCrop.ordinal()]) return false;
                target[rewardCrop.ordinal()] += amount;
            } else {
                return false;
            }
        }
        pixelGardenUpgradeCoins = (int) newCoins;
        System.arraycopy(newSeeds, 0, seeds, 0, seeds.length);
        System.arraycopy(newInventory, 0, inventory, 0, inventory.length);
        return true;
    }

    public synchronized boolean sellAll(Crop crop) {
        return crop != null && sell(crop, inventory[crop.ordinal()]);
    }
    public synchronized boolean buyAutoHarvest() {
        if (autoHarvest || pixelGardenUpgradeCoins < AUTO_HARVEST_COST) return false;
        pixelGardenUpgradeCoins -= AUTO_HARVEST_COST;
        autoHarvest = true;
        purchasedUpgradeSteps++;
        progressionData.recordActionProgress("BUY_UPGRADE", "auto_harvest", 1);
        progressionData.recordTutorialAction("BUY_UPGRADE", "auto_harvest");
        return true;
    }
    public synchronized boolean buyGrowthBoost() {
        if (growthLevels >= 3) return false;
        int cost = GROWTH_BOOST_COST * (growthLevels + 1);
        if (pixelGardenUpgradeCoins < cost) return false;
        pixelGardenUpgradeCoins -= cost;
        growthLevels++;
        purchasedUpgradeSteps++;
        progressionData.recordActionProgress("BUY_UPGRADE", "growth_boost", 1);
        progressionData.recordTutorialAction("BUY_UPGRADE", "growth_boost");
        return true;
    }
    public synchronized boolean buyMoreCoins() {
        if (moreCoinsLevel >= MORE_COINS_PRICES.length) return false;
        int cost = MORE_COINS_PRICES[moreCoinsLevel];
        if (pixelGardenUpgradeCoins < cost) return false;
        pixelGardenUpgradeCoins -= cost;
        moreCoinsLevel++;
        purchasedUpgradeSteps++;
        progressionData.recordActionProgress("BUY_UPGRADE", "more_coins", 1);
        progressionData.recordTutorialAction("BUY_UPGRADE", "more_coins");
        return true;
    }
    public synchronized boolean buyMoreCrops() {
        if (moreCropsLevel >= MORE_CROPS_PRICES.length) return false;
        int cost = MORE_CROPS_PRICES[moreCropsLevel];
        if (pixelGardenUpgradeCoins < cost) return false;
        pixelGardenUpgradeCoins -= cost;
        moreCropsLevel++;
        purchasedUpgradeSteps++;
        progressionData.recordActionProgress("BUY_UPGRADE", "more_crops", 1);
        progressionData.recordTutorialAction("BUY_UPGRADE", "more_crops");
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
        if (unlocked > 0) {
            purchasedUpgradeSteps++;
            progressionData.recordActionProgress("EXPAND_GARDEN", "*", unlocked);
            progressionData.recordTutorialAction("EXPAND_GARDEN", "*");
        }
        return unlocked > 0;
    }
    public synchronized int getUpgradeCost(String upgrade) {
        if ("auto_harvest".equals(upgrade)) return autoHarvest ? -1 : AUTO_HARVEST_COST;
        if ("growth_boost".equals(upgrade)) return growthLevels >= 3 ? -1 : GROWTH_BOOST_COST * (growthLevels + 1);
        if ("more_coins".equals(upgrade)) return moreCoinsLevel >= MORE_COINS_PRICES.length ? -1 : MORE_COINS_PRICES[moreCoinsLevel];
        if ("more_crops".equals(upgrade)) return moreCropsLevel >= MORE_CROPS_PRICES.length ? -1 : MORE_CROPS_PRICES[moreCropsLevel];
        if ("prestige".equals(upgrade)) return PRESTIGE_COST;
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
                    inventory[plot.crop.ordinal()] += getCropYield();
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
        root.addProperty("version", 2);
        root.addProperty("pixelGardenUpgradeCoins", pixelGardenUpgradeCoins);
        root.addProperty("auto_harvest", autoHarvest);
        root.addProperty("growth_levels", growthLevels);
        root.addProperty("more_coins_level", moreCoinsLevel);
        root.addProperty("more_crops_level", moreCropsLevel);
        root.addProperty("reset_tokens", resetTokens);
        root.addProperty("purchased_upgrade_steps", purchasedUpgradeSteps);
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
            savedPlot.addProperty("normal_planted_at", plot.normalPlantedAt);
            savedPlot.addProperty("normal_grow_duration", plot.normalGrowDurationMillis);
            savedPlot.addProperty("harvest_time_overridden", plot.harvestTimeOverridden);
            plotArray.add(savedPlot);
        }
        root.add("plots", plotArray);
        root.add("progression", progressionData.toJson());
        return root;
    }

    /** Restores a save after validating every value and then applies capped offline progress. */
    public synchronized void loadSaveData(JsonObject root) {
        if (root == null) return;
        progressionData.loadJson(root.get("progression"));
        int savedCoins = root.has("pixelGardenUpgradeCoins") ? root.get("pixelGardenUpgradeCoins").getAsInt() : STARTING_COINS;
        int savedGrowth = root.has("growth_levels") ? root.get("growth_levels").getAsInt() : 0;
        int savedMoreCoins = root.has("more_coins_level") ? Math.max(0, Math.min(MORE_COINS_PRICES.length, root.get("more_coins_level").getAsInt())) : 0;
        int savedMoreCrops = root.has("more_crops_level") ? Math.max(0, Math.min(MORE_CROPS_PRICES.length, root.get("more_crops_level").getAsInt())) : 0;
        int savedResetTokens = root.has("reset_tokens") ? Math.max(0, root.get("reset_tokens").getAsInt()) : 0;
        int savedPurchasedSteps = root.has("purchased_upgrade_steps") ? Math.max(0, root.get("purchased_upgrade_steps").getAsInt()) : 0;
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
        long[] normalPlanted = new long[plots.size()];
        long[] normalDurations = new long[plots.size()];
        boolean[] timerOverridden = new boolean[plots.size()];
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
            if (savedPlot.has("normal_planted_at")) normalPlanted[i] = Math.max(0L, savedPlot.get("normal_planted_at").getAsLong());
            if (savedPlot.has("normal_grow_duration")) normalDurations[i] = Math.max(0L, savedPlot.get("normal_grow_duration").getAsLong());
            timerOverridden[i] = savedPlot.has("harvest_time_overridden")
                    && savedPlot.get("harvest_time_overridden").getAsBoolean();
        }
        moreCoinsLevel = savedMoreCoins;
        moreCropsLevel = savedMoreCrops;
        restore(savedCoins, savedSeeds, savedInventory, savedAuto, savedGrowth,
                Math.max(unlocked, restoredUnlocked), crops, planted, durations, savedAt);
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            if (timerOverridden[i] && plot.crop != null && normalPlanted[i] > 0L && normalDurations[i] > 0L) {
                plot.normalPlantedAt = normalPlanted[i];
                plot.normalGrowDurationMillis = normalDurations[i];
                plot.harvestTimeOverridden = true;
            }
        }
        resetTokens = savedResetTokens;
        purchasedUpgradeSteps = savedPurchasedSteps;
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
