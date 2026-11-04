package com.alpharip.staffmod.waypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Central local store for waypoint records. Each record is tagged by server and dimension. */
public final class WaypointManager {
    private static final WaypointManager INSTANCE = new WaypointManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<ArrayList<StaffWaypoint>>() { }.getType();
    private final List<StaffWaypoint> points = new ArrayList<StaffWaypoint>();
    private String selectedId = "";
    private boolean loaded;
    private boolean loadFailed;
    private RouteBetweenWaypoints route = new RouteBetweenWaypoints();
    private String activeWorldKey = "";

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
            if (saved == null) throw new IOException("Waypoint file did not contain a JSON array.");
            for (StaffWaypoint point : saved) {
                if (point == null) continue;
                point.normalize();
                if (finite(point.getX()) && finite(point.getY()) && finite(point.getZ())) points.add(point);
            }
        } catch (Exception e) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load waypoints; saving is disabled to protect the existing file: " + e.getMessage());
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
        if (loadFailed) return;
        Writer writer = null;
        File temporary = null;
        try {
            File file = getFile();
            temporary = new File(file.getParentFile(), file.getName() + ".tmp");
            writer = new OutputStreamWriter(new FileOutputStream(temporary), "UTF-8");
            GSON.toJson(points, LIST_TYPE, writer);
            writer.close();
            writer = null;
            WaypointFileUtils.replaceAtomically(temporary,file);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save waypoints: " + e.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (IOException ignored) { }
        }
    }

    private String currentProfile() { return WaypointSettings.getString("activeProfile","default"); }

    private void ensureCurrentWorldContext() {
        String current = ServerWaypointIntegration.currentWorldKey(Minecraft.getMinecraft());
        if (!current.equals(activeWorldKey)) {
            activeWorldKey = current;
            selectedId = "";
            route.clear();
        }
    }

    public synchronized StaffWaypoint add(String name, double x, double y, double z) {
        if (!finite(x) || !finite(y) || !finite(z))
            throw new IllegalArgumentException("Waypoint coordinates must be finite.");
        load();
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        int dimension = mc != null && mc.thePlayer != null ? mc.thePlayer.dimension : 0;
        String worldKey = ServerWaypointIntegration.currentWorldKey(mc);
        if (worldKey.isEmpty()) throw new IllegalStateException("The current Minecraft world is not ready for waypoint storage.");
        StaffWaypoint point = new StaffWaypoint(name, server, dimension, x, y, z);
        point.setWorldKey(worldKey);
        point.setColor(WaypointCategoriesColors.colorFor(point.getCategory()));
        point.setIconId(WaypointSettings.getString("defaultIcon","pin"));
        point.setProfileId(currentProfile());
        points.add(point);
        save();
        return point;
    }

    public synchronized boolean addImported(StaffWaypoint incoming) {
        load();
        if (!copyImported(incoming)) return false;
        save();
        return true;
    }

    private boolean copyImported(StaffWaypoint incoming) {
        if (incoming == null || incoming.getLabel() == null || incoming.getLabel().trim().isEmpty()
                || !finite(incoming.getX()) || !finite(incoming.getY()) || !finite(incoming.getZ())) return false;
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        String profile=currentProfile();
        String worldKey=ServerWaypointIntegration.currentWorldKey(mc);
        if(worldKey.isEmpty())return false;
        for (StaffWaypoint existing : points) {
            if (server.equals(existing.getServerKey()) && worldKey.equals(existing.getWorldKey())
                    && profile.equalsIgnoreCase(existing.getProfileId())
                    && existing.getDimensionId() == (mc.thePlayer == null ? 0 : mc.thePlayer.dimension)
                    && existing.getLabel().equalsIgnoreCase(incoming.getLabel().trim())
                    && Math.abs(existing.getX() - incoming.getX()) < 0.001D
                    && Math.abs(existing.getY() - incoming.getY()) < 0.001D
                    && Math.abs(existing.getZ() - incoming.getZ()) < 0.001D) return false;
        }
        StaffWaypoint point = new StaffWaypoint(incoming.getLabel(), server, mc.thePlayer == null ? 0 : mc.thePlayer.dimension,
                incoming.getX(), incoming.getY(), incoming.getZ());
        point.setWorldKey(worldKey);
        point.setCategory(incoming.getCategory());
        point.setColor(incoming.getColor());
        point.setBeamColor(incoming.getBeamColor());
        point.setNameColor(incoming.getNameColor());
        point.setIconId(incoming.getIconId());
        point.setDeathWaypoint(incoming.isDeathWaypoint());
        point.setFavorite(incoming.isFavorite());
        point.setVisited(incoming.isVisited());
        point.setChromaEnabled(incoming.isChromaEnabled());
        point.setProfileId(profile);
        points.add(point);
        return true;
    }

    private static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public synchronized List<StaffWaypoint> getAll() {
        load();
        return Collections.unmodifiableList(new ArrayList<StaffWaypoint>(points));
    }

    public synchronized List<String> getProfilesForCurrentServer() {
        load();
        Minecraft mc=Minecraft.getMinecraft();
        String server=ServerWaypointIntegration.currentServerKey(mc);
        List<String> result=new ArrayList<String>();
        result.add("default");
        for(StaffWaypoint point:points)
            if(server.equals(point.getServerKey())&&!containsIgnoreCase(result,point.getProfileId()))result.add(point.getProfileId());
        String active=currentProfile();
        if(!containsIgnoreCase(result,active))result.add(active);
        return Collections.unmodifiableList(result);
    }
    private static boolean containsIgnoreCase(List<String> values,String value) {
        for(String existing:values)if(existing.equalsIgnoreCase(value))return true;
        return false;
    }

    public synchronized List<StaffWaypoint> getForCurrentServer() {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        String server = ServerWaypointIntegration.currentServerKey(mc);
        String profile = currentProfile();
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        for (StaffWaypoint point : points)
            if (server.equals(point.getServerKey()) && profile.equalsIgnoreCase(point.getProfileId())) result.add(point);
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffWaypoint> getForCurrentWorld() {
        load();
        ensureCurrentWorldContext();
        Minecraft mc = Minecraft.getMinecraft();
        List<StaffWaypoint> result = new ArrayList<StaffWaypoint>();
        if (mc == null || mc.thePlayer == null) return Collections.unmodifiableList(result);
        String server = ServerWaypointIntegration.currentServerKey(mc);
        String profile = currentProfile();
        for (StaffWaypoint point : points) {
            if (ServerWaypointIntegration.belongsToCurrentWorld(point, mc) && server.equals(point.getServerKey())
                    && profile.equalsIgnoreCase(point.getProfileId()) && point.getDimensionId() == mc.thePlayer.dimension) result.add(point);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized StaffWaypoint find(String key) {
        ensureCurrentWorldContext();
        if (key == null) return null;
        load();
        String normalized = key.trim();
        if (normalized.isEmpty()) return null;
        String server = ServerWaypointIntegration.currentServerKey(Minecraft.getMinecraft());
        for (StaffWaypoint point : points) {
            if (!server.equals(point.getServerKey()) || !currentProfile().equalsIgnoreCase(point.getProfileId())
                    || !ServerWaypointIntegration.belongsToCurrentWorld(point, Minecraft.getMinecraft())) continue;
            if (point.getId().equalsIgnoreCase(normalized)
                    || point.getLabel().equalsIgnoreCase(normalized)) return point;
        }
        for (StaffWaypoint point : points) {
            if (server.equals(point.getServerKey()) && currentProfile().equalsIgnoreCase(point.getProfileId())
                    && ServerWaypointIntegration.belongsToCurrentWorld(point, Minecraft.getMinecraft())
                    && point.getId().regionMatches(true, 0, normalized, 0, normalized.length())) return point;
        }
        return null;
    }

    public synchronized boolean remove(String key) {
        StaffWaypoint point = find(key);
        if (point == null) return false;
        points.remove(point);
        if (point.getId().equals(selectedId)) selectedId = "";
        if (getRoute().contains(point.getId())) clearRoute();
        save();
        return true;
    }

    public synchronized void clearCurrentServer() {
        load();
        ensureCurrentWorldContext();
        String server = ServerWaypointIntegration.currentServerKey(Minecraft.getMinecraft());
        String profile = currentProfile();
        Minecraft mc=Minecraft.getMinecraft();
        for (int i=points.size()-1; i>=0; i--)
            if (server.equals(points.get(i).getServerKey()) && profile.equalsIgnoreCase(points.get(i).getProfileId())
                    && ServerWaypointIntegration.belongsToCurrentWorld(points.get(i),mc)) points.remove(i);
        selectedId = "";
        clearRoute();
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
    public synchronized void setRoute(String startId, String endId) {
        if (route.set(startId, endId)) select(endId);
    }
    public synchronized boolean setRoute(List<StaffWaypoint> waypoints) {
        boolean set=route.setPath(waypoints);
        if(set)select(route.getEndWaypointId());
        return set;
    }
    public synchronized List<StaffWaypoint> getRouteWaypoints() {
        List<StaffWaypoint> result=new ArrayList<StaffWaypoint>();
        for(String id:route.getWaypointIds()) {
            StaffWaypoint point=find(id);
            if(point==null)return Collections.emptyList();
            result.add(point);
        }
        return Collections.unmodifiableList(result);
    }
    public synchronized void clearRoute() { route.clear(); }
    public synchronized void setActiveProfile(String profile) {
        if (profile == null || profile.trim().isEmpty()) throw new IllegalArgumentException("Profile name cannot be empty.");
        String normalized=profile.trim();
        if(normalized.length()>32)throw new IllegalArgumentException("Profile names can contain at most 32 characters.");
        WaypointSettings.setString("activeProfile",normalized);
        selectedId="";
        route.clear();
    }
    public synchronized void saveChanges() { save(); }

    public synchronized List<StaffWaypoint> getSortedCurrentWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return getForCurrentWorld();
        return new WaypointsSortedByDistance().sort(getForCurrentWorld(),
                ServerWaypointIntegration.currentServerKey(mc), currentProfile(), mc.thePlayer.dimension,
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                WaypointSettings.getString("sortMode","distance"));
    }

    public synchronized int addAllImported(List<StaffWaypoint> imported) {
        if (imported == null || imported.isEmpty()) return 0;
        load();
        int added = 0;
        for (StaffWaypoint point : imported) if (copyImported(point)) added++;
        if (added > 0) save();
        return added;
    }

    public File getExportFile() {
        File base = new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient");
        if (!base.exists()) base.mkdirs();
        return new File(base, "waypoints-share.tsv");
    }
}
