// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Crafting recipes, active orders, queue and manual crafted-product sales. */
public final class PixelGardenCraftingGUI extends GuiScreen {
    private static final int CRAFT_BASE = 1000;
    private static final int SELL_BASE = 1100;
    private static final int TAB_RECIPES = 1200, TAB_PRODUCTS = 1201, BACK = 1202;
    private static final int CARD_HEIGHT = 68, CARD_GAP = 6;
    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private boolean productsPage;
    private int scrollRow;
    private long lastRefresh;

    public PixelGardenCraftingGUI(GuiScreen parent) { this.parent = parent; }

    private List<PixelGardenGame.CraftingRecipe> visibleProducts() {
        List<PixelGardenGame.CraftingRecipe> result = new ArrayList<PixelGardenGame.CraftingRecipe>();
        for (PixelGardenGame.CraftingRecipe recipe : PixelGardenGame.getCraftingRecipes()) {
            if (game.getCraftedProductBatches(recipe.id) > 0) result.add(recipe);
        }
        return result;
    }

    private int rowCount() {
        int count = productsPage ? visibleProducts().size() : PixelGardenGame.getCraftingRecipes().size();
        return (count + 1) / 2;
    }

    private int visibleRows() {
        return Math.max(1, (height - 168) / (CARD_HEIGHT + CARD_GAP));
    }

    private void clampScroll() {
        scrollRow = Math.max(0, Math.min(Math.max(0, rowCount() - visibleRows()), scrollRow));
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollRow += wheel < 0 ? 1 : -1;
            clampScroll();
            initGui();
        }
    }

    @Override public void initGui() {
        buttonList.clear();
        game.processTime(System.currentTimeMillis());
        clampScroll();
        int left = 24;
        int gap = 8;
        int cardWidth = Math.max(120, (width - 48 - gap) / 2);
        int first = scrollRow * 2;
        int count = productsPage ? visibleProducts().size() : PixelGardenGame.getCraftingRecipes().size();
        int last = Math.min(count, first + visibleRows() * 2);
        for (int index = first; index < last; index++) {
            int col = index % 2;
            int row = index / 2 - scrollRow;
            int x = left + col * (cardWidth + gap);
            int y = 99 + row * (CARD_HEIGHT + CARD_GAP);
            int id = productsPage ? SELL_BASE + index : CRAFT_BASE + index;
            String label = productsPage ? "Sell all" : "Craft / Queue";
            buttonList.add(new AlphaButton(id, x + 8, y + 48, cardWidth - 16, 16, label));
        }
        int navY = height - 32;
        int navW = Math.min(130, (width - 64) / 3);
        int navX = (width - (navW * 3 + 16)) / 2;
        buttonList.add(new AlphaButton(TAB_RECIPES, navX, navY, navW, 20, "Recipes"));
        buttonList.add(new AlphaButton(TAB_PRODUCTS, navX + navW + 8, navY, navW, 20, "Crafted Products"));
        buttonList.add(new AlphaButton(BACK, navX + (navW + 8) * 2, navY, navW, 20, "Garden"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == TAB_RECIPES || button.id == TAB_PRODUCTS) {
            productsPage = button.id == TAB_PRODUCTS;
            scrollRow = 0;
            initGui();
            return;
        }
        if (button.id == BACK) {
            saveParent();
            mc.displayGuiScreen(parent);
            return;
        }
        if (!productsPage && button.id >= CRAFT_BASE && button.id < CRAFT_BASE + PixelGardenGame.getCraftingRecipes().size()) {
            PixelGardenGame.CraftingRecipe recipe = PixelGardenGame.getCraftingRecipes().get(button.id - CRAFT_BASE);
            if (!game.queueCraftingOrder(recipe.id, System.currentTimeMillis())) {
                notifyPlayer("Crafting order not added. The queue may be full.");
            } else {
                notifyPlayer("Crafting order added: " + recipe.productName + ".");
            }
            saveParent();
            initGui();
            return;
        }
        if (productsPage && button.id >= SELL_BASE) {
            List<PixelGardenGame.CraftingRecipe> products = visibleProducts();
            int index = button.id - SELL_BASE;
            if (index >= 0 && index < products.size()) {
                PixelGardenGame.CraftingRecipe recipe = products.get(index);
                if (!game.sellAllCraftedProductBatches(recipe.id)) notifyPlayer("Could not sell " + recipe.productName + ".");
                else notifyPlayer("Sold " + recipe.productName + " batches.");
            }
            saveParent();
            initGui();
        }
    }

    private void notifyPlayer(String message) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(
                new net.minecraft.util.ChatComponentText("§6[Pixel Garden] §f" + message));
    }

    private void saveParent() {
        if (parent instanceof PixelGardenGUI) ((PixelGardenGUI) parent).saveCloudAsync();
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xF20B111A);
        drawRect(x, y, x + w, y + 2, 0xFFFFC83D);
        drawRect(x, y + h - 2, x + w, y + h, 0xFF714323);
        drawRect(x, y, x + 2, y + h, 0xFF714323);
        drawRect(x + w - 2, y, x + w, y + h, 0xFF714323);
    }

    private void drawProductIcon(PixelGardenGame.CraftingRecipe recipe, int x, int y) {
        ResourceLocation texture = new ResourceLocation("alphastaffmod",
                "textures/gui/" + recipe.id + ".png");
        mc.getTextureManager().bindTexture(texture);
        net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        drawScaledCustomSizeModalRect(x, y, 0, 0, 16, 16, 26, 26, 16, 16);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    private String ingredientText(PixelGardenGame.CraftingRecipe recipe) {
        StringBuilder text = new StringBuilder();
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            int amount = recipe.getCropIngredient(crop);
            if (amount <= 0) continue;
            if (text.length() > 0) text.append(" + ");
            text.append(amount).append(" ").append(crop.displayName);
        }
        for (java.util.Map.Entry<String, Integer> entry : recipe.getProductIngredients().entrySet()) {
            PixelGardenGame.CraftingRecipe ingredient = PixelGardenGame.getCraftingRecipe(entry.getKey());
            if (ingredient == null) continue;
            if (text.length() > 0) text.append(" + ");
            text.append(entry.getValue()).append(" ").append(ingredient.productName);
        }
        return text.toString();
    }

    private String formatTime(long millis) {
        long seconds = Math.max(0L, (millis + 999L) / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainder = seconds % 60L;
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m " + remainder + "s";
        return remainder + "s";
    }

    private void drawOrdersSummary() {
        int activeCapacity = game.getCraftingCapacity();
        int queueCapacity = game.getCraftingQueueCapacity();
        List<PixelGardenGame.CraftingOrder> active = game.getActiveCraftingOrders();
        List<String> queued = game.getQueuedCraftingOrders();
        drawString(fontRendererObj, "Active: " + active.size() + "/" + activeCapacity
                + "    Queue: " + queued.size() + "/" + queueCapacity, 34, 59, 0xFFFFD45A);
        StringBuilder detail = new StringBuilder("Running: ");
        long now = System.currentTimeMillis();
        for (int i = 0; i < active.size(); i++) {
            PixelGardenGame.CraftingOrder order = active.get(i);
            PixelGardenGame.CraftingRecipe recipe = PixelGardenGame.getCraftingRecipe(order.recipeId);
            if (i > 0) detail.append(" | ");
            detail.append(recipe == null ? order.recipeId : recipe.productName)
                    .append(" (").append(formatTime(order.completesAt - now)).append(")");
        }
        if (active.isEmpty()) detail.append("None");
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(detail.toString(), width - 68),
                34, 73, 0xFFB7C4D5);
    }

    private void drawRecipeCards(int mouseX, int mouseY) {
        int left = 24, gap = 8, cardWidth = Math.max(120, (width - 48 - gap) / 2);
        int first = scrollRow * 2;
        List<PixelGardenGame.CraftingRecipe> recipes = productsPage
                ? visibleProducts() : PixelGardenGame.getCraftingRecipes();
        int last = Math.min(recipes.size(), first + visibleRows() * 2);
        for (int index = first; index < last; index++) {
            PixelGardenGame.CraftingRecipe recipe = recipes.get(index);
            int col = index % 2, row = index / 2 - scrollRow;
            int x = left + col * (cardWidth + gap);
            int y = 99 + row * (CARD_HEIGHT + CARD_GAP);
            drawPanel(x, y, cardWidth, CARD_HEIGHT);
            drawProductIcon(recipe, x + 8, y + 7);
            drawString(fontRendererObj, recipe.productName, x + 40, y + 7, 0xFFF0F2F6);
            if (!productsPage) {
                drawString(fontRendererObj, fontRendererObj.trimStringToWidth("Needs: " + ingredientText(recipe), cardWidth - 52),
                        x + 40, y + 20, 0xFFB7C4D5);
                drawString(fontRendererObj, "Makes: " + recipe.outputQuantity + "  |  Time: " + formatTime(recipe.durationMillis),
                        x + 8, y + 35, 0xFFE3E7EE);
                drawString(fontRendererObj, "Batch sale: " + recipe.sellingPrice + " coins",
                        x + cardWidth - 126, y + 35, 0xFFFFD45A);
            } else {
                int batches = game.getCraftedProductBatches(recipe.id);
                drawString(fontRendererObj, "Stock: " + batches + " batches (" + (batches * recipe.outputQuantity) + " items)",
                        x + 40, y + 22, 0xFFB7C4D5);
                drawString(fontRendererObj, "Batch sale: " + recipe.sellingPrice + " coins",
                        x + 40, y + 36, 0xFFFFD45A);
            }
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long now = System.currentTimeMillis();
        game.processTime(now);
        if (now - lastRefresh >= 1000L) { lastRefresh = now; initGui(); }
        drawDefaultBackground();
        drawRect(0, 0, width, 50, 0xF00B1018);
        drawRect(0, 48, width, 52, 0xFFFFC83D);
        drawString(fontRendererObj, "PIXEL", 20, 18, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 60, 18, 0xFFFFC83D);
        drawString(fontRendererObj, productsPage ? " / CRAFTED PRODUCTS" : " / CRAFTING",
                130, 18, 0xFFE3E7EE);
        drawString(fontRendererObj, "Coins: " + game.getCoins(), width - 115, 18, 0xFFFFD45A);
        drawPanel(18, 54, width - 36, height - 94);
        if (!productsPage) {
            drawOrdersSummary();
            drawString(fontRendererObj, "Recipes  •  ingredients are consumed when an order starts",
                    34, 86, 0xFFB7C4D5);
        } else {
            drawString(fontRendererObj, "Manual sales only  •  More Coins applies to every crafted product",
                    34, 65, 0xFFB7C4D5);
        }
        if (rowCount() > visibleRows()) {
            String hint = "Mouse wheel to scroll";
            drawCenteredString(fontRendererObj, hint, width / 2, height - 49, 0xFFB7C4D5);
        }
        if (productsPage && visibleProducts().isEmpty()) {
            drawCenteredString(fontRendererObj, "No crafted products in your inventory yet.", width / 2, height / 2, 0xFFB7C4D5);
        } else {
            drawRecipeCards(mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void onGuiClosed() { saveParent(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
