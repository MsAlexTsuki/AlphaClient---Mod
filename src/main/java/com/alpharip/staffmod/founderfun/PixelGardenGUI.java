// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Pixel-art farming screen with account-linked cloud saving. */
public final class PixelGardenGUI extends GuiScreen {
    private static final int CROP_BASE = 10, BUY_BASE = 20, PLOT_BASE = 100, SELL_BASE = 200;
    private static final int AUTO = 300, GROWTH = 301, EXPAND = 302, BACK = 4;
    private static String loadedAccount;
    private static boolean loadInProgress;
    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private PixelGardenGame.Crop selectedCrop = PixelGardenGame.Crop.WHEAT;
    private volatile String cloudStatus = "Cloud save: checking account...";
    private volatile boolean loadingCloud, cloudReady;
    private long lastRefresh;

    public PixelGardenGUI(GuiScreen parent) {
        this.parent = parent;
        loadCloudSaveIfNeeded();
    }

    private void loadCloudSaveIfNeeded() {
        if (!SupabaseCommunityService.isSignedIn()) {
            synchronized (PixelGardenGUI.class) { loadedAccount = null; }
            game.resetToDefaults();
            cloudReady = false;
            cloudStatus = "Cloud save unavailable: sign in to your Alpha Account.";
            return;
        }
        final String account = SupabaseCommunityService.getSignedInEmail();
        synchronized (PixelGardenGUI.class) {
            if (account != null && account.equals(loadedAccount)) {
                cloudReady = true;
                cloudStatus = "Cloud save: connected to " + account;
                return;
            }
            if (loadInProgress) {
                cloudStatus = "Cloud save: loading account data...";
                return;
            }
            loadInProgress = true;
        }
        game.resetToDefaults();
        cloudReady = false;
        loadingCloud = true;
        cloudStatus = "Cloud save: loading account data...";
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    JsonObject saved = SupabaseCommunityService.loadPixelGardenSave();
                    if (saved != null) game.loadSaveData(saved);
                    synchronized (PixelGardenGUI.class) { loadedAccount = account; }
                    cloudReady = true;
                    cloudStatus = saved == null ? "Cloud save: new garden linked to " + account
                            : "Cloud save: synced with " + account;
                } catch (Exception exception) {
                    cloudReady = false;
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
        if (!SupabaseCommunityService.isSignedIn() || loadingCloud || !cloudReady) return;
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

    private int gardenCell() { return Math.max(52, Math.min(86, (height - 230) / 6)); }
    private int gardenWidth() { return gardenCell() * 3 + 12; }
    private int gardenLeft() { return (width - gardenWidth()) / 2; }
    private int gardenTop() { return Math.max(112, (height - (gardenCell() * 3 + 12)) / 2 - 2); }
    private int leftPanelX() { return Math.max(8, gardenLeft() - 172); }
    private int rightPanelX() { return Math.min(width - 168, gardenLeft() + gardenWidth() + 12); }

    @Override
    public void initGui() {
        buttonList.clear();
        game.processTime(System.currentTimeMillis());
        int cell = gardenCell(), gap = 4, gx = gardenLeft(), gy = gardenTop();
        int leftX = leftPanelX(), rightX = rightPanelX();
        int sideWidth = Math.max(142, Math.min(164, gx - leftX - 8));
        int cropY = 72;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            int rowY = cropY + crop.ordinal() * 43;
            buttonList.add(new AlphaButton(CROP_BASE + crop.ordinal(), leftX + 5, rowY, sideWidth - 10, 19,
                    (selectedCrop == crop ? "> " : "") + crop.displayName + "  Seeds: " + game.getSeeds(crop)));
            buttonList.add(new AlphaButton(BUY_BASE + crop.ordinal(), leftX + 5, rowY + 20, sideWidth - 10, 18,
                    "Buy seed  (" + crop.seedPrice + " coins)"));
            buttonList.add(new AlphaButton(SELL_BASE + crop.ordinal(), leftX + 5, gy + cell * 3 + 24 + crop.ordinal() * 21,
                    sideWidth - 10, 19, "Sell " + crop.displayName + " x" + game.getInventory(crop)));
        }
        List<PixelGardenGame.Plot> plots = game.getPlots();
        for (int i = 0; i < plots.size(); i++) {
            int x = gx + (i % 3) * (cell + gap);
            int y = gy + (i / 3) * (cell + gap);
            buttonList.add(new AlphaButton(PLOT_BASE + i, x, y, cell, cell,
                    plotButtonLabel(plots.get(i), System.currentTimeMillis(), i)));
        }
        int upgradeY = 75;
        int upgradeWidth = Math.max(140, Math.min(164, width - rightX - 10));
        buttonList.add(new AlphaButton(AUTO, rightX + 4, upgradeY + 20, upgradeWidth - 8, 22,
                game.hasAutoHarvest() ? "Auto Harvest: ON" : "Auto Harvest (" + game.getUpgradeCost("auto_harvest") + ")"));
        buttonList.add(new AlphaButton(GROWTH, rightX + 4, upgradeY + 83, upgradeWidth - 8, 22,
                "Growth Boost L" + game.getGrowthLevels() + " (" + game.getUpgradeCost("growth_boost") + ")"));
        buttonList.add(new AlphaButton(EXPAND, rightX + 4, upgradeY + 146, upgradeWidth - 8, 22,
                "Expand Garden (" + game.getUpgradeCost("expansion") + ")"));
        buttonList.add(new AlphaButton(BACK, width / 2 - 45, height - 25, 90, 20, "Back"));
    }

    private String plotButtonLabel(PixelGardenGame.Plot plot, long now, int index) {
        switch (plot.getState(now)) {
            case LOCKED: return "LOCKED";
            case EMPTY: return "+ Plant";
            case GROWING: return plot.getCrop().displayName + " " + ((plot.getRemainingMillis(now) + 59999L) / 60000L) + "m";
            case READY: return "HARVEST!";
            default: return "Plot " + (index + 1);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null || loadingCloud) return;
        long now = System.currentTimeMillis();
        game.processTime(now);
        if (button.id == BACK) {
            saveCloudAsync();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= CROP_BASE && button.id < CROP_BASE + 4) {
            selectedCrop = PixelGardenGame.Crop.values()[button.id - CROP_BASE];
        } else if (button.id >= BUY_BASE && button.id < BUY_BASE + 4) {
            selectedCrop = PixelGardenGame.Crop.values()[button.id - BUY_BASE];
            game.buySeeds(selectedCrop, 1);
        } else if (button.id >= SELL_BASE && button.id < SELL_BASE + 4) {
            game.sellAll(PixelGardenGame.Crop.values()[button.id - SELL_BASE]);
        } else if (button.id == AUTO) game.buyAutoHarvest();
        else if (button.id == GROWTH) game.buyGrowthBoost();
        else if (button.id == EXPAND) game.expandGarden();
        else if (button.id >= PLOT_BASE && button.id < PLOT_BASE + game.getPlots().size()) {
            int index = button.id - PLOT_BASE;
            PixelGardenGame.Plot plot = game.getPlots().get(index);
            if (plot.getState(now) == PixelGardenGame.PlotState.EMPTY) game.plant(index, selectedCrop, now);
            else if (plot.getState(now) == PixelGardenGame.PlotState.READY) game.harvest(index, now);
        }
        saveCloudAsync();
        initGui();
    }

    private void panel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xE510141C);
        drawRect(x, y, x + w, y + 1, 0xFFFFC83D);
        drawRect(x, y + h - 1, x + w, y + h, 0xFF454B58);
        drawRect(x, y, x + 1, y + h, 0xFF454B58);
        drawRect(x + w - 1, y, x + w, y + h, 0xFF454B58);
    }

    private void pixel(int x, int y, int size, int color) {
        drawRect(x, y, x + size, y + size, color);
    }

    private void drawCropArt(PixelGardenGame.Crop crop, int x, int y, int size, long now,
                             PixelGardenGame.PlotState state) {
        int p = Math.max(3, size / 12);
        if (state == PixelGardenGame.PlotState.EMPTY) {
            pixel(x + size / 2 - p, y + size / 2, p, 0xFF65B94B);
            pixel(x + size / 2, y + size / 2 - p, p, 0xFF8BDD54);
            return;
        }
        if (crop == PixelGardenGame.Crop.WHEAT) {
            for (int i = 0; i < 4; i++) {
                int stemX = x + size / 2 - p * 3 + i * p * 2;
                int stemH = state == PixelGardenGame.PlotState.READY ? size / 2 : Math.max(p, size / 5);
                drawRect(stemX, y + size - p * 2 - stemH, stemX + p, y + size - p, 0xFF3D8C37);
                if (state == PixelGardenGame.PlotState.READY) {
                    pixel(stemX - p, y + size - p * 2 - stemH, p * 2, 0xFFE5C33B);
                    pixel(stemX, y + size - p * 3 - stemH, p * 2, 0xFFFFE46B);
                } else pixel(stemX, y + size - p * 2 - stemH, p, 0xFF75C84D);
            }
        } else {
            int leaf = crop == PixelGardenGame.Crop.PUMPKIN ? 0xFF4B9E35 : 0xFF3EAD46;
            pixel(x + size / 2 - p * 2, y + size / 2, p * 2, leaf);
            pixel(x + size / 2, y + size / 2 - p, p * 2, 0xFF79C84B);
            pixel(x + size / 2 + p, y + size / 2, p * 2, leaf);
            int fruit = crop == PixelGardenGame.Crop.CARROT ? 0xFFFF8B26
                    : crop == PixelGardenGame.Crop.POTATO ? 0xFFC9A05B : 0xFFE98222;
            pixel(x + size / 2 - p, y + size / 2 + p, p * 2, fruit);
            pixel(x + size / 2, y + size / 2 + p * 2, p, fruit);
            if (state == PixelGardenGame.PlotState.READY) pixel(x + size / 2, y + size / 2, p, 0xFFFFD45C);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long now = System.currentTimeMillis();
        game.processTime(now);
        if (now - lastRefresh >= 1000L) { lastRefresh = now; initGui(); }
        drawDefaultBackground();
        drawRect(0, 0, width, 48, 0xF00B1018);
        drawRect(0, 46, width, 49, 0xFFFFC83D);
        drawString(fontRendererObj, "PIXEL", 18, 15, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 58, 15, 0xFFFFC83D);
        drawString(fontRendererObj, "Coins: " + game.getCoins(), width / 2 - 35, 15, 0xFFFFD45A);
        drawString(fontRendererObj, game.getUnlockedPlots() + " / " + PixelGardenGame.MAX_PLOTS + " plots",
                width / 2 + 55, 15, 0xFFE3E7EE);

        int gx = gardenLeft(), gy = gardenTop(), cell = gardenCell(), gap = 4;
        int gardenW = gardenWidth();
        int sideW = Math.max(142, Math.min(164, gx - leftPanelX() - 8));
        int rightW = Math.max(140, Math.min(164, width - rightPanelX() - 8));
        panel(leftPanelX(), 58, sideW, height - 92);
        panel(gx - 8, gy - 30, gardenW + 16, cell * 3 + gap * 2 + 48);
        panel(rightPanelX(), 58, rightW, height - 92);

        drawString(fontRendererObj, "SEEDS & SHOP", leftPanelX() + 8, 65, 0xFFFFD45A);
        drawString(fontRendererObj, "INVENTORY / SELL", leftPanelX() + 8, gy + cell * 3 + 5, 0xFFFFD45A);
        drawString(fontRendererObj, "MY GARDEN", gx + 2, gy - 22, 0xFFFFD45A);
        drawString(fontRendererObj, "UPGRADES", rightPanelX() + 8, 65, 0xFFFFD45A);

        List<PixelGardenGame.Plot> plots = game.getPlots();
        for (int i = 0; i < plots.size(); i++) {
            int x = gx + (i % 3) * (cell + gap);
            int y = gy + (i / 3) * (cell + gap);
            PixelGardenGame.Plot plot = plots.get(i);
            PixelGardenGame.PlotState state = plot.getState(now);
            drawRect(x - 2, y - 2, x + cell + 2, y + cell + 2, state == PixelGardenGame.PlotState.LOCKED
                    ? 0xFF252A32 : 0xFF5A3823);
            drawRect(x, y, x + cell, y + cell, state == PixelGardenGame.PlotState.LOCKED
                    ? 0xFF30343A : 0xFF86552E);
            for (int row = 0; row < 4; row++)
                drawRect(x + 3, y + 5 + row * Math.max(5, cell / 5), x + cell - 3,
                        y + 7 + row * Math.max(5, cell / 5), 0xFF704326);
            if (state == PixelGardenGame.PlotState.LOCKED) {
                drawCenteredString(fontRendererObj, "LOCKED", x + cell / 2, y + cell / 2 - 4, 0xFF8E949D);
            } else if (state == PixelGardenGame.PlotState.EMPTY) {
                drawCropArt(selectedCrop, x, y, cell, now, state);
                drawCenteredString(fontRendererObj, "EMPTY", x + cell / 2, y + cell - 11, 0xFFD7C4A3);
            } else {
                drawCropArt(plot.getCrop(), x, y, cell, now, state);
                if (state == PixelGardenGame.PlotState.READY) {
                    drawRect(x + 3, y + 3, x + cell - 3, y + 14, 0xDD172015);
                    drawCenteredString(fontRendererObj, "READY!", x + cell / 2, y + 5, 0xFFFFE46B);
                } else {
                    long secs = plot.getRemainingMillis(now) / 1000L;
                    String timer = (secs / 60) + "m " + (secs % 60) + "s";
                    drawRect(x + 3, y + cell - 13, x + cell - 3, y + cell - 3, 0xDD172015);
                    drawCenteredString(fontRendererObj, timer, x + cell / 2, y + cell - 11, 0xFFF4F1E8);
                }
            }
        }

        int invY = gy + cell * 3 + 27;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            drawString(fontRendererObj, crop.displayName + ": " + game.getInventory(crop),
                    leftPanelX() + 8, invY + crop.ordinal() * 21, 0xFFE6E9EF);
        }
        int upY = 99;
        String[] names = {"Auto Harvest", "Growth Boost", "Garden Expansion"};
        String[] desc = {"Auto-collect ripe crops", "Faster new crop growth", "Unlock 3 more plots"};
        int[] costs = {game.getUpgradeCost("auto_harvest"), game.getUpgradeCost("growth_boost"), game.getUpgradeCost("expansion")};
        for (int i = 0; i < 3; i++) {
            int y = upY + i * 63;
            drawRect(rightPanelX() + 7, y, rightPanelX() + rightW - 7, y + 54, 0xFF151B25);
            drawString(fontRendererObj, names[i], rightPanelX() + 13, y + 6, 0xFFF0F2F6);
            drawString(fontRendererObj, desc[i], rightPanelX() + 13, y + 19, 0xFFADB8C8);
            drawString(fontRendererObj, costs[i] < 0 ? "OWNED / MAX" : "Cost: " + costs[i],
                    rightPanelX() + 13, y + 32, costs[i] < 0 ? 0xFF83D79A : 0xFFFFD45A);
        }
        drawCenteredString(fontRendererObj, "Plant  >  Grow  >  Harvest  >  Sell  >  Upgrade",
                width / 2, height - 31, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, cloudStatus, width / 2, height - 18,
                cloudStatus.startsWith("Cloud save failed") ? 0xFFFF7777 : 0xFF9DA8B8);
        if (loadingCloud) drawCenteredString(fontRendererObj, "Loading account garden...", width / 2, height - 43, 0xFFFFD45A);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void onGuiClosed() { saveCloudAsync(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
