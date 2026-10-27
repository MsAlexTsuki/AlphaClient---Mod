// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Pixel Garden prototype screen with crop selection, inventory and upgrades. */
public final class PixelGardenGUI extends GuiScreen {
    private static final int CROP_BUTTON_BASE = 10;
    private static final int PLOT_BUTTON_BASE = 100;
    private static final int SELL_BUTTON_BASE = 200;
    private static final int AUTO_HARVEST_BUTTON = 300;
    private static final int GROWTH_BUTTON = 301;
    private static final int EXPAND_BUTTON = 302;
    private static final int BACK_BUTTON = 4;
    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private PixelGardenGame.Crop selectedCrop = PixelGardenGame.Crop.WHEAT;

    public PixelGardenGUI(GuiScreen parent) { this.parent = parent; }

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
        int startY = 128;
        List<PixelGardenGame.Plot> plots = game.getPlots();

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

        int cropButtonWidth = Math.min(100, (gridWidth - gap * 3) / 4);
        int cropX = (width - (cropButtonWidth * 4 + gap * 3)) / 2;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            String prefix = selectedCrop == crop ? "> " : "";
            buttonList.add(new AlphaButton(CROP_BUTTON_BASE + crop.ordinal(),
                    cropX + crop.ordinal() * (cropButtonWidth + gap), 91, cropButtonWidth, 20,
                    prefix + crop.displayName + " (" + game.getSeeds(crop) + ")"));
        }

        int sellWidth = cropButtonWidth;
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            buttonList.add(new AlphaButton(SELL_BUTTON_BASE + crop.ordinal(),
                    cropX + crop.ordinal() * (sellWidth + gap), 232, sellWidth, 20,
                    "Sell " + crop.displayName + " (" + game.getInventory(crop) + ")"));
        }

        int upgradeWidth = Math.min(132, (gridWidth - gap * 2) / 3);
        int upgradeX = (width - (upgradeWidth * 3 + gap * 2)) / 2;
        buttonList.add(new AlphaButton(AUTO_HARVEST_BUTTON, upgradeX, 258, upgradeWidth, 20,
                game.hasAutoHarvest() ? "Auto Harvest: ON" : "Auto Harvest (" + game.getUpgradeCost("auto_harvest") + ")"));
        buttonList.add(new AlphaButton(GROWTH_BUTTON, upgradeX + upgradeWidth + gap, 258, upgradeWidth, 20,
                "Growth Boost L" + game.getGrowthLevels() + " (" + game.getUpgradeCost("growth_boost") + ")"));
        buttonList.add(new AlphaButton(EXPAND_BUTTON, upgradeX + (upgradeWidth + gap) * 2, 258, upgradeWidth, 20,
                "Expand (" + game.getUpgradeCost("expansion") + ")"));
        buttonList.add(new AlphaButton(BACK_BUTTON, width / 2 - 45, height - 27, 90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        long now = System.currentTimeMillis();
        game.processTime(now);
        if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= CROP_BUTTON_BASE && button.id < CROP_BUTTON_BASE + PixelGardenGame.Crop.values().length) {
            selectedCrop = PixelGardenGame.Crop.values()[button.id - CROP_BUTTON_BASE];
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
        drawCenteredString(fontRendererObj, "Select a seed, then click an empty plot. Crops mature in 20 minutes.",
                width / 2, 82, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Offline progress is capped at 2 hours; Auto Harvest collects ripe crops.",
                width / 2, 286, 0xFF9CE2AC);
        drawCenteredString(fontRendererObj, "Account cloud saving is being connected separately.",
                width / 2, 299, 0xFF8F96A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
