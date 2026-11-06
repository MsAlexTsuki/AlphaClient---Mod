// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

/** Recipe price comparison showing current ingredient sale value and crafted batch value. */
public final class PixelGardenPriceInfoGUI extends GuiScreen {
    private static final int BACK = 1300;
    private static final int CARD_HEIGHT = 82;
    private static final int CARD_GAP = 7;
    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private int scrollRow;

    public PixelGardenPriceInfoGUI(GuiScreen parent) { this.parent = parent; }

    private int rowCount() { return (PixelGardenGame.getCraftingRecipes().size() + 1) / 2; }
    private int visibleRows() { return Math.max(1, (height - 154) / (CARD_HEIGHT + CARD_GAP)); }
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
        clampScroll();
        buttonList.add(new AlphaButton(BACK, (width - 120) / 2, height - 34, 120, 20, "Garden"));
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xF20B111A);
        drawRect(x, y, x + w, y + 2, 0xFFFFC83D);
        drawRect(x, y + h - 2, x + w, y + h, 0xFF714323);
        drawRect(x, y, x + 2, y + h, 0xFF714323);
        drawRect(x + w - 2, y, x + w, y + h, 0xFF714323);
    }

    private String ingredientText(PixelGardenGame.CraftingRecipe recipe) {
        StringBuilder value = new StringBuilder();
        for (PixelGardenGame.Crop crop : PixelGardenGame.Crop.values()) {
            int amount = recipe.getCropIngredient(crop);
            if (amount <= 0) continue;
            if (value.length() > 0) value.append(" + ");
            value.append(amount).append(" ").append(crop == PixelGardenGame.Crop.BERRIES && amount == 1
                    ? "Berry" : crop.displayName);
        }
        for (java.util.Map.Entry<String, Integer> entry : recipe.getProductIngredients().entrySet()) {
            PixelGardenGame.CraftingRecipe product = PixelGardenGame.getCraftingRecipe(entry.getKey());
            if (product == null) continue;
            if (value.length() > 0) value.append(" + ");
            value.append(entry.getValue()).append(" ").append(product.productName);
        }
        return value.length() == 0 ? "No ingredients" : value.toString();
    }

    private void drawCards() {
        List<PixelGardenGame.CraftingRecipe> recipes = PixelGardenGame.getCraftingRecipes();
        int left = Math.max(18, width / 2 - 270);
        int panelWidth = Math.min(540, width - 36);
        int gap = 8;
        int cardWidth = (panelWidth - 28 - gap) / 2;
        int first = scrollRow * 2;
        int last = Math.min(recipes.size(), first + visibleRows() * 2);
        for (int index = first; index < last; index++) {
            PixelGardenGame.CraftingRecipe recipe = recipes.get(index);
            int col = index % 2;
            int row = index / 2 - scrollRow;
            int x = left + 10 + col * (cardWidth + gap);
            int y = 92 + row * (CARD_HEIGHT + CARD_GAP);
            int cropValue = game.getCraftingCropIngredientValue(recipe);
            int productValue = game.getCraftingProductIngredientValue(recipe);
            int inputValue = cropValue + productValue;
            int batchValue = game.getCraftingBatchSaleValue(recipe);
            drawPanel(x, y, cardWidth, CARD_HEIGHT);
            drawString(fontRendererObj, recipe.productName, x + 8, y + 7, 0xFFF0F2F6);
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(ingredientText(recipe), cardWidth - 16),
                    x + 8, y + 21, 0xFFB7C4D5);
            drawString(fontRendererObj, "Output: " + recipe.outputQuantity + "  |  Time: "
                    + formatTime(recipe.durationMillis), x + 8, y + 35, 0xFFE3E7EE);
            drawString(fontRendererObj, "Crop value: " + cropValue + "  |  Crafted inputs: " + productValue,
                    x + 8, y + 49, 0xFFB7C4D5);
            drawString(fontRendererObj, "Input total: " + inputValue + "  |  Batch sale: " + batchValue,
                    x + 8, y + 64, batchValue >= inputValue ? 0xFF83D79A : 0xFFFF7777);
        }
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

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null || button.id != BACK) return;
        if (parent instanceof PixelGardenGUI) ((PixelGardenGUI) parent).saveCloudAsync();
        mc.displayGuiScreen(parent);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        game.processTime(System.currentTimeMillis());
        drawDefaultBackground();
        drawRect(0, 0, width, 50, 0xF00B1018);
        drawRect(0, 48, width, 52, 0xFFFFC83D);
        drawString(fontRendererObj, "PIXEL", 20, 18, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 60, 18, 0xFFFFC83D);
        drawString(fontRendererObj, " / RECIPE PRICE INFO", 130, 18, 0xFFE3E7EE);
        drawString(fontRendererObj, "Coins: " + game.getCoins(), width - 115, 18, 0xFFFFD45A);
        int left = Math.max(18, width / 2 - 270);
        int panelWidth = Math.min(540, width - 36);
        drawPanel(left, 58, panelWidth, height - 98);
        drawString(fontRendererObj, "Current sale values include your More Coins multiplier.",
                left + 12, 68, 0xFFB7C4D5);
        drawCards();
        if (rowCount() > visibleRows())
            drawCenteredString(fontRendererObj, "Mouse wheel to scroll recipes", width / 2, height - 50, 0xFFB7C4D5);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void onGuiClosed() {
        if (parent instanceof PixelGardenGUI) {
            ((PixelGardenGUI) parent).saveCloudAsync();
            return;
        }
        if (!SupabaseCommunityService.isSignedIn()) return;
        final String account = SupabaseCommunityService.getSignedInEmail();
        final JsonObject snapshot = game.toSaveData();
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (SupabaseCommunityService.isSignedIn()
                            && (account == null || account.equals(SupabaseCommunityService.getSignedInEmail())))
                        SupabaseCommunityService.savePixelGardenSave(snapshot);
                } catch (Exception exception) {
                    System.err.println("[AlphaClient] Pixel Garden price info save failed: " + exception.getMessage());
                }
            }
        }, "AlphaClient-PixelGardenPriceInfoSave");
        thread.setDaemon(true);
        thread.start();
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
