package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.hud.InfoHudModules;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Drag-and-drop editor for AlphaClient HUD elements. */
public final class HudEditorGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int RESET_ALL = 2;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private boolean dragging;
    private boolean fpsDragging;
    private boolean cpsDragging;
    private boolean afkTimerDragging;
    private boolean sessionTimerDragging;
    private boolean playtimeTrackerDragging;
    private boolean armorDragging;
    private boolean potionDragging;
    private int dragX, dragY;

    public HudEditorGui(GuiScreen parent) { this.parent = parent; }

    private static boolean hasBhopAccess() {
        AlphaModule module = AlphaModuleRegistry.find("staffmods");
        return module != null && module.isUnlocked();
    }

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
        InfoHudModules.load();
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

        if (hasBhopAccess()) {
            BhopHud.renderEditorPreview();
        }

        AfkTimerHud.renderEditorPreview();

        if (SessionTimerHud.isEnabled()) {
            SessionTimerHud.renderEditorPreview();
        }

        if (PlaytimeTrackerHud.isEnabled()) {
            PlaytimeTrackerHud.renderEditorPreview();
        }

        if (isFpsModuleEnabled()) {
            String fps = "FPS " + net.minecraft.client.Minecraft.getDebugFPS();
            int fx = FpsMod.getHudX(), fy = FpsMod.getHudY();
            int fw = fontRendererObj.getStringWidth(fps) + 10;
            if (FpsMod.isShowBackground()) drawRect(fx - 3, fy - 3, fx + fw, fy + fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(fps, fx, fy, FpsMod.getTextColor());
        }

        InfoHudModules.renderEditorPreviews();

        if (isCpsModuleEnabled()) {
            String cps = CpsMod.getDisplayText();
            int ahx = InfoHudModules.getArmorX(), ahy = InfoHudModules.getArmorY();
        if (mouseButton == 0 && InfoHudModules.armorEnabled()
                && mouseX >= ahx && mouseX <= ahx + 440 && mouseY >= ahy && mouseY <= ahy + 120) {
            dragging = true; armorDragging = true; dragX = mouseX - ahx; dragY = mouseY - ahy; return;
        }
        int phx = InfoHudModules.getPotionX(), phy = InfoHudModules.getPotionY();
        if (mouseButton == 0 && InfoHudModules.potionEnabled()
                && mouseX >= phx && mouseX <= phx + 220 && mouseY >= phy && mouseY <= phy + 180) {
            dragging = true; potionDragging = true; dragX = mouseX - phx; dragY = mouseY - phy; return;
        }

        int cx = CpsMod.getHudX(), cy = CpsMod.getHudY();
            int cw = fontRendererObj.getStringWidth(cps) + 10;
            if (CpsMod.isShowBackground()) drawRect(cx - 3, cy - 3, cx + cw, cy + fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(cps, cx, cy, CpsMod.getTextColor());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int ax = AfkTimerHud.getHudX(), ay = AfkTimerHud.getHudY();
        int aw = fontRendererObj.getStringWidth("AFK 00:00") + 10;
        if (mouseButton == 0 && mouseX >= ax - 3 && mouseX <= ax + aw
                && mouseY >= ay - 3 && mouseY <= ay + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - ax;
            dragY = mouseY - ay;
            afkTimerDragging = true;
            return;
        }

        int sx = SessionTimerHud.getHudX(), sy = SessionTimerHud.getHudY();
        String sessionText = "SESSION 00:00:00";
        int sw = fontRendererObj.getStringWidth(sessionText) + 10;
        if (SessionTimerHud.isEnabled() && mouseButton == 0 && mouseX >= sx - 3 && mouseX <= sx + sw
                && mouseY >= sy - 3 && mouseY <= sy + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - sx;
            dragY = mouseY - sy;
            sessionTimerDragging = true;
            return;
        }

        int px = PlaytimeTrackerHud.getHudX(), py = PlaytimeTrackerHud.getHudY();
        String playtimeText = "PLAYTIME 1 Y and 2 Mo";
        int pw = fontRendererObj.getStringWidth(playtimeText) + 10;
        if (PlaytimeTrackerHud.isEnabled() && mouseButton == 0 && mouseX >= px - 3 && mouseX <= px + pw
                && mouseY >= py - 3 && mouseY <= py + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - px;
            dragY = mouseY - py;
            playtimeTrackerDragging = true;
            return;
        }

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
        if (hasBhopAccess() && mouseButton == 0 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 28) {
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
            if (!afkTimerDragging && !fpsDragging && !cpsDragging && !sessionTimerDragging && !playtimeTrackerDragging && !armorDragging && !potionDragging && !hasBhopAccess()) {
                dragging = false;
                return;
            }
            int x = mouseX - dragX;
            int y = mouseY - dragY;
            if (afkTimerDragging) {
                int itemWidth = fontRendererObj.getStringWidth("AFK 00:00") + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                AfkTimerHud.setHudPosition(x, y);
            } else if (fpsDragging) {
                int itemWidth = fontRendererObj.getStringWidth("FPS " + net.minecraft.client.Minecraft.getDebugFPS()) + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                FpsMod.setHudPosition(x, y);
            } else if (cpsDragging) {
                int itemWidth = fontRendererObj.getStringWidth(CpsMod.getDisplayText()) + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                CpsMod.setHudPosition(x, y);
            } else if (sessionTimerDragging) {
                int itemWidth = fontRendererObj.getStringWidth("SESSION 00:00:00") + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                SessionTimerHud.setHudPosition(x, y);
            } else if (armorDragging) {
                x = Math.max(0, Math.min(width - 440, x));
                y = Math.max(0, Math.min(height - 120, y));
                InfoHudModules.setArmorPosition(x, y);
            } else if (potionDragging) {
                x = Math.max(0, Math.min(width - 220, x));
                y = Math.max(0, Math.min(height - 180, y));
                InfoHudModules.setPotionPosition(x, y);
            } else if (playtimeTrackerDragging) {
                int itemWidth = fontRendererObj.getStringWidth("PLAYTIME 1 Y and 2 Mo") + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                PlaytimeTrackerHud.setHudPosition(x, y);
            } else {
                float scale = BhopHud.getVisualScale();
                int itemWidth = (int) Math.ceil(BhopHud.getHudWidth() * scale);
                int itemHeight = (int) Math.ceil(20 * scale);
                x = Math.max(0, Math.min(width - itemWidth, x));
                y = Math.max(0, Math.min(height - itemHeight, y));
                BhopHud.setHudPosition(x, y);
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
        afkTimerDragging = false;
        sessionTimerDragging = false;
        playtimeTrackerDragging = false;
        armorDragging = false;
        potionDragging = false;
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
            AfkTimerHud.setHudPosition(6, 6);
            SessionTimerHud.setHudPosition(6, 22);
            PlaytimeTrackerHud.setHudPosition(6, 38);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
