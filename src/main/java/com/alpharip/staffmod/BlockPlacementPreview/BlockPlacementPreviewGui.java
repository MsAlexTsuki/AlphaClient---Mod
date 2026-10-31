package com.alpharip.staffmod.BlockPlacementPreview;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Small in-game settings screen for the block placement preview. */
public final class BlockPlacementPreviewGui extends GuiScreen {
    private final GuiScreen parent;
    private int left, top, panelWidth, panelHeight;

    public BlockPlacementPreviewGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(340, Math.max(260, width - 24));
        panelHeight = 154;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int buttonWidth = panelWidth - 32;
        buttonList.add(new GuiButton(1, left + 16, top + 42, buttonWidth, 20,
                "Preview: " + onOff(BlockPlacementPreviewSettings.isEnabled())));
        buttonList.add(new GuiButton(2, left + 16, top + 66, buttonWidth, 20,
                "Collision colors: " + onOff(BlockPlacementPreviewSettings.isShowCollision())));
        buttonList.add(new GuiButton(3, left + 16, top + 90, buttonWidth, 20,
                "Opacity: " + Math.round(BlockPlacementPreviewSettings.getOpacity() * 100.0F) + "%"));
        buttonList.add(new GuiButton(4, left + panelWidth - 92, top + panelHeight - 25, 76, 18, "Done"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            BlockPlacementPreviewSettings.setEnabled(!BlockPlacementPreviewSettings.isEnabled());
        } else if (button.id == 2) {
            BlockPlacementPreviewSettings.setShowCollision(!BlockPlacementPreviewSettings.isShowCollision());
        } else if (button.id == 3) {
            float current = BlockPlacementPreviewSettings.getOpacity();
            float next = current >= 0.95F ? 0.0F : Math.min(1.0F, current + 0.1F);
            BlockPlacementPreviewSettings.setOpacity(next);
        } else if (button.id == 4) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "BLOCK PLACEMENT PREVIEW", left + 16, top + 13, 0xFFF2F3F5);
        drawString(fontRendererObj, "Green = clear · red = blocked", left + 16, top + 29, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String onOff(boolean enabled) { return enabled ? "On" : "Off"; }
    @Override public boolean doesGuiPauseGame() { return false; }
}
