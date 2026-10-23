package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Tracks elapsed time for the lifetime of the running Minecraft client. */
public final class SessionTimerHud {
    private static final String MODULE_ID = "sessionTimer";
    private static long sessionStartedAt;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Start once per Minecraft process, including time spent in menus or between worlds.
        // Leaving a server/world must not reset the session timer.
        if (sessionStartedAt <= 0L) {
            sessionStartedAt = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI
                || !isEnabled() || sessionStartedAt <= 0L) return;

        long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
        String text = "SESSION " + formatDuration(elapsedSeconds);
        int x = getHudX();
        int y = getHudY();
        int textWidth = mc.fontRendererObj.getStringWidth(text) + 10;
        Gui.drawRect(x - 3, y - 3, x + textWidth, y + mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
        Gui.drawRect(x - 3, y - 3, x + textWidth, y - 2, 0xFFFFD866);
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
    }

    public static boolean isEnabled() {
        JsonObject settings = getSettings();
        return settings.has("enabled") && settings.get("enabled").getAsBoolean();
    }

    public static void setEnabled(boolean enabled) {
        getSettings().addProperty("enabled", enabled);
        ModuleProfileManager.save();
    }

    public static int getHudX() {
        JsonObject settings = getSettings();
        return settings.has("hudX") ? Math.max(0, settings.get("hudX").getAsInt()) : 6;
    }

    public static int getHudY() {
        JsonObject settings = getSettings();
        return settings.has("hudY") ? Math.max(0, settings.get("hudY").getAsInt()) : 22;
    }

    private static JsonObject getSettings() {
        return ModuleProfileManager.getSelectedModule(MODULE_ID);
    }

    private static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0L) return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
