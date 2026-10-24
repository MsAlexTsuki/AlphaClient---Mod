package com.alpharip.staffmod.quality;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Reconnects to the last multiplayer server after an unexpected disconnect. */
public final class AutoReconnectMod {
    private static final int DEFAULT_DELAY_TICKS = 60;
    private static final int MAX_DELAY_TICKS = 600;

    private static boolean enabled;
    private static int delayTicks = DEFAULT_DELAY_TICKS;
    private static ServerData lastServer;
    private static int reconnectTicks = -1;
    private static GuiScreen disconnectScreen;
    private static boolean attemptStarted;

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        saveSettings();
        if (!enabled) resetPending();
    }

    public static double getDelaySeconds() { return delayTicks / 20.0D; }

    public static void setDelaySeconds(double seconds) {
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) return;
        delayTicks = Math.max(0, Math.min(MAX_DELAY_TICKS, (int)Math.round(seconds * 20.0D)));
        saveSettings();
    }

    public static void loadSettings() {
        FileReader reader = null;
        try {
            File file = settingsFile();
            if (!file.isFile()) { saveSettings(); return; }
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root != null && root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                JsonElement e = object.get("enabled");
                if (e != null && e.isJsonPrimitive()) enabled = e.getAsBoolean();
                JsonElement d = object.get("delayTicks");
                if (d != null && d.isJsonPrimitive()) delayTicks = Math.max(0, Math.min(MAX_DELAY_TICKS, d.getAsInt()));
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Auto Reconnect settings: " + e.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
    }

    private static void saveSettings() {
        File file = settingsFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("delayTicks", delayTicks);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(object.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Auto Reconnect settings: " + e.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }

    private static File settingsFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-autoreconnect.json");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        if (mc.theWorld != null && mc.thePlayer != null && mc.getCurrentServerData() != null) {
            lastServer = mc.getCurrentServerData();
            resetPending();
            return;
        }

        if (!enabled || !(mc.currentScreen instanceof GuiDisconnected)) {
            if (!(mc.currentScreen instanceof GuiConnecting)) resetPending();
            return;
        }

        if (lastServer == null) lastServer = mc.getCurrentServerData();
        if (lastServer == null) return;

        if (disconnectScreen != mc.currentScreen) {
            disconnectScreen = mc.currentScreen;
            reconnectTicks = delayTicks;
            attemptStarted = false;
        }
        if (attemptStarted) return;
        if (reconnectTicks > 0) {
            reconnectTicks--;
            return;
        }

        attemptStarted = true;
        mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, lastServer));
    }

    private static void resetPending() {
        reconnectTicks = -1;
        disconnectScreen = null;
        attemptStarted = false;
    }
}
