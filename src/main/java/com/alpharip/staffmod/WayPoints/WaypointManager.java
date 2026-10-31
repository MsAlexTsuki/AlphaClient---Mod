package com.alpharip.staffmod.WayPoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Central local store for waypoint records. Each record is tagged by server and dimension. */
public final class WaypointManager {
    private static final WaypointManager INSTANCE = new WaypointManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<StaffWaypoint>>() { }.getType();
    private final List<StaffWaypoint> points = new ArrayList<StaffWaypoint>();
    private String selectedId = "";
    private boolean loaded;
    private RouteBetweenWaypoints route = new RouteBetweenWaypoints();

    private WaypointManager() { }
    public static WaypointManager getInstance() { return INSTANCE; }

    public synchronized void load() {
        if (loaded) return;
        loaded = true;
        File file = getFile();
        if (!file.isFile()) return;
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            List<StaffWaypoint> saved = GSON.fromJson(reader, LIST_TYPE);
            if (saved != null) for (StaffWaypoint point : saved) if (point != null) points.add(point);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load waypoints: " + e.getMessage());
        } finally { if (reader != null) try { reader.close(); } catch (IOException ignored) { } }
    }

    private File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = new File(mc == null ? new File(".") : mc.mcDataDir, "config/alphaclient");
        if (!base.exists()) base.mkdirs();
        return new File(base, "waypoints.json");
    }

    private synchronized void save() {
        load();
        Writer writer = null;
        try {
            File file = getFile();
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            GSON.toJson(points, LIST_TYPE, writer);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save waypoints: " + e.getMessage());
        } finally { if (writer != null) try { writer.close(); } catch (IOException ignored) { } }
    }

    public synchronized StaffWaypoint add(String name, double x, double y, double z) {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        int dimension = mc != null && mc.thePlayer != null ? mc.thePlayer.dimension : 0;
        StaffWaypoint point = new StaffWaypoint(name, server, dimension, x, y, z);
        point.setColor(WaypointCategoriesColors.colorFor(point.getCategory()));
        points.add(point);
        save();
        return point;
    }

    public synchronized boolean addImported(StaffWaypoint incoming) {
        if (incoming == null || incoming.getLabel().trim().isEmpty()) return false;
        load();
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        StaffWaypoint point = new StaffWaypoint(incoming.getLabel(), server, incoming.getDimensionId(),
                incoming.getX(), incoming.getY(), incoming.getZ());
        point.setCategory(incoming.getCategory());
        point.setColor(incoming.getColor());
        point.setIconId(incoming.getIconId());
        point.setDeathWaypoint(incoming.isDeathWaypoint());
        points.add(point);
        save();
        return true;
    }

    public synchronized List<StaffWaypoint> getAll() {
        load();
        return Collections.unmodifiableList(new ArrayList<StaffWaypoint>(points));
    }

    public synchronized List<StaffWaypoint> getForCurrentServer() {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        for (StaffWaypoint point : points) if (server.equals(point.getServerKey())) result.add(point);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffWaypoint> getForCurrentWorld() {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        if (mc == null || mc.thePlayer == null) return Collections.unmodifiableList(result);
        String server = ServerWaypointIntegration.currentServerKey(mc);
        for (StaffWaypoint point : points) {
            if (server.equals(point.getServerKey()) && point.getDimensionId() == mc.thePlayer.dimension) result.add(point);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized StaffWaypoint find(String key) {
        if (key == null) return null;
        load();
        String normalized = key.trim();
        for (StaffWaypoint point : points) {
            if (point.getId().equalsIgnoreCase(normalized)
                    || point.getLabel().equalsIgnoreCase(normalized)) return point;
        }
        for (StaffWaypoint point : points) if (point.getId().startsWith(normalized)) return point;
        return null;
    }

    public synchronized boolean remove(String key) {
        StaffWaypoint point = find(key);
        if (point == null) return false;
        points.remove(point);
        if (point.getId().equals(selectedId)) selectedId = "";
        save();
        return true;
    }

    public synchronized void clearCurrentServer() {
        load();
        String server = ServerWaypointIntegration.currentServerKey(Minecraft.getMinecraft());
        for (int i=points.size()-1; i>=0; i--) if (server.equals(points.get(i).getServerKey())) points.remove(i);
        selectedId = "";
        save();
    }

    public synchronized void select(String id) {
        selectedId = id == null ? "" : id;
    }

    public synchronized StaffWaypoint getSelected() {
        return selectedId.isEmpty() ? null : find(selectedId);
    }

    public synchronized String getSelectedId() { return selectedId; }
    public synchronized RouteBetweenWaypoints getRoute() { return route; }
    public synchronized void setRoute(String startId, String endId) { route.set(startId, endId); select(endId); }
    public synchronized void clearRoute() { route.clear(); }
    public synchronized void saveChanges() { save(); }

    public synchronized List<StaffWaypoint> getSortedCurrentWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return getForCurrentWorld();
        return new WaypointsSortedByDistance().sort(getAll(),
                ServerWaypointIntegration.currentServerKey(mc), mc.thePlayer.dimension,
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public synchronized void addAllImported(List<StaffWaypoint> imported) {
        if (imported == null) return;
        for (StaffWaypoint point : imported) addImported(point);
    }

    public File getExportFile() {
        File base = new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient");
        if (!base.exists()) base.mkdirs();
        return new File(base, "waypoints-share.tsv");
    }
}
