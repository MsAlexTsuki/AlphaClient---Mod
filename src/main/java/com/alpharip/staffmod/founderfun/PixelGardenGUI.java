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
    private static final int SHOP = 400, INVENTORY = 401, SELL = 402, UPGRADES = 403;
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

    void saveCloudAsync() {
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

    private int gardenCell() {
        int byHeight = (height - 205) / 3 - 8;
        int byWidth = (width - 110) / 3 - 8;
        return Math.max(54, Math.min(136, Math.min(byHeight, byWidth)));
    }
    private int gardenWidth() { return gardenCell() * 3 + 12; }
    private int gardenLeft() { return (width - gardenWidth()) / 2; }
    private int gardenTop() { return Math.max(120, (height - (gardenCell() * 3 + 72)) / 2); }

    @Override
    public void initGui() {
        buttonList.clear();
        game.processTime(System.currentTimeMillis());
        int cell = gardenCell(), gap = 4, gx = gardenLeft(), gy = gardenTop();
        List<PixelGardenGame.Plot> plots = game.getPlots();
        for (int i = 0; i < plots.size(); i++) {
            int x = gx + (i % 3) * (cell + gap);
            int y = gy + (i / 3) * (cell + gap);
            buttonList.add(new AlphaButton(PLOT_BASE + i, x, y, cell, cell,
                    plotButtonLabel(plots.get(i), System.currentTimeMillis(), i)));
        }
        int navY = height - 55;
        int navWidth = Math.min(104, (width - 50) / 5);
        int totalNav = navWidth * 5 + 8 * 4;
        int navX = (width - totalNav) / 2;
        buttonList.add(new AlphaButton(SHOP, navX, navY, navWidth, 22, "Shop"));
        buttonList.add(new AlphaButton(INVENTORY, navX + navWidth + 8, navY, navWidth, 22, "Inventory"));
        buttonList.add(new AlphaButton(SELL, navX + (navWidth + 8) * 2, navY, navWidth, 22, "Sell"));
        buttonList.add(new AlphaButton(UPGRADES, navX + (navWidth + 8) * 3, navY, navWidth, 22, "Upgrades"));
        buttonList.add(new AlphaButton(BACK, navX + (navWidth + 8) * 4, navY, navWidth, 22, "Back"));
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
        if (button.id == SHOP || button.id == INVENTORY || button.id == SELL || button.id == UPGRADES) {
            saveCloudAsync();
            if (button.id == SHOP) mc.displayGuiScreen(new PixelGardenShopGUI(this));
            else if (button.id == INVENTORY) mc.displayGuiScreen(new PixelGardenInventoryGUI(this));
            else if (button.id == SELL) mc.displayGuiScreen(new PixelGardenSellGUI(this));
            else mc.displayGuiScreen(new PixelGardenUpgradeGUI(this));
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

    /** Tiny blocky sprout emblem drawn at integer pixel scale for the 1.8.9 GUI. */
    private void drawPixelSprout(int x, int y, int scale) {
        pixel(x + 4 * scale, y + 7 * scale, 2 * scale, 0xFF9A5B27);
        pixel(x + 5 * scale, y + 5 * scale, scale, 0xFF3C9C39);
        pixel(x + 3 * scale, y + 2 * scale, 3 * scale, 2 * scale, 0xFF62C943);
        pixel(x + 6 * scale, y, 3 * scale, 2 * scale, 0xFF8AE34D);
        pixel(x + 5 * scale, y + 2 * scale, scale, 4 * scale, 0xFF3C9C39);
    }

    private void pixel(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + h, color);
    }

    private int inventoryTotal() {
        int total = 0;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) total += game.getInventory(crop);
        return total;
    }

    private void drawPixelGardenBorder(int x, int y, int w, int h) {
        int tile = 6;
        for (int px = x; px < x + w; px += tile) {
            drawRect(px, y, Math.min(px + tile - 2, x + w), y + 3, 0xFF6AA84F);
            drawRect(px, y + h - 3, Math.min(px + tile - 2, x + w), y + h, 0xFF4D823B);
        }
        for (int py = y; py < y + h; py += tile) {
            drawRect(x, py, x + 3, Math.min(py + tile - 2, y + h), 0xFF4D823B);
            drawRect(x + w - 3, py, x + w, Math.min(py + tile - 2, y + h), 0xFF6AA84F);
        }
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
        drawPixelSprout(18, 10, 2);
        drawString(fontRendererObj, "PIXEL", 43, 15, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 83, 15, 0xFFFFC83D);
        drawString(fontRendererObj, "Plant  •  Grow  •  Harvest  •  Sell", 150, 15, 0xFFB7C4D5);
        drawString(fontRendererObj, "Coins: " + game.getCoins(), width / 2 - 45, 15, 0xFFFFD45A);
        drawString(fontRendererObj, game.getUnlockedPlots() + " / " + PixelGardenGame.MAX_PLOTS + " plots",
                width / 2 + 75, 15, 0xFFE3E7EE);

        int gx = gardenLeft(), gy = gardenTop(), cell = gardenCell(), gap = 4;
        int gardenW = gardenWidth();
        panel(gx - 22, gy - 36, gardenW + 44, cell * 3 + gap * 2 + 58);
        drawString(fontRendererObj, "MY GARDEN", gx + 2, gy - 25, 0xFFFFD45A);
        drawString(fontRendererObj, "Choose a plot to plant or harvest", gx + 2, gy - 13, 0xFFB7C4D5);
        drawPixelGardenBorder(gx - 15, gy - 5, gardenW + 30, cell * 3 + gap * 2 + 20);

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
            for (int row = 0; row < 5; row++) {
                int soilY = y + 5 + row * Math.max(4, cell / 6);
                drawRect(x + 3, soilY, x + cell - 3, soilY + 2, 0xFF704326);
                drawRect(x + 5, soilY + 2, x + cell - 5, soilY + 3, 0xFF9A6639);
            }
            drawRect(x + 4, y + 4, x + cell - 4, y + 6, 0xFFB47A45);
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

        drawCenteredString(fontRendererObj, "Seeds: " + selectedCrop.displayName + " x" + game.getSeeds(selectedCrop)
                + "    Harvested inventory: " + inventoryTotal(), width / 2, height - 72, 0xFFE6E9EF);
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
