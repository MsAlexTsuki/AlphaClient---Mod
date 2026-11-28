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
        panelWidth = Math.min(360, Math.max(260, width - 20));
        panelHeight = 362;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 16;
        int buttonWidth = panelWidth - 32;
        BuildSymmetryGuideConfig config = module.getConfig();
        addOption(1, x, top + 48, buttonWidth, "Module: " + onOff(config.isEnabled()));
        addOption(2, x, top + 70, buttonWidth,
                "Mirror plane: " + config.getAxis().getDisplayName());
        addOption(3, x, top + 92, buttonWidth,
                "Mirror block preview: " + onOff(config.isShowMirrorPreview()));
        addOption(4, x, top + 114, buttonWidth,
                "Axis guide lines: " + onOff(config.isShowAxisLines()));
        addOption(5, x, top + 136, buttonWidth,
                "Source-to-mirror lines: " + onOff(config.isShowConnectionLines()));
        addOption(6, x, top + 158, buttonWidth,
                "Guide distance: " + config.getGuideDistance() + " blocks");
        addOption(7, x, top + 180, buttonWidth,
                "Overlay opacity: " + Math.round(config.getOpacity() * 100.0F) + "%");
        addOption(8, x, top + 202, buttonWidth,
                "Guide color: " + config.getColor().getDisplayName());
        addOption(9, x, top + 224, buttonWidth, "Origin X: -1 block");
        addOption(10, x, top + 246, buttonWidth, "Origin X: +1 block");
        addOption(11, x, top + 268, buttonWidth, "Origin Z: -1 block");
        addOption(12, x, top + 290, buttonWidth, "Origin Z: +1 block");
        addOption(13, x, top + 312, buttonWidth,
                "Set origin to current player position");
        buttonList.add(new GuiButton(14, left + panelWidth - 92,
                top + panelHeight - 24, 76, 18, "Done"));
    }

    private void addOption(int id, int x, int y, int buttonWidth, String label) {
        buttonList.add(new GuiButton(id, x, y, buttonWidth, 20, label));
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
            case 5: config.setShowConnectionLines(!config.isShowConnectionLines()); break;
            case 6: config.setGuideDistance(config.getGuideDistance() >= 64
                    ? 8 : config.getGuideDistance() + 8); break;
            case 7: config.setOpacity(config.getOpacity() >= 0.95F
                    ? 0.25F : config.getOpacity() + 0.15F); break;
            case 8: config.setColor(config.getColor().next()); break;
            case 9: module.adjustOrigin(-1, 0); break;
            case 10: module.adjustOrigin(1, 0); break;
            case 11: module.adjustOrigin(0, -1); break;
            case 12: module.adjustOrigin(0, 1); break;
            case 13: module.setOriginToPlayer(); break;
            case 14:
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
                width / 2, top + 10, 0xFFF2F3F5);
        BuildSymmetryGuideConfig config = module.getConfig();
        drawCenteredString(fontRendererObj, "Origin  X: " + config.getOriginX()
                + "   Z: " + config.getOriginZ(), width / 2, top + 28, 0xFFB8BDC8);
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
