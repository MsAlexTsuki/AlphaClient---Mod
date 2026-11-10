package com.alpharip.staffmod.timing.brewing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Standalone Brewing Timer prototype for Minecraft 1.8.9 / Forge.
 *
 * Reads the vanilla brewing stand's remaining brew ticks while its GUI is
 * open, then keeps a local countdown visible after the GUI is closed.
 * This prototype is intentionally not registered with Alpha Client Core.
 */
public final class BrewingTimer {
    public static final String NAME = "Brewing Timer";
    public static final String CATEGORY = "Timing";
    public static final boolean REQUIRES_SUPABASE = false;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final BrewingStandReader reader = new BrewingStandReader();
    private final Settings settings = new Settings();
    private final EventHandler eventHandler = new EventHandler();

    private boolean registered;
    private long estimatedFinishMillis;
    private boolean hasObservedBrew;

    /** Register the standalone Forge listeners. Safe to call more than once. */
    public void start() {
        if (registered) return;
        MinecraftForge.EVENT_BUS.register(eventHandler);
        registered = true;
    }

    /** Unregister listeners and clear the current local estimate. */
    public void stop() {
        if (!registered) return;
        MinecraftForge.EVENT_BUS.unregister(eventHandler);
        registered = false;
        clearTimer();
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isEnabled() {
        return settings.enabled;
    }

    public void setEnabled(boolean enabled) {
        settings.enabled = enabled;
        if (!enabled) clearTimer();
    }

    public boolean isHudEnabled() {
        return settings.showHud;
    }

    public void setHudEnabled(boolean showHud) {
        settings.showHud = showHud;
    }

    public long getRemainingMillis() {
        if (!hasObservedBrew) return 0L;
        return Math.max(0L, estimatedFinishMillis - System.currentTimeMillis());
    }

    public int getRemainingSeconds() {
        return (int) Math.ceil(getRemainingMillis() / 1000.0D);
    }

    public boolean isBrewing() {
        return getRemainingMillis() > 0L;
    }

    public void openSettings(GuiScreen parent) {
        mc.displayGuiScreen(new BrewingTimerSettingsScreen(parent, this));
    }

    private void observeRemainingTicks(int ticks) {
        if (ticks <= 0) {
            clearTimer();
            return;
        }
        estimatedFinishMillis = System.currentTimeMillis() + ticks * 50L;
        hasObservedBrew = true;
    }

    private void clearTimer() {
        estimatedFinishMillis = 0L;
        hasObservedBrew = false;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, remainder);
    }

    private final class EventHandler {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !registered || !settings.enabled) return;
            if (mc.thePlayer == null || mc.theWorld == null) {
                clearTimer();
                return;
            }

            if (mc.currentScreen instanceof GuiBrewingStand) {
                int remainingTicks = reader.readRemainingTicks(mc.thePlayer.openContainer);
                if (remainingTicks >= 0) {
                    observeRemainingTicks(remainingTicks);
                }
            } else if (getRemainingMillis() <= 0L) {
                clearTimer();
            }
        }

        @SubscribeEvent
        public void onOverlay(RenderGameOverlayEvent.Post event) {
            if (!registered || !settings.enabled || !settings.showHud
                    || event.type != RenderGameOverlayEvent.ElementType.ALL
                    || mc.thePlayer == null || mc.theWorld == null || !isBrewing()) {
                return;
            }

            String text = "Brewing: " + formatTime(getRemainingSeconds());
            mc.fontRendererObj.drawStringWithShadow(text, 6, 20, 0xFFFFC83D);
        }
    }

    private static final class Settings {
        private boolean enabled = true;
        private boolean showHud = true;
    }
}
