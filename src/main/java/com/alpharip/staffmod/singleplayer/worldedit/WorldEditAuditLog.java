// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Persistent, world-local seven-day audit trail for Alpha World Edit block changes. */
public final class WorldEditAuditLog {
    private static final long RETENTION_MILLIS = 7L * 24L * 60L * 60L * 1000L;
    private static final int MAX_ENTRIES = 2000;
    private static final int MAX_SAMPLES = 64;
    private static final List<PendingChange> PENDING = new ArrayList<PendingChange>();
    private static String currentAction = "Alpha World Edit operation";

    private WorldEditAuditLog() { }

    public static void setCurrentAction(String action) {
        currentAction = action == null || action.trim().isEmpty()
                ? "Alpha World Edit operation" : action;
    }

    public static void clearCurrentAction() {
        currentAction = "Alpha World Edit operation";
    }

    public static void captureBefore(WorldServer world, WorldEditSelection.Bounds bounds) {
        if (world == null || bounds == null || bounds.minY < 0 || bounds.maxY > 255
                || bounds.volume() < 1 || bounds.volume() > 1000000L
                || !WorldEditOperations.regionLoaded(world, bounds)) return;
        try {
            PENDING.add(new PendingChange(world, currentAction, bounds,
                    WorldEditOperations.copy(world, bounds), System.currentTimeMillis()));
        } catch (RuntimeException ignored) {
            // Audit capture must never interrupt an edit operation.
        }
    }

    /** Called at the end of each server world tick, after scheduled edit tasks have run. */
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END
                || !(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        Iterator<PendingChange> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            PendingChange pending = iterator.next();
            if (pending.world != world) continue;
            iterator.remove();
            try {
                append(world, pending);
            } catch (Exception exception) {
                System.err.println("[AlphaClient] World Edit audit write failed: " + exception.getMessage());
            }
        }
    }

    private static void append(WorldServer world, PendingChange pending) throws Exception {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setLong("timestamp", pending.timestamp);
        entry.setString("action", pending.action);
        entry.setInteger("dimension", world.provider.getDimensionId());
        entry.setInteger("minX", pending.bounds.minX);
        entry.setInteger("minY", pending.bounds.minY);
        entry.setInteger("minZ", pending.bounds.minZ);
        entry.setInteger("maxX", pending.bounds.maxX);
        entry.setInteger("maxY", pending.bounds.maxY);
        entry.setInteger("maxZ", pending.bounds.maxZ);

        Map<String, Integer> changes = new LinkedHashMap<String, Integer>();
        List<String> samples = new ArrayList<String>();
        int changed = 0;
        IBlockState[][][] before = pending.before;
        for (int x = 0; x < before.length; x++) {
            for (int y = 0; y < before[x].length; y++) {
                for (int z = 0; z < before[x][y].length; z++) {
                    BlockPos pos = new BlockPos(pending.bounds.minX + x,
                            pending.bounds.minY + y, pending.bounds.minZ + z);
                    IBlockState oldState = before[x][y][z];
                    IBlockState newState = world.getBlockState(pos);
                    if (oldState.equals(newState)) continue;
                    changed++;
                    String transition = stateName(oldState) + " -> " + stateName(newState);
                    Integer count = changes.get(transition);
                    changes.put(transition, count == null ? 1 : count + 1);
                    if (samples.size() < MAX_SAMPLES) {
                        samples.add(pos.getX() + "," + pos.getY() + "," + pos.getZ()
                                + ": " + transition);
                    }
                }
            }
        }
        entry.setInteger("changed", changed);
        if (changed == 0) return;

        NBTTagList changeList = new NBTTagList();
        for (Map.Entry<String, Integer> change : changes.entrySet()) {
            NBTTagCompound item = new NBTTagCompound();
            item.setString("transition", change.getKey());
            item.setInteger("count", change.getValue());
            changeList.appendTag(item);
        }
        entry.setTag("changes", changeList);
        NBTTagList sampleList = new NBTTagList();
        for (String sample : samples) {
            NBTTagCompound item = new NBTTagCompound();
            item.setString("value", sample);
            sampleList.appendTag(item);
        }
        entry.setTag("samples", sampleList);

        File file = getFile(world);
        NBTTagCompound root = readRoot(file);
        NBTTagList entries = root.getTagList("entries", 10);
        entries.appendTag(entry);
        root.setTag("entries", entries);
        prune(root, System.currentTimeMillis());
        writeRoot(file, root);
    }

    private static String stateName(IBlockState state) {
        Object key = Block.blockRegistry.getNameForObject(state.getBlock());
        return String.valueOf(key) + ":" + state.getBlock().getMetaFromState(state);
    }

    private static File getFile(WorldServer world) {
        String name = world.getWorldInfo().getWorldName();
        if (name == null || name.trim().isEmpty()) name = "Singleplayer";
        name = name.replaceAll("[^A-Za-z0-9_-]", "_");
        File directory = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir,
                "AlphaWorldEdit/logs");
        if (!directory.exists()) directory.mkdirs();
        return new File(directory, name + "-dim" + world.provider.getDimensionId() + ".aclog");
    }

    private static NBTTagCompound readRoot(File file) {
        if (!file.isFile()) return new NBTTagCompound();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return CompressedStreamTools.readCompressed(input);
        } catch (Exception ignored) {
            return new NBTTagCompound();
        } finally {
            if (input != null) try { input.close(); } catch (Exception ignored) { }
        }
    }

    private static void writeRoot(File file, NBTTagCompound root) throws Exception {
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        FileOutputStream output = new FileOutputStream(temporary);
        try { CompressedStreamTools.writeCompressed(root, output); }
        finally { output.close(); }
        Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void prune(NBTTagCompound root, long now) {
        NBTTagList old = root.getTagList("entries", 10);
        NBTTagList kept = new NBTTagList();
        for (int i = 0; i < old.tagCount(); i++) {
            NBTTagCompound entry = old.getCompoundTagAt(i);
            if (now - entry.getLong("timestamp") <= RETENTION_MILLIS) kept.appendTag(entry);
        }
        while (kept.tagCount() > MAX_ENTRIES) {
            NBTTagList trimmed = new NBTTagList();
            for (int i = 1; i < kept.tagCount(); i++) trimmed.appendTag(kept.getCompoundTagAt(i));
            kept = trimmed;
        }
        root.setTag("entries", kept);
    }

    public static void recordCurrentSummary(WorldServer world, String summary,
                                            int count, BlockPos location) {
        recordSummary(world, currentAction, summary, count, location);
    }

    public static void recordSummary(WorldServer world, String action, String summary,
                                     int count, BlockPos location) {
        if (world == null || count <= 0) return;
        try {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setLong("timestamp", System.currentTimeMillis());
            entry.setString("action", action == null ? "Alpha World Edit action" : action);
            entry.setInteger("dimension", world.provider.getDimensionId());
            entry.setInteger("changed", count);
            if (location != null) {
                entry.setInteger("minX", location.getX());
                entry.setInteger("minY", location.getY());
                entry.setInteger("minZ", location.getZ());
                entry.setInteger("maxX", location.getX());
                entry.setInteger("maxY", location.getY());
                entry.setInteger("maxZ", location.getZ());
                NBTTagList samples = new NBTTagList();
                NBTTagCompound sample = new NBTTagCompound();
                sample.setString("value", location.getX() + "," + location.getY() + "," + location.getZ());
                samples.appendTag(sample);
                entry.setTag("samples", samples);
            }
            NBTTagList changes = new NBTTagList();
            NBTTagCompound change = new NBTTagCompound();
            change.setString("transition", summary);
            change.setInteger("count", count);
            changes.appendTag(change);
            entry.setTag("changes", changes);
            File file = getFile(world);
            NBTTagCompound root = readRoot(file);
            NBTTagList entries = root.getTagList("entries", 10);
            entries.appendTag(entry);
            root.setTag("entries", entries);
            prune(root, System.currentTimeMillis());
            writeRoot(file, root);
        } catch (Exception exception) {
            System.err.println("[AlphaClient] World Edit audit summary failed: " + exception.getMessage());
        }
    }

    public static List<AuditEntry> getEntries() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || !mc.isSingleplayer()
                || !(mc.theWorld instanceof net.minecraft.world.World)) return Collections.emptyList();
        WorldServer world = mc.getIntegratedServer() == null ? null
                : mc.getIntegratedServer().worldServerForDimension(mc.theWorld.provider.getDimensionId());
        if (world == null) return Collections.emptyList();
        File file = getFile(world);
        NBTTagCompound root = readRoot(file);
        prune(root, System.currentTimeMillis());
        try { writeRoot(file, root); } catch (Exception ignored) { }
        NBTTagList list = root.getTagList("entries", 10);
        List<AuditEntry> result = new ArrayList<AuditEntry>();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            List<String> details = new ArrayList<String>();
            NBTTagList changes = tag.getTagList("changes", 10);
            for (int j = 0; j < changes.tagCount(); j++) {
                NBTTagCompound change = changes.getCompoundTagAt(j);
                details.add(change.getString("transition") + " x" + change.getInteger("count"));
            }
            NBTTagList samples = tag.getTagList("samples", 10);
            List<String> coordinates = new ArrayList<String>();
            for (int j = 0; j < samples.tagCount(); j++)
                coordinates.add(samples.getCompoundTagAt(j).getString("value"));
            result.add(new AuditEntry(tag.getLong("timestamp"), tag.getString("action"),
                    tag.getInteger("dimension"), tag.getInteger("changed"),
                    tag.getInteger("minX"), tag.getInteger("minY"), tag.getInteger("minZ"),
                    tag.getInteger("maxX"), tag.getInteger("maxY"), tag.getInteger("maxZ"),
                    details, coordinates));
        }
        Collections.sort(result, new Comparator<AuditEntry>() {
            public int compare(AuditEntry a, AuditEntry b) {
                return a.timestamp < b.timestamp ? 1 : (a.timestamp == b.timestamp ? 0 : -1);
            }
        });
        return result;
    }

    private static final class PendingChange {
        private final WorldServer world;
        private final String action;
        private final WorldEditSelection.Bounds bounds;
        private final IBlockState[][][] before;
        private final long timestamp;
        private PendingChange(WorldServer world, String action, WorldEditSelection.Bounds bounds,
                              IBlockState[][][] before, long timestamp) {
            this.world = world; this.action = action; this.bounds = bounds;
            this.before = before; this.timestamp = timestamp;
        }
    }

    public static final class AuditEntry {
        public final long timestamp;
        public final String action;
        public final int dimension, changed, minX, minY, minZ, maxX, maxY, maxZ;
        public final List<String> changes, samples;
        private AuditEntry(long timestamp, String action, int dimension, int changed,
                           int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                           List<String> changes, List<String> samples) {
            this.timestamp=timestamp; this.action=action; this.dimension=dimension;
            this.changed=changed; this.minX=minX; this.minY=minY; this.minZ=minZ;
            this.maxX=maxX; this.maxY=maxY; this.maxZ=maxZ;
            this.changes=changes; this.samples=samples;
        }
    }
}
