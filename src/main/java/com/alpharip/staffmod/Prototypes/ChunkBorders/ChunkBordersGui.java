package com.alpharip.staffmod.chunkborders;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Standalone settings screen for the Chunk Borders prototype. */
public final class ChunkBordersGui extends GuiScreen {
    private final ChunkBorders module;
    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public ChunkBordersGui(ChunkBorders module, GuiScreen parent) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(340, Math.max(280, width - 16));
        panelHeight = 238;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 12;
        int buttonWidth = panelWidth - 24;
        ChunkBordersSettings settings = module.getSettings();

        addOption(1, x, top + 42, buttonWidth,
                "Chunk borders: " + onOff(settings.isEnabled()));
        addOption(2, x, top + 64, buttonWidth,
                "Style: " + settings.getStyle().getDisplayName());
        addOption(3, x, top + 86, buttonWidth,
                "Render radius: " + settings.getRenderRadius() + " chunks");
        addOption(4, x, top + 108, buttonWidth,
                "Vertical corner pillars: " + onOff(settings.isShowVerticalPillars()));
        addOption(5, x, top + 130, buttonWidth,
                "Horizontal grid: " + onOff(settings.isShowHorizontalGrid()));
        addOption(6, x, top + 152, buttonWidth,
                "Highlight current chunk: " + onOff(settings.isHighlightCurrentChunk()));
        addOption(7, x, top + 174, buttonWidth,
                "Opacity: " + Math.round(settings.getOpacity() * 100.0F) + "%");
        buttonList.add(new GuiButton(8, left + 12, top + 204,
                buttonWidth, 20, "Save and return"));
    }

    private void addOption(int id, int x, int y, int buttonWidth, String label) {
        buttonList.add(new GuiButton(id, x, y, buttonWidth, 20, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        ChunkBordersSettings settings = module.getSettings();
        switch (button.id) {
            case 1:
                settings.setEnabled(!settings.isEnabled());
                break;
            case 2:
                settings.setStyle(settings.getStyle().next());
                break;
            case 3:
                settings.setRenderRadius(settings.getRenderRadius() >= 8
                        ? 1 : settings.getRenderRadius() + 1);
                break;
            case 4:
                settings.setShowVerticalPillars(!settings.isShowVerticalPillars());
                break;
            case 5:
                settings.setShowHorizontalGrid(!settings.isShowHorizontalGrid());
                break;
            case 6:
                settings.setHighlightCurrentChunk(!settings.isHighlightCurrentChunk());
                break;
            case 7:
                settings.setOpacity(settings.getOpacity() >= 0.95F
                        ? 0.25F : settings.getOpacity() + 0.15F);
                break;
            case 8:
                module.saveSettings();
                mc.displayGuiScreen(parent);
                return;
            default:
                return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "CHUNK BORDERS",
                width / 2, top + 10, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "World diagnostics overlay",
                width / 2, top + 25, 0xFFB8BDC8);
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
