package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Settings for the compact FPS counter; performance optimization is automatic. */
public final class FpsSettingsGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int FPS = 2;
    private static final int BACKGROUND = 3;
    private static final int ENABLE = 4;
    private final GuiScreen parent;
    private int left, top, right, bottom;

    public FpsSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 82);
        bottom = Math.min(height - 12, top + 164);
        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 52, 260, 20, moduleLabel()));
        buttonList.add(new AlphaButton(FPS, left + 20, top + 78, 260, 20, fpsLabel()));
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 104, 260, 20, backgroundLabel()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    private String moduleLabel() { AlphaModule module = AlphaModuleRegistry.find("fps"); return "FPS MODULE: " + (module != null && module.isEnabled() ? "ON" : "OFF"); }
    private String fpsLabel() { return "FPS COUNTER: " + (FpsMod.isShowFps() ? "ON" : "OFF"); }
    private String backgroundLabel() { return "HUD BACKGROUND: " + (FpsMod.isShowBackground() ? "ON" : "OFF"); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FPS SETTINGS", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Automatic client-wide FPS optimization", width / 2, top + 34, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK: mc.displayGuiScreen(parent); return;
            case ENABLE: { AlphaModule module = AlphaModuleRegistry.find("fps"); if (module != null) module.setEnabled(!module.isEnabled()); break; }
            case FPS: FpsMod.setShowFps(!FpsMod.isShowFps()); break;
            case BACKGROUND: FpsMod.setShowBackground(!FpsMod.isShowBackground()); break;
            default: return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
