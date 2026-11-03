package com.alpharip.staffmod.winlosetracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.util.Locale;

/**
 * Forge event adapter. AlphaClient must initialize and register this listener
 * from its existing central initialization method.
 */
public final class WinLossTrackerRuntime {
    private static final WinLossTracker TRACKER = WinLossTracker.getInstance();
    private static final File DATA_FILE = createDataFile();
    private String currentAddress = "";
    private String lastResultMessage = "";
    private long lastResultAt;
    private boolean loaded;

    private static File createDataFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(new File(base, "config"), "alphaclient"), "winlosetracker.properties");
    }

    /** Call once during AlphaClient initialization before registering this listener. */
    public void initialize() {
        if (loaded) return;
        WinLossTrackerStorage.load(TRACKER, DATA_FILE);
        loaded = true;
        updateCurrentAddress();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        if (!loaded) initialize();
        updateCurrentAddress();
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!loaded) initialize();
        if (event == null || event.message == null || event.type == 2) return;

        String address = getCurrentAddress();
        if (address.isEmpty()) return;

        String message = event.message.getUnformattedText();
        WinLossResult result = WinLossResultDetector.detect(message);
        if (result == null) return;

        long now = System.currentTimeMillis();
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(lastResultMessage) && now - lastResultAt < 10000L) return;

        if (TRACKER.recordMatch(address, result, now)) {
            lastResultMessage = normalized;
            lastResultAt = now;
            WinLossTrackerStorage.save(TRACKER, DATA_FILE);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!loaded) initialize();
        if (event == null || event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;

        WinLossServerProfile profile = TRACKER.findProfileByAddress(getCurrentAddress());
        if (profile == null) return;

        WinLossTrackerSettings settings = TRACKER.getSettings();
        int y = 6;
        if (settings.isShowCurrentAddressStatistics()) {
            WinLossStatistics stats = profile.getAddressStatistics(currentAddress);
            drawLine(mc, currentAddress + " W/L: " + stats.getWins() + "/" + stats.getLosses()
                    + " (" + formatRate(stats) + ")", 6, y);
            y += 12;
        }
        if (settings.isShowOverallStatistics()) {
            WinLossStatistics stats = profile.getOverallStatistics();
            drawLine(mc, profile.getDisplayName() + " Overall W/L: " + stats.getWins() + "/"
                    + stats.getLosses() + " (" + formatRate(stats) + ")", 6, y);
            y += 12;
        }
        if (settings.isShowIndividualAddressStatistics()) {
            for (String address : profile.getAddresses()) {
                if (address.equals(currentAddress)) continue;
                WinLossStatistics stats = profile.getAddressStatistics(address);
                drawLine(mc, address + " W/L: " + stats.getWins() + "/" + stats.getLosses()
                        + " (" + formatRate(stats) + ")", 6, y);
                y += 12;
            }
        }
    }

    private static void drawLine(Minecraft mc, String text, int x, int y) {
        int width = mc.fontRendererObj.getStringWidth(text);
        Gui.drawRect(x - 3, y - 2, x + width + 3,
                y + mc.fontRendererObj.FONT_HEIGHT + 2, 0x990A0C11);
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
    }

    private static String formatRate(WinLossStatistics stats) {
        return String.format(Locale.ROOT, "%.1f%%", stats.getWinRate());
    }

    private void updateCurrentAddress() {
        String address = getCurrentAddress();
        if (!address.equals(currentAddress)) {
            currentAddress = address;
            TRACKER.setCurrentServerAddress(address);
            lastResultMessage = "";
            lastResultAt = 0L;
            // Persist newly discovered profiles and the last connected address
            // even if the player leaves before completing a match.
            WinLossTrackerStorage.save(TRACKER, DATA_FILE);
        }
    }

    private String getCurrentAddress() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return "";
        ServerData server = mc.getCurrentServerData();
        return server == null ? "" : WinLossServerProfile.normalizeAddress(server.serverIP);
    }
}
