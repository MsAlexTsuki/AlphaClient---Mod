package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.TargetHud;
import com.alpharip.staffmod.util.KnockbackTracker;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Drag-and-drop editor for the implemented AlphaClient HUD indicators. */
public final class HudEditorGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int RESET = 2;
    private static final int ACCENT = 0xFFFFD866;
    private static final int NONE = 0;
    private static final int BHOP = 1;
    private static final int TARGET_CPS = 2;
    private static final int KNOCKBACK = 3;

    private final GuiScreen parent;
    private int dragging = NONE;
    private int dragX;
    private int dragY;

    public HudEditorGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        TargetHud.loadHudPosition();
        KnockbackTracker.loadHudPosition();
        BhopHud.loadHudPosition();

        buttonList.clear();
        int buttonWidth = 104;
        int gap = 10;
        int startX = (width - (buttonWidth * 2 + gap)) / 2;
        int y = height - 38;
        buttonList.add(new AlphaButton(BACK, startX, y, buttonWidth, 24, "BACK"));
        buttonList.add(new AlphaButton(RESET, startX + buttonWidth + gap, y, buttonWidth, 24, "RESET ALL"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x8806090F);

        int panelWidth = Math.min(430, width - 32);
        int panelHeight = 108;
        int left = (width - panelWidth) / 2;
        int top = 18;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE10D1017);
        drawRect(left, top, left + panelWidth, top + 1, 0x554B515D);
        drawString(fontRendererObj, "ALPHA", left + 16, top + 12, ACCENT);
        drawString(fontRendererObj, "HUD EDITOR", left + 16, top + 27, 0xFFF1F3F7);
        drawString(fontRendererObj, "Drag any preview to move it. Positions are saved automatically.", left + 16,
                top + 48, 0xFFB9C0CB);
        drawString(fontRendererObj, "BHOP  |  TARGET + CPS  |  KNOCKBACK", left + 16,
                top + 66, 0xFFFFD866);
        drawString(fontRendererObj, "Use RESET ALL to restore the default positions.", left + 16,
                top + 83, 0xFF858D99);

        drawBhopPreview();
        drawTargetPreview();
        drawKnockbackPreview();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBhopPreview() {
        int x = BhopHud.getHudX();
        int y = BhopHud.getHudY();
        int previewWidth = Math.max(110, BhopHud.getHudWidth());
        int previewHeight = 28;
        drawRect(x, y, x + previewWidth, y + previewHeight, 0xE0161921);
        drawRect(x, y, x + previewWidth, y + 2, ACCENT);
        drawRect(x, y, x + 2, y + previewHeight, ACCENT);
        String label = BhopHud.getDisplayedProfileName() + "  |  " + BhopHud.getDisplayedSpeedName();
        drawString(fontRendererObj, label, x + 8, y + 10, 0xFFF3F4F6);
        drawString(fontRendererObj, "BHOP HUD", x + 8, y + previewHeight + 3, ACCENT);
    }

    private void drawTargetPreview() {
        int x = TargetHud.getHudX();
        int y = TargetHud.getHudY();
        int w = 145;
        int h = 48;
        drawRect(x, y, x + w, y + h, 0xE0101218);
        drawRect(x, y, x + w, y + 2, ACCENT);
        drawRect(x, y, x + 2, y + h, ACCENT);
        drawString(fontRendererObj, "TARGET", x + 8, y + 7, 0xFFF3F4F6);
        drawString(fontRendererObj, "Player: Example", x + 8, y + 20, 0xFFB9C0CB);
        drawString(fontRendererObj, "CPS: 12  |  Distance: 4.2m", x + 8, y + 33, 0xFFFFD866);
        drawString(fontRendererObj, "TARGET + CPS HUD", x + 8, y + h + 3, ACCENT);
    }

    private void drawKnockbackPreview() {
        int x = KnockbackTracker.getHudX();
        int y = KnockbackTracker.getHudY();
        int w = 175;
        int h = 70;
        drawRect(x, y, x + w, y + h, 0xE0101218);
        drawRect(x, y, x + w, y + 2, ACCENT);
        drawRect(x, y, x + 2, y + h, ACCENT);
        drawString(fontRendererObj, "KNOCKBACK", x + 8, y + 6, 0xFFF3F4F6);
        drawString(fontRendererObj, "Target: Example", x + 8, y + 19, 0xFFB9C0CB);
        drawString(fontRendererObj, "Expected: 0.420", x + 8, y + 32, 0xFFB9C0CB);
        drawString(fontRendererObj, "Actual: 0.310", x + 8, y + 45, 0xFFB9C0CB);
        drawString(fontRendererObj, "Ratio: 74%  |  VL: 0", x + 8, y + 58, 0xFFFFD866);
        drawString(fontRendererObj, "KNOCKBACK HUD", x + 8, y + h + 3, ACCENT);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (inside(mouseX, mouseY, BhopHud.getHudX(), BhopHud.getHudY(),
                    Math.max(110, BhopHud.getHudWidth()), 28)) {
                beginDrag(BHOP, mouseX, mouseY, BhopHud.getHudX(), BhopHud.getHudY());
                return;
            }
            if (inside(mouseX, mouseY, TargetHud.getHudX(), TargetHud.getHudY(), 145, 48)) {
                beginDrag(TARGET_CPS, mouseX, mouseY, TargetHud.getHudX(), TargetHud.getHudY());
                return;
            }
            if (inside(mouseX, mouseY, KnockbackTracker.getHudX(), KnockbackTracker.getHudY(), 175, 70)) {
                beginDrag(KNOCKBACK, mouseX, mouseY,
                        KnockbackTracker.getHudX(), KnockbackTracker.getHudY());
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void beginDrag(int widget, int mouseX, int mouseY, int x, int y) {
        dragging = widget;
        dragX = mouseX - x;
        dragY = mouseY - y;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging != NONE && clickedMouseButton == 0) {
            int x = Math.max(0, Math.min(width - 40, mouseX - dragX));
            int y = Math.max(0, Math.min(height - 70, mouseY - dragY));
            if (dragging == BHOP) {
                BhopHud.setHudPosition(x, y);
            } else if (dragging == TARGET_CPS) {
                TargetHud.setHudPosition(x, y);
            } else if (dragging == KNOCKBACK) {
                KnockbackTracker.setHudPosition(x, y);
            }
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = NONE;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == RESET) {
            BhopHud.resetPosition();
            TargetHud.resetPosition();
            KnockbackTracker.resetHudPosition();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
