// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Polished two-column settings screen for the Keystrokes HUD. */
public final class KeystrokesSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int BG = 2, CORNERS = 3, OPACITY_DOWN = 4, OPACITY_UP = 5;
    private static final int WASD = 10, SPACE = 11, MOUSE = 12, CPS = 13;
    private static final int SCALE_DOWN = 20, SCALE_UP = 21;
    private static final int ACTIVE_BASE = 100, INACTIVE_BASE = 110, TEXT_BASE = 120;
    private static final int[] COLORS = {0xFFFFD866, 0xFF55FF55, 0xFF55FFFF, 0xFFFF5555, 0xFFFFFFFF, 0xFFAA55FF, 0xFF343943};
    private static final String[] COLOR_NAMES = {"GOLD", "GREEN", "CYAN", "RED", "WHITE", "PURPLE", "SLATE"};
    private final GuiScreen parent;
    private int panelX, panelY;

    public KeystrokesSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        KeystrokesSettings.load();
        buttonList.clear();
        panelX = (width - 360) / 2;
        panelY = (height - 300) / 2;
        int left = panelX + 12, right = panelX + 188, top = panelY + 38;
        int colWidth = 160, rowHeight = 23;

        buttonList.add(new AlphaButton(WASD, left, top, colWidth, 20, state("WASD", KeystrokesSettings.showMovement()), false, KeystrokesSettings.showMovement()));
        buttonList.add(new AlphaButton(SPACE, left, top + rowHeight, colWidth, 20, state("SPACE", KeystrokesSettings.showSpace()), false, KeystrokesSettings.showSpace()));
        buttonList.add(new AlphaButton(MOUSE, left, top + rowHeight * 2, colWidth, 20, state("MOUSE BUTTONS", KeystrokesSettings.showMouse()), false, KeystrokesSettings.showMouse()));
        buttonList.add(new AlphaButton(CPS, left, top + rowHeight * 3, colWidth, 20, state("CPS COUNTERS", KeystrokesSettings.showCps()), false, KeystrokesSettings.showCps()));

        buttonList.add(new AlphaButton(BG, right, top, colWidth, 20, state("HUD BACKGROUND", KeystrokesSettings.hasBackground()), false, KeystrokesSettings.hasBackground()));
        buttonList.add(new AlphaButton(CORNERS, right, top + rowHeight, colWidth, 20, "CORNERS: " + (KeystrokesSettings.isRounded() ? "SOFT" : "SQUARE"), false, KeystrokesSettings.isRounded()));
        buttonList.add(new AlphaButton(OPACITY_DOWN, right, top + rowHeight * 2, 76, 20, "OPACITY -"));
        buttonList.add(new AlphaButton(OPACITY_UP, right + 84, top + rowHeight * 2, 76, 20, "OPACITY +"));
        buttonList.add(new AlphaButton(SCALE_DOWN, right, top + rowHeight * 3, 76, 20, "SCALE -"));
        buttonList.add(new AlphaButton(SCALE_UP, right + 84, top + rowHeight * 3, 76, 20, "SCALE +"));

        addPalette(ACTIVE_BASE, panelY + 158, "ACTIVE", KeystrokesSettings.getActiveColor());
        addPalette(INACTIVE_BASE, panelY + 198, "IDLE", KeystrokesSettings.getInactiveColor());
        addPalette(TEXT_BASE, panelY + 238, "TEXT", KeystrokesSettings.getTextColor());
        buttonList.add(new AlphaButton(BACK, panelX + 12, panelY + 274, 336, 18, "DONE", true));
    }

    private static String state(String name, boolean enabled) {
        return name + "  " + (enabled ? "ON" : "OFF");
    }

    private void addPalette(int baseId, int y, String label, int selectedColor) {
        int startX = panelX + 12;
        for (int i = 0; i < COLORS.length; i++) {
            boolean selected = COLORS[i] == selectedColor;
            String caption = selected ? "[" + COLOR_NAMES[i] + "]" : COLOR_NAMES[i];
            buttonList.add(new AlphaButton(baseId + i, startX + i * 48, y, 46, 19, caption, false, selected));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        int id = button.id;
        if (id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (id == WASD) KeystrokesSettings.toggleMovement();
        else if (id == SPACE) KeystrokesSettings.toggleSpace();
        else if (id == MOUSE) KeystrokesSettings.toggleMouse();
        else if (id == CPS) KeystrokesSettings.toggleCps();
        else if (id == BG) KeystrokesSettings.toggleBackground();
        else if (id == CORNERS) KeystrokesSettings.toggleRounded();
        else if (id == OPACITY_DOWN) KeystrokesSettings.adjustBackgroundAlpha(-15);
        else if (id == OPACITY_UP) KeystrokesSettings.adjustBackgroundAlpha(15);
        else if (id == SCALE_DOWN) HudScaleConfig.set("keystrokes", HudScaleConfig.get("keystrokes") - 0.1F);
        else if (id == SCALE_UP) HudScaleConfig.set("keystrokes", HudScaleConfig.get("keystrokes") + 0.1F);
        else if (id >= ACTIVE_BASE && id < ACTIVE_BASE + COLORS.length) KeystrokesSettings.setActiveColor(COLORS[id - ACTIVE_BASE]);
        else if (id >= INACTIVE_BASE && id < INACTIVE_BASE + COLORS.length) KeystrokesSettings.setInactiveColor(COLORS[id - INACTIVE_BASE]);
        else if (id >= TEXT_BASE && id < TEXT_BASE + COLORS.length) KeystrokesSettings.setTextColor(COLORS[id - TEXT_BASE]);
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xC8000000);
        drawRect(panelX, panelY, panelX + 360, panelY + 300, 0xF012141B);
        drawRect(panelX, panelY, panelX + 360, panelY + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "KEYSTROKES", width / 2, panelY + 12, 0xFFFFD866);
        drawString(fontRendererObj, "HUD COMPONENTS", panelX + 12, panelY + 27, 0xFF9DA4B1);
        drawString(fontRendererObj, "APPEARANCE", panelX + 188, panelY + 27, 0xFF9DA4B1);
        drawString(fontRendererObj, "ACTIVE KEY COLOR", panelX + 12, panelY + 148, 0xFF9DA4B1);
        drawString(fontRendererObj, "IDLE KEY COLOR", panelX + 12, panelY + 188, 0xFF9DA4B1);
        drawString(fontRendererObj, "TEXT COLOR", panelX + 12, panelY + 228, 0xFF9DA4B1);
        drawString(fontRendererObj, "Opacity: " + KeystrokesSettings.getBackgroundAlpha()
                + "    Scale: " + String.format("%.1f", HudScaleConfig.get("keystrokes")),
                panelX + 188, panelY + 138, 0xFFE7E9EE);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
