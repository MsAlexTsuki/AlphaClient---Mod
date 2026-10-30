// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Pixel Garden-styled Prestige Coins shop with server-backed purchases. */
public final class PixelGardenPrestigeUpgradesGUI extends GuiScreen {
    public static final String MENU_ID = "menu.pixelgarden.prestige";
    public static final String PIXEL_GARDEN_ID = "pixelgarden";
    private static final int YIELD = 1, BONUS = 2, COIN_CACHE = 3, REFRESH = 4, BACK = 5;

    private final GuiScreen parent;
    private JsonObject state;
    private String status = "";
    private boolean busy;

    public PixelGardenPrestigeUpgradesGUI(GuiScreen parent) {
        this.parent = parent;
    }

    public static GuiScreen forMenu(String menuId, String gardenId, GuiScreen parent) {
        if (!MENU_ID.equals(menuId) || !PIXEL_GARDEN_ID.equals(gardenId)) {
            throw new IllegalArgumentException("Unknown Pixel Garden menu identifier.");
        }
        return new PixelGardenPrestigeUpgradesGUI(parent);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(560, width - 36);
        int left = (width - panelWidth) / 2;
        int cardWidth = (panelWidth - 36) / 3;
        int cardsY = 150;
        int buttonY = cardsY + 78;
        buttonList.add(new PrestigeButton(YIELD, left + 10, buttonY, cardWidth - 4, 20, "Buy next level"));
        buttonList.add(new PrestigeButton(BONUS, left + 18 + cardWidth, buttonY, cardWidth - 4, 20, "Buy next level"));
        buttonList.add(new PrestigeButton(COIN_CACHE, left + 26 + cardWidth * 2, buttonY, cardWidth - 4, 20, "Buy next level"));
        int navWidth = Math.min(100, (width - 60) / 2);
        int navY = height - 34;
        buttonList.add(new PrestigeButton(REFRESH, width / 2 - navWidth - 4, navY, navWidth, 20, "Refresh"));
        buttonList.add(new PrestigeButton(BACK, width / 2 + 4, navY, navWidth, 20, "Garden"));
        refreshState();
        updateButtons();
    }

    private void refreshState() {
        try {
            state = SupabaseCommunityService.getPixelGardenPrestigeState();
            status = "";
        } catch (Exception exception) {
            status = "Could not load Prestige data: " + safeMessage(exception);
        }
    }

    private void updateButtons() {
        for (GuiButton button : buttonList) button.enabled = !busy;
        if (state == null) return;

        JsonObject upgrades = state.has("upgrades") && state.get("upgrades").isJsonObject()
                ? state.getAsJsonObject("upgrades") : new JsonObject();
        int[] levels = { level(upgrades, "prestige_yield"), level(upgrades, "prestige_bonus"), level(upgrades, "coin_cache") };
        int[] costs = { 25, 40, 30 };
        for (int i = 0; i < 3; i++) {
            GuiButton button = buttonById(i + 1);
            if (button == null) continue;
            button.displayString = levels[i] >= 5 ? "MAX LEVEL"
                    : "Buy L" + (levels[i] + 1) + " (" + (costs[i] * (levels[i] + 1)) + " PC)";
            button.enabled = !busy && levels[i] < 5 && num(state, "prestige_count") > 0
                    && num(state, "upgrade_steps") >= 18;
        }
    }

    private GuiButton buttonById(int id) {
        for (GuiButton button : buttonList) if (button.id == id) return button;
        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || busy) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == REFRESH) {
            refreshState();
            updateButtons();
            return;
        }

        String key = button.id == YIELD ? "prestige_yield"
                : button.id == BONUS ? "prestige_bonus"
                : button.id == COIN_CACHE ? "coin_cache" : null;
        if (key == null) return;

        busy = true;
        updateButtons();
        try {
            SupabaseCommunityService.purchasePixelGardenPrestigeUpgrade(key);
            refreshState();
            status = "Prestige upgrade purchased.";
        } catch (Exception exception) {
            status = "Purchase failed: " + safeMessage(exception);
        } finally {
            busy = false;
            updateButtons();
        }
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, 0xF20B111A);
        drawRect(x, y, x + w, y + 3, 0xFFFFC83D);
        drawRect(x, y + h - 3, x + w, y + h, 0xFF714323);
        drawRect(x, y, x + 3, y + h, 0xFF714323);
        drawRect(x + w - 3, y, x + w, y + h, 0xFF714323);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 50, 0xF00B1018);
        drawRect(0, 48, width, 52, 0xFFFFC83D);
        drawString(fontRendererObj, "PIXEL", 20, 18, 0xFFF3F5F8);
        drawString(fontRendererObj, "GARDEN", 60, 18, 0xFFFFC83D);
        drawString(fontRendererObj, " / PRESTIGE UPGRADES", 130, 18, 0xFFE3E7EE);

        int panelWidth = Math.min(560, width - 36);
        int left = (width - panelWidth) / 2;
        int panelTop = 62;
        int panelHeight = height - 102;
        drawPanel(left, panelTop, panelWidth, panelHeight);
        drawString(fontRendererObj, "PRESTIGE SHOP", left + 14, panelTop + 12, 0xFFFFD45A);

        if (state != null) {
            drawString(fontRendererObj, "Prestige Coins: " + value(state, "prestige_coins"), left + 14, panelTop + 30, 0xFFFFD45A);
            drawString(fontRendererObj, "Multiplier: " + value(state, "effective_multiplier") + "x (" + value(state, "multiplier_source") + ")",
                    left + 14, panelTop + 43, 0xFFE3E7EE);
            drawString(fontRendererObj, "Next reward: " + (num(state, "seconds_to_reward") / 60) + "m",
                    left + 14, panelTop + 56, 0xFFB7C4D5);
            drawString(fontRendererObj, "Upgrade steps: " + num(state, "upgrade_steps") + "/18   |   Prestiges: "
                    + num(state, "prestige_count"), left + 14, panelTop + 69, 0xFFB7C4D5);
        }

        int cardWidth = (panelWidth - 36) / 3;
        int cardGap = 8;
        int cardsY = 150;
        String[] titles = { "PRESTIGE YIELD", "PRESTIGE BONUS", "COIN CACHE" };
        String[] descriptions = { "+1 coin per reward", "+10% reward per level", "+250 coins after Prestige" };
        String[] keys = { "prestige_yield", "prestige_bonus", "coin_cache" };
        int[] costs = { 25, 40, 30 };
        JsonObject upgrades = state != null && state.has("upgrades") && state.get("upgrades").isJsonObject()
                ? state.getAsJsonObject("upgrades") : new JsonObject();
        for (int i = 0; i < 3; i++) {
            int x = left + 10 + i * (cardWidth + cardGap);
            drawPanel(x, cardsY, cardWidth, 110);
            drawCenteredString(fontRendererObj, titles[i], x + cardWidth / 2, cardsY + 12, 0xFFFFC83D);
            drawCenteredString(fontRendererObj, descriptions[i], x + cardWidth / 2, cardsY + 30, 0xFFE3E7EE);
            int level = level(upgrades, keys[i]);
            drawCenteredString(fontRendererObj, "Level " + level + "/5", x + cardWidth / 2, cardsY + 47, 0xFFB7C4D5);
            drawCenteredString(fontRendererObj, level >= 5 ? "MAX LEVEL" : "Cost: " + (costs[i] * (level + 1)) + " PC",
                    x + cardWidth / 2, cardsY + 61, 0xFFFFD45A);
        }

        if (!status.isEmpty()) {
            drawCenteredString(fontRendererObj, status, width / 2, height - 48,
                    status.startsWith("Could not") || status.startsWith("Purchase failed") ? 0xFFFF7777 : 0xFF9CE2AC);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static int level(JsonObject object, String key) {
        return Math.max(0, Math.min(5, num(object, key)));
    }

    private static int num(JsonObject object, String key) {
        try { return object != null && object.has(key) ? object.get(key).getAsInt() : 0; }
        catch (Exception ignored) { return 0; }
    }

    private static String value(JsonObject object, String key) {
        try { return object != null && object.has(key) ? object.get(key).getAsString() : "unavailable"; }
        catch (Exception ignored) { return "unavailable"; }
    }

    private static String safeMessage(Exception exception) {
        return exception.getMessage() == null ? "unknown error" : exception.getMessage();
    }

    private final class PrestigeButton extends GuiButton {
        PrestigeButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            int fill = !enabled ? 0xFF292D33 : hovered ? 0xFF39332A : 0xFF171B22;
            PixelGardenPrestigeUpgradesGUI.this.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, fill);
            PixelGardenPrestigeUpgradesGUI.this.drawRect(xPosition, yPosition, xPosition + width, yPosition + 1,
                    enabled ? 0xFFFFC83D : 0xFF555555);
            PixelGardenPrestigeUpgradesGUI.this.drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, 0xFF714323);
            PixelGardenPrestigeUpgradesGUI.this.drawCenteredString(fontRendererObj, displayString, xPosition + width / 2,
                    yPosition + (height - 8) / 2, enabled ? 0xFFF3F5F8 : 0xFF858B94);
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && !busy) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
