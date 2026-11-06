// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;
import org.lwjgl.input.Mouse;

/** Shared pixel-art menu frame for Pixel Garden's dedicated shop and management screens. */
abstract class PixelGardenMenuGUI extends GuiScreen {
    enum Page { SHOP, INVENTORY, SELL, UPGRADES }
    private static final int ACTION_BASE = 20, PRODUCT_SELL_BASE = 500;
    private static final int TAB_SHOP = 100, TAB_INVENTORY = 101, TAB_SELL = 102, TAB_UPGRADES = 103, TAB_CRAFTING = 104, BACK = 105;
    protected final GuiScreen parent;
    protected final PixelGardenGame game = PixelGardenGame.getInstance();
    private final Page page;
    private int cropScrollRow;
    private static final int CROP_CARD_HEIGHT = 78, CROP_ROW_GAP = 8;

    PixelGardenMenuGUI(GuiScreen parent, Page page) {
        this.parent = parent;
        this.page = page;
    }

    private String title() {
        switch (page) {
            case SHOP: return "GARDEN SHOP";
            case INVENTORY: return "INVENTORY";
            case SELL: return "SELL CROPS + CRAFTED PRODUCTS";
            default: return "UPGRADES";
        }
    }

    private static final class SellEntry {
        final PixelGardenGame.Crop crop;
        final PixelGardenGame.CraftingRecipe recipe;

        private SellEntry(PixelGardenGame.Crop crop, PixelGardenGame.CraftingRecipe recipe) {
            this.crop = crop;
            this.recipe = recipe;
        }

        static SellEntry crop(PixelGardenGame.Crop crop) { return new SellEntry(crop, null); }
        static SellEntry product(String recipeId) {
            PixelGardenGame.CraftingRecipe recipe = PixelGardenGame.getCraftingRecipe(recipeId);
            return recipe == null ? null : new SellEntry(null, recipe);
        }
    }

    private List<SellEntry> sellEntries() {
        List<SellEntry> entries = new java.util.ArrayList<SellEntry>();
        addCropAndProducts(entries, PixelGardenGame.Crop.WHEAT, "bread", "camberry_bread", "sugar_bread");
        addCropAndProducts(entries, PixelGardenGame.Crop.POTATO, "chips", "potato_mush");
        addCropAndProducts(entries, PixelGardenGame.Crop.CARROT, "carrot_juice");
        addCropAndProducts(entries, PixelGardenGame.Crop.PUMPKIN, "pumpkin_head");
        addCropAndProducts(entries, PixelGardenGame.Crop.MELON, "melon_slices");
        addCropAndProducts(entries, PixelGardenGame.Crop.CACTUS, "cactus_juice");
        addCropAndProducts(entries, PixelGardenGame.Crop.SUGARCANE, "sugar");
        addCropAndProducts(entries, PixelGardenGame.Crop.BERRIES, "basket_of_berries");
        return entries;
    }

    private void addCropAndProducts(List<SellEntry> entries, PixelGardenGame.Crop crop, String... recipeIds) {
        entries.add(SellEntry.crop(crop));
        for (String recipeId : recipeIds) {
            SellEntry entry = SellEntry.product(recipeId);
            if (entry != null) entries.add(entry);
        }
    }

    private int cropRowCount() {
        int itemCount = page == Page.SELL || page == Page.INVENTORY
                ? sellEntries().size() : PixelGardenGame.Crop.values().length;
        return (itemCount + 1) / 2;
    }

    private int cropCardHeight() { return CROP_CARD_HEIGHT; }

    private int visibleCropRows() {
        int cardHeight = cropCardHeight();
        int availableHeight = Math.max(cardHeight, height - 88 - 58);
        return Math.max(1, availableHeight / (cardHeight + CROP_ROW_GAP));
    }

    private void clampCropScroll() {
        cropScrollRow = Math.max(0, Math.min(cropRowCount() - visibleCropRows(), cropScrollRow));
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && (page == Page.SHOP || page == Page.SELL || page == Page.INVENTORY)) {
            cropScrollRow += wheel < 0 ? 1 : -1;
            clampCropScroll();
            initGui();
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
            int cardHeight = cropCardHeight();
            clampCropScroll();
            int firstRow = cropScrollRow;
            int lastRow = Math.min(cropRowCount(), firstRow + visibleCropRows());
            if (page == Page.SELL) {
                List<SellEntry> entries = sellEntries();
                for (int i = 0; i < entries.size(); i++) {
                    int row = i / 2;
                    if (row < firstRow || row >= lastRow) continue;
                    SellEntry entry = entries.get(i);
                    int col = i % 2;
                    int x = left + 10 + col * (cardWidth + gap);
                    int y = top + (row - cropScrollRow) * (cardHeight + gap);
                    if (entry.crop != null) {
                        PixelGardenGame.Crop crop = entry.crop;
                        buttonList.add(new AlphaButton(ACTION_BASE + crop.ordinal(), x + 8,
                                y + cardHeight - 21, cardWidth - 16, 16,
                                "Sell crop x" + game.getInventory(crop)));
                    } else {
                        int recipeIndex = PixelGardenGame.getCraftingRecipes().indexOf(entry.recipe);
                        buttonList.add(new AlphaButton(PRODUCT_SELL_BASE + recipeIndex, x + 8,
                                y + cardHeight - 21, cardWidth - 16, 16,
                                "Sell x" + game.getCraftedProductBatches(entry.recipe.id)));
                    }
                }
            } else if (page == Page.SHOP) {
                for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                    int row = crop.ordinal() / 2;
                    if (row < firstRow || row >= lastRow) continue;
                    int col = crop.ordinal() % 2;
                    int x = left + 10 + col * (cardWidth + gap);
                    int y = top + (row - cropScrollRow) * (cardHeight + gap);
                    String label = "Buy seed (" + crop.seedPrice + ")";
                    buttonList.add(new AlphaButton(ACTION_BASE + crop.ordinal(), x + 8,
                            y + cardHeight - 25, cardWidth - 16, 18, label));
                }
            }
                } else {
            int gap = 6;
            int cardWidth = (panelWidth - 32 - gap * 2) / 3;
            int cardHeight = 68;
            for (int i = 0; i < 11; i++) {
                int x = left + 10 + (i % 3) * (cardWidth + gap);
                int y = 86 + (i / 3) * (cardHeight + gap);
                String key = upgradeKey(i);
                String label;
                if ("auto_harvest".equals(key)) label = game.hasAutoHarvest() ? "OWNED" : "Buy (" + game.getUpgradeCost(key) + ")";
                else if ("auto_plant".equals(key)) label = game.hasAutoPlant() ? "OWNED" : "Buy (" + game.getUpgradeCost(key) + ")";
                else if ("auto_sell".equals(key)) label = game.hasAutoSell() ? (game.isAutoSellEnabled() ? "ON" : "OFF") : "Buy (" + game.getUpgradeCost(key) + ")";
                else if ("auto_quest".equals(key)) label = game.hasAutoQuest() ? "OWNED" : "Buy (" + game.getUpgradeCost(key) + ")";
                else if ("prestige".equals(key)) label = "Prestige (20000)";
                else label = game.getUpgradeCost(key) < 0 ? "MAX" : ("expansion".equals(key) ? "Unlock (" : "Buy (") + game.getUpgradeCost(key) + ")";
                buttonList.add(new AlphaButton(ACTION_BASE + i, x + 6, y + cardHeight - 18, cardWidth - 12, 16, label));
            }
        }
        int navY = height - 35;
        int navW = Math.min(82, (width - 50) / 6);
        int navX = (width - (navW * 6 + 8 * 5)) / 2;
        buttonList.add(new AlphaButton(TAB_SHOP, navX, navY, navW, 20, "Shop"));
        buttonList.add(new AlphaButton(TAB_INVENTORY, navX + navW + 8, navY, navW, 20, "Inventory"));
        buttonList.add(new AlphaButton(TAB_SELL, navX + (navW + 8) * 2, navY, navW, 20, "Sell"));
        buttonList.add(new AlphaButton(TAB_UPGRADES, navX + (navW + 8) * 3, navY, navW, 20, "Upgrades"));
        buttonList.add(new AlphaButton(TAB_CRAFTING, navX + (navW + 8) * 4, navY, navW, 20, "Crafting"));
        buttonList.add(new AlphaButton(BACK, navX + (navW + 8) * 5, navY, navW, 20, "Garden"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            saveIfGardenParent();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id >= TAB_SHOP && button.id <= TAB_CRAFTING) {
            saveIfGardenParent();
            if (button.id == TAB_SHOP) mc.displayGuiScreen(new PixelGardenShopGUI(parent));
            else if (button.id == TAB_INVENTORY) mc.displayGuiScreen(new PixelGardenInventoryGUI(parent));
            else if (button.id == TAB_SELL) mc.displayGuiScreen(new PixelGardenSellGUI(parent));
            else if (button.id == TAB_UPGRADES) mc.displayGuiScreen(new PixelGardenUpgradeGUI(parent));
            else mc.displayGuiScreen(new PixelGardenCraftingGUI(parent));
            return;
        }
        if (page == Page.SELL && button.id >= PRODUCT_SELL_BASE
                && button.id < PRODUCT_SELL_BASE + PixelGardenGame.getCraftingRecipes().size()) {
            int recipeIndex = button.id - PRODUCT_SELL_BASE;
            PixelGardenGame.CraftingRecipe recipe = PixelGardenGame.getCraftingRecipes().get(recipeIndex);
            if (!game.sellAllCraftedProductBatches(recipe.id)) {
                if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                        "§6[Pixel Garden] §cCould not sell " + recipe.productName + "."));
            } else if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                        "§6[Pixel Garden] §fSold " + recipe.productName + " batches."));
            }
        } else if (page == Page.UPGRADES && button.id >= ACTION_BASE && button.id < ACTION_BASE + 11) {
            int upgrade = button.id - ACTION_BASE;
            String key = upgradeKey(upgrade);
            if ("prestige".equals(key)) { mc.displayGuiScreen(new PixelGardenPrestigeGUI(this)); return; }
            if ("auto_sell".equals(key) && game.hasAutoSell()) {
                game.toggleAutoSell();
                saveIfGardenParent();
                initGui();
                return;
            }
            try {
                JsonObject result = SupabaseCommunityService.purchasePixelGardenUpgrade(key);
                if (result.has("save_data") && result.get("save_data").isJsonObject()) game.loadSaveData(result.getAsJsonObject("save_data"));
            } catch (Exception exception) {
                System.err.println("[AlphaClient] Pixel Garden upgrade purchase failed: " + exception.getMessage());
                if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("§6[Pixel Garden] §cUpgrade purchase failed: " + (exception.getMessage() == null ? "unknown error" : exception.getMessage())));
            }
        } else if (button.id >= ACTION_BASE && button.id < ACTION_BASE + PixelGardenGame.Crop.values().length) {
            int index = button.id - ACTION_BASE;
            if (page == Page.SHOP) game.buySeeds(PixelGardenGame.Crop.values()[index], 1);
            else if (page == Page.SELL) game.sellAll(PixelGardenGame.Crop.values()[index]);
        }
        saveIfGardenParent();
        initGui();
    }

    private String upgradeKey(int index) {
        switch (index) {
            case 0: return "expansion";
            case 1: return "auto_harvest";
            case 2: return "auto_plant";
            case 3: return "auto_sell";
            case 4: return "auto_quest";
            case 5: return "growth_boost";
            case 6: return "more_coins";
            case 7: return "more_crops";
            case 8: return "prestige";
            case 9: return "crafting_capacity";
            default: return "crafting_queue";
        }
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

    private void drawProductIcon(PixelGardenGame.CraftingRecipe recipe, int x, int y, int size) {
        ResourceLocation texture = new ResourceLocation("alphastaffmod",
                "textures/gui/" + recipe.id + ".png");
        mc.getTextureManager().bindTexture(texture);
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(x, y, 0, 0, 16, 16, size, size, 16, 16);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    private void drawCropIcon(PixelGardenGame.Crop crop, int x, int y, int s) {
        if (crop == PixelGardenGame.Crop.MELON || crop == PixelGardenGame.Crop.CACTUS
                || crop == PixelGardenGame.Crop.SUGARCANE
                || crop == PixelGardenGame.Crop.BERRIES) {
            ResourceLocation cropTexture = new ResourceLocation("alphastaffmod",
                    "textures/gui/" + crop.name().toLowerCase(java.util.Locale.ROOT) + ".png");
            mc.getTextureManager().bindTexture(cropTexture);
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            net.minecraft.client.renderer.GlStateManager.enableBlend();
            drawScaledCustomSizeModalRect(x, y, 0, 0, 16, 16, s, s, 16, 16);
            net.minecraft.client.renderer.GlStateManager.disableBlend();
            return;
        }
        int spriteU = crop == PixelGardenGame.Crop.POTATO ? 32
                : crop == PixelGardenGame.Crop.CARROT ? 16 : crop.ordinal() * 16;
        drawSprite(x, y, s, s, spriteU, 0);
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
            int gap = CROP_ROW_GAP, cardWidth = (panelWidth - 28 - gap) / 2, cardHeight = cropCardHeight();
            clampCropScroll();
            int firstRow = cropScrollRow;
            int lastRow = Math.min(cropRowCount(), firstRow + visibleCropRows());
            if (page == Page.SELL) {
                List<SellEntry> entries = sellEntries();
                for (int i = 0; i < entries.size(); i++) {
                    int row = i / 2;
                    if (row < firstRow || row >= lastRow) continue;
                    SellEntry entry = entries.get(i);
                    int col = i % 2;
                    int x = left + 10 + col * (cardWidth + gap);
                    int y = 88 + (row - cropScrollRow) * (cardHeight + gap);
                    drawPanel(x, y, cardWidth, cardHeight);
                    if (entry.crop != null) {
                        PixelGardenGame.Crop crop = entry.crop;
                        drawCropIcon(crop, x + 8, y + 7, 24);
                        drawString(fontRendererObj, crop.displayName, x + 38, y + 10, 0xFFF0F2F6);
                        drawString(fontRendererObj, "Stock: " + game.getInventory(crop), x + 38, y + 25, 0xFFB7C4D5);
                        drawString(fontRendererObj, "Each: " + (int)Math.floor(crop.sellPrice * game.getCoinMultiplier()), x + 38, y + 38, 0xFFFFD45A);
                    } else {
                        PixelGardenGame.CraftingRecipe recipe = entry.recipe;
                        int batches = game.getCraftedProductBatches(recipe.id);
                        drawProductIcon(recipe, x + 8, y + 7, 24);
                        drawString(fontRendererObj, recipe.productName, x + 38, y + 10, 0xFFF0F2F6);
                        drawString(fontRendererObj, "Stock: " + batches + " batch(es)", x + 38, y + 25, 0xFFB7C4D5);
                        drawString(fontRendererObj, "Value: " + game.getCraftingBatchSaleValue(recipe) + " coins", x + 38, y + 38, 0xFFFFD45A);
                    }
                }
            } else if (page == Page.INVENTORY) {
                List<SellEntry> entries = sellEntries();
                for (int i = 0; i < entries.size(); i++) {
                    int row = i / 2;
                    if (row < firstRow || row >= lastRow) continue;
                    SellEntry entry = entries.get(i);
                    int col = i % 2;
                    int x = left + 10 + col * (cardWidth + gap);
                    int y = 88 + (row - cropScrollRow) * (cardHeight + gap);
                    drawPanel(x, y, cardWidth, cardHeight);
                    if (entry.crop != null) {
                        PixelGardenGame.Crop crop = entry.crop;
                        drawCropIcon(crop, x + 8, y + 7, 24);
                        drawString(fontRendererObj, crop.displayName, x + 38, y + 10, 0xFFF0F2F6);
                        drawString(fontRendererObj, "Seeds: " + game.getSeeds(crop), x + 38, y + 25, 0xFFB7C4D5);
                        drawString(fontRendererObj, "Harvest: " + game.getInventory(crop), x + 38, y + 38, 0xFFB7C4D5);
                    } else {
                        PixelGardenGame.CraftingRecipe recipe = entry.recipe;
                        drawProductIcon(recipe, x + 8, y + 7, 24);
                        drawString(fontRendererObj, recipe.productName, x + 38, y + 10, 0xFFF0F2F6);
                        drawString(fontRendererObj, "Quantity: " + game.getCraftedProductItems(recipe.id),
                                x + 38, y + 25, 0xFFB7C4D5);
                        drawString(fontRendererObj, "Crafted product", x + 38, y + 38, 0xFFFFD45A);
                    }
                }
            } else {
                for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
                    int row = crop.ordinal() / 2;
                    if (row < firstRow || row >= lastRow) continue;
                    int col = crop.ordinal() % 2;
                    int x = left + 10 + col * (cardWidth + gap);
                    int y = 88 + (row - cropScrollRow) * (cardHeight + gap);
                    drawPanel(x, y, cardWidth, cardHeight);
                    drawCropIcon(crop, x + 8, y + 7, 24);
                    drawString(fontRendererObj, crop.displayName, x + 38, y + 10, 0xFFF0F2F6);
                    drawString(fontRendererObj, "Seeds: " + game.getSeeds(crop), x + 38, y + 25, 0xFFB7C4D5);
                    drawString(fontRendererObj, "Price: " + crop.seedPrice, x + 38, y + 38, 0xFFFFD45A);
                }
            }
            int totalRows = cropRowCount();
            if (totalRows > visibleCropRows()) {
                String scrollLabel = (cropScrollRow > 0 ? "▲ " : "")
                        + (page == Page.SHOP ? "Mouse wheel to scroll crops" : "Mouse wheel to scroll inventory items")
                        + (cropScrollRow + visibleCropRows() < totalRows ? " ▼" : "");
                drawCenteredString(fontRendererObj, scrollLabel, width / 2, height - 53, 0xFFB7C4D5);
            }
        } else {
            int extensionNumber = Math.max(1, game.getUnlockedPlots() - PixelGardenGame.STARTING_PLOTS + 1);
            String[] names = {"Garden Extension " + extensionNumber, "Auto Harvest", "Auto Plant", "Auto Sell", "Auto Quest", "Growth Boost", "More Coins", "More Crops", "Prestige", "Crafting Capacity", "Crafting Queue"};
            String[] desc = {"Unlock the next plot", "Auto-collect ripe crops", "Plant available seeds automatically",
                    "Automatically sell harvested crops", "Deliver crops and claim quest rewards", "Faster growth",
                    "Sell multiplier: " + game.getCoinMultiplier() + "x", "Harvest yield: " + game.getCropYield() + "x", "Full reset for 20,000 coins",
                    "Active slots: " + game.getCraftingCapacity(), "Queued slots: " + game.getCraftingQueueCapacity()};
            int[] costs = {game.getUpgradeCost("expansion"), game.getUpgradeCost("auto_harvest"), game.getUpgradeCost("auto_plant"),
                    game.getUpgradeCost("auto_sell"), game.getUpgradeCost("auto_quest"), game.getUpgradeCost("growth_boost"),
                    game.getUpgradeCost("more_coins"), game.getUpgradeCost("more_crops"), game.getUpgradeCost("prestige"),
                    game.getUpgradeCost("crafting_capacity"), game.getUpgradeCost("crafting_queue")};
            int gap = 6, cardWidth = (panelWidth - 32 - gap * 2) / 3, cardHeight = 68;
            for (int i = 0; i < 11; i++) {
                int x = left + 10 + (i % 3) * (cardWidth + gap), y = 86 + (i / 3) * (cardHeight + gap);
                drawPanel(x, y, cardWidth, cardHeight);
                drawString(fontRendererObj, names[i], x + 7, y + 8, 0xFFF0F2F6);
                drawString(fontRendererObj, fontRendererObj.trimStringToWidth(desc[i], cardWidth - 14), x + 7, y + 25, 0xFFB7C4D5);
                String status = i == 3 && game.hasAutoSell()
                        ? (game.isAutoSellEnabled() ? "Auto Sell: ON" : "Auto Sell: OFF")
                        : costs[i] < 0 ? "OWNED / MAX" : "Cost: " + costs[i];
                drawString(fontRendererObj, status, x + 7, y + 39,
                        (costs[i] < 0 || (i == 3 && game.hasAutoSell())) ? 0xFF83D79A : 0xFFFFD45A);
                if (i < 5) drawUpgradeIcon(x + cardWidth - 30, y + 2, i);
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
