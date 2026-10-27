// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Shared pixel-art menu frame for Pixel Garden's dedicated shop and management screens. */
abstract class PixelGardenMenuGUI extends GuiScreen {
    enum Page { SHOP, INVENTORY, SELL, UPGRADES }
    private static final int ACTION_BASE = 20;
    private static final int TAB_SHOP = 100, TAB_INVENTORY = 101, TAB_SELL = 102, TAB_UPGRADES = 103, BACK = 104;
    protected final GuiScreen parent;
    protected final PixelGardenGame game = PixelGardenGame.getInstance();
    private final Page page;

    PixelGardenMenuGUI(GuiScreen parent, Page page) {
        this.parent = parent;
        this.page = page;
    }

    private String title() {
        switch (page) {
            case SHOP: return "GARDEN SHOP";
            case INVENTORY: return "INVENTORY";
            case SELL: return "SELL CROPS";
            default: return "UPGRADES";
        }
    }

    @Override public void initGui() {
        buttonList.clear();
        int left = Math.max(18, width / 2 - 250);
        int panelWidth = Math.min(500, width - 36);
        int top = 72;
        if (page == Page.SHOP || page == Page.SELL || page == Page.INVENTORY) {
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                int y = top + crop.ordinal() * 66;
                String label;
                if (page == Page.SHOP) label = "Buy " + crop.displayName + " seed (" + crop.seedPrice + ")";
                else if (page == Page.SELL) label = "Sell all " + crop.displayName + " x" + game.getInventory(crop);
                else label = crop.displayName + "  Seeds: " + game.getSeeds(crop) + "  Harvest: " + game.getInventory(crop);
                buttonList.add(new AlphaButton(ACTION_BASE + crop.ordinal(), left + 12, y + 34, panelWidth - 24, 22, label));
            }
        } else {
            String[] labels = {
                game.hasAutoHarvest() ? "Auto Harvest - OWNED" : "Buy Auto Harvest (" + game.getUpgradeCost("auto_harvest") + " coins)",
                "Growth Boost L" + game.getGrowthLevels() + " (" + game.getUpgradeCost("growth_boost") + " coins)",
                "Expand Garden (" + game.getUpgradeCost("expansion") + " coins)"
            };
            for (int i = 0; i < labels.length; i++)
                buttonList.add(new AlphaButton(ACTION_BASE + i, left + 12, top + i * 88 + 45, panelWidth - 24, 22, labels[i]));
        }
        int navY = height - 35;
        int navW = Math.min(90, (width - 45) / 5);
        int navX = (width - (navW * 5 + 8 * 4)) / 2;
        buttonList.add(new AlphaButton(TAB_SHOP, navX, navY, navW, 20, "Shop"));
        buttonList.add(new AlphaButton(TAB_INVENTORY, navX + navW + 8, navY, navW, 20, "Inventory"));
        buttonList.add(new AlphaButton(TAB_SELL, navX + (navW + 8) * 2, navY, navW, 20, "Sell"));
        buttonList.add(new AlphaButton(TAB_UPGRADES, navX + (navW + 8) * 3, navY, navW, 20, "Upgrades"));
        buttonList.add(new AlphaButton(BACK, navX + (navW + 8) * 4, navY, navW, 20, "Garden"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            saveIfGardenParent();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= TAB_SHOP && button.id <= TAB_UPGRADES) {
            saveIfGardenParent();
            if (button.id == TAB_SHOP) mc.displayGuiScreen(new PixelGardenShopGUI(parent));
            else if (button.id == TAB_INVENTORY) mc.displayGuiScreen(new PixelGardenInventoryGUI(parent));
            else if (button.id == TAB_SELL) mc.displayGuiScreen(new PixelGardenSellGUI(parent));
            else mc.displayGuiScreen(new PixelGardenUpgradeGUI(parent));
            return;
        }
        if (button.id >= ACTION_BASE && button.id < ACTION_BASE + 4) {
            int index = button.id - ACTION_BASE;
            if (page == Page.SHOP) game.buySeeds(PixelGardenGame.Crop.values()[index], 1);
            else if (page == Page.SELL) game.sellAll(PixelGardenGame.Crop.values()[index]);
        } else if (page == Page.UPGRADES) {
            if (button.id == ACTION_BASE) game.buyAutoHarvest();
            else if (button.id == ACTION_BASE + 1) game.buyGrowthBoost();
            else if (button.id == ACTION_BASE + 2) game.expandGarden();
        }
        saveIfGardenParent();
        initGui();
    }

    private void saveIfGardenParent() {
        if (parent instanceof PixelGardenGUI) ((PixelGardenGUI) parent).saveCloudAsync();
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xF20B111A);
        drawRect(x, y, x + w, y + 3, 0xFFFFC83D);
        drawRect(x, y + h - 3, x + w, y + h, 0xFF714323);
        drawRect(x, y, x + 3, y + h, 0xFF714323);
        drawRect(x + w - 3, y, x + w, y + h, 0xFF714323);
    }

    private void drawCropIcon(PixelGardenGame.Crop crop, int x, int y, int s) {
        int soil = 0xFF86552E, green = 0xFF4A9B39;
        drawRect(x, y + s - 5, x + s, y + s, soil);
        if (crop == PixelGardenGame.Crop.WHEAT) {
            for (int i = 0; i < 3; i++) {
                int sx = x + 5 + i * 6;
                drawRect(sx, y + 8, sx + 2, y + s - 5, green);
                drawRect(sx - 2, y + 5 + (i % 2) * 3, sx + 4, y + 10 + (i % 2) * 3, 0xFFE8C64A);
            }
        } else {
            drawRect(x + s / 2 - 7, y + 9, x + s / 2 + 7, y + 19, green);
            drawRect(x + s / 2 - 3, y + 4, x + s / 2 + 3, y + 12, 0xFF75C84D);
            int fruit = crop == PixelGardenGame.Crop.CARROT ? 0xFFFF8B26
                    : crop == PixelGardenGame.Crop.POTATO ? 0xFFC9A05B : 0xFFE98222;
            drawRect(x + s / 2 - 5, y + 16, x + s / 2 + 5, y + s - 7, fruit);
            drawRect(x + s / 2 - 3, y + s - 10, x + s / 2 + 3, y + s - 7, fruit);
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 50, 0xF00B1018);
        drawRect(0, 48, width, 52, 0xFFFFC83D);
        drawString(fontRendererObj, "PIXEL", 20, 18, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 60, 18, 0xFFFFC83D);
        drawString(fontRendererObj, " / " + title(), 130, 18, 0xFFE3E7EE);
        drawString(fontRendererObj, "Coins: " + game.getCoins(), width - 115, 18, 0xFFFFD45A);
        int left = Math.max(18, width / 2 - 250), panelWidth = Math.min(500, width - 36);
        int top = 60, panelHeight = height - 105;
        drawPanel(left, top, panelWidth, panelHeight);
        drawString(fontRendererObj, title(), left + 18, top + 14, 0xFFFFD45A);
        if (page == Page.SHOP || page == Page.SELL || page == Page.INVENTORY) {
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                int y = 88 + crop.ordinal() * 66;
                drawPanel(left + 10, y, panelWidth - 20, 58);
                drawCropIcon(crop, left + 20, y + 7, 40);
                drawString(fontRendererObj, crop.displayName, left + 70, y + 9, 0xFFF0F2F6);
                if (page == Page.SHOP) {
                    drawString(fontRendererObj, "Seeds owned: " + game.getSeeds(crop) + "   Price: " + crop.seedPrice + " coins",
                            left + 70, y + 25, 0xFFB7C4D5);
                } else if (page == Page.SELL) {
                    drawString(fontRendererObj, "In inventory: " + game.getInventory(crop) + "   Sell value: " + crop.sellPrice + " coins each",
                            left + 70, y + 25, 0xFFB7C4D5);
                } else {
                    drawString(fontRendererObj, "Seeds: " + game.getSeeds(crop) + "   Harvested: " + game.getInventory(crop),
                            left + 70, y + 25, 0xFFB7C4D5);
                }
            }
        } else {
            String[] names = {"Auto Harvest", "Growth Boost", "Garden Expansion"};
            String[] desc = {"Automatically collect ripe crops.", "Reduce growth time for newly planted crops.", "Unlock three additional garden plots."};
            int[] costs = {game.getUpgradeCost("auto_harvest"), game.getUpgradeCost("growth_boost"), game.getUpgradeCost("expansion")};
            for (int i = 0; i < 3; i++) {
                int y = 91 + i * 88;
                drawPanel(left + 12, y, panelWidth - 24, 76);
                drawString(fontRendererObj, names[i], left + 24, y + 10, 0xFFF0F2F6);
                drawString(fontRendererObj, desc[i], left + 24, y + 27, 0xFFB7C4D5);
                drawString(fontRendererObj, costs[i] < 0 ? "OWNED / MAX" : "Cost: " + costs[i] + " coins",
                        left + 24, y + 45, costs[i] < 0 ? 0xFF83D79A : 0xFFFFD45A);
                drawUpgradeIcon(left + panelWidth - 75, y + 17, i);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawUpgradeIcon(int x, int y, int kind) {
        drawRect(x, y + 12, x + 30, y + 42, 0xFF26313C);
        if (kind == 0) {
            drawRect(x + 9, y + 17, x + 21, y + 33, 0xFFD6DCE5);
            drawRect(x + 12, y + 13, x + 18, y + 17, 0xFFFFC83D);
            drawRect(x + 12, y + 33, x + 18, y + 37, 0xFF75C84D);
        } else if (kind == 1) {
            drawRect(x + 12, y + 19, x + 18, y + 38, 0xFF4A9B39);
            drawRect(x + 4, y + 17, x + 14, y + 25, 0xFF75C84D);
            drawRect(x + 17, y + 13, x + 27, y + 21, 0xFF8AE34D);
        } else {
            for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++)
                drawRect(x + 5 + i * 10, y + 17 + j * 10, x + 14 + i * 10, y + 26 + j * 10, 0xFF55A9D6);
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
