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
        int colW = (right - left - 38) / 2;
        int x1 = left + 12, x2 = left + 26 + colW;
        int[] ids = {SHOW_CLICKS, SHOW_CPS, SHOW_SPACE, SHOW_MOVEMENT, ARROWS, BORDER, INNER_BORDER, SHADOW};
        String[] names = {"SHOW CLICKS", "SHOW LMB & RMB CPS", "SHOW SPACE BAR", "SHOW MOVEMENT KEYS",
                "REPLACE NAMES WITH ARROWS", "OUTER BORDER", "INNER BORDER", "TEXT SHADOW"};
        boolean[] states = {KeystrokesMod.isShowClicks(), KeystrokesMod.isShowCps(), KeystrokesMod.isShowSpaceBar(),
                KeystrokesMod.isShowMovementKeys(), KeystrokesMod.isUseArrows(), KeystrokesMod.isBorder(),
                KeystrokesMod.isInnerBorder(), KeystrokesMod.isTextShadow()};
        for (int i = 0; i < ids.length; i++) {
            int x = (i % 2 == 0) ? x1 : x2;
            int y = top + 50 + (i / 2) * 23;
            buttonList.add(new AlphaButton(ids[i], x, y, colW, 19, label(names[i], states[i])));
        }
        int y = top + 151;
        buttonList.add(new AlphaButton(FADE_DOWN, x1, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(FADE_UP, right - 42, y, 30, 18, "+")); y += 28;
        buttonList.add(new AlphaButton(SIZE_DOWN, x1, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(SIZE_UP, right - 42, y, 30, 18, "+")); y += 28;
        buttonList.add(new AlphaButton(BORDER_DOWN, x1, y, 30, 18, "-"));
        buttonList.add(new AlphaButton(BORDER_UP, right - 42, y, 30, 18, "+"));
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
        int y = top + 155;
        drawCenteredString(fontRendererObj, "KEY FADE DELAY: " + KeystrokesMod.getFadeDelay() + " ms", width / 2, y + 4, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "BOX SIZE: " + KeystrokesMod.getBoxSize(), width / 2, y + 32, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "BORDER THICKNESS: " + KeystrokesMod.getBorderThickness(), width / 2, y + 60, 0xFFF3F4F6);
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
