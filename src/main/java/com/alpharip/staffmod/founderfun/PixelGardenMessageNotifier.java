// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Local preferences and chat reminders for Pixel Garden. */
public final class PixelGardenMessageNotifier {
    private static final PixelGardenMessageNotifier INSTANCE = new PixelGardenMessageNotifier();
    private static final ZoneId NETHERLANDS_ZONE = ZoneId.of("Europe/Amsterdam");
    private static final String READY_MESSAGE = "Your crops are ready to harvest, give them some love!";
    private static final String DAILY_MESSAGE = "Pixel Garden needs some love <3";
    private static final String FIRST_JOIN_MESSAGE = "If you want to turn this off you can use /garden msg off";

    private boolean enabled = true;
    private boolean firstJoinTipShown;
    private String lastDailyDate = "";
    private boolean[] notifiedReadyPlots = new boolean[PixelGardenGame.MAX_PLOTS];

    private PixelGardenMessageNotifier() {
        load();
    }

    public static PixelGardenMessageNotifier getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean value) {
        enabled = value;
        save();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.getCurrentServerData() == null) {
            return;
        }
        if (!enabled) return;

        LocalDate today = LocalDate.now(NETHERLANDS_ZONE);
        String date = today.toString();
        if (!date.equals(lastDailyDate)) {
            send(minecraft, DAILY_MESSAGE);
            lastDailyDate = date;
            save();
        }

        if (!firstJoinTipShown) {
            send(minecraft, FIRST_JOIN_MESSAGE);
            firstJoinTipShown = true;
            save();
        }

        checkReadyCrops(minecraft);
    }

    private void checkReadyCrops(Minecraft minecraft) {
        List<PixelGardenGame.Plot> plots = PixelGardenGame.getInstance().getPlots();
        if (notifiedReadyPlots.length != plots.size()) {
            boolean[] resized = new boolean[plots.size()];
            System.arraycopy(notifiedReadyPlots, 0, resized, 0, Math.min(notifiedReadyPlots.length, resized.length));
            notifiedReadyPlots = resized;
        }
        boolean changed = false;
        boolean anyNewReadyCrop = false;
        long now = System.currentTimeMillis();
        for (int i = 0; i < plots.size(); i++) {
            boolean ready = plots.get(i).getState(now) == PixelGardenGame.PlotState.READY;
            if (ready && !notifiedReadyPlots[i]) {
                notifiedReadyPlots[i] = true;
                anyNewReadyCrop = true;
                changed = true;
            } else if (!ready && notifiedReadyPlots[i]) {
                notifiedReadyPlots[i] = false;
                changed = true;
            }
        }
        if (anyNewReadyCrop) send(minecraft, READY_MESSAGE);
        if (changed) save();
    }

    private static void send(Minecraft minecraft, String message) {
        if (minecraft != null && minecraft.thePlayer != null) {
            minecraft.thePlayer.addChatMessage(new ChatComponentText("§e" + message));
        }
    }

    private File getFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File base = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-pixelgarden-messages.json");
    }

    private synchronized void load() {
        File file = getFile();
        if (!file.isFile()) {
            save();
            return;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (root.has("enabled")) enabled = root.get("enabled").getAsBoolean();
            if (root.has("first_join_tip_shown")) firstJoinTipShown = root.get("first_join_tip_shown").getAsBoolean();
            if (root.has("last_daily_date")) lastDailyDate = root.get("last_daily_date").getAsString();
            if (root.has("notified_ready_plots") && root.get("notified_ready_plots").isJsonArray()) {
                com.google.gson.JsonArray array = root.getAsJsonArray("notified_ready_plots");
                notifiedReadyPlots = new boolean[Math.max(PixelGardenGame.MAX_PLOTS, array.size())];
                for (int i = 0; i < Math.min(array.size(), notifiedReadyPlots.length); i++) {
                    notifiedReadyPlots[i] = array.get(i).getAsBoolean();
                }
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not load Pixel Garden message settings: " + exception.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
    }

    private synchronized void save() {
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("first_join_tip_shown", firstJoinTipShown);
        root.addProperty("last_daily_date", lastDailyDate);
        com.google.gson.JsonArray ready = new com.google.gson.JsonArray();
        for (boolean value : notifiedReadyPlots) ready.add(new JsonPrimitive(value));
        root.add("notified_ready_plots", ready);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not save Pixel Garden message settings: " + exception.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }
}
