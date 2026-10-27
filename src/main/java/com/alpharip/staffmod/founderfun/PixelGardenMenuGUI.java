// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

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
        int top = 88;
        if (page == Page.SHOP || page == Page.SELL || page == Page.INVENTORY) {
            int gap = 8;
            int cardWidth = (panelWidth - 28 - gap) / 2;
            int cardHeight = 78;
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                int col = crop.ordinal() % 2;
                int row = crop.ordinal() / 2;
                int x = left + 10 + col * (cardWidth + gap);
                int y = top + row * (cardHeight + gap);
                String label;
                if (page == Page.SHOP) label = "Buy seed (" + crop.seedPrice + ")";
                else if (page == Page.SELL) label = "Sell all x" + game.getInventory(crop);
                else label = "Seeds " + game.getSeeds(crop) + " / Harvest " + game.getInventory(crop);
                buttonList.add(new AlphaButton(ACTION_BASE + crop.ordinal(), x + 8, y + cardHeight - 25, cardWidth - 16, 18, label));
            }
        } else {
            int gap = 8;
            int cardWidth = (panelWidth - 32 - gap * 2) / 3;
            int cardHeight = 112;
            for (int i = 0; i < 3; i++) {
                int x = left + 10 + i * (cardWidth + gap);
                int y = top + 12;
                String label;
                if (i == 0) label = game.hasAutoHarvest() ? "OWNED" : "Buy (" + game.getUpgradeCost("auto_harvest") + ")";
                else if (i == 1) label = "Buy (" + game.getUpgradeCost("growth_boost") + ")";
                else label = "Expand (" + game.getUpgradeCost("expansion") + ")";
                buttonList.add(new AlphaButton(ACTION_BASE + i, x + 6, y + cardHeight - 25, cardWidth - 12, 18, label));
            }
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

    private static final ResourceLocation PIXEL_GARDEN_SHEET =
            new ResourceLocation("alphastaffmod", "textures/gui/spritesheet.png");

    private void drawSprite(int x, int y, int width, int height, int u, int v) {
        mc.getTextureManager().bindTexture(PIXEL_GARDEN_SHEET);
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(x, y, u, v, 16, 16, width, height, 96, 64);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    private void drawCropIcon(PixelGardenGame.Crop crop, int x, int y, int s) {
        drawSprite(x, y, s, s, crop.ordinal() * 16, 0);
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
        drawString(fontRendererObj, title(), left + 14, top + 12, 0xFFFFD45A);
        if (page == Page.SHOP || page == Page.SELL || page == Page.INVENTORY) {
            int gap = 8, cardWidth = (panelWidth - 28 - gap) / 2, cardHeight = 78;
            for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                int col = crop.ordinal() % 2, row = crop.ordinal() / 2;
                int x = left + 10 + col * (cardWidth + gap);
                int y = 88 + row * (cardHeight + gap);
                drawPanel(x, y, cardWidth, cardHeight);
                drawCropIcon(crop, x + 8, y + 7, 24);
                drawString(fontRendererObj, crop.displayName, x + 38, y + 10, 0xFFF0F2F6);
                if (page == Page.SHOP) {
                    drawString(fontRendererObj, "Seeds: " + game.getSeeds(crop), x + 38, y + 25, 0xFFB7C4D5);
                    drawString(fontRendererObj, "Price: " + crop.seedPrice, x + 38, y + 38, 0xFFFFD45A);
                } else if (page == Page.SELL) {
                    drawString(fontRendererObj, "Stock: " + game.getInventory(crop), x + 38, y + 25, 0xFFB7C4D5);
                    drawString(fontRendererObj, "Each: " + crop.sellPrice, x + 38, y + 38, 0xFFFFD45A);
                } else {
                    drawString(fontRendererObj, "Seeds: " + game.getSeeds(crop), x + 38, y + 25, 0xFFB7C4D5);
                    drawString(fontRendererObj, "Harvest: " + game.getInventory(crop), x + 38, y + 38, 0xFFB7C4D5);
                }
            }
        } else {
            String[] names = {"Auto Harvest", "Growth Boost", "Garden Expansion"};
            String[] desc = {"Auto-collect ripe crops", "Faster growth", "Three more plots"};
            int[] costs = {game.getUpgradeCost("auto_harvest"), game.getUpgradeCost("growth_boost"), game.getUpgradeCost("expansion")};
            int gap = 8, cardWidth = (panelWidth - 32 - gap * 2) / 3, cardHeight = 112;
            for (int i = 0; i < 3; i++) {
                int x = left + 10 + i * (cardWidth + gap), y = 100;
                drawPanel(x, y, cardWidth, cardHeight);
                drawString(fontRendererObj, names[i], x + 7, y + 9, 0xFFF0F2F6);
                drawString(fontRendererObj, desc[i], x + 7, y + 27, 0xFFB7C4D5);
                drawString(fontRendererObj, costs[i] < 0 ? "OWNED / MAX" : "Cost: " + costs[i], x + 7, y + 44,
                        costs[i] < 0 ? 0xFF83D79A : 0xFFFFD45A);
                drawUpgradeIcon(x + cardWidth - 42, y + 2, i);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawUpgradeIcon(int x, int y, int kind) {
        int u = kind == 0 ? 64 : kind == 1 ? 48 : 0;
        int v = kind == 0 ? 16 : kind == 1 ? 48 : 48;
        if (kind == 2) u = 16;
        drawSprite(x, y, 24, 24, u, v);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
