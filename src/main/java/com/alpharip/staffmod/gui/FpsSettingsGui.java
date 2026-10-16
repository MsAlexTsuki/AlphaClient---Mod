package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Settings for the compact FPS HUD; optimization follows the module state automatically. */
public final class FpsSettingsGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int FPS = 2;
    private static final int FRAME = 3;
    private static final int BACKGROUND = 4;
    private static final int OPTIMIZE = 5;
    private static final int ENABLE = 6;
    private final GuiScreen parent;
    private int left, top, right, bottom;

    public FpsSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 105);
        bottom = Math.min(height - 12, top + 210);
        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 55, 260, 20, moduleLabel()));
        buttonList.add(new AlphaButton(FPS, left + 20, top + 81, 260, 20, fpsLabel()));
        buttonList.add(new AlphaButton(FRAME, left + 20, top + 107, 260, 20, frameLabel()));
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 133, 260, 20, backgroundLabel()));
        buttonList.add(new AlphaButton(OPTIMIZE, left + 20, top + 159, 260, 20, optimizeLabel()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    private String moduleLabel() { AlphaModule module = AlphaModuleRegistry.find("fps"); return "FPS MODULE: " + (module != null && module.isEnabled() ? "ON" : "OFF"); }
    private String fpsLabel() { return "FPS COUNTER: " + (FpsMod.isShowFps() ? "ON" : "OFF"); }
    private String frameLabel() { return "MIN FRAME TIME: " + (FpsMod.isShowFrameTime() ? "ON" : "OFF"); }
    private String backgroundLabel() { return "HUD BACKGROUND: " + (FpsMod.isShowBackground() ? "ON" : "OFF"); }
    private String optimizeLabel() { AlphaModule module = AlphaModuleRegistry.find("fps"); return "AUTO OPTIMIZATION: " + (module != null && module.isEnabled() ? "ON" : "OFF"); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FPS SETTINGS", width / 2, top + 16, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Compact HUD with automatic FPS optimization", width / 2, top + 37, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK: mc.displayGuiScreen(parent); return;
            case ENABLE: { AlphaModule module = AlphaModuleRegistry.find("fps"); if (module != null) module.setEnabled(!module.isEnabled()); break; }
            case FPS: FpsMod.setShowFps(!FpsMod.isShowFps()); break;
            case FRAME: FpsMod.setShowFrameTime(!FpsMod.isShowFrameTime()); break;
            case BACKGROUND: FpsMod.setShowBackground(!FpsMod.isShowBackground()); break;
            case OPTIMIZE: return;
            default: return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
