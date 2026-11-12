package com.alpharip.staffmod.Viewer.PlayerDirectionViewer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Samples the facing direction of player entities currently present in the
 * local client's world. It does not query server-side or hidden player data.
 */
public final class PlayerDirectionViewer {
    public static final String NAME = "Player Direction Viewer";
    public static final String CATEGORY = "Viewer";

    private static final PlayerDirectionViewer INSTANCE = new PlayerDirectionViewer();
    private final PlayerDirectionViewerEventHandler eventHandler =
            new PlayerDirectionViewerEventHandler(this);
    private List<PlayerDirectionSnapshot> snapshots = Collections.emptyList();
    private boolean registered;

    private PlayerDirectionViewer() { }

    public static PlayerDirectionViewer getInstance() {
        return INSTANCE;
    }

    /** Register from the client initialization layer when the feature is enabled. */
    public synchronized void register() {
        if (registered) return;
        net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().register(eventHandler);
        registered = true;
        captureVisiblePlayers();
    }

    public synchronized void unregister() {
        if (!registered) return;
        net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().unregister(eventHandler);
        registered = false;
        snapshots = Collections.emptyList();
    }

    /** Refreshes samples from the player entities currently loaded by this client. */
    public synchronized void captureVisiblePlayers() {
        Minecraft minecraft = Minecraft.getMinecraft();
        WorldClient world = minecraft == null ? null : minecraft.theWorld;
        EntityPlayerSP localPlayer = minecraft == null ? null : minecraft.thePlayer;
        if (world == null || localPlayer == null) {
            snapshots = Collections.emptyList();
            return;
        }

        long now = System.currentTimeMillis();
        List<PlayerDirectionSnapshot> captured = new ArrayList<PlayerDirectionSnapshot>();
        for (Object entry : world.playerEntities) {
            if (!(entry instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entry;
            if (player == localPlayer || player.isDead) continue;

            double distance = localPlayer.getDistanceToEntity(player);
            captured.add(new PlayerDirectionSnapshot(player.getName(),
                    player.rotationYawHead, player.rotationPitch, distance, now));
        }
        snapshots = Collections.unmodifiableList(captured);
    }

    public synchronized List<PlayerDirectionSnapshot> getSnapshots() {
        return Collections.unmodifiableList(new ArrayList<PlayerDirectionSnapshot>(snapshots));
    }

    public PlayerDirectionViewerGui createGui(GuiScreen parent) {
        return new PlayerDirectionViewerGui(parent, this);
    }

    /** Opens the standalone viewer; no menu button or keybind is registered here. */
    public void openGui(GuiScreen parent) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null) minecraft.displayGuiScreen(createGui(parent));
    }
}
