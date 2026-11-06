package com.alpharip.staffmod.blockbreakring;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone settings GUI with an interactive preview. */
public final class BlockBreakRingGui extends GuiScreen {
    private final GuiScreen parent;
    private final BlockBreakRingConfig config;
    private int left, top, panelWidth, panelHeight;

    public BlockBreakRingGui(GuiScreen parent, BlockBreakRingConfig config) {
        this.parent = parent;
        this.config = config;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(360, Math.max(280, width - 20));
        panelHeight = 218;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 16, w = panelWidth - 32;
        buttonList.add(new GuiButton(1, x, top + 42, w, 20, "Module: " + onOff(config.isEnabled())));
        buttonList.add(new GuiButton(2, x, top + 66, w, 20, "Color: " + config.getColorName()));
        buttonList.add(new GuiButton(3, x, top + 90, w, 20, "Line width: " + config.getLineWidth()));
        buttonList.add(new GuiButton(4, x, top + 114, w, 20, "Ring size: " + Math.round(config.getRadius() * 100) + "%"));
        buttonList.add(new GuiButton(5, x, top + 138, w, 20, "Opacity: " + Math.round(config.getOpacity() * 100) + "%"));
        buttonList.add(new GuiButton(6, x, top + 162, w, 20,
                "Background: " + onOff(config.isShowBackground()) + "  |  Smooth: " + onOff(config.isSmoothAnimation())));
        buttonList.add(new GuiButton(7, left + panelWidth - 92, top + panelHeight - 27, 76, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case 1: config.setEnabled(!config.isEnabled()); break;
            case 2: config.setColorIndex(config.getColorIndex() + 1); break;
            case 3: config.setLineWidth(config.getLineWidth() >= 12 ? 1 : config.getLineWidth() + 1); break;
            case 4: config.setRadius(config.getRadius() >= 0.48F ? 0.18F : config.getRadius() + 0.05F); break;
            case 5: config.setOpacity(config.getOpacity() >= 1.0F ? 0.2F : Math.min(1.0F, config.getOpacity() + 0.1F)); break;
            case 6:
                config.setShowBackground(!config.isShowBackground());
                config.setSmoothAnimation(!config.isSmoothAnimation());
                break;
            case 7: mc.displayGuiScreen(parent); return;
            default: return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "BLOCK BREAK PROGRESS RING", left + 16, top + 13, 0xFFF2F3F5);
        drawString(fontRendererObj, "Preview", left + 16, top + 190, 0xFFB8BDC8);
        drawPreview(left + panelWidth - 42, top + 194);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPreview(int cx, int cy) {
        int rgb = config.getColorRgb();
        int color = 0xFF000000 | rgb;
        int segments = 48;
        int radius = Math.round(config.getRadius() * 28.0F);
        if (config.isShowBackground()) drawCircle(cx, cy, radius, 1.0F, (color & 0x00FFFFFF) | 0x55000000, segments);
        drawCircle(cx, cy, radius, 0.68F, color, segments);
    }

    private void drawCircle(int cx, int cy, int radius, float progress, int color, int segments) {
        int lastX = 0, lastY = 0;
        for (int i = 0; i <= (int) (segments * progress); i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / segments;
            int px = cx + (int) Math.round(Math.cos(angle) * radius);
            int py = cy + (int) Math.round(Math.sin(angle) * radius);
            if (i > 0) drawRect(Math.min(lastX, px), Math.min(lastY, py),
                    Math.max(lastX, px) + 2, Math.max(lastY, py) + 2, color);
            lastX = px; lastY = py;
        }
    }

    private static String onOff(boolean value) { return value ? "On" : "Off"; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
