package com.alpharip.staffmod.quality;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Sends one GG after a detected player kill or match-end message.
 * Detection is based on common server chat messages because Forge has no
 * universal match-result event shared by SkyWars, BedWars and duel servers.
 */
public final class AutoGGMod {
    private static final int DEFAULT_DELAY_TICKS = 40; // 2 seconds
    private static final int COOLDOWN_TICKS = 160; // 8 seconds
    private static final int MAX_DELAY_TICKS = 200; // 10 seconds
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    private static int delayTicks = DEFAULT_DELAY_TICKS;
    private static boolean enabled;
    private int pendingTicks = -1;
    private int cooldownTicks;
    private boolean pending;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean shouldEnable) {
        if (enabled == shouldEnable) return;
        enabled = shouldEnable;
        saveSettings();
    }

    public static double getDelaySeconds() {
        return delayTicks / 20.0D;
    }

    public static void setDelaySeconds(double seconds) {
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) return;
        delayTicks = Math.max(0, Math.min(MAX_DELAY_TICKS, (int) Math.round(seconds * 20.0D)));
        saveSettings();
    }

    public static void loadSettings() {
        FileReader reader = null;
        try {
            File file = getSettingsFile();
            if (!file.isFile()) {
                saveSettings();
                return;
            }
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root != null && root.isJsonObject()) {
                JsonElement enabledValue = root.getAsJsonObject().get("enabled");
                if (enabledValue != null && enabledValue.isJsonPrimitive()) {
                    enabled = enabledValue.getAsBoolean();
                }
                JsonElement delay = root.getAsJsonObject().get("delayTicks");
                if (delay != null && delay.isJsonPrimitive()) {
                    delayTicks = Math.max(0, Math.min(MAX_DELAY_TICKS, delay.getAsInt()));
                }
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Auto GG settings: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static void saveSettings() {
        File file = getSettingsFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("delayTicks", delayTicks);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Auto GG settings: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getSettingsFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File base = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-autogg.json");
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event == null || event.message == null || event.type == 2 || !isEnabled()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) return;
        if (pending || cooldownTicks > 0) return;

        String message = event.message.getUnformattedText();
        if (message == null || message.trim().isEmpty()) return;
        String normalized = SPACE_PATTERN.matcher(message.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        if (isKillMessage(normalized, minecraft.thePlayer.getName())
                || isMatchEndMessage(normalized)) {
            pending = true;
            pendingTicks = delayTicks;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (cooldownTicks > 0) cooldownTicks--;

        if (!pending) return;
        if (!isEnabled()) {
            pending = false;
            pendingTicks = -1;
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) {
            pending = false;
            pendingTicks = -1;
            return;
        }

        if (pendingTicks > 0) {
            pendingTicks--;
            return;
        }

        pending = false;
        pendingTicks = -1;
        minecraft.thePlayer.sendChatMessage("GG");
        cooldownTicks = COOLDOWN_TICKS;
    }

    private static boolean isKillMessage(String message, String playerName) {
        String ownName = playerName == null ? "" : playerName.toLowerCase(Locale.ROOT);
        if (message.contains("you killed ") || message.contains("you have killed ")
                || message.contains("you slew ")) {
            return true;
        }
        if (message.matches(".*\\bwas killed by (you|" + Pattern.quote(ownName) + ")\\b.*")
                || message.matches(".*\\bkilled by (you|" + Pattern.quote(ownName) + ")\\b.*")) {
            return true;
        }
        return !ownName.isEmpty()
                && message.matches(".*\\b" + Pattern.quote(ownName) + " killed [^.!?]+[.!?]?.*");
    }

    private static boolean isMatchEndMessage(String message) {
        return message.contains("game over")
                || message.contains("match over")
                || message.contains("match ended")
                || message.contains("match has ended")
                || message.contains("game ended")
                || message.contains("game has ended")
                || message.contains("round over")
                || message.contains("round has ended")
                || message.contains("you won")
                || message.contains("you lost")
                || message.contains("you are the winner")
                || message.contains("victory!")
                || message.contains("defeat!")
                || message.contains("winner:");
    }
}
