// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.config.HudScaleConfig;
import net.minecraft.client.renderer.GlStateManager;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;

/** AFK timer HUD. AFK is based on player position, never mouse/camera movement. */
public final class AfkTimerHud {
    public static final int MODE_ALWAYS_VISIBLE = 0;
    public static final int MODE_ONLY_WHILE_AFK = 1;
    private static final long AFK_DELAY_MS = 10_000L;
    private static final String MODULE_ID = "afkTimer";
    private static final String[] COLOR_NAMES = {
            "White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"
    };
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA
    };

    private static boolean positionInitialized;
    private static double lastX, lastY, lastZ;
    private static long lastMovementTime;
    private static long now;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            positionInitialized = false;
            lastMovementTime = 0L;
            return;
        }

        now = System.currentTimeMillis();
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        if (!positionInitialized) {
            positionInitialized = true;
            lastX = x;
            lastY = y;
            lastZ = z;
            lastMovementTime = now;
            return;
        }

        if (Math.abs(x - lastX) > 0.001D
                || Math.abs(y - lastY) > 0.001D
                || Math.abs(z - lastZ) > 0.001D) {
            lastX = x;
            lastY = y;
            lastZ = z;
            lastMovementTime = now;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        if (!isEnabled() || !positionInitialized || lastMovementTime == 0L) return;
        long currentTime = System.currentTimeMillis();
        boolean afk = currentTime - lastMovementTime >= AFK_DELAY_MS;
        if (getDisplayMode() == MODE_ONLY_WHILE_AFK && !afk) return;
        long elapsed = afk ? currentTime - (lastMovementTime + AFK_DELAY_MS) : 0L;
        renderHud(mc, "AFK " + formatDuration(elapsed));
    }

    /** Draws a selectable preview in the HUD editor, even before the player is AFK. */
    public static void renderEditorPreview() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;
        renderHud(mc, "AFK 00:00");
    }

    private static void renderHud(Minecraft mc, String text) {
        int x = getHudX();
        int y = getHudY();
        float scale = HudScaleConfig.get(MODULE_ID);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        int textWidth = mc.fontRendererObj.getStringWidth(text) + 10;
        if (isShowBackground()) {
            Gui.drawRect(-3, -3, textWidth, mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            Gui.drawRect(-3, -3, textWidth, -2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(com.alpharip.staffmod.gui.UiText.normalize(text), 0, 0, getTextColor());
        GlStateManager.popMatrix();
    }

    public static int getHudX() {
        JsonObject settings = getSettings();
        return settings.has("hudX") ? Math.max(0, settings.get("hudX").getAsInt()) : 6;
    }

    public static int getHudY() {
        JsonObject settings = getSettings();
        return settings.has("hudY") ? Math.max(0, settings.get("hudY").getAsInt()) : 6;
    }

    public static void setHudPosition(int x, int y) {
        JsonObject settings = getSettings();
        settings.addProperty("hudX", Math.max(0, x));
        settings.addProperty("hudY", Math.max(0, y));
        saveSettings();
    }

    public static boolean isShowBackground() {
        JsonObject settings = getSettings();
        return !settings.has("showBackground") || settings.get("showBackground").getAsBoolean();
    }

    public static void setShowBackground(boolean show) {
        getSettings().addProperty("showBackground", show);
        saveSettings();
    }

    public static boolean isEnabled() {
        return getSettings().has("enabled") && getSettings().get("enabled").getAsBoolean();
    }

    public static void setEnabled(boolean enabled) {
        JsonObject settings = getSettings();
        settings.addProperty("enabled", enabled);
        saveSettings();
    }

    public static int getDisplayMode() {
        JsonObject settings = getSettings();
        int mode = settings.has("displayMode") ? settings.get("displayMode").getAsInt() : MODE_ALWAYS_VISIBLE;
        return mode == MODE_ONLY_WHILE_AFK ? MODE_ONLY_WHILE_AFK : MODE_ALWAYS_VISIBLE;
    }

    public static void setDisplayMode(int mode) {
        getSettings().addProperty("displayMode",
                mode == MODE_ONLY_WHILE_AFK ? MODE_ONLY_WHILE_AFK : MODE_ALWAYS_VISIBLE);
        saveSettings();
    }

    public static String getDisplayModeName() {
        return getDisplayMode() == MODE_ONLY_WHILE_AFK ? "Only While AFK" : "Always Visible";
    }

    public static void cycleDisplayMode() {
        setDisplayMode(getDisplayMode() == MODE_ALWAYS_VISIBLE
                ? MODE_ONLY_WHILE_AFK : MODE_ALWAYS_VISIBLE);
    }

    public static String getColorName() {
        int index = getColorIndex();
        return COLOR_NAMES[index];
    }

    public static void cycleColor() {
        int next = (getColorIndex() + 1) % COLOR_NAMES.length;
        getSettings().addProperty("colorIndex", next);
        saveSettings();
    }

    private static int getColorIndex() {
        JsonObject settings = getSettings();
        int index = settings.has("colorIndex") ? settings.get("colorIndex").getAsInt() : 3;
        return index < 0 || index >= COLOR_NAMES.length ? 3 : index;
    }

    private static int getTextColor() {
        int index = getColorIndex();
        if (index == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[index];
    }

    private static JsonObject getSettings() {
        return ModuleProfileManager.getSelectedModule(MODULE_ID);
    }

    private static void saveSettings() {
        ModuleProfileManager.save();
    }

    private static String formatDuration(long elapsedMillis) {
        long totalSeconds = Math.max(0L, elapsedMillis / 1000L);
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0L) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        if (totalSeconds >= 3600L) {
            return String.format("%02d:%02d:%02d", totalSeconds / 3600L, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
