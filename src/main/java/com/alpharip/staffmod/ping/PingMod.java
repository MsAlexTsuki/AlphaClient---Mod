package com.alpharip.staffmod.ping;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;

/** Compact server-ping HUD using the latency reported by the active server connection. */
public final class PingMod {
    private static final String[] COLOR_NAMES = {"White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"};
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};
    private static int hudX = 6;
    private static int hudY = 22;
    private static int colorIndex;
    private static boolean showBackground = true;
    private static boolean loaded;

    public PingMod() { }

    public static void load() {
        if (loaded) return;
        HudPositionConfig.load();
        hudX = HudPositionConfig.getPingX();
        hudY = HudPositionConfig.getPingY();
        colorIndex = HudPositionConfig.getPingColorIndex();
        if (colorIndex < 0 || colorIndex >= COLOR_NAMES.length) colorIndex = 0;
        showBackground = HudPositionConfig.isPingShowBackground();
        loaded = true;
    }

    public static int getPing() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.getNetHandler() == null) return -1;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        if (info == null) return -1;
        int responseTime = info.getResponseTime();
        return responseTime < 0 ? -1 : responseTime;
    }

    public static String getDisplayText() {
        int ping = getPing();
        return ping < 0 ? "PING -- ms" : "PING " + ping + " ms";
    }

    public static int getHudX() { load(); return hudX; }
    public static int getHudY() { load(); return hudY; }
    public static void setHudPosition(int x, int y) {
        load();
        hudX = Math.max(0, x);
        hudY = Math.max(0, y);
        HudPositionConfig.setPingPosition(hudX, hudY);
    }
    public static boolean isShowBackground() { load(); return showBackground; }
    public static void setShowBackground(boolean value) {
        load();
        showBackground = value;
        HudPositionConfig.setPingShowBackground(value);
    }
    public static String getColorName() { load(); return COLOR_NAMES[colorIndex]; }
    public static void cycleColor() {
        load();
        colorIndex = (colorIndex + 1) % COLOR_NAMES.length;
        HudPositionConfig.setPingColorIndex(colorIndex);
    }
    public static int getTextColor() {
        load();
        if (colorIndex == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[colorIndex];
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("ping");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        String line = getDisplayText();
        int width = mc.fontRendererObj.getStringWidth(line) + 10;
        if (showBackground) {
            Gui.drawRect(hudX - 3, hudY - 3, hudX + width, hudY + mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            Gui.drawRect(hudX - 3, hudY - 3, hudX + width, hudY - 2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(line, hudX, hudY, getTextColor());
    }
}
