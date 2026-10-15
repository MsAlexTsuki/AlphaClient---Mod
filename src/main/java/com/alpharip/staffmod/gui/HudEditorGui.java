package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/**
 * AlphaClient HUD editor.
 * Retired Target/CPS and Knockback indicators are intentionally not exposed.
 * The retained BHOP HUD can be repositioned here.
 */
public final class HudEditorGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int RESET = 2;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private boolean dragging;
    private int dragX;
    private int dragY;

    public HudEditorGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int buttonWidth = 104;
        int gap = 10;
        int startX = (width - (buttonWidth * 2 + gap)) / 2;
        int y = height - 38;
        buttonList.add(new AlphaButton(BACK, startX, y, buttonWidth, 24, "BACK"));
        buttonList.add(new AlphaButton(RESET, startX + buttonWidth + gap, y, buttonWidth, 24, "RESET HUD"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x8806090F);
        int panelWidth = Math.min(390, width - 32);
        int panelHeight = Math.min(190, height - 70);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE10D1017);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawString(fontRendererObj, "ALPHA", left + 16, top + 14, ACCENT);
        drawString(fontRendererObj, "HUD EDITOR", left + 16, top + 29, 0xFFF1F3F7);
        drawString(fontRendererObj, "Drag the BHOP HUD preview to reposition it.", left + 16, top + 48, 0xFFB9C0CB);
        drawString(fontRendererObj, "Retired CPS, Target and Knockback indicators are not available.", left + 16, top + 63, 0xFF858D99);

        int x = BhopHud.getHudX();
        int y = BhopHud.getHudY();
        int previewWidth = Math.max(110, BhopHud.getHudWidth());
        int previewHeight = 24;
        drawRect(x, y, x + previewWidth, y + previewHeight, 0xD9161921);
        drawRect(x, y, x + previewWidth, y + 1, ACCENT);
        drawRect(x, y, x + 2, y + previewHeight, ACCENT);
        String label = BhopHud.getDisplayedProfileName() + "  |  " + BhopHud.getDisplayedSpeedName();
        drawString(fontRendererObj, label, x + 8, y + 8, 0xFFF3F4F6);
        drawString(fontRendererObj, "BHOP HUD", left + 16, top + 91, ACCENT);
        drawString(fontRendererObj, "Position: " + x + ", " + y, left + 16, top + 106, 0xFFB9C0CB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int x = BhopHud.getHudX();
            int y = BhopHud.getHudY();
            int w = Math.max(110, BhopHud.getHudWidth());
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 24) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging && clickedMouseButton == 0) {
            int x = Math.max(0, Math.min(width - 20, mouseX - dragX));
            int y = Math.max(0, Math.min(height - 12, mouseY - dragY));
            BhopHud.setHudPosition(x, y);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == RESET) {
            BhopHud.resetPosition();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
