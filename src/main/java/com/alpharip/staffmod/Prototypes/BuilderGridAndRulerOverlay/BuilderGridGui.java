package com.alpharip.staffmod.buildergridandruleroverlay;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Standalone settings screen for the builder grid and ruler prototype. */
public final class BuilderGridGui extends GuiScreen {
    private final BuilderGridAndRulerOverlay module;
    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public BuilderGridGui(BuilderGridAndRulerOverlay module, GuiScreen parent) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(340, Math.max(280, width - 16));
        panelHeight = 220;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 12;
        int buttonWidth = panelWidth - 24;
        BuilderGridSettings settings = module.getSettings();

        addOption(1, x, top + 42, buttonWidth,
                "Build grid: " + onOff(settings.isGridEnabled()));
        addOption(2, x, top + 64, buttonWidth,
                "Ruler: " + onOff(settings.isRulerEnabled()));
        addOption(3, x, top + 86, buttonWidth,
                "Grid spacing: " + settings.getGridSpacing() + " block(s)");
        addOption(4, x, top + 108, buttonWidth,
                "Guide area radius: " + settings.getAreaRadius() + " blocks");
        addOption(5, x, top + 130, buttonWidth, "Set origin to current position");
        buttonList.add(new GuiButton(6, left + 12, top + 174,
                buttonWidth, 20, "Save and return"));
    }

    private void addOption(int id, int x, int y, int buttonWidth, String label) {
        buttonList.add(new GuiButton(id, x, y, buttonWidth, 20, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        BuilderGridSettings settings = module.getSettings();
        switch (button.id) {
            case 1:
                settings.setGridEnabled(!settings.isGridEnabled());
                break;
            case 2:
                settings.setRulerEnabled(!settings.isRulerEnabled());
                break;
            case 3:
                settings.setGridSpacing(nextSpacing(settings.getGridSpacing()));
                break;
            case 4:
                settings.setAreaRadius(settings.getAreaRadius() >= 64
                        ? 8 : settings.getAreaRadius() + 8);
                break;
            case 5:
                module.captureAnchorFromPlayer();
                break;
            case 6:
                module.saveSettings();
                mc.displayGuiScreen(parent);
                return;
            default:
                return;
        }
        initGui();
    }

    private static int nextSpacing(int current) {
        switch (current) {
            case 1: return 2;
            case 2: return 4;
            case 4: return 8;
            case 8: return 16;
            case 16: return 32;
            case 32: return 64;
            default: return 1;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "BUILDER GRID & RULER",
                width / 2, top + 10, 0xFFF2F3F5);
        BuilderGridAnchor anchor = module.getAnchor();
        String origin = anchor.isInitialized()
                ? "Origin  X: " + anchor.getX() + "  Y: " + anchor.getY()
                    + "  Z: " + anchor.getZ()
                : "Origin will be set at first world render";
        drawCenteredString(fontRendererObj, origin,
                width / 2, top + 26, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String onOff(boolean value) { return value ? "On" : "Off"; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            module.saveSettings();
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
