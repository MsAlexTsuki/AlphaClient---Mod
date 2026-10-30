// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.serverhistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Records previously connected multiplayer servers for the Server History screen. */
public final class ServerHistoryManager {
    public static final int MAX_SAVED_SERVERS = 20;
    private static final ServerHistoryManager INSTANCE = new ServerHistoryManager();
    private static final List<Entry> ENTRIES = new ArrayList<Entry>();

    private static boolean enabled = true;
    private boolean connectedToServer;

    private ServerHistoryManager() {
    }

    public static ServerHistoryManager getInstance() {
        return INSTANCE;
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(boolean value) {
        enabled = value;
        if (!value) INSTANCE.connectedToServer = false;
        save();
    }

    public static synchronized List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(ENTRIES));
    }

    public static synchronized void clearHistory() {
        ENTRIES.clear();
        save();
    }

    public static synchronized void loadSettings() {
        File file = settingsFile();
        if (!file.isFile()) {
            save();
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root == null || !root.isJsonObject()) return;

            JsonObject object = root.getAsJsonObject();
            JsonElement enabledValue = object.get("enabled");
            if (enabledValue != null && enabledValue.isJsonPrimitive()) {
                enabled = enabledValue.getAsBoolean();
            }

            ENTRIES.clear();
            JsonElement serversValue = object.get("servers");
            if (serversValue != null && serversValue.isJsonArray()) {
                JsonArray servers = serversValue.getAsJsonArray();
                for (JsonElement element : servers) {
                    if (!element.isJsonObject()) continue;
                    JsonObject server = element.getAsJsonObject();
                    String name = readString(server, "name");
                    String address = readString(server, "address");
                    long lastVisited = readLong(server, "lastVisited");
                    int joinCount = readInt(server, "joinCount");
                    if (address.isEmpty()) continue;
                    addLoadedEntry(new Entry(name.isEmpty() ? address : name, address,
                            lastVisited > 0L ? lastVisited : System.currentTimeMillis(),
                            Math.max(1, joinCount)));
                }
            }
            trimEntries();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Server History: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        boolean connected = mc != null && mc.theWorld != null && mc.thePlayer != null
                && mc.getCurrentServerData() != null;

        if (!connected) {
            connectedToServer = false;
            return;
        }

        if (!connectedToServer) {
            if (isEnabled()) recordServer(mc.getCurrentServerData());
            connectedToServer = true;
        }
    }

    private static synchronized void recordServer(ServerData serverData) {
        if (!enabled || serverData == null) return;
        String address = serverData.serverIP == null ? "" : serverData.serverIP.trim();
        if (address.isEmpty()) return;

        String key = address.toLowerCase(Locale.ROOT);
        int joinCount = 1;
        for (int i = ENTRIES.size() - 1; i >= 0; i--) {
            Entry existing = ENTRIES.get(i);
            if (existing.address.toLowerCase(Locale.ROOT).equals(key)) {
                joinCount = existing.joinCount + 1;
                ENTRIES.remove(i);
            }
        }

        String name = serverData.serverName == null || serverData.serverName.trim().isEmpty()
                ? address : serverData.serverName.trim();
        ENTRIES.add(0, new Entry(name, address, System.currentTimeMillis(), joinCount));
        trimEntries();
        save();
    }

    private static void addLoadedEntry(Entry entry) {
        String key = entry.address.toLowerCase(Locale.ROOT);
        for (int i = ENTRIES.size() - 1; i >= 0; i--) {
            if (ENTRIES.get(i).address.toLowerCase(Locale.ROOT).equals(key)) {
                ENTRIES.remove(i);
            }
        }
        ENTRIES.add(entry);
    }

    private static void trimEntries() {
        while (ENTRIES.size() > MAX_SAVED_SERVERS) {
            ENTRIES.remove(ENTRIES.size() - 1);
        }
    }

    private static String readString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static long readLong(JsonObject object, String key) {
        JsonElement value = object.get(key);
        try {
            return value != null && value.isJsonPrimitive() ? value.getAsLong() : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static int readInt(JsonObject object, String key) {
        JsonElement value = object.get(key);
        try {
            return value != null && value.isJsonPrimitive() ? value.getAsInt() : 1;
        } catch (Exception ignored) {
            return 1;
        }
    }

    private static synchronized void save() {
        File file = settingsFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("[AlphaClient] Could not create Server History config directory.");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        JsonArray servers = new JsonArray();
        for (Entry entry : ENTRIES) {
            JsonObject server = new JsonObject();
            server.addProperty("name", entry.name);
            server.addProperty("address", entry.address);
            server.addProperty("lastVisited", entry.lastVisited);
            server.addProperty("joinCount", entry.joinCount);
            servers.add(server);
        }
        root.add("servers", servers);

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Server History: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File settingsFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-server-history.json");
    }

    public static final class Entry {
        private final String name;
        private final String address;
        private final long lastVisited;
        private final int joinCount;

        private Entry(String name, String address, long lastVisited, int joinCount) {
            this.name = name;
            this.address = address;
            this.lastVisited = lastVisited;
            this.joinCount = joinCount;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public long getLastVisited() { return lastVisited; }
        public int getJoinCount() { return joinCount; }
    }
}
