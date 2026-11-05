// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.config.HudScaleConfig;
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
        if (event == null || event.message == null || event.type == 2) return;

        updateCurrentAddress();
        if (currentAddress.isEmpty()) return;

        String message = event.message.getUnformattedText();
        Minecraft minecraft = Minecraft.getMinecraft();
        String playerName = minecraft == null || minecraft.thePlayer == null ? null : minecraft.thePlayer.getName();
        WinLossResult result = WinLossResultDetector.detect(message, playerName);
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
        if (currentAddress.isEmpty()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.fontRendererObj == null || minecraft.gameSettings == null
                || minecraft.gameSettings.showDebugInfo || minecraft.gameSettings.hideGUI) return;

        WinLossServerProfile profile = TRACKER.findProfileByAddress(currentAddress);
        WinLossStatistics current = profile == null
                ? new WinLossStatistics(0, 0) : profile.getAddressStatistics(currentAddress);
        WinLossStatistics overall = profile == null
                ? new WinLossStatistics(0, 0) : profile.getOverallStatistics();
        drawHud(minecraft, current, overall, profile, currentAddress, TRACKER.getSettings());
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

    public static void renderEditorPreview(int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.fontRendererObj == null) return;
        drawHud(minecraft, new WinLossStatistics(12, 4),
                new WinLossStatistics(28, 12), null, "", TRACKER.getSettings());
    }

    public static int getHudWidth(Minecraft minecraft) {
        return (int) Math.ceil(170 * HudScaleConfig.get("win-loss-tracker"));
    }

    public static int getHudHeight() {
        WinLossTrackerSettings settings = TRACKER.getSettings();
        WinLossServerProfile profile = TRACKER.getCurrentServerProfile();
        int extraRows = 0;
        if (settings.isShowIndividualAddressStatistics() && profile != null) {
            for (String address : profile.getAddresses()) {
                if (!address.equals(TRACKER.getCurrentServerAddress())) extraRows++;
            }
        }
        int rows = (settings.isShowCurrentAddressStatistics() ? 1 : 0)
                + (settings.isShowOverallStatistics() ? 1 : 0) + extraRows;
        int baseHeight = Math.max(32, 22 + rows * 9);
        return (int) Math.ceil(baseHeight * HudScaleConfig.get("win-loss-tracker"));
    }

    private static void drawHud(Minecraft minecraft, WinLossStatistics current,
                                WinLossStatistics overall, WinLossServerProfile profile,
                                String currentAddress, WinLossTrackerSettings settings) {
        int x = HudPositionConfig.getWinLossX();
        int y = HudPositionConfig.getWinLossY();
        boolean showCurrent = settings.isShowCurrentAddressStatistics();
        boolean showOverall = settings.isShowOverallStatistics();
        boolean showIndividual = settings.isShowIndividualAddressStatistics() && profile != null;
        if (!showCurrent && !showOverall && !showIndividual) return;

        int extraRows = 0;
        if (showIndividual) {
            for (String address : profile.getAddresses()) {
                if (!address.equals(currentAddress)) extraRows++;
            }
        }
        int rows = (showCurrent ? 1 : 0) + (showOverall ? 1 : 0) + extraRows;
        int height = Math.max(32, 22 + rows * 9);
        int width = 170;
        float scale = HudScaleConfig.get("win-loss-tracker");

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        Gui.drawRect(0, 0, width, height, 0xD9101219);
        Gui.drawRect(0, 0, width, 2, 0xFFFFD866);
        Gui.drawRect(0, 3, 1, height - 1, 0xAAFFD866);
        minecraft.fontRendererObj.drawString("W&L TRACKER", 6, 4, 0xFFFFD866, false);
        minecraft.fontRendererObj.drawString("LIVE", width - 30, 4, 0xFF8D929E, false);
        int rowY = 15;
        if (showCurrent) {
            drawStatistics(minecraft, current, 6, rowY);
            rowY += 9;
        }
        if (showOverall) {
            if (showCurrent) {
                String summary = "ALL " + overall.getWins() + "/" + overall.getLosses()
                        + "  " + String.format(Locale.ROOT, "%.1f%%", overall.getWinRate());
                minecraft.fontRendererObj.drawString(summary, 6, rowY, 0xFFB8BEC9, false);
            } else {
                drawStatistics(minecraft, overall, 6, rowY);
            }
            rowY += 9;
        }
        if (showIndividual) {
            for (String address : profile.getAddresses()) {
                if (address.equals(currentAddress)) continue;
                WinLossStatistics stats = profile.getAddressStatistics(address);
                String line = address + "  W/L " + stats.getWins() + "/" + stats.getLosses()
                        + "  " + String.format(Locale.ROOT, "%.1f%%", stats.getWinRate());
                line = minecraft.fontRendererObj.trimStringToWidth(line, width - 12);
                minecraft.fontRendererObj.drawString(line, 6, rowY, 0xFF9CA3AF, false);
                rowY += 11;
            }
        }
        GlStateManager.popMatrix();
    }

    private static void drawStatistics(Minecraft minecraft, WinLossStatistics statistics, int x, int y) {
        minecraft.fontRendererObj.drawString("W " + statistics.getWins(), x, y, 0xFF55E58A, false);
        minecraft.fontRendererObj.drawString("L " + statistics.getLosses(), x + 34, y, 0xFFFF6868, false);
        minecraft.fontRendererObj.drawString(String.format(Locale.ROOT, "%.1f%%", statistics.getWinRate()),
                x + 68, y, 0xFFFFFFFF, false);
    }
}
