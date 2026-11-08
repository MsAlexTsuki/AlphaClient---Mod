// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.BlockPlacementPreview.BlockPlacementPreviewSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings screen for Block Placement Preview in the Quality category. */
public final class BlockPlacementPreviewSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;
    private static final int SHOW_COLLISION = 3;
    private static final int OPACITY_DOWN = 4;
    private static final int OPACITY_UP = 5;

    private final GuiScreen parent;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public BlockPlacementPreviewSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 160;
        right = left + 320;
        top = Math.max(12, height / 2 - 100);
        bottom = Math.min(height - 12, top + 200);

        boolean enabled = BlockPlacementPreviewSettings.isEnabled();
        boolean showCollision = BlockPlacementPreviewSettings.isShowCollision();
        int opacity = Math.round(BlockPlacementPreviewSettings.getOpacity() * 100.0F);

        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 68, 280, 20,
                "BLOCK PLACEMENT PREVIEW: " + (enabled ? "ON" : "OFF"), enabled));
        buttonList.add(new AlphaButton(SHOW_COLLISION, left + 20, top + 94, 280, 20,
                "SHOW COLLISION: " + (showCollision ? "ON" : "OFF"), showCollision));
        buttonList.add(new AlphaButton(OPACITY_DOWN, left + 20, top + 120, 135, 20, "OPACITY -"));
        buttonList.add(new AlphaButton(OPACITY_UP, left + 165, top + 120, 135, 20, "OPACITY +"));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "BLOCK PLACEMENT PREVIEW",
                width / 2, top + 18, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Preview the target block position before placing it.",
                width / 2, top + 39, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Opacity: "
                        + Math.round(BlockPlacementPreviewSettings.getOpacity() * 100.0F) + "%",
                width / 2, top + 151, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Collision highlighting marks blocked placements.",
                width / 2, top + 166, 0xFF8E95A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == ENABLE) {
            BlockPlacementPreviewSettings.setEnabled(!BlockPlacementPreviewSettings.isEnabled());
            initGui();
        } else if (button.id == SHOW_COLLISION) {
            BlockPlacementPreviewSettings.setShowCollision(!BlockPlacementPreviewSettings.isShowCollision());
            initGui();
        } else if (button.id == OPACITY_DOWN) {
            BlockPlacementPreviewSettings.setOpacity(BlockPlacementPreviewSettings.getOpacity() - 0.1F);
            initGui();
        } else if (button.id == OPACITY_UP) {
            BlockPlacementPreviewSettings.setOpacity(BlockPlacementPreviewSettings.getOpacity() + 0.1F);
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
