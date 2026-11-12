package com.alpharip.staffmod.playerhistory;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Polls the player list exposed by the active client connection. */
public final class PlayerHistoryTracker {
    private final PlayerHistory owner;
    private final Set<UUID> onlinePlayers = new HashSet<UUID>();
    private boolean initialized;
    private String connectionContext = "Unknown";
    private int tickCounter;

    PlayerHistoryTracker(PlayerHistory owner) {
        if (owner == null) throw new IllegalArgumentException("owner cannot be null");
        this.owner = owner;
    }

    public void update() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.getNetHandler() == null) {
            stopAndClose();
            return;
        }

        if (++tickCounter < 20) return;
        tickCounter = 0;

        String currentContext = resolveConnectionContext(minecraft);
        if (initialized && !currentContext.equals(connectionContext)) {
            owner.getStore().closeAll(System.currentTimeMillis(), connectionContext);
            onlinePlayers.clear();
            initialized = false;
            owner.save();
        }
        connectionContext = currentContext;

        NetHandlerPlayClient handler = minecraft.getNetHandler();
        Collection<NetworkPlayerInfo> playerInfo = handler.getPlayerInfoMap();
        Set<UUID> currentIds = new HashSet<UUID>();
        long now = System.currentTimeMillis();

        if (playerInfo != null) {
            for (NetworkPlayerInfo info : playerInfo) {
                if (info == null) continue;
                GameProfile profile = info.getGameProfile();
                if (profile == null || profile.getId() == null) continue;
                UUID id = profile.getId();
                if (minecraft.thePlayer.getUniqueID().equals(id)) continue;
                currentIds.add(id);
                if (initialized) owner.getStore().updateName(id, profile.getName(), now);
            }
        }

        if (!initialized) {
            for (UUID id : currentIds) {
                String name = findName(playerInfo, id);
                owner.getStore().startSession(id, name, connectionContext, now,
                        PlayerHistoryEvent.Type.FIRST_OBSERVED);
            }
            onlinePlayers.clear();
            onlinePlayers.addAll(currentIds);
            initialized = true;
            owner.save();
            return;
        }

        boolean changed = false;
        for (UUID id : currentIds) {
            if (!onlinePlayers.contains(id)) {
                owner.getStore().startSession(id, findName(playerInfo, id),
                        connectionContext, now, PlayerHistoryEvent.Type.JOINED);
                changed = true;
            }
        }
        for (UUID id : new HashSet<UUID>(onlinePlayers)) {
            if (!currentIds.contains(id)) {
                owner.getStore().endSession(id, connectionContext, now);
                changed = true;
            }
        }
        onlinePlayers.clear();
        onlinePlayers.addAll(currentIds);
        if (changed) owner.save();
    }

    public void stopAndClose() {
        if (!onlinePlayers.isEmpty()) {
            owner.getStore().closeAll(System.currentTimeMillis(), connectionContext);
            owner.save();
        }
        onlinePlayers.clear();
        initialized = false;
        tickCounter = 0;
    }

    public void reset() {
        onlinePlayers.clear();
        initialized = false;
        tickCounter = 0;
    }

    private static String findName(Collection<NetworkPlayerInfo> infos, UUID id) {
        if (infos != null) {
            for (NetworkPlayerInfo info : infos) {
                if (info == null || info.getGameProfile() == null) continue;
                if (id.equals(info.getGameProfile().getId())) {
                    return info.getGameProfile().getName();
                }
            }
        }
        return "Unknown";
    }

    private static String resolveConnectionContext(Minecraft minecraft) {
        if (minecraft.isSingleplayer()) return "Singleplayer";
        ServerData server = minecraft.getCurrentServerData();
        return server == null || server.serverIP == null ? "Multiplayer (unknown server)"
                : server.serverIP;
    }
}
