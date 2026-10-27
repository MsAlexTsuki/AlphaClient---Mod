// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.ping.PingMod;
import com.alpharip.staffmod.hud.InfoHudModules;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Drag-and-drop editor for AlphaClient HUD elements. */
public class HudEditorGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int RESET_ALL = 2;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private boolean dragging;
    private boolean fpsDragging;
    private boolean pingDragging;
    private boolean cpsDragging;
    private boolean afkTimerDragging;
    private boolean sessionTimerDragging;
    private boolean playtimeTrackerDragging;
    private boolean armorDragging;
    private boolean potionDragging;
    private int dragX, dragY;
    private boolean scaleDragging;
    private String scaleTarget;
    private int scaleStartY;
    private float scaleStartValue;

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

    private static boolean isPingModuleEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("ping");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @Override
    public void initGui() {
        BhopHud.loadHudPosition();
        FpsMod.loadHudPosition();
        PingMod.load();
        CpsMod.load();
        InfoHudModules.load();
        buttonList.clear();
        int w = 132, gap = 10, x = (width - (w * 2 + gap)) / 2, y = height - 38;
        buttonList.add(new AlphaButton(BACK, x, y, w, 24, "BACK"));
        buttonList.add(new AlphaButton(RESET_ALL, x + w + gap, y, w, 24, "RESET ALL"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Minimal editor overlay: keep the game unobstructed and remove the large panel.
        drawRect(0, 0, width, 1, 0x33FFD866);

        if (hasBhopAccess() && BhopHud.isEnabled()) {
            BhopHud.renderEditorPreview();
        }

        if (AfkTimerHud.isEnabled()) {
            AfkTimerHud.renderEditorPreview();
        }

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
            float scale = HudScaleConfig.get("fps");
            GlStateManager.pushMatrix(); GlStateManager.translate(fx, fy, 0.0F); GlStateManager.scale(scale, scale, 1.0F);
            if (FpsMod.isShowBackground()) drawRect(-3, -3, fw, fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(fps, 0, 0, FpsMod.getTextColor());
            GlStateManager.popMatrix();
        }

        if (isPingModuleEnabled()) {
            String ping = PingMod.getDisplayText();
            int px = PingMod.getHudX(), py = PingMod.getHudY();
            int pw = fontRendererObj.getStringWidth(ping) + 10;
            float scale = HudScaleConfig.get("ping");
            GlStateManager.pushMatrix(); GlStateManager.translate(px, py, 0.0F); GlStateManager.scale(scale, scale, 1.0F);
            if (PingMod.isShowBackground()) drawRect(-3, -3, pw, fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(ping, 0, 0, PingMod.getTextColor());
            GlStateManager.popMatrix();
        }

        InfoHudModules.renderEditorPreviews();

        if (isCpsModuleEnabled()) {
            String cps = CpsMod.getDisplayText();
            int cx = CpsMod.getHudX(), cy = CpsMod.getHudY();
            int cw = fontRendererObj.getStringWidth(cps) + 10;
            float scale = HudScaleConfig.get("cps");
            GlStateManager.pushMatrix(); GlStateManager.translate(cx, cy, 0.0F); GlStateManager.scale(scale, scale, 1.0F);
            if (CpsMod.isShowBackground()) drawRect(-3, -3, cw, fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            fontRendererObj.drawStringWithShadow(cps, 0, 0, CpsMod.getTextColor());
            GlStateManager.popMatrix();
        }
        if (hasBhopAccess() && BhopHud.isEnabled()) drawScaleHandle(BhopHud.getHudX(), BhopHud.getHudY(), (int)(BhopHud.getHudWidth() * BhopHud.getVisualScale() * HudScaleConfig.get("bhop")), 20);
        if (AfkTimerHud.isEnabled()) drawScaleHandle(AfkTimerHud.getHudX(), AfkTimerHud.getHudY(), (int)((fontRendererObj.getStringWidth("AFK 00:00") + 10) * HudScaleConfig.get("afkTimer")), fontRendererObj.FONT_HEIGHT + 6);
        if (SessionTimerHud.isEnabled()) drawScaleHandle(SessionTimerHud.getHudX(), SessionTimerHud.getHudY(), (int)((fontRendererObj.getStringWidth("SESSION 00:00:00") + 10) * HudScaleConfig.get("sessionTimer")), fontRendererObj.FONT_HEIGHT + 6);
        if (PlaytimeTrackerHud.isEnabled()) drawScaleHandle(PlaytimeTrackerHud.getHudX(), PlaytimeTrackerHud.getHudY(), (int)((fontRendererObj.getStringWidth("PLAYTIME 1 Y and 2 Mo") + 10) * HudScaleConfig.get("playtimeTracker")), fontRendererObj.FONT_HEIGHT + 6);
        if (isFpsModuleEnabled()) drawScaleHandle(FpsMod.getHudX(), FpsMod.getHudY(), (int)((fontRendererObj.getStringWidth("FPS " + net.minecraft.client.Minecraft.getDebugFPS()) + 10) * HudScaleConfig.get("fps")), fontRendererObj.FONT_HEIGHT + 6);
        if (isPingModuleEnabled()) drawScaleHandle(PingMod.getHudX(), PingMod.getHudY(), (int)((fontRendererObj.getStringWidth(PingMod.getDisplayText()) + 10) * HudScaleConfig.get("ping")), fontRendererObj.FONT_HEIGHT + 6);
        if (isCpsModuleEnabled()) drawScaleHandle(CpsMod.getHudX(), CpsMod.getHudY(), (int)((fontRendererObj.getStringWidth(CpsMod.getDisplayText()) + 10) * HudScaleConfig.get("cps")), fontRendererObj.FONT_HEIGHT + 6);
        drawScaleHandle(InfoHudModules.getArmorX(), InfoHudModules.getArmorY(), (int)(InfoHudModules.getArmorEditorWidth() * HudScaleConfig.get("armor")), (int)(InfoHudModules.getArmorEditorHeight() * HudScaleConfig.get("armor")));
        drawScaleHandle(InfoHudModules.getPotionX(), InfoHudModules.getPotionY(), (int)(InfoHudModules.getPotionEditorWidth() * HudScaleConfig.get("potion")), (int)(InfoHudModules.getPotionEditorHeight() * HudScaleConfig.get("potion")));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawScaleHandle(int x, int y, int w, int h) {
        int right = x + Math.max(2, w);
        int bottom = y + Math.max(2, h);
        // Tiny 2x2 corner marker, not the old oversized yellow square.
        drawRect(right - 2, bottom - 2, right, bottom, ACCENT);
    }

    private boolean scaleHandleHit(int x, int y, int w, int h, int mx, int my) {
        int right = x + Math.max(2, w);
        int bottom = y + Math.max(2, h);
        // Slightly larger invisible hit target keeps the tiny marker usable.
        return mx >= right - 5 && mx <= right + 2 && my >= bottom - 5 && my <= bottom + 2;
    }

    private void beginScale(String target, int mouseY) {
        scaleDragging = true; scaleTarget = target; scaleStartY = mouseY;
        scaleStartValue = HudScaleConfig.get(target);
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (hasBhopAccess() && BhopHud.isEnabled() && scaleHandleHit(BhopHud.getHudX(), BhopHud.getHudY(), (int)(BhopHud.getHudWidth() * BhopHud.getVisualScale() * HudScaleConfig.get("bhop")), 20, mouseX, mouseY)) { beginScale("bhop", mouseY); return; }
            if (AfkTimerHud.isEnabled() && scaleHandleHit(AfkTimerHud.getHudX(), AfkTimerHud.getHudY(), (int)((fontRendererObj.getStringWidth("AFK 00:00") + 10) * HudScaleConfig.get("afkTimer")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("afkTimer", mouseY); return; }
            if (SessionTimerHud.isEnabled() && scaleHandleHit(SessionTimerHud.getHudX(), SessionTimerHud.getHudY(), (int)((fontRendererObj.getStringWidth("SESSION 00:00:00") + 10) * HudScaleConfig.get("sessionTimer")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("sessionTimer", mouseY); return; }
            if (PlaytimeTrackerHud.isEnabled() && scaleHandleHit(PlaytimeTrackerHud.getHudX(), PlaytimeTrackerHud.getHudY(), (int)((fontRendererObj.getStringWidth("PLAYTIME 1 Y and 2 Mo") + 10) * HudScaleConfig.get("playtimeTracker")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("playtimeTracker", mouseY); return; }
            if (isFpsModuleEnabled() && scaleHandleHit(FpsMod.getHudX(), FpsMod.getHudY(), (int)((fontRendererObj.getStringWidth("FPS " + net.minecraft.client.Minecraft.getDebugFPS()) + 10) * HudScaleConfig.get("fps")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("fps", mouseY); return; }
            if (isPingModuleEnabled() && scaleHandleHit(PingMod.getHudX(), PingMod.getHudY(), (int)((fontRendererObj.getStringWidth(PingMod.getDisplayText()) + 10) * HudScaleConfig.get("ping")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("ping", mouseY); return; }
            if (isCpsModuleEnabled() && scaleHandleHit(CpsMod.getHudX(), CpsMod.getHudY(), (int)((fontRendererObj.getStringWidth(CpsMod.getDisplayText()) + 10) * HudScaleConfig.get("cps")), fontRendererObj.FONT_HEIGHT + 6, mouseX, mouseY)) { beginScale("cps", mouseY); return; }
            if (true && scaleHandleHit(InfoHudModules.getArmorX(), InfoHudModules.getArmorY(), (int)(InfoHudModules.getArmorEditorWidth() * HudScaleConfig.get("armor")), (int)(InfoHudModules.getArmorEditorHeight() * HudScaleConfig.get("armor")), mouseX, mouseY)) { beginScale("armor", mouseY); return; }
            if (true && scaleHandleHit(InfoHudModules.getPotionX(), InfoHudModules.getPotionY(), (int)(InfoHudModules.getPotionEditorWidth() * HudScaleConfig.get("potion")), (int)(InfoHudModules.getPotionEditorHeight() * HudScaleConfig.get("potion")), mouseX, mouseY)) { beginScale("potion", mouseY); return; }
        }
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

        int ahx = InfoHudModules.getArmorX(), ahy = InfoHudModules.getArmorY();
        if (mouseButton == 0 && mouseX >= ahx && mouseX <= ahx + InfoHudModules.getArmorEditorWidth()
                && mouseY >= ahy && mouseY <= ahy + InfoHudModules.getArmorEditorHeight()) {
            dragging = true;
            armorDragging = true;
            dragX = mouseX - ahx;
            dragY = mouseY - ahy;
            return;
        }

        int phx = InfoHudModules.getPotionX(), phy = InfoHudModules.getPotionY();
        if (mouseButton == 0 && mouseX >= phx && mouseX <= phx + InfoHudModules.getPotionEditorWidth()
                && mouseY >= phy && mouseY <= phy + InfoHudModules.getPotionEditorHeight()) {
            dragging = true;
            potionDragging = true;
            dragX = mouseX - phx;
            dragY = mouseY - phy;
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

        int pingX = PingMod.getHudX(), pingY = PingMod.getHudY();
        int pingWidth = fontRendererObj.getStringWidth(PingMod.getDisplayText()) + 10;
        if (isPingModuleEnabled() && mouseButton == 0 && mouseX >= pingX - 3 && mouseX <= pingX + pingWidth
                && mouseY >= pingY - 3 && mouseY <= pingY + fontRendererObj.FONT_HEIGHT + 3) {
            dragging = true;
            dragX = mouseX - pingX;
            dragY = mouseY - pingY;
            pingDragging = true;
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
        if (scaleDragging && button == 0) { HudScaleConfig.set(scaleTarget, scaleStartValue + (scaleStartY - mouseY) * 0.01F); return; }
        if (dragging && button == 0) {
            if (!afkTimerDragging && !fpsDragging && !pingDragging && !cpsDragging && !sessionTimerDragging && !playtimeTrackerDragging && !armorDragging && !potionDragging && !hasBhopAccess()) {
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
            } else if (pingDragging) {
                int itemWidth = fontRendererObj.getStringWidth(PingMod.getDisplayText()) + 10;
                x = Math.max(3, Math.min(width - itemWidth - 3, x));
                y = Math.max(3, Math.min(height - fontRendererObj.FONT_HEIGHT - 3, y));
                PingMod.setHudPosition(x, y);
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
                x = Math.max(0, Math.min(Math.max(0, width - InfoHudModules.getArmorEditorWidth()), x));
                y = Math.max(0, Math.min(Math.max(0, height - InfoHudModules.getArmorEditorHeight()), y));
                InfoHudModules.setArmorPosition(x, y);
            } else if (potionDragging) {
                // Keep the potion icon draggable all the way to the right edge.
                // The effect text flips to the left of the icon on the right half.
                int maxPotionX = x > width / 2 ? width - 18 : width - InfoHudModules.getPotionEditorWidth();
                x = Math.max(0, Math.min(Math.max(0, maxPotionX), x));
                y = Math.max(0, Math.min(Math.max(0, height - InfoHudModules.getPotionEditorHeight()), y));
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
        scaleDragging = false; scaleTarget = null;
        dragging = false;
        fpsDragging = false;
        pingDragging = false;
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
            HudScaleConfig.resetAll();
            BhopHud.loadHudPosition();
            FpsMod.setHudPosition(6, 6);
            PingMod.setHudPosition(6, 22);
            CpsMod.setHudPosition(5, 28);
            AfkTimerHud.setHudPosition(6, 6);
            SessionTimerHud.setHudPosition(6, 22);
            PlaytimeTrackerHud.setHudPosition(6, 38);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
