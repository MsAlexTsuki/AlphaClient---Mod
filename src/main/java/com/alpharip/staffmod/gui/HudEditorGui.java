package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Drag-and-drop editor for AlphaClient HUD elements. */
public final class HudEditorGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int RESET_ALL = 2;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private boolean dragging;
    private boolean fpsDragging;
    private boolean cpsDragging;
    private int dragX, dragY;

    public HudEditorGui(GuiScreen parent) { this.parent = parent; }

    private static boolean isCpsModuleEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("cps");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    private static boolean isFpsModuleEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("fps");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @Override
    public void initGui() {
        BhopHud.loadHudPosition();
        FpsMod.loadHudPosition();
        CpsMod.load();
        buttonList.clear();
        int w = 132, gap = 10, x = (width - (w * 2 + gap)) / 2, y = height - 38;
        buttonList.add(new AlphaButton(BACK, x, y, w, 24, "BACK"));
        buttonList.add(new AlphaButton(RESET_ALL, x + w + gap, y, w, 24, "RESET ALL"));
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
        drawString(fontRendererObj, "Drag any HUD preview to reposition it.", left + 16, top + 48, 0xFFB9C0CB);
        drawString(fontRendererObj, "Use RESET ALL to restore default HUD positions.", left + 16, top + 65, 0xFFB8BDC8);

        int x = BhopHud.getHudX(), y = BhopHud.getHudY();
        int w = Math.max(110, BhopHud.getHudWidth()), h = 28;
        drawRect(x, y, x + w, y + h, 0xE0161921);
        drawRect(x, y, x + w, y + 1, 0x665B626E);
        drawRect(x, y, x + 1, y + h, 0x665B626E);
        drawString(fontRendererObj, BhopHud.getDisplayedProfileName() + "  |  "
                + BhopHud.getDisplayedSpeedName(), x + 8, y + 10, 0xFFF3F4F6);

        if (isFpsModuleEnabled()) {
            String fps = "FPS " + net.minecraft.client.Minecraft.getDebugFPS();
            int fx = FpsMod.getHudX(), fy = FpsMod.getHudY();
            int fw = fontRendererObj.getStringWidth(fps) + 10;
            if (FpsMod.isShowBackground()) drawRect(fx - 3, fy - 3, fx + fw, fy + fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(fps, fx, fy, FpsMod.getTextColor());
        }

        if (isCpsModuleEnabled()) {
            String cps = CpsMod.getDisplayText();
            int cx = CpsMod.getHudX(), cy = CpsMod.getHudY();
            int cw = fontRendererObj.getStringWidth(cps) + 10;
            if (CpsMod.isShowBackground()) drawRect(cx - 3, cy - 3, cx + cw, cy + fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(cps, cx, cy, CpsMod.getTextColor());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int cx = CpsMod.getHudX(), cy = CpsMod.getHudY();
        int cw = fontRendererObj.getStringWidth(CpsMod.getDisplayText()) + 10;
        if (isCpsModuleEnabled() && mouseButton == 0 && mouseX >= cx - 3 && mouseX <= cx + cw
                && mouseY >= cy - 3 && mouseY <= cy + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - cx;
            dragY = mouseY - cy;
            cpsDragging = true;
            return;
        }

        int fx = FpsMod.getHudX(), fy = FpsMod.getHudY();
        int fw = fontRendererObj.getStringWidth("FPS " + net.minecraft.client.Minecraft.getDebugFPS()) + 10;
        if (isFpsModuleEnabled() && mouseButton == 0 && mouseX >= fx - 3 && mouseX <= fx + fw
                && mouseY >= fy - 3 && mouseY <= fy + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - fx;
            dragY = mouseY - fy;
            fpsDragging = true;
            return;
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
            int x = Math.max(0, Math.min(width - 40, mouseX - dragX));
            int y = Math.max(0, Math.min(height - 40, mouseY - dragY));
            if (fpsDragging) {
                FpsMod.setHudPosition(x, y);
            } else if (cpsDragging) {
                CpsMod.setHudPosition(x, y);
            } else {
                BhopHud.setHudPosition(x, Math.min(height - 60, y));
            }
            return;
        }
        super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        fpsDragging = false;
        cpsDragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == RESET_ALL) {
            HudPositionConfig.resetAll();
            BhopHud.loadHudPosition();
            FpsMod.setHudPosition(6, 6);
            CpsMod.setHudPosition(5, 28);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
