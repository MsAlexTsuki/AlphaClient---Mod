// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Pixel Garden screen with crops, inventory, upgrades and account-linked cloud saving. */
public final class PixelGardenGUI extends GuiScreen {
    private static final int CROP_BUTTON_BASE = 10;
    private static final int BUY_SEED_BASE = 20;
    private static final int PLOT_BUTTON_BASE = 100;
    private static final int SELL_BUTTON_BASE = 200;
    private static final int AUTO_HARVEST_BUTTON = 300;
    private static final int GROWTH_BUTTON = 301;
    private static final int EXPAND_BUTTON = 302;
    private static final int BACK_BUTTON = 4;
    private static String loadedAccount = null;
    private static boolean loadInProgress = false;

    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private PixelGardenGame.Crop selectedCrop = PixelGardenGame.Crop.WHEAT;
    private volatile String cloudStatus = "Cloud save: checking account...";
    private volatile boolean loadingCloud = false;

    public PixelGardenGUI(GuiScreen parent) {
        this.parent = parent;
        loadCloudSaveIfNeeded();
    }

    private void loadCloudSaveIfNeeded() {
        if (!SupabaseCommunityService.isSignedIn()) {
            cloudStatus = "Cloud save unavailable: sign in to your Alpha Account.";
            return;
        }
        final String account = SupabaseCommunityService.getSignedInEmail();
        synchronized (PixelGardenGUI.class) {
            if (account != null && account.equals(loadedAccount)) {
                cloudStatus = "Cloud save: connected to " + account;
                return;
            }
            if (loadInProgress) {
                cloudStatus = "Cloud save: loading account data...";
                return;
            }
            loadInProgress = true;
        }
        loadingCloud = true;
        cloudStatus = "Cloud save: loading account data...";
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    JsonObject saved = SupabaseCommunityService.loadPixelGardenSave();
                    if (saved != null) game.loadSaveData(saved);
                    synchronized (PixelGardenGUI.class) { loadedAccount = account; }
                    cloudStatus = saved == null
                            ? "Cloud save: new garden linked to " + account
                            : "Cloud save: synced with " + account;
                } catch (Exception exception) {
                    cloudStatus = "Cloud save failed: " + safeMessage(exception);
                    System.err.println("[AlphaClient] Pixel Garden load failed: " + exception.getMessage());
                } finally {
                    loadingCloud = false;
                    synchronized (PixelGardenGUI.class) { loadInProgress = false; }
                    if (cloudStatus.startsWith("Cloud save: new garden")) saveCloudAsync();
                }
            }
        }, "AlphaClient-PixelGardenLoad");
        thread.setDaemon(true);
        thread.start();
    }

    private void saveCloudAsync() {
        if (!SupabaseCommunityService.isSignedIn() || loadingCloud) return;
        final String account = SupabaseCommunityService.getSignedInEmail();
        final JsonObject snapshot = game.toSaveData();
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (!SupabaseCommunityService.isSignedIn()
                            || (account != null && !account.equals(SupabaseCommunityService.getSignedInEmail()))) return;
                    SupabaseCommunityService.savePixelGardenSave(snapshot);
                    cloudStatus = "Cloud save: synced with " + account;
                } catch (Exception exception) {
                    cloudStatus = "Cloud save failed: " + safeMessage(exception);
                    System.err.println("[AlphaClient] Pixel Garden save failed: " + exception.getMessage());
                }
            }
        }, "AlphaClient-PixelGardenSave");
        thread.setDaemon(true);
        thread.start();
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty() ? "unknown error" : message;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        long now = System.currentTimeMillis();
        game.processTime(now);
        int gap = 5;
        int plotWidth = Math.max(72, Math.min(96, (width - 30 - gap * 2) / 3));
        int plotHeight = 25;
        int gridWidth = plotWidth * 3 + gap * 2;
        int startX = (width - gridWidth) / 2;
        int startY = 137;
        List<PixelGardenGame.Plot> plots = game.getPlots();

        int cropButtonWidth = Math.min(100, (gridWidth - gap * 3) / 4);
        int cropX = (width - (cropButtonWidth * 4 + gap * 3)) / 2;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            String prefix = selectedCrop == crop ? "> " : "";
            buttonList.add(new AlphaButton(CROP_BUTTON_BASE + crop.ordinal(),
                    cropX + crop.ordinal() * (cropButtonWidth + gap), 91, cropButtonWidth, 20,
                    prefix + crop.displayName + " (" + game.getSeeds(crop) + ")"));
            buttonList.add(new AlphaButton(BUY_SEED_BASE + crop.ordinal(),
                    cropX + crop.ordinal() * (cropButtonWidth + gap), 113, cropButtonWidth, 19,
                    "Buy seed (" + crop.seedPrice + ")"));
        }

        for (int i = 0; i < plots.size(); i++) {
            PixelGardenGame.Plot plot = plots.get(i);
            String label;
            switch (plot.getState(now)) {
                case LOCKED: label = "LOCKED"; break;
                case EMPTY: label = "EMPTY"; break;
                case GROWING:
                    long minutes = (plot.getRemainingMillis(now) + 59999L) / 60000L;
                    label = "Growing " + minutes + "m";
                    break;
                case READY: label = "HARVEST"; break;
                default: label = "PLOT"; break;
            }
            int x = startX + (i % 3) * (plotWidth + gap);
            int y = startY + (i / 3) * (plotHeight + gap);
            buttonList.add(new AlphaButton(PLOT_BUTTON_BASE + i, x, y, plotWidth, plotHeight,
                    (i + 1) + ". " + (plot.getCrop() == null ? "" : plot.getCrop().displayName + " ") + label));
        }

        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            buttonList.add(new AlphaButton(SELL_BUTTON_BASE + crop.ordinal(),
                    cropX + crop.ordinal() * (cropButtonWidth + gap), 229, cropButtonWidth, 20,
                    "Sell all " + crop.displayName + " (" + game.getInventory(crop) + ")"));
        }

        int upgradeWidth = Math.min(132, (gridWidth - gap * 2) / 3);
        int upgradeX = (width - (upgradeWidth * 3 + gap * 2)) / 2;
        buttonList.add(new AlphaButton(AUTO_HARVEST_BUTTON, upgradeX, 255, upgradeWidth, 20,
                game.hasAutoHarvest() ? "Auto Harvest: ON" : "Auto Harvest (" + game.getUpgradeCost("auto_harvest") + ")"));
        buttonList.add(new AlphaButton(GROWTH_BUTTON, upgradeX + upgradeWidth + gap, 255, upgradeWidth, 20,
                "Growth Boost L" + game.getGrowthLevels() + " (" + game.getUpgradeCost("growth_boost") + ")"));
        buttonList.add(new AlphaButton(EXPAND_BUTTON, upgradeX + (upgradeWidth + gap) * 2, 255, upgradeWidth, 20,
                "Expand (" + game.getUpgradeCost("expansion") + ")"));
        buttonList.add(new AlphaButton(BACK_BUTTON, width / 2 - 45, height - 27, 90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (loadingCloud) return;
        long now = System.currentTimeMillis();
        game.processTime(now);
        if (button.id == BACK_BUTTON) {
            saveCloudAsync();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= CROP_BUTTON_BASE && button.id < CROP_BUTTON_BASE + PixelGardenGame.Crop.values().length) {
            selectedCrop = PixelGardenGame.Crop.values()[button.id - CROP_BUTTON_BASE];
        } else if (button.id >= BUY_SEED_BASE && button.id < BUY_SEED_BASE + PixelGardenGame.Crop.values().length) {
            PixelGardenGame.Crop crop = PixelGardenGame.Crop.values()[button.id - BUY_SEED_BASE];
            game.buySeeds(crop, 1);
            selectedCrop = crop;
        } else if (button.id >= SELL_BUTTON_BASE && button.id < SELL_BUTTON_BASE + PixelGardenGame.Crop.values().length) {
            game.sellAll(PixelGardenGame.Crop.values()[button.id - SELL_BUTTON_BASE]);
        } else if (button.id == AUTO_HARVEST_BUTTON) {
            game.buyAutoHarvest();
        } else if (button.id == GROWTH_BUTTON) {
            game.buyGrowthBoost();
        } else if (button.id == EXPAND_BUTTON) {
            game.expandGarden();
        } else if (button.id >= PLOT_BUTTON_BASE && button.id < PLOT_BUTTON_BASE + game.getPlots().size()) {
            int plotIndex = button.id - PLOT_BUTTON_BASE;
            PixelGardenGame.Plot plot = game.getPlots().get(plotIndex);
            if (plot.getState(now) == PixelGardenGame.PlotState.EMPTY) {
                game.plant(plotIndex, selectedCrop, now);
            } else if (plot.getState(now) == PixelGardenGame.PlotState.READY) {
                game.harvest(plotIndex, now);
            }
        }
        saveCloudAsync();
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        game.processTime(System.currentTimeMillis());
        drawRect(0, 0, width, height, 0xD90A0C11);
        drawRect(width / 2 - 150, 34, width / 2 + 150, 36, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN", width / 2, 43, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Coins: " + game.getCoins() + "    Unlocked plots: "
                + game.getUnlockedPlots() + "/" + PixelGardenGame.MAX_PLOTS, width / 2, 60, 0xFFF0F1F4);
        StringBuilder stock = new StringBuilder("Inventory: ");
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            if (crop.ordinal() > 0) stock.append("   ");
            stock.append(crop.displayName).append(": ").append(game.getInventory(crop));
        }
        drawCenteredString(fontRendererObj, stock.toString(), width / 2, 74, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Select a crop or buy its seed, then plant an empty plot.",
                width / 2, 82, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Growth: 20 minutes (Growth Boost shortens new crops). Offline cap: 2 hours.",
                width / 2, 282, 0xFF9CE2AC);
        drawCenteredString(fontRendererObj, cloudStatus, width / 2, 296,
                cloudStatus.startsWith("Cloud save failed") ? 0xFFFF7777 : 0xFF8F96A3);
        if (loadingCloud) drawCenteredString(fontRendererObj, "Loading account garden...", width / 2, 310, 0xFFFFD866);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
