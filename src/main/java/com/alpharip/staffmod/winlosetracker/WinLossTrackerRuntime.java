// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Forge event adapter for the W&L Tracker module. */
public final class WinLossTrackerRuntime {
    private static final WinLossTracker TRACKER = WinLossTracker.getInstance();
    private static final String MODULE_ID = "win-loss-tracker";
    private static final long DUPLICATE_WINDOW_MS = 10000L;

    private final File dataFile;
    private String currentAddress = "";
    private String lastResultMessage = "";
    private long lastResultAt;
    private boolean initialized;
    private boolean persistenceAvailable;

    public WinLossTrackerRuntime() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null ? new File(".") : minecraft.mcDataDir;
        this.dataFile = new File(new File(new File(gameDirectory, "config"), "alphaclient"),
                "winlosetracker.properties");
    }

    /** Loads local data once before event handling begins. */
    public synchronized void initialize() {
        if (initialized) return;

        try {
            WinLossTrackerStorage.load(dataFile, TRACKER);
            persistenceAvailable = true;
        } catch (IOException invalidStorage) {
            // Do not overwrite a file that could not be read or validated.
            persistenceAvailable = false;
            System.err.println("[AlphaClient] W&L Tracker data was not loaded: "
                    + invalidStorage.getMessage());
        }

        initialized = true;
        updateCurrentAddress();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        if (!initialized) initialize();
        updateCurrentAddress();
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!initialized) initialize();
        if (!isModuleEnabled() || event == null || event.message == null || event.type == 2) return;

        updateCurrentAddress();
        if (currentAddress.isEmpty()) return;

        String message = event.message.getUnformattedText();
        WinLossResult result = WinLossResultDetector.detect(message);
        if (result == null) return;

        long now = System.currentTimeMillis();
        String duplicateKey = currentAddress + "\n" + message.trim().toLowerCase(Locale.ROOT);
        if (duplicateKey.equals(lastResultMessage) && now - lastResultAt < DUPLICATE_WINDOW_MS) return;

        if (TRACKER.recordMatch(currentAddress, result, now)) {
            lastResultMessage = duplicateKey;
            lastResultAt = now;
            saveIfAvailable();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!initialized) initialize();
        if (!isModuleEnabled() || event == null
                || event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        updateCurrentAddress();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.fontRendererObj == null || minecraft.gameSettings == null
                || minecraft.gameSettings.showDebugInfo || minecraft.gameSettings.hideGUI) return;

        WinLossServerProfile profile = TRACKER.findProfileByAddress(currentAddress);
        if (profile == null) return;

        WinLossTrackerSettings settings = TRACKER.getSettings();
        int y = 6;
        if (settings.isShowCurrentAddressStatistics()) {
            WinLossStatistics statistics = profile.getAddressStatistics(currentAddress);
            drawLine(minecraft, currentAddress + " W/L: " + statistics.getWins() + "/"
                    + statistics.getLosses() + " (" + formatRate(statistics) + ")", 6, y);
            y += 12;
        }
        if (settings.isShowOverallStatistics()) {
            WinLossStatistics statistics = profile.getOverallStatistics();
            drawLine(minecraft, profile.getDisplayName() + " Overall W/L: "
                    + statistics.getWins() + "/" + statistics.getLosses()
                    + " (" + formatRate(statistics) + ")", 6, y);
            y += 12;
        }
        if (settings.isShowIndividualAddressStatistics()) {
            for (String address : profile.getAddresses()) {
                if (address.equals(currentAddress)) continue;
                WinLossStatistics statistics = profile.getAddressStatistics(address);
                drawLine(minecraft, address + " W/L: " + statistics.getWins() + "/"
                        + statistics.getLosses() + " (" + formatRate(statistics) + ")", 6, y);
                y += 12;
            }
        }
    }

    private boolean isModuleEnabled() {
        AlphaModule module = AlphaModuleRegistry.find(MODULE_ID);
        return module != null && module.isImplemented() && module.isEnabled();
    }

    private void updateCurrentAddress() {
        String connectedAddress = getConnectedAddress();
        if (connectedAddress.equals(currentAddress)) return;

        currentAddress = connectedAddress;
        TRACKER.setCurrentServerAddress(connectedAddress);
        lastResultMessage = "";
        lastResultAt = 0L;
        saveIfAvailable();
    }

    private String getConnectedAddress() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return "";
        ServerData server = minecraft.getCurrentServerData();
        return server == null ? "" : WinLossServerProfile.normalizeAddress(server.serverIP);
    }

    private void saveIfAvailable() {
        if (!persistenceAvailable) return;
        try {
            WinLossTrackerStorage.save(TRACKER, dataFile);
        } catch (IOException saveFailure) {
            System.err.println("[AlphaClient] Could not save W&L Tracker data: "
                    + saveFailure.getMessage());
        }
    }

    private static void drawLine(Minecraft minecraft, String text, int x, int y) {
        int textWidth = minecraft.fontRendererObj.getStringWidth(text);
        Gui.drawRect(x - 3, y - 2, x + textWidth + 3,
                y + minecraft.fontRendererObj.FONT_HEIGHT + 2, 0x990A0C11);
        minecraft.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
    }

    private static String formatRate(WinLossStatistics statistics) {
        return String.format(Locale.ROOT, "%.1f%%", statistics.getWinRate());
    }
}
