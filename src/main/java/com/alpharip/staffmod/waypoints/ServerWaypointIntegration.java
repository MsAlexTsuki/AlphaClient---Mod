package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import java.util.Locale;

/**
 * Keeps local waypoints separated by the current server and world. This does not
 * connect to a server or send waypoint data over the network.
 */
public final class ServerWaypointIntegration {
    private ServerWaypointIntegration() { }

    public static String currentServerKey(Minecraft mc) {
        if (mc == null) return "singleplayer";
        ServerData server = mc.getCurrentServerData();
        if (server != null && server.serverIP != null && !server.serverIP.trim().isEmpty())
            return "server:" + server.serverIP.trim().toLowerCase(Locale.ROOT);
        if (mc.theWorld != null && mc.theWorld.getWorldInfo() != null) {
            String world = mc.theWorld.getWorldInfo().getWorldName();
            if (world != null && !world.trim().isEmpty())
                return "world:" + world.trim().toLowerCase(Locale.ROOT);
        }
        return "singleplayer";
    }

    public static boolean belongsToCurrentWorld(StaffWaypoint waypoint, Minecraft mc) {
        return waypoint != null && mc != null && mc.thePlayer != null
                && currentServerKey(mc).equals(waypoint.getServerKey())
                && mc.thePlayer.dimension == waypoint.getDimensionId();
    }
}
