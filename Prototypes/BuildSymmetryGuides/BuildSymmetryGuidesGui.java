package com.alpharip.staffmod.buildsymmetryguides;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Standalone settings screen for the Build Symmetry Guides prototype. */
public final class BuildSymmetryGuidesGui extends GuiScreen {
    private final BuildSymmetryGuides module;
    private final GuiScreen parent;
    private int left, top, panelWidth, panelHeight;

    public BuildSymmetryGuidesGui(BuildSymmetryGuides module, GuiScreen parent) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(360, Math.max(280, width - 20));
        panelHeight = 260;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 16;
        int buttonWidth = panelWidth - 32;
        BuildSymmetryGuideConfig config = module.getConfig();
        buttonList.add(new GuiButton(1, x, top + 39, buttonWidth, 20,
                "Module: " + onOff(config.isEnabled())));
        buttonList.add(new GuiButton(2, x, top + 63, buttonWidth, 20,
                "Mirror plane: " + config.getAxis().getDisplayName()));
        buttonList.add(new GuiButton(3, x, top + 87, buttonWidth, 20,
                "Mirror block preview: " + onOff(config.isShowMirrorPreview())));
        buttonList.add(new GuiButton(4, x, top + 111, buttonWidth, 20,
                "Axis guide lines: " + onOff(config.isShowAxisLines())));
        buttonList.add(new GuiButton(5, x, top + 135, buttonWidth, 20,
                "Guide distance: " + config.getGuideDistance() + " blocks"));
        buttonList.add(new GuiButton(6, x, top + 159, buttonWidth, 20,
                "Overlay opacity: " + Math.round(config.getOpacity() * 100.0F) + "%"));
        buttonList.add(new GuiButton(7, x, top + 183, buttonWidth, 20,
                "Guide color: " + config.getColor().getDisplayName()));
        buttonList.add(new GuiButton(8, x, top + 207, buttonWidth, 20,
                "Set origin to current player position"));
        buttonList.add(new GuiButton(9, left + panelWidth - 92,
                top + panelHeight - 26, 76, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        BuildSymmetryGuideConfig config = module.getConfig();
        switch (button.id) {
            case 1: module.setEnabled(!config.isEnabled()); break;
            case 2: config.setAxis(config.getAxis().next()); break;
            case 3: config.setShowMirrorPreview(!config.isShowMirrorPreview()); break;
            case 4: config.setShowAxisLines(!config.isShowAxisLines()); break;
            case 5: config.setGuideDistance(config.getGuideDistance() >= 64
                    ? 8 : config.getGuideDistance() + 8); break;
            case 6: config.setOpacity(config.getOpacity() >= 0.95F
                    ? 0.25F : config.getOpacity() + 0.15F); break;
            case 7: config.setColor(config.getColor().next()); break;
            case 8: module.setOriginToPlayer(); break;
            case 9:
                module.saveSettings();
                mc.displayGuiScreen(parent);
                return;
            default: return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "BUILD SYMMETRY GUIDES",
                width / 2, top + 12, 0xFFF2F3F5);
        BuildSymmetryGuideConfig config = module.getConfig();
        drawString(fontRendererObj, "Origin: X " + config.getOriginX()
                + "  Z " + config.getOriginZ(), left + 16, top + 231, 0xFFB8BDC8);
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
