package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Staff-only utility HUDs for world state and the player currently under the crosshair.
 * Join/leave/reconnect events are inferred from client-visible world player lists,
 * not from an authoritative server moderation log.
 */
public final class StaffUtilityTrackingModules {
    private static final long EVENT_VISIBLE_MS = 5000L;

    private Object trackedWorld;
    private boolean initialized;
    private Set<UUID> previousPlayers = new HashSet<UUID>();
    private final Set<UUID> knownPlayers = new HashSet<UUID>();
    private final Set<UUID> departedPlayers = new HashSet<UUID>();
    private final Map<UUID, String> lastKnownNames = new HashMap<UUID, String>();

    private String lastJoin = "";
    private String lastLeave = "";
    private String lastReconnect = "";
    private long lastJoinAt;
    private long lastLeaveAt;
    private long lastReconnectAt;

    private boolean enabled(String id) {
        AlphaModule module = AlphaModuleRegistry.find(id);
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            resetTracking();
            return;
        }

        if (trackedWorld != mc.theWorld) {
            resetTracking();
            trackedWorld = mc.theWorld;
            previousPlayers = currentRemotePlayers(mc);
            for (Object value : mc.theWorld.playerEntities) {
                if (!(value instanceof EntityPlayer)) continue;
                EntityPlayer player = (EntityPlayer) value;
                if (player == mc.thePlayer) continue;
                UUID id = player.getUniqueID();
                knownPlayers.add(id);
                lastKnownNames.put(id, player.getName());
            }
            initialized = true;
            return;
        }

        Set<UUID> current = currentRemotePlayers(mc);
        if (!initialized) {
            previousPlayers = current;
            initialized = true;
            return;
        }

        for (Object value : mc.theWorld.playerEntities) {
            if (!(value instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) value;
            if (player == mc.thePlayer) continue;

            UUID id = player.getUniqueID();
            String name = player.getName();
            lastKnownNames.put(id, name);

            if (!previousPlayers.contains(id)) {
                lastJoin = name;
                lastJoinAt = System.currentTimeMillis();
                if (knownPlayers.contains(id) && departedPlayers.remove(id)) {
                    lastReconnect = name;
                    lastReconnectAt = lastJoinAt;
                }
                knownPlayers.add(id);
            }
        }

        for (UUID id : previousPlayers) {
            if (!current.contains(id)) {
                String name = lastKnownNames.get(id);
                if (name != null) {
                    lastLeave = name;
                    lastLeaveAt = System.currentTimeMillis();
                }
                if (knownPlayers.contains(id)) departedPlayers.add(id);
            }
        }

        previousPlayers = current;
    }

    private Set<UUID> currentRemotePlayers(Minecraft mc) {
        Set<UUID> current = new HashSet<UUID>();
        if (mc.theWorld == null) return current;
        for (Object value : mc.theWorld.playerEntities) {
            if (value instanceof EntityPlayer && value != mc.thePlayer) {
                current.add(((EntityPlayer) value).getUniqueID());
            }
        }
        return current;
    }

    private void resetTracking() {
        trackedWorld = null;
        initialized = false;
        previousPlayers.clear();
        knownPlayers.clear();
        departedPlayers.clear();
        lastKnownNames.clear();
        lastJoin = "";
        lastLeave = "";
        lastReconnect = "";
        lastJoinAt = 0L;
        lastLeaveAt = 0L;
        lastReconnectAt = 0L;
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.fontRendererObj == null || mc.gameSettings == null
                || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) return;

        int row = 0;
        if (enabled("staff-player-counter")) {
            int count = 0;
            for (Object value : mc.theWorld.playerEntities) {
                if (value instanceof EntityPlayer && value != mc.thePlayer) count++;
            }
            drawRight(mc, "Other players: " + count, row++, 0xFFFFFFFF);
        }
        if (enabled("staff-chunk-counter")) {
            int count = -1;
            if (mc.theWorld.getChunkProvider() instanceof ChunkProviderClient) {
                count = ((ChunkProviderClient) mc.theWorld.getChunkProvider()).getLoadedChunkCount();
            }
            drawRight(mc, count < 0 ? "Loaded chunks: unavailable" : "Loaded chunks: " + count, row++, 0xFFFFFFFF);
        }
        if (enabled("staff-weather-hud")) {
            String weather = mc.theWorld.isThundering() ? "Thunderstorm"
                    : mc.theWorld.isRaining() ? "Rain" : "Clear";
            drawRight(mc, "Weather: " + weather, row++, 0xFFFFFFFF);
        }

        EntityPlayer target = getTargetPlayer(mc);
        if (target != null) {
            if (enabled("player-direction-viewer")) {
                drawRight(mc, "Target direction: " + direction(target.rotationYaw), row++, 0xFFFFFFFF);
            }
            if (enabled("player-velocity-viewer")) {
                double horizontal = Math.sqrt(target.motionX * target.motionX + target.motionZ * target.motionZ);
                drawRight(mc, String.format("Target velocity: H %.3f / V %.3f", horizontal, target.motionY), row++, 0xFFFFFFFF);
            }
            NetworkPlayerInfo info = mc.getNetHandler() == null ? null
                    : mc.getNetHandler().getPlayerInfo(target.getUniqueID());
            if (enabled("player-ping-viewer")) {
                int ping = info == null ? -1 : info.getResponseTime();
                drawRight(mc, ping < 0 ? "Target ping: unavailable" : "Target ping: " + ping + " ms", row++, 0xFFFFFFFF);
            }
            if (enabled("player-connection-viewer")) {
                drawRight(mc, "Target tab status: " + (info == null ? "not listed" : "listed"), row++, 0xFFFFFFFF);
            }
        }

        long now = System.currentTimeMillis();
        if (enabled("player-join-tracker") && isRecent(lastJoinAt, now)) {
            drawRight(mc, "Player joined: " + lastJoin, row++, 0xFF55FF55);
        }
        if (enabled("player-leave-tracker") && isRecent(lastLeaveAt, now)) {
            drawRight(mc, "Player left: " + lastLeave, row++, 0xFFFFAA55);
        }
        if (enabled("player-reconnect-tracker") && isRecent(lastReconnectAt, now)) {
            drawRight(mc, "Player reconnected: " + lastReconnect, row++, 0xFFFFD866);
        }
    }

    private boolean isRecent(long timestamp, long now) {
        return timestamp > 0L && now - timestamp <= EVENT_VISIBLE_MS;
    }

    private EntityPlayer getTargetPlayer(Minecraft mc) {
        if (mc.objectMouseOver == null || !(mc.objectMouseOver.entityHit instanceof EntityPlayer)) return null;
        EntityPlayer target = (EntityPlayer) mc.objectMouseOver.entityHit;
        return target == mc.thePlayer ? null : target;
    }

    private String direction(float yaw) {
        int index = Math.floorMod((int) Math.floor(yaw * 8.0F / 360.0F + 0.5D), 8);
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return directions[index];
    }

    private void drawRight(Minecraft mc, String text, int row, int color) {
        ScaledResolution resolution = new ScaledResolution(mc);
        int x = resolution.getScaledWidth() - mc.fontRendererObj.getStringWidth(text) - 6;
        int y = 6 + row * 12;
        mc.fontRendererObj.drawStringWithShadow(text, Math.max(2, x), y, color);
    }
}
