package com.alpharip.staffmod.Viewer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Local Player Connection Viewer prototype.
 *
 * Reads only connection information exposed to this Minecraft client. It cannot
 * inspect server-side diagnostics, other players' private connection data, or
 * information hidden from the local client.
 */
public final class PlayerConnectionViewer {
    public static final String NAME = "Player Connection Viewer";
    public static final String CATEGORY = "Viewer";

    private static final PlayerConnectionViewer INSTANCE = new PlayerConnectionViewer();
    private final ConnectionTracker tracker = new ConnectionTracker();

    private PlayerConnectionViewer() { }

    public static PlayerConnectionViewer getInstance() {
        return INSTANCE;
    }

    public ConnectionSnapshot captureCurrentClientState() {
        Minecraft minecraft = Minecraft.getMinecraft();
        long now = System.currentTimeMillis();

        if (minecraft == null || minecraft.thePlayer == null) {
            return tracker.capture(ConnectionState.DISCONNECTED, "", "",
                    "Not connected", now);
        }

        String playerName = minecraft.thePlayer.getName();
        if (minecraft.isSingleplayer()) {
            return tracker.capture(ConnectionState.CONNECTED, playerName,
                    "Singleplayer world", "Singleplayer", now);
        }

        ServerData serverData = minecraft.getCurrentServerData();
        String address = serverData == null ? "Unknown" : serverData.serverIP;
        return tracker.capture(ConnectionState.CONNECTED, playerName, address,
                "Multiplayer", now);
    }

    public ConnectionSnapshot getLatestSnapshot() {
        return tracker.getLatest();
    }

    public PlayerConnectionViewerGui createGui(net.minecraft.client.gui.GuiScreen parent) {
        return new PlayerConnectionViewerGui(parent, this);
    }
}
