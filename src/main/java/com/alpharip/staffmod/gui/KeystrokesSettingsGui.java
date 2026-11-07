// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.keystrokes.KeystrokesMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Keystrokes settings corresponding to Lunar Client's documented controls. */
public final class KeystrokesSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int SHOW_CLICKS = 10, SHOW_CPS = 11, SHOW_SPACE = 12, SHOW_MOVEMENT = 13;
    private static final int ARROWS = 14, BORDER = 15, INNER_BORDER = 16, SHADOW = 17;
    private static final int FADE_DOWN = 20, FADE_UP = 21, SIZE_DOWN = 22, SIZE_UP = 23;
    private static final int BORDER_DOWN = 24, BORDER_UP = 25;
    private final GuiScreen parent;
    private int left, top, right, bottom;

    public KeystrokesSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 145;
        right = left + 290;
        top = Math.max(8, height / 2 - 145);
        bottom = Math.min(height - 8, top + 290);
        int x = left + 14, w = right - left - 28, y = top + 48;
        buttonList.add(new AlphaButton(SHOW_CLICKS, x, y, w, 18, label("SHOW CLICKS", KeystrokesMod.isShowClicks()))); y += 21;
        buttonList.add(new AlphaButton(SHOW_CPS, x, y, w, 18, label("SHOW LMB & RMB CPS", KeystrokesMod.isShowCps()))); y += 21;
        buttonList.add(new AlphaButton(SHOW_SPACE, x, y, w, 18, label("SHOW SPACE BAR", KeystrokesMod.isShowSpaceBar()))); y += 21;
        buttonList.add(new AlphaButton(SHOW_MOVEMENT, x, y, w, 18, label("SHOW MOVEMENT KEYS", KeystrokesMod.isShowMovementKeys()))); y += 21;
        buttonList.add(new AlphaButton(ARROWS, x, y, w, 18, label("REPLACE NAMES WITH ARROWS", KeystrokesMod.isUseArrows()))); y += 21;
        buttonList.add(new AlphaButton(BORDER, x, y, w, 18, label("OUTER BORDER", KeystrokesMod.isBorder()))); y += 21;
        buttonList.add(new AlphaButton(INNER_BORDER, x, y, w, 18, label("INNER BORDER", KeystrokesMod.isInnerBorder()))); y += 21;
        buttonList.add(new AlphaButton(SHADOW, x, y, w, 18, label("TEXT SHADOW", KeystrokesMod.isTextShadow()))); y += 24;
        buttonList.add(new AlphaButton(FADE_DOWN, x, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(FADE_UP, right - 44, y, 30, 18, "+")); y += 22;
        buttonList.add(new AlphaButton(SIZE_DOWN, x, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(SIZE_UP, right - 44, y, 30, 18, "+")); y += 22;
        buttonList.add(new AlphaButton(BORDER_DOWN, x, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(BORDER_UP, right - 44, y, 30, 18, "+"));
        buttonList.add(new AlphaButton(BACK, left + 12, bottom - 25, 72, 18, "BACK"));
    }

    private String label(String name, boolean enabled) { return name + ": " + (enabled ? "ON" : "OFF"); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "KEYSTROKES SETTINGS", width / 2, top + 12, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "AlphaClient color palette", width / 2, top + 28, 0xFFB8BDC8);
        int x = left + 50, y = top + 239;
        drawString(fontRendererObj, "KEY FADE DELAY: " + KeystrokesMod.getFadeDelay() + " ms", x, y, 0xFFF3F4F6);
        drawString(fontRendererObj, "BOX SIZE: " + KeystrokesMod.getBoxSize(), x, y + 22, 0xFFF3F4F6);
        drawString(fontRendererObj, "BORDER THICKNESS: " + KeystrokesMod.getBorderThickness(), x, y + 44, 0xFFF3F4F6);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK: mc.displayGuiScreen(parent); return;
            case SHOW_CLICKS: KeystrokesMod.setShowClicks(!KeystrokesMod.isShowClicks()); break;
            case SHOW_CPS: KeystrokesMod.setShowCps(!KeystrokesMod.isShowCps()); break;
            case SHOW_SPACE: KeystrokesMod.setShowSpaceBar(!KeystrokesMod.isShowSpaceBar()); break;
            case SHOW_MOVEMENT: KeystrokesMod.setShowMovementKeys(!KeystrokesMod.isShowMovementKeys()); break;
            case ARROWS: KeystrokesMod.setUseArrows(!KeystrokesMod.isUseArrows()); break;
            case BORDER: KeystrokesMod.setBorder(!KeystrokesMod.isBorder()); break;
            case INNER_BORDER: KeystrokesMod.setInnerBorder(!KeystrokesMod.isInnerBorder()); break;
            case SHADOW: KeystrokesMod.setTextShadow(!KeystrokesMod.isTextShadow()); break;
            case FADE_DOWN: KeystrokesMod.adjustFadeDelay(-25); break;
            case FADE_UP: KeystrokesMod.adjustFadeDelay(25); break;
            case SIZE_DOWN: KeystrokesMod.adjustBoxSize(-1); break;
            case SIZE_UP: KeystrokesMod.adjustBoxSize(1); break;
            case BORDER_DOWN: KeystrokesMod.adjustBorderThickness(-0.5F); break;
            case BORDER_UP: KeystrokesMod.adjustBorderThickness(0.5F); break;
            default: return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
