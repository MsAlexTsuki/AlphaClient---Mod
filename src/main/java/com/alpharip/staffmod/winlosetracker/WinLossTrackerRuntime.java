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
 * Forge event adapter. Registration is intentionally left to the actual mod
 * integration layer; this feature module does not assume an entrypoint class.
 */
public final class WinLossTrackerRuntime {
    private static final WinLossTracker TRACKER = WinLossTracker.getInstance();
    private static final File DATA_FILE = createDataFile();
    private String currentAddress = "";
    private String lastResultMessage = "";
    private long lastResultAt;
    private boolean loaded;
    private boolean persistenceReady;

    private static File createDataFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(new File(base, "config"), "alphaclient"), "winlosetracker.properties");
    }

    /** Call once from the real mod integration layer before event handling begins. */
    public void initialize() {
        if (loaded) return;
        try {
            WinLossTrackerStorage.load(DATA_FILE, TRACKER);
            persistenceReady = true;
        } catch (java.io.IOException exception) {
            // Keep the in-memory tracker usable, but never overwrite unreadable saved data.
            persistenceReady = false;
            System.err.println("[AlphaClient] Win/Loss Tracker storage was not loaded: " + exception.getMessage());
        }
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

        // Synchronize the active address before applying duplicate-result protection.
        updateCurrentAddress();
        String address = currentAddress;
        if (address.isEmpty()) return;

        String message = event.message.getUnformattedText();
        WinLossResult result = WinLossResultDetector.detect(message);
        if (result == null) return;

        long now = System.currentTimeMillis();
        String normalized = address + "\n" + message.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(lastResultMessage) && now - lastResultAt < 10000L) return;

        if (TRACKER.recordMatch(address, result, now)) {
            lastResultMessage = normalized;
            lastResultAt = now;
            if (persistenceReady) WinLossTrackerStorage.save(TRACKER, DATA_FILE);
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
            if (persistenceReady) WinLossTrackerStorage.save(TRACKER, DATA_FILE);
        }
    }

    private String getCurrentAddress() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return "";
        ServerData server = mc.getCurrentServerData();
        return server == null ? "" : WinLossServerProfile.normalizeAddress(server.serverIP);
    }
}
