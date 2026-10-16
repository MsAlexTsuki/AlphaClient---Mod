package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.fps.FpsMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Drag-and-drop editor for the retained BHOP HUD only. */
public final class HudEditorGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int RESET = 2;
    private static final int RESET_FPS = 3;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private boolean dragging;
    private boolean fpsDragging;
    private int dragX, dragY;

    public HudEditorGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        BhopHud.loadHudPosition();
        buttonList.clear();
        int w = 104, gap = 8, x = (width - (w * 3 + gap * 2)) / 2, y = height - 38;
        buttonList.add(new AlphaButton(BACK, x, y, w, 24, "BACK"));
        buttonList.add(new AlphaButton(RESET, x + w + gap, y, w, 24, "RESET BHOP"));
        buttonList.add(new AlphaButton(RESET_FPS, x + (w + gap) * 2, y, w, 24, "RESET FPS"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x8806090F);
        int panelWidth = Math.min(430, width - 32), panelHeight = 86;
        int left = (width - panelWidth) / 2, top = 18;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE10D1017);
        drawRect(left, top, left + panelWidth, top + 1, 0x554B515D);
        drawString(fontRendererObj, "ALPHA", left + 16, top + 12, ACCENT);
        drawString(fontRendererObj, "HUD EDITOR", left + 16, top + 27, 0xFFF1F3F7);
        drawString(fontRendererObj, "Drag the BHOP or FPS preview to move it.", left + 16, top + 48, 0xFFB9C0CB);
        drawString(fontRendererObj, "Drag either HUD element to reposition it.", left + 16, top + 65, 0xFFB8BDC8);

        int x = BhopHud.getHudX(), y = BhopHud.getHudY();
        int w = Math.max(110, BhopHud.getHudWidth()), h = 28;
        drawRect(x, y, x + w, y + h, 0xE0161921);
        drawRect(x, y, x + w, y + 1, 0x665B626E);
        drawRect(x, y, x + 1, y + h, 0x665B626E);
        drawString(fontRendererObj, BhopHud.getDisplayedProfileName() + "  |  "
                + BhopHud.getDisplayedSpeedName(), x + 8, y + 10, 0xFFF3F4F6);
        if (FpsMod.isShowFps()) {
            String fps = "FPS " + net.minecraft.client.Minecraft.getDebugFPS();
            int fx = FpsMod.getHudX(), fy = FpsMod.getHudY();
            int fw = fontRendererObj.getStringWidth(fps) + 10;
            if (FpsMod.isShowBackground()) drawRect(fx - 3, fy - 3, fx + fw, fy + fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            drawStringWithShadow(fontRendererObj, fps, fx, fy, FpsMod.getTextColor());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int fx = FpsMod.getHudX(), fy = FpsMod.getHudY();
        int fw = fontRendererObj.getStringWidth("FPS " + net.minecraft.client.Minecraft.getDebugFPS()) + 10;
        if (FpsMod.isShowFps() && mouseButton == 0 && mouseX >= fx - 3 && mouseX <= fx + fw && mouseY >= fy - 3 && mouseY <= fy + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true; dragX = mouseX - fx; dragY = mouseY - fy; fpsDragging = true; return;
        }
        int x = BhopHud.getHudX(), y = BhopHud.getHudY(), w = Math.max(110, BhopHud.getHudWidth());
        if (mouseButton == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28) {
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (dragging && button == 0) {
            BhopHud.setHudPosition(Math.max(0, Math.min(width - 40, mouseX - dragX)),
                    Math.max(0, Math.min(height - 60, mouseY - dragY)));
            return;
        }
        super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        fpsDragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) mc.displayGuiScreen(parent);
        else if (button.id == RESET) BhopHud.resetPosition();
        else if (button.id == RESET_FPS) FpsMod.setHudPosition(6, 6);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
