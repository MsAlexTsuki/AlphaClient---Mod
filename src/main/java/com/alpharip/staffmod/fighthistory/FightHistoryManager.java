// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.fighthistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks confirmed player hits independently for every opponent.
 * A fight ends after 12 seconds without another confirmed hit on that opponent.
 */
public final class FightHistoryManager {
    private static final long FIGHT_TIMEOUT_MS = 12000L;
    private static final long HIT_CONFIRMATION_TIMEOUT_MS = 1000L;
    private static final FightHistoryManager INSTANCE = new FightHistoryManager();

    private final Map<UUID, PendingHit> pendingHits = new HashMap<UUID, PendingHit>();
    private final Map<UUID, FightEntry> activeFights = new HashMap<UUID, FightEntry>();
    private final List<FightEntry> history = new ArrayList<FightEntry>();
    private long lastPresenceSaveAt;
    private static boolean enabled = true;

    private FightHistoryManager() { }

    public static FightHistoryManager getInstance() {
        return INSTANCE;
    }

    public static synchronized void loadHistory() {
        INSTANCE.load();
    }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            INSTANCE.pendingHits.clear();
            for (FightEntry entry : INSTANCE.activeFights.values()) entry.active = false;
            INSTANCE.activeFights.clear();
        }
        INSTANCE.save();
    }

    public static synchronized void clearHistory() {
        INSTANCE.history.clear();
        INSTANCE.pendingHits.clear();
        INSTANCE.activeFights.clear();
        INSTANCE.save();
    }

    public static synchronized List<FightEntry> getEntries() {
        List<FightEntry> entries = new ArrayList<FightEntry>(INSTANCE.history);
        Collections.sort(entries, (a, b) -> Long.compare(b.lastDamageAt, a.lastDamageAt));
        return Collections.unmodifiableList(entries);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!isEnabled() || event == null || event.isCanceled() || !(event.target instanceof EntityPlayer)) return;
        EntityPlayer attacker = event.entityPlayer;
        EntityPlayer target = (EntityPlayer) event.target;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || attacker == null || attacker != mc.thePlayer || target == attacker
                || target.worldObj != mc.theWorld || attacker.getUniqueID().equals(target.getUniqueID())) return;

        // The attack attempt alone is not recorded. Wait for the target's hurt
        // animation to change, which is sent after the server accepts damage.
        pendingHits.put(target.getUniqueID(),
                new PendingHit(target, target.hurtTime, System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            pendingHits.clear();
            if (!activeFights.isEmpty()) {
                for (FightEntry entry : activeFights.values()) entry.active = false;
                activeFights.clear();
                save();
            }
            return;
        }

        if (!isEnabled()) {
            pendingHits.clear();
            return;
        }

        long now = System.currentTimeMillis();
        boolean presenceChanged = false;
        for (Object playerObject : mc.theWorld.playerEntities) {
            EntityPlayer seenPlayer = (EntityPlayer) playerObject;
            if (seenPlayer == mc.thePlayer) continue;
            FightEntry knownEntry = findHistoryEntry(seenPlayer.getUniqueID());
            if (knownEntry != null && now - knownEntry.lastSeenAt >= 1000L) {
                knownEntry.lastSeenAt = now;
                knownEntry.playerName = seenPlayer.getName();
                presenceChanged = true;
            }
        }
        if (presenceChanged && now - lastPresenceSaveAt >= 10000L) {
            save();
            lastPresenceSaveAt = now;
        }

        Iterator<Map.Entry<UUID, PendingHit>> pendingIterator = pendingHits.entrySet().iterator();
        while (pendingIterator.hasNext()) {
            Map.Entry<UUID, PendingHit> item = pendingIterator.next();
            PendingHit pending = item.getValue();
            Entity target = pending.target;
            if (target == null || target.isDead || target.worldObj != mc.theWorld
                    || now - pending.createdAt > HIT_CONFIRMATION_TIMEOUT_MS) {
                pendingIterator.remove();
                continue;
            }

            int currentHurtTime = ((EntityPlayer) target).hurtTime;
            if (currentHurtTime > pending.lastObservedHurtTime) {
                recordConfirmedHit((EntityPlayer) target, now);
                pendingIterator.remove();
            } else {
                pending.lastObservedHurtTime = currentHurtTime;
            }
        }

        Iterator<Map.Entry<UUID, FightEntry>> activeIterator = activeFights.entrySet().iterator();
        boolean changed = false;
        while (activeIterator.hasNext()) {
            Map.Entry<UUID, FightEntry> item = activeIterator.next();
            if (now - item.getValue().lastDamageAt >= FIGHT_TIMEOUT_MS) {
                item.getValue().active = false;
                activeIterator.remove();
                changed = true;
            }
        }
        if (changed) save();
    }

    private void recordConfirmedHit(EntityPlayer target, long now) {
        UUID playerId = target.getUniqueID();
        FightEntry entry = activeFights.get(playerId);
        if (entry == null) {
            entry = findHistoryEntry(playerId);
            if (entry == null) {
                entry = new FightEntry(playerId.toString(), target.getName(), now, now, now, 1, true);
                history.add(entry);
            } else {
                entry.fightCount++;
                entry.active = true;
                entry.playerName = target.getName();
            }
            activeFights.put(playerId, entry);
        }
        entry.lastDamageAt = now;
        entry.lastSeenAt = now;
        entry.playerName = target.getName();
        save();
    }

    private FightEntry findHistoryEntry(UUID playerId) {
        String id = playerId.toString();
        for (FightEntry entry : history) {
            if (entry.playerId.equals(id)) return entry;
        }
        return null;
    }

    private synchronized void load() {
        history.clear();
        activeFights.clear();
        pendingHits.clear();

        File file = historyFile();
        if (!file.isFile()) {
            save();
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root == null || !root.isJsonObject()) return;
            JsonElement entriesValue = root.getAsJsonObject().get("fights");
            if (entriesValue == null || !entriesValue.isJsonArray()) return;

            JsonArray entries = entriesValue.getAsJsonArray();
            for (JsonElement element : entries) {
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();
                String playerId = readString(object, "playerId");
                String playerName = readString(object, "playerName");
                long startedAt = readLong(object, "startedAt");
                long lastDamageAt = readLong(object, "lastDamageAt");
                int storedFightCount = (int) readLong(object, "fightCount");
                long lastSeenAt = readLong(object, "lastSeenAt");
                if (playerId.isEmpty() || playerName.isEmpty() || startedAt <= 0L || lastDamageAt <= 0L) continue;
                UUID parsedPlayerId;
                try { parsedPlayerId = UUID.fromString(playerId); }
                catch (IllegalArgumentException ignored) { continue; }
                FightEntry existing = findHistoryEntry(parsedPlayerId);
                if (existing == null) {
                    // Older entries represent individual encounters; hit totals are not fights.
                    int fightCount = storedFightCount > 0 ? storedFightCount : 1;
                    history.add(new FightEntry(playerId, playerName, startedAt, lastDamageAt,
                            lastSeenAt > 0L ? lastSeenAt : lastDamageAt, fightCount, false));
                } else {
                    existing.fightCount += storedFightCount > 0 ? storedFightCount : 1;
                    existing.startedAt = Math.min(existing.startedAt, startedAt);
                    if (lastSeenAt > existing.lastSeenAt) existing.lastSeenAt = lastSeenAt;
                    if (lastDamageAt > existing.lastDamageAt) existing.lastDamageAt = lastDamageAt;
                    existing.playerName = playerName;
                }
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Fight History: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    private synchronized void save() {
        File file = historyFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("[AlphaClient] Could not create Fight History config directory.");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        JsonArray entries = new JsonArray();
        for (FightEntry entry : history) {
            JsonObject object = new JsonObject();
            object.addProperty("playerId", entry.playerId);
            object.addProperty("playerName", entry.playerName);
            object.addProperty("startedAt", entry.startedAt);
            object.addProperty("lastDamageAt", entry.lastDamageAt);
            object.addProperty("lastSeenAt", entry.lastSeenAt);
            object.addProperty("fightCount", entry.fightCount);
            entries.add(object);
        }
        root.add("fights", entries);

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Fight History: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File historyFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-fight-history.json");
    }

    private static String readString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static long readLong(JsonObject object, String key) {
        JsonElement value = object.get(key);
        try { return value != null && value.isJsonPrimitive() ? value.getAsLong() : 0L; }
        catch (Exception ignored) { return 0L; }
    }

    public static final class FightEntry {
        private final String playerId;
        private String playerName;
        private long startedAt;
        private long lastDamageAt;
        private long lastSeenAt;
        private int fightCount;
        private boolean active;

        private FightEntry(String playerId, String playerName, long startedAt,
                           long lastDamageAt, long lastSeenAt, int fightCount, boolean active) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.startedAt = startedAt;
            this.lastDamageAt = lastDamageAt;
            this.lastSeenAt = lastSeenAt;
            this.fightCount = fightCount;
            this.active = active;
        }

        public String getPlayerName() { return playerName; }
        public long getStartedAt() { return startedAt; }
        public long getLastSeenAt() { return lastSeenAt; }
        public int getFightCount() { return fightCount; }
        public boolean isActive() { return active; }
    }

    private static final class PendingHit {
        private final EntityPlayer target;
        private int lastObservedHurtTime;
        private final long createdAt;

        private PendingHit(EntityPlayer target, int lastObservedHurtTime, long createdAt) {
            this.target = target;
            this.lastObservedHurtTime = lastObservedHurtTime;
            this.createdAt = createdAt;
        }
    }
}
