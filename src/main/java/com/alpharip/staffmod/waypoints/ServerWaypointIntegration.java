package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Best-effort persistent identity; 1.8.9 exposes no universal remote world UUID. */
public final class ServerWaypointIntegration {
    private static World cachedWorld;
    private static String cachedWorldKey = "";
    private static World cachedLegacyWorld;
    private static String cachedLegacyWorldKey = "";

    private ServerWaypointIntegration() { }

    public static String currentServerKey(Minecraft mc) {
        if (mc == null) return "singleplayer";
        ServerData s = mc.getCurrentServerData();
        if (s != null && s.serverIP != null && !s.serverIP.trim().isEmpty())
            return "server:" + s.serverIP.trim().toLowerCase(Locale.ROOT);
        if (mc.theWorld != null && mc.theWorld.getWorldInfo() != null) {
            String w = mc.theWorld.getWorldInfo().getWorldName();
            if (w != null && !w.trim().isEmpty())
                return "world:" + w.trim().toLowerCase(Locale.ROOT);
        }
        return "singleplayer";
    }

    /**
     * Builds the current identity from world metadata only. Do not inspect spawn chunks here:
     * they may be unloaded when the player creates a waypoint far away from world spawn.
     */
    public static synchronized String currentWorldKey(Minecraft mc) {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return "";
        World world = mc.theWorld;
        if (world == cachedWorld && !cachedWorldKey.isEmpty()) return cachedWorldKey;

        WorldInfo info = world.getWorldInfo();
        if (info == null) return "";

        String identity = currentServerKey(mc)
                + "|dim=" + mc.thePlayer.dimension
                + "|name=" + safe(info.getWorldName())
                + "|spawn=" + info.getSpawnX() + "," + info.getSpawnY() + "," + info.getSpawnZ();

        cachedWorld = world;
        cachedWorldKey = "world-v3:" + sha256(identity);
        return cachedWorldKey;
    }

    /**
     * Computes the previous terrain-based identity only for compatibility with waypoints
     * saved by older AlphaClient versions. New waypoint creation never depends on this.
     */
    private static synchronized String legacyWorldKey(Minecraft mc) {
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return "";
        World world = mc.theWorld;
        if (world == cachedLegacyWorld && !cachedLegacyWorldKey.isEmpty()) return cachedLegacyWorldKey;

        WorldInfo info = world.getWorldInfo();
        if (info == null) return "";

        int sx = info.getSpawnX();
        int sz = info.getSpawnZ();
        String identity = currentServerKey(mc) + "|dim=" + mc.thePlayer.dimension
                + "|name=" + safe(info.getWorldName())
                + "|spawn=" + sx + "," + info.getSpawnY() + "," + sz;

        int[] offsets = { -8, 0, 8 };
        for (int dx : offsets) {
            for (int dz : offsets) {
                BlockPos column = new BlockPos(sx + dx, 0, sz + dz);
                if (!world.isBlockLoaded(column)) return "";

                BlockPos top = world.getHeight(column);
                BlockPos surface = new BlockPos(sx + dx, top.getY() - 1, sz + dz);
                BlockPos below = new BlockPos(sx + dx, top.getY() - 2, sz + dz);
                if (!world.isBlockLoaded(surface) || !world.isBlockLoaded(below)) return "";

                identity += "|" + Block.getIdFromBlock(world.getBlockState(surface).getBlock()) + ":"
                        + world.getBlockState(surface).getBlock().getMetaFromState(world.getBlockState(surface))
                        + "," + Block.getIdFromBlock(world.getBlockState(below).getBlock()) + ":"
                        + world.getBlockState(below).getBlock().getMetaFromState(world.getBlockState(below));
            }
        }

        cachedLegacyWorld = world;
        cachedLegacyWorldKey = "world-v2:" + sha256(identity);
        return cachedLegacyWorldKey;
    }

    private static String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 255));
            return out.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static boolean belongsToCurrentWorld(StaffWaypoint point, Minecraft mc) {
        if (point == null || mc == null || mc.thePlayer == null) return false;

        String currentKey = currentWorldKey(mc);
        if (currentKey.isEmpty()) return false;

        boolean sameWorld = currentKey.equals(point.getWorldKey());
        if (!sameWorld && point.getWorldKey().startsWith("world-v2:")) {
            String legacyKey = legacyWorldKey(mc);
            sameWorld = !legacyKey.isEmpty() && legacyKey.equals(point.getWorldKey());
        }

        return sameWorld
                && currentServerKey(mc).equals(point.getServerKey())
                && mc.thePlayer.dimension == point.getDimensionId();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
