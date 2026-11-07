package com.alpharip.staffmod.singleplayer.alphahomes;

import com.alpharip.staffmod.waypoints.ServerWaypointIntegration;
import com.alpharip.staffmod.waypoints.StaffWaypoint;
import com.alpharip.staffmod.waypoints.WaypointFileUtils;
import com.alpharip.staffmod.waypoints.WaypointManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** World-local AlphaHomes storage and singleplayer-only teleport operations. */
public final class AlphaHomesManager {
    private static final AlphaHomesManager INSTANCE = new AlphaHomesManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type HOME_LIST_TYPE = new TypeToken<ArrayList<AlphaHome>>() { }.getType();

    private final List<AlphaHome> homes = new ArrayList<AlphaHome>();
    private File loadedFile;
    private boolean loaded;
    private boolean loadFailed;

    private AlphaHomesManager() { }

    public static AlphaHomesManager getInstance() { return INSTANCE; }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null && mc.isSingleplayer() && mc.getIntegratedServer() != null
                && mc.thePlayer != null && mc.theWorld != null;
    }

    private File getStorageFile() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) return null;

        String folderName = mc.getIntegratedServer().getFolderName();
        if (folderName == null || folderName.trim().isEmpty()) return null;

        File savesDirectory = new File(mc.mcDataDir, "saves");
        File worldDirectory = new File(savesDirectory, folderName);
        try {
            String root = savesDirectory.getCanonicalPath() + File.separator;
            String world = worldDirectory.getCanonicalPath();
            if (!world.startsWith(root)) return null;
        } catch (IOException invalidPath) {
            return null;
        }

        return new File(new File(worldDirectory, "alphaclient"), "alphahomes.json");
    }

    private synchronized boolean ensureLoaded() {
        File file = getStorageFile();
        if (file == null) return false;
        try {
            file = file.getCanonicalFile();
        } catch (IOException invalidPath) {
            return false;
        }

        if (loaded && file.equals(loadedFile)) return !loadFailed;
        homes.clear();
        loadedFile = file;
        loaded = true;
        loadFailed = false;

        if (!file.isFile()) return true;

        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            List<AlphaHome> saved = GSON.fromJson(reader, HOME_LIST_TYPE);
            if (saved == null) throw new IOException("AlphaHomes file did not contain a JSON array.");
            for (AlphaHome home : saved) {
                if (home == null) continue;
                home.normalize();
                if (!home.getName().isEmpty() && finite(home.getX()) && finite(home.getY()) && finite(home.getZ())) {
                    homes.add(home);
                }
            }
            return true;
        } catch (Exception failure) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load AlphaHomes; saving is disabled to protect the existing file: "
                    + failure.getMessage());
            return false;
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException ignored) { }
        }
    }

    private synchronized boolean save() {
        if (!ensureLoaded() || loadFailed || loadedFile == null) return false;
        Writer writer = null;
        File temporary = new File(loadedFile.getParentFile(), loadedFile.getName() + ".tmp");
        try {
            File parent = loadedFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Could not create the AlphaHomes save directory.");
            }
            writer = new OutputStreamWriter(new FileOutputStream(temporary), "UTF-8");
            GSON.toJson(homes, HOME_LIST_TYPE, writer);
            writer.close();
            writer = null;
            WaypointFileUtils.replaceAtomically(temporary, loadedFile);
            return true;
        } catch (Exception failure) {
            System.err.println("[AlphaClient] Could not save AlphaHomes: " + failure.getMessage());
            return false;
        } finally {
            if (writer != null) try { writer.close(); } catch (IOException ignored) { }
            if (temporary.exists()) temporary.delete();
        }
    }

    public synchronized List<AlphaHome> getHomes() {
        if (!ensureLoaded()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<AlphaHome>(homes));
    }

    public synchronized AlphaHome find(String name) {
        if (name == null || !ensureLoaded()) return null;
        String normalized = name.trim();
        if (normalized.isEmpty()) return null;
        for (AlphaHome home : homes) {
            if (home.getName().equalsIgnoreCase(normalized)) return home;
        }
        return null;
    }

    public synchronized AlphaHome setHome(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) throw new IllegalStateException("AlphaHomes is available only in a loaded singleplayer world.");
        if (!ensureLoaded()) throw new IllegalStateException("AlphaHomes could not read this world's save file.");
        String normalized = normalizeName(name);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Enter a home name.");

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        int dimension = mc.thePlayer.dimension;
        String serverKey = ServerWaypointIntegration.currentServerKey(mc);
        String worldKey = ServerWaypointIntegration.currentWorldKey(mc);
        if (worldKey.isEmpty()) throw new IllegalStateException("The current world is not ready for home storage.");

        AlphaHome existing = find(normalized);
        AlphaHome previous = existing == null ? null : new AlphaHome(existing.getName(),
                existing.getX(), existing.getY(), existing.getZ(), existing.getDimension(),
                existing.getWaypointId(), existing.getWaypointServerKey(), existing.getWaypointWorldKey());
        String waypointId = existing == null ? "" : existing.getWaypointId();
        WaypointManager waypoints = WaypointManager.getInstance();
        boolean waypointUpdated = !waypointId.isEmpty()
                && waypoints.updateHomeWaypoint(waypointId, existing.getWaypointServerKey(),
                existing.getWaypointWorldKey(), normalized, x, y, z, dimension, serverKey, worldKey);

        if (!waypointUpdated) {
            StaffWaypoint waypoint = waypoints.add(normalized, x, y, z);
            waypoint.setCategory("Homes");
            waypoint.setColor(0xFFC83D);
            waypoints.saveChanges();
            waypointId = waypoint.getId();
        }

        if (existing == null) {
            existing = new AlphaHome(normalized, x, y, z, dimension, waypointId, serverKey, worldKey);
            homes.add(existing);
        } else {
            existing.update(x, y, z, dimension, waypointId, serverKey, worldKey);
        }

        if (!save()) {
            if (previous != null && waypointUpdated) {
                waypoints.updateHomeWaypoint(waypointId, serverKey, worldKey, previous.getName(),
                        previous.getX(), previous.getY(), previous.getZ(), previous.getDimension(),
                        previous.getWaypointServerKey(), previous.getWaypointWorldKey());
                existing.update(previous.getX(), previous.getY(), previous.getZ(), previous.getDimension(),
                        previous.getWaypointId(), previous.getWaypointServerKey(), previous.getWaypointWorldKey());
            } else {
                if (!waypointId.isEmpty()) waypoints.removeHomeWaypoint(waypointId, serverKey, worldKey);
                if (previous == null) homes.remove(existing);
                else existing.update(previous.getX(), previous.getY(), previous.getZ(), previous.getDimension(),
                        previous.getWaypointId(), previous.getWaypointServerKey(), previous.getWaypointWorldKey());
            }
            throw new IllegalStateException("AlphaHomes could not save this home. The existing save file was left unchanged.");
        }
        return existing;
    }

    public synchronized boolean renameHome(String oldName, String newName) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc)) throw new IllegalStateException("AlphaHomes is available only in a loaded singleplayer world.");
        if (!ensureLoaded()) throw new IllegalStateException("AlphaHomes could not read this world's save file.");

        String normalizedNewName = normalizeName(newName);
        if (normalizedNewName.isEmpty()) throw new IllegalArgumentException("Enter a new home name.");
        AlphaHome home = find(oldName);
        if (home == null) return false;
        AlphaHome conflict = find(normalizedNewName);
        if (conflict != null && conflict != home) {
            throw new IllegalArgumentException("A home with that name already exists.");
        }

        String previousName = home.getName();
        if (previousName.equals(normalizedNewName)) return true;
        // Rename the linked waypoint first; its identity is validated against the saved world keys.
        boolean waypointRenamed = home.getWaypointId().isEmpty()
                || WaypointManager.getInstance().renameHomeWaypoint(home.getWaypointId(),
                home.getWaypointServerKey(), home.getWaypointWorldKey(), normalizedNewName);
        if (!waypointRenamed) {
            throw new IllegalStateException("The linked waypoint could not be found; the home was not renamed.");
        }

        // AlphaHome has no separate name setter; replace this entry while preserving its waypoint identity.
        AlphaHome renamed = new AlphaHome(normalizedNewName, home.getX(), home.getY(), home.getZ(),
                home.getDimension(), home.getWaypointId(), home.getWaypointServerKey(), home.getWaypointWorldKey());
        int index = homes.indexOf(home);
        homes.set(index, renamed);
        if (!save()) {
            homes.set(index, home);
            if (!home.getWaypointId().isEmpty()) {
                WaypointManager.getInstance().renameHomeWaypoint(home.getWaypointId(),
                        home.getWaypointServerKey(), home.getWaypointWorldKey(), previousName);
            }
            throw new IllegalStateException("AlphaHomes could not save the renamed home.");
        }
        return true;
    }

    public synchronized boolean deleteHome(String name) {
        AlphaHome home = find(name);
        if (home == null) return false;
        homes.remove(home);
        if (!save()) {
            homes.add(home);
            return false;
        }
        if (!home.getWaypointId().isEmpty()) {
            WaypointManager.getInstance().removeHomeWaypoint(home.getWaypointId(),
                    home.getWaypointServerKey(), home.getWaypointWorldKey());
        }
        return true;
    }

    public synchronized boolean teleportTo(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isAvailable(mc) || !ensureLoaded()) return false;
        AlphaHome home = find(name);
        if (home == null) return false;

        MinecraftServer server = mc.getIntegratedServer();
        if (server == null) return false;
        EntityPlayerMP serverPlayer = server.getConfigurationManager().getPlayerByUsername(mc.thePlayer.getName());
        if (serverPlayer == null) return false;

        try {
            if (serverPlayer.dimension != home.getDimension()) {
                server.getConfigurationManager().transferPlayerToDimension(serverPlayer, home.getDimension());
            }
            serverPlayer.setPositionAndUpdate(home.getX(), home.getY(), home.getZ());
            return true;
        } catch (Exception failure) {
            System.err.println("[AlphaClient] Could not teleport to AlphaHome '" + home.getName() + "': "
                    + failure.getMessage());
            return false;
        }
    }

    private static String normalizeName(String name) {
        if (name == null) return "";
        return name.trim();
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
